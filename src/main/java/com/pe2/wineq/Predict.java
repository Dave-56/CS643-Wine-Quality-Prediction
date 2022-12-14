package com.pe2.wineq;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static com.pe2.wineq.Constants.*;

import java.io.File;

public class Predict {

    public static void main(String[] args) {

        Logger.getLogger("org").setLevel(Level.ERROR);
        Logger.getLogger("akka").setLevel(Level.ERROR);
        Logger.getLogger("breeze.optimize").setLevel(Level.ERROR);
        Logger.getLogger("com.amazonaws.auth").setLevel(Level.DEBUG);
        Logger.getLogger("com.github").setLevel(Level.ERROR);


        SparkSession spark = SparkSession
                .builder()
                .appName(APP_NAME)
                .master("local[*]")
                .config("spark.executor.memory", VALUE)
                .config("spark.driver.memory", VALUE)
                .config("spark.testing.memory", VALUE)
                .getOrCreate();

        if (StringUtils.isNotEmpty(ACCESS_KEY_ID) && StringUtils.isNotEmpty(SECRET_KEY)) {

            spark.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", ACCESS_KEY_ID);
            spark.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", SECRET_KEY);

        }

        File tempFile = new File(VALIDATION_DATASET);
        boolean exists = tempFile.exists();
        if(exists){
            Predict parser = new Predict();
            parser.logisticRegression(spark);
        }else{
            logger.error("The Validation dataset.csv file does not exist..ensure the file is in data folder");
        }



    }


    public void logisticRegression(SparkSession spark) {
        logger.info("ValidationDataSet Metrics \n");
        PipelineModel pipelineModel = PipelineModel.load(MODEL_PATH);
        Dataset<Row> testDf = getDataFrame(spark, true, VALIDATION_DATASET).cache();
        Dataset<Row> predictionDF = pipelineModel.transform(testDf).cache();
        predictionDF.select(FEATURES, LABEL, "prediction").show(5, false);
        printMertics(predictionDF);

    }

    public Dataset<Row> getDataFrame(SparkSession spark, boolean transform, String name) {

        Dataset<Row> validationDf = spark.read().format("csv").option("header", "true")
                .option("multiline", true).option("sep", ";").option("quote", "\"")
                .option("dateFormat", "M/d/y").option("inferSchema", true).load(name);

        Dataset<Row> lblFeatureDf = validationDf.withColumnRenamed("quality", LABEL).select(LABEL,
                "alcohol", "sulphates", "pH", "density", "free sulfur dioxide", "total sulfur dioxide",
                "chlorides", "residual sugar", "citric acid", "volatile acidity", "fixed acidity");

        lblFeatureDf = lblFeatureDf.na().drop().cache();

        VectorAssembler assembler =
                new VectorAssembler().setInputCols(new String[]{"alcohol", "sulphates", "pH", "density",
                        "free sulfur dioxide", "total sulfur dioxide", "chlorides", "residual sugar",
                        "citric acid", "volatile acidity", "fixed acidity"}).setOutputCol(FEATURES);

        if (transform)
            lblFeatureDf = assembler.transform(lblFeatureDf).select(LABEL, FEATURES);


        return lblFeatureDf;
    }


    public void printMertics(Dataset<Row> predictions) {
        System.out.println();
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator();
        evaluator.setMetricName("accuracy");
        logger.info("The accuracy of this model is " + evaluator.evaluate(predictions));

        evaluator.setMetricName("f1");
        double f1 = evaluator.evaluate(predictions);

        logger.info("F1: " + f1);
    }
}
