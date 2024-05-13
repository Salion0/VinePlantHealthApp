package it.unipi.mobile.vineplanthealthapp;

import org.tensorflow.lite.DataType;

import java.util.ArrayList;

import it.unipi.mobile.vineplanthealthapp.ml.Cropnet;

public class Config {

    public static String PATH_TAG = "PATH";
    public static String URI_TAG = "URI";
    public static DataType MODEL_INPUT_DATA_TYPE = DataType.FLOAT32;

    public static String[] LABELS = new String[]{"Black Rot","ESCA","Healty","Leaf Blight"};
    public static int TARGET_HEIGHT = 256;
    public static int WIDTH_HEIGHT= 256;
}
