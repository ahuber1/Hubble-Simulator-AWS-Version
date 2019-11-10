package ahuber.hubble.utils;

import ahuber.hubble.aws.SatelliteConfiguration;
import ahuber.hubble.aws.SparkJobConfiguration;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SerializationTest {
    @Test
    public void testSatelliteConfigurationSerialization() throws IOException {
        SatelliteConfiguration original = new SatelliteConfiguration(1, 2);
        String json = ObjectMapperSingleton.getObjectMapper().writeValueAsString(original);
        SatelliteConfiguration deserialized = ObjectMapperSingleton.getObjectMapper()
                .readValue(json, SatelliteConfiguration.class);
        Assert.assertEquals(deserialized, original);
    }

    @Test
    public void testSparkJobConfigurationSerialization() throws IOException {
        SparkJobConfiguration original = new SparkJobConfiguration("Satellite Name", 1, 1, 2, 3);
        String json = ObjectMapperSingleton.getObjectMapper().writeValueAsString(original);
        SparkJobConfiguration deserialized = ObjectMapperSingleton.getObjectMapper()
                .readValue(json, SparkJobConfiguration.class);
        Assert.assertEquals(deserialized, original);
    }
}
