package it.unipi.mobile.vineplanthealthapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InferenceActivityTest {

    private val fileName:String ="immagineTest.jpg"
    private val file:File = File("${getGalleryPath()}/$fileName")
    private fun appContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }
    private fun getGalleryPath():File {
        val folder: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

        if (!folder.exists()) {
            folder.mkdir();
        }

        return folder
    }
    @Test
    fun startInferenceActivityWithIntentUsingUriTag(){

        saveFile()
        val uri: Uri = file.toUri()
        Log.d("uri","$uri")

        //create intent to starts InferencePhaseActivity
        val intent: Intent = Intent(appContext(),InferencePhaseActivity::class.java)
        intent.putExtra(Config.URI_TAG,uri.toString())


        val activity  = ActivityScenario.launch<InferencePhaseActivity>(intent)
        assert(activity.state.isAtLeast(Lifecycle.State.CREATED))
    }
    private fun saveFile() {
        val imageToSave:Bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getInstrumentation().context.resources,
            it.unipi.mobile.vineplanthealthapp.test.R.drawable.test_img_0)
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}