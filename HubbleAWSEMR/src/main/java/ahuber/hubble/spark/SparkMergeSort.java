//package ahuber.hubble.spark;
//
//import ahuber.hubble.sort.MergeSortInt;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.api.java.function.Function;
//import org.apache.spark.api.java.function.Function2;
//import org.apache.spark.sql.SparkSession;
//
//import java.util.Arrays;
//import java.util.List;
//
//class SparkMergeSort {
////    static int[] sort(int[] array, int threshold) {
////        SparkConf sparkConf = new SparkConf().setAppName("Hubble_AWS_EMR").setMaster("local[1]");
////        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
////        int[] leftHalf = Arrays.copyOfRange(array, 0, array.length / 2);
////        int[] rightHalf = Arrays.copyOfRange(array, array.length / 2, array.length);
////        List<int[]> list = Arrays.asList(leftHalf, rightHalf);
////        JavaRDD<int[]> dataSet = jsc.parallelize(list, 2);
////        int[] sortedData = dataSet.map((Function<int[], int[]>) half -> {
////            MergeSortInt.sort(half, threshold);
////            return half;
////        }).reduce((Function2<int[], int[], int[]>) MergeSortInt::merge);
////        jsc.stop();
////        return sortedData;
////    }
//
//    static int[] sort(int[] array, int threshold) {
//        SparkSession session = SparkSession.builder().appName("Hubble_AWS_EMR").getOrCreate();
//    }
//}
