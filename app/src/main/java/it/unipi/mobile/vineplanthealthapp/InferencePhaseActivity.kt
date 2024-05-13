package it.unipi.mobile.vineplanthealthapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import it.unipi.mobile.vineplanthealthapp.ml.Model
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp


class InferencePhaseActivity : AppCompatActivity() {
//This activity should be started after taking a picture, //intent extras below:
    //<PATH>,"path-to-the-img-file"
    //<BYTE>,ByteArray of the image

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inference_results)
       if (intent != null) {
           //get data images from intent
           try {
               val extras: Bundle = intent.extras!!
               val tensorImage: TensorImage = getTensorFromExtras(extras)
               val labelRes:String = classify(tensorImage)

               val imageView: ImageView = findViewById(R.id.classifiedImg)!!
               imageView.setImageBitmap(tensorImage.bitmap)
               val labelView = findViewById<TextView>(R.id.label)
               labelView.text = labelRes


           }catch(e: Exception){
               Log.e("NoDATA","No data send to the activity.\n${e.printStackTrace()}")
           }


       }
    }
    private fun classify(tensorImage: TensorImage):String{
        val model:Model = Model.newInstance(baseContext)
        val label:String
        val results = model.process(tensorImage.tensorBuffer).outputFeature0AsTensorBuffer.floatArray
        Log.d("Output Shape","${results.size}")
        var logValue:String = ""
        results.forEach { logValue = logValue.plus(" $it ") }
        Log.d("Output values",logValue)
        label = Config.LABELS[results.indices.maxBy { results[it] }]
        return label
    }
    private fun getTensorFromExtras(extras:Bundle):TensorImage{
        //extract,resize and load an image into an image-
        //-tensor from the intent extras.
        //Intent extras can contain image data in different forms
        //It can contain a Path,URI ecc.
        var tensorImage:TensorImage = TensorImage(Config.MODEL_INPUT_DATA_TYPE)
        if (extras.keySet().size == 1){
            when(val tag : String = extras.keySet().first())
            {
                Config.PATH_TAG -> {
                    try{
                        val bitmap:Bitmap = BitmapFactory.decodeFile(extras.getString(Config.PATH_TAG))
                        tensorImage.load(bitmap)
                    }
                    catch (e : Exception) {
                        Log.e("ImageTensorLoading","Error in decoding and loading file:\n ${e.printStackTrace()}")
                    }

                }
                Config.URI_TAG -> {
                    val uri:Uri= extras.getString(Config.URI_TAG)!!.toUri()
                    val bitmap:Bitmap = BitmapFactory.decodeFile(uri.path)
                    tensorImage.load(bitmap)
                }
                else -> {
                    Log.e("SwitchERROR","Not recognized tag!: $tag")
                }
            }
            tensorImage = ImageProcessor.Builder()
                .add(ResizeOp(Config.TARGET_HEIGHT, Config.WIDTH_HEIGHT, ResizeOp.ResizeMethod.BILINEAR))
                .build().process(tensorImage)
        }
        return tensorImage
    }


}