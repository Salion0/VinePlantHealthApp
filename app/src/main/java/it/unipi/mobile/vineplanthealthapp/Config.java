package it.unipi.mobile.vineplanthealthapp;

import org.tensorflow.lite.DataType;

import it.unipi.mobile.vineplanthealthapp.ml.Cropnet;

public class Config {

    public static String PATH_TAG = "PATH";
    public static String URI_TAG = "URI";
    public static DataType MODEL_INPUT_DATA_TYPE = DataType.UINT8;


    public static int TARGET_HEIGHT = 224;
    public static int WIDTH_HEIGHT= 224;
}
