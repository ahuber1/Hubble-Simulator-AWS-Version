package ahuber.hubble;

import ahuber.hubble.adt.ArrayUtils;
import ahuber.hubble.adt.IntArrayWrapper;
import ahuber.hubble.aws.LocalizedS3ObjectId;
import ahuber.hubble.aws.S3Helpers;
import ahuber.hubble.aws.SparkJobConfiguration;
import ahuber.hubble.utils.Utils;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An object that processes data that is produced by our {@linkplain Satellite Hubble Space Telescope}.
 */
public class SatelliteProcessor implements Processor<IntArrayWrapper, SparkJobConfiguration>, Runnable {

    private static final String TERMINATE_CLUSTER_ACTION = "TERMINATE_CLUSTER";

    private final Semaphore semaphore = new Semaphore(1);
    private final Regions emrRegion;
    private final boolean launchEmrCluster;

    @NotNull
    private final String satelliteName;
    @NotNull
    private final LocalizedS3ObjectId sparkJobConfigLocation;
    @NotNull
    private final LocalizedS3ObjectId logFolderLocation;
    @NotNull
    private final LocalizedS3ObjectId sparkJobJarLocation;
    @NotNull
    private final String[] sparkJobJarArgs;
    @NotNull
    private final String sparkJobClass;
    @NotNull
    private final Function<int[], SparkJobConfiguration> configurationSupplier;
    @Nullable
    private SparkJobConfiguration configuration;

    /**
     * Creates a new {@link SatelliteProcessor}
     *
     * @param launchEmrCluster A boolean value indicating whether the EMR cluster should launched.
     * @param configurationSupplier  A function that returns a {@link SparkJobConfiguration} containing the specified
     * @param emrRegion              The AWS region where the EMR cluster will be created.
     * @param logFolderLocation      The location where logs for the EMR cluster will be stored.
     * @param sparkJobConfigLocation The location where the {@link SparkJobConfiguration} serialized as JSON will be
     *                               stored in S3 for consumption by the Spark Job.
     * @param sparkJobJarLocation    The location of the executable JAR that will run on the EMR cluster to process the
     *                               data.
     * @param sparkJobClass          The class containing the entry point of the executable JAR that will run on the EMR
     *                               cluster to process the data.
     * @param sparkJobJarArgs        Additional arguments to pass to the executable JAR that will run on the EMR
     *                               cluster.
     */
    public SatelliteProcessor(boolean launchEmrCluster, Function<int[], SparkJobConfiguration> configurationSupplier,
            String satelliteName, Regions emrRegion, LocalizedS3ObjectId logFolderLocation,
            LocalizedS3ObjectId sparkJobConfigLocation,
            LocalizedS3ObjectId sparkJobJarLocation, String sparkJobClass, String... sparkJobJarArgs) {

        this.launchEmrCluster = launchEmrCluster;
        this.configurationSupplier = Objects.requireNonNull(configurationSupplier,
                "'configurationSupplier' cannot be null.");
        this.satelliteName = Objects.requireNonNull(satelliteName, "'satelliteName' cannot be null.");
        this.sparkJobConfigLocation = Objects.requireNonNull(sparkJobConfigLocation,
                "'sparkJobConfigUri' cannot be null.");
        this.emrRegion = Objects.requireNonNull(emrRegion, "'emrRegion' cannot be null.");
        this.logFolderLocation = Objects.requireNonNull(logFolderLocation, "'logUri' cannot be null.");
        this.sparkJobJarLocation = Objects.requireNonNull(sparkJobJarLocation, "'sparkJobJarUri' cannot be null.");
        this.sparkJobClass = Objects.requireNonNull(sparkJobClass, "'sparkJobClass' cannot be null.");
        this.sparkJobJarArgs = Objects.requireNonNull(sparkJobJarArgs, "'sparkJobJarArgs' cannot be null.");

        semaphore.acquireUninterruptibly();
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            // Ignore
        } finally {
            semaphore.release();
        }
    }

    @NotNull
    @Override
    public Optional<SparkJobConfiguration> getResult() {
        return configuration == null ? Optional.empty() : Optional.of(configuration);
    }

    @Override
    public void onReceived(@NotNull IntArrayWrapper data) {
        // Get the data and upload it to Amazon S3 for processing.
        int[] array = data.getArray();
        configuration = configurationSupplier.apply(array);

        if (!launchEmrCluster) {
            semaphore.release();
            return;
        }

        try {
            uploadSparkJobConfigurationToAmazonS3(configuration, sparkJobConfigLocation);
        } catch (IOException e) {
            String message = String.format("Unable to load SparkJobConfiguration at %s", sparkJobConfigLocation);
            throw new RuntimeException(message, e);
        }

        System.out.println("SparkJobConfiguration was successfully converted to JSON and uploaded to Amazon S3.");

        RunJobFlowResult runJobFlowResult = startHadoopCluster(emrRegion, logFolderLocation, sparkJobJarLocation,
                sparkJobClass, sparkJobJarArgs);
        System.out.printf("An Amazon ECR request was submitted and approved. Job Flow ID is %s\n",
                runJobFlowResult.getJobFlowId());

        semaphore.release();
    }

    /**
     * Starts the Hadoop Cluster
     *
     * @param emrRegion             The region where the Hadoop Cluster is located.
     * @param logLocationId         The location of the folder where logs for the Hadoop job be stored.
     * @param sparkJobJarLocationId The location of the JAR on Amazon S3 containing the code that will be executed
     *                              during the Spark Job.
     * @param sparkJobClass         The fully-qualified class name containing the entry point of the Spark job.
     * @param sparkJobJarArgs       Additional arguments that will be passed to {@code sparkJobJarLocationId}
     * @return A {@link RunJobFlowResult} containing additional information pertaining to the request made to start
     * the Hadoop Cluster.
     */
    private RunJobFlowResult startHadoopCluster(Regions emrRegion, LocalizedS3ObjectId logLocationId,
            LocalizedS3ObjectId sparkJobJarLocationId, String sparkJobClass, String... sparkJobJarArgs) {

        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(emrRegion)
                .build();

        String[] commandRunnerArgs = Utils.arrayOf("spark-submit", "--deploy-mode", "cluster",
                "--executor-memory", "1g", "--conf", "spark.driver.memoryOverhead=4096", "--conf",
                "spark.executor.memoryOverhead=4096", "--class", sparkJobClass,
                sparkJobJarLocationId.getStringUri());
        String[] allArgs = ArrayUtils.combine(String[]::new, commandRunnerArgs, sparkJobJarArgs);

        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar("command-runner.jar")
                .withArgs(allArgs)
                .withArgs(sparkJobJarArgs);

        StepConfig customJarStep = new StepConfig()
                .withName("Process Data")
                .withActionOnFailure(TERMINATE_CLUSTER_ACTION)
                .withHadoopJarStep(hadoopJarStep);

        StepConfig[] steps = prefaceStepsWithEnableDebugStep(customJarStep);
        Application sparkApplication = new Application().withName("Spark");

        JobFlowInstancesConfig instancesConfig = new JobFlowInstancesConfig()
                .withInstanceCount(3)
                .withKeepJobFlowAliveWhenNoSteps(true)
                .withMasterInstanceType("m5.xlarge")
                .withSlaveInstanceType("m5.xlarge")
                .withKeepJobFlowAliveWhenNoSteps(false);

        RunJobFlowRequest request = new RunJobFlowRequest()
                .withName(satelliteName)
                .withReleaseLabel("emr-5.27.0")
                .withSteps(steps)
                .withApplications(sparkApplication)
                .withLogUri(logLocationId.getStringUri())
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withInstances(instancesConfig);

        return emr.runJobFlow(request);
    }

    /**
     * Creates an array of {@link StepConfig} objects containing a step for enabling debugging in the AWS Management
     * Console followed by the provided steps.
     *
     * @param steps The additional steps to execute following the "enable debugging" step.
     * @return An array containing the step for enabling debugging int the AWS Management Console followed by the
     * steps in {@code steps}
     * @apiNote Any {@link StepConfig} object in {@code steps} that is {@code null} will not be in the returned array.
     */
    @NotNull
    private StepConfig[] prefaceStepsWithEnableDebugStep(StepConfig... steps) {
        // Create a step to enable debugging in the AWS Management Console
        StepFactory stepFactory = new StepFactory();

        StepConfig enableDebugging = new StepConfig()
                .withName("Enable debugging")
                .withActionOnFailure(TERMINATE_CLUSTER_ACTION)
                .withHadoopJarStep(stepFactory.newEnableDebuggingStep());

        // Perform a null check on 'steps' and filter out all StepConfig objects that are null.
        Stream<StepConfig> stepsStream =
                Utils.requireNonNullElse(steps, Stream.<StepConfig>empty(), Arrays::stream).filter(Objects::nonNull);


        // Preface the non-null steps to the enable debugging step, and collect the resulting Stream<StepConfig> into
        // a StepConfig[]
        return Stream.concat(Arrays.stream(Utils.arrayOf(enableDebugging)), stepsStream).toArray(StepConfig[]::new);
    }

    /**
     * Uploads the provided {@link SparkJobConfiguration} to Amazon S3 as JSON
     *
     * @param configuration                   The {@link SparkJobConfiguration} to upload.
     * @param sparkJobConfigurationS3Location The location in Amazon S3 where the JSON should be uploaded to.
     * @return A {@link PutObjectResult} containing information about the upload to Amazon S3.
     * @throws IOException          If an error occurs while serializing {@code configuration} as a JSON string or if
     *                              that
     *                              JSON string was unable to be converted to a stream for upload to Amazon S3.
     * @throws NullPointerException If any parameter is {@code null}
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    private PutObjectResult uploadSparkJobConfigurationToAmazonS3(@NotNull SparkJobConfiguration configuration,
            LocalizedS3ObjectId sparkJobConfigurationS3Location) throws IOException {
        // Null checking
        Objects.requireNonNull(configuration, "'configuration' cannot be null.");

        // Serialize 'configuration' as JSON
        String json;

        try {
            json = ObjectMapperSingleton.getObjectMapper().writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            String message = String.format("Cannot serialize %s the following as JSON:\n%s",
                    SparkJobConfiguration.class.getName(), configuration);
            throw new JsonGenerationException(message, e);
        }

        // Upload JSON to Amazon S3
        try {
            return S3Helpers.uploadJson(json, sparkJobConfigurationS3Location);
        } catch (UnsupportedEncodingException e) {
            String message = String.format("Cannot upload the following JSON to \"%s\": %s",
                    sparkJobConfigurationS3Location, json);
            throw new IOException(message, e);
        }
    }
}