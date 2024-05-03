package it.unipi.mobile.vineplanthealthapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.tensorflow.lite.support.image.TensorImage

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InferenceActivityTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("it.unipi.mobile.vineplanthealthapp", appContext.packageName)
    }

    @Test
    fun startInferenceActivityWithIntentUsingByteTAG(){
        //TODO pick the first image from the galleryù

        val bitmapImage : Bitmap = BitmapFactory.decodeFile()


    }

}