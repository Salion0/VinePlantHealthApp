package it.unipi.mobile.vineplanthealthapp;

import org.tensorflow.lite.DataType;

import java.util.ArrayList;

import it.unipi.mobile.vineplanthealthapp.ml.Cropnet;

public class Config {

    public static String EXIF_PLANT_STATUS_TAG = "ImageDescription";
    public static String PATH_TAG = "PATH";
    public static String LABEL_TAG = "LABEL";
    public static String URI_TAG = "URI";
    public static DataType MODEL_INPUT_DATA_TYPE = DataType.FLOAT32;

    public static String[] LABELS = new String[]{"Black Rot","ESCA","Healthy","Leaf Blight"};

    public static String HEALTHY_LABEL  =  LABELS[2];
    public static int TARGET_HEIGHT = 256;
    public static int WIDTH_HEIGHT= 256;
}
