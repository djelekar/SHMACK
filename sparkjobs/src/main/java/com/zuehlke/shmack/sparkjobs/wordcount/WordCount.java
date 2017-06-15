package com.zuehlke.shmack.sparkjobs.wordcount;

import com.zuehlke.shmack.sparkjobs.base.SortedCounts;
import com.zuehlke.shmack.sparkjobs.base.TestableSparkJob;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @see http://spark.apache.org/examples.html
 */
public class WordCount extends TestableSparkJob<SortedCounts<String>> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String inputFile;

    public WordCount(final String inputFile) {
        this.inputFile = inputFile;
    }

    @SuppressWarnings("serial")
    @Override
    public SortedCounts<String> execute(final JavaSparkContext spark) {
        final JavaRDD<String> textFile = spark.textFile(inputFile);
        final JavaRDD<String> words = textFile.flatMap(s -> {
            final Status tweet = TwitterObjectFactory.createStatus(s);
            String text = tweet.getText();
            return Arrays.asList(text.split(" "));
        });
        final JavaPairRDD<String, Integer> pairs = words.mapToPair( s -> new Tuple2<>(s.toLowerCase(), 1));
        final JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b);
        return SortedCounts.create(counts);
    }

    @Override
    public String getApplicationName() {
        return "WordCount";
    }

}
