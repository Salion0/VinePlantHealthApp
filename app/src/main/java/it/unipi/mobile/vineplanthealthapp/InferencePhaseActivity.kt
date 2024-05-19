package it.unipi.mobile.vineplanthealthapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import it.unipi.mobile.vineplanthealthapp.ml.Model
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp


class InferencePhaseActivity : AppCompatActivity() {
//This activity should be started after taking a picture, //intent extras below:
    //<PATH>,"path-to-the-img-file"
    //<BYTE>,ByteArray of the image

    private lateinit var storageActivityResultLauncher: ActivityResultLauncher<Intent>
    private fun showPermissionToast() {
        Toast.makeText(
            baseContext,
            "Permission required to access images",
            Toast.LENGTH_SHORT
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inference_results)
        storageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}


        if (intent != null) {
            //get data images from intent
            try {
                val extras: Bundle = intent.extras!!
                val tensorImage: TensorImage = getTensorFromExtras(extras)
                val labelRes: String = classify(tensorImage)

                val imageView: ImageView = findViewById(R.id.classifiedImg)!!
                imageView.setImageBitmap(tensorImage.bitmap)
                val labelView = findViewById<TextView>(R.id.label)
                labelView.text = labelRes
                val saveButton = findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener() {
                    if (checkStoragePermissions()) {
                        val returnIntent = Intent()
                        returnIntent.putExtra(Config.LABEL_TAG, labelRes)
                        returnIntent.putExtra(Config.URI_TAG, extras.getString(Config.URI_TAG))
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    } else {
                        //Android is 11 (R) or above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                val intent = Intent()
                                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                val uri = Uri.fromParts("package", this.getPackageName(),null);
                                intent.setData(uri);
                                storageActivityResultLauncher.launch(intent);
                            } catch (e:Exception) {
                                val intent = Intent()
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                storageActivityResultLauncher.launch(intent);
                            }
                        }
                    }
                }
            } catch (e: Exception){
                    Log.e("NoDATA", "No data send to the activity.\n${e.printStackTrace()}")

            }
        }
    }


    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
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