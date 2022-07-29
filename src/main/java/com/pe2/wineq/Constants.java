package com.pe2.wineq;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Constants {
    public static final Logger logger =
            LogManager.getLogger(MLTrainer.class);

    public static final String BUCKET_NAME = System.getProperty("BUCKET_NAME");

    public static final String ACCESS_KEY_ID = System.getProperty("ACCESS_KEY_ID");
    public static final String SECRET_KEY = System.getProperty("SECRET_KEY");

    public static final String TRAINING_DATASET = "TrainingDataset.csv";
    public static final String VALIDATION_DATASET =  "data/ValidationDataset.csv";
    public static final String MODEL_PATH = "data/TrainingModel";
    public static final String TESTING_DATASET =  "data/TestDataset.csv";

    public static final String VALUE = "2147480000";
    public static final String FEATURES = "features";
    public static final String LABEL = "label";

    public static final String APP_NAME = "Wine-quality-predictor";
}
