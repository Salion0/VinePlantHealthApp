package it.unipi.mobile.vineplanthealthapp.ui.home

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.unipi.mobile.vineplanthealthapp.utils.GalleryUtils
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils
import java.io.File

class HomeViewModel : ViewModel() {


    private var mainUtils: MainUtils = MainUtils()
    private val _text = MutableLiveData<String>().apply {
        value = "Hello! This application is a university project, the aim of this app is to recognize " +
                "diseases in winegrapes, in order to do so there is a menu on the left where you can find all the functionalities"
    }
    private val _contactText = MutableLiveData<String>().apply {
        value =" · Edoardo Focacci, e.focacci@studenti.unipi.it · Salvatore Patisso, s.patisso@studenti.unipi.it · Antonino Patania, a.patania4@studenti.unipi.it · Emmanuel Piazza, e.piazza3@studenti.unipi.it"
    }

    private val _statsGallery = MutableLiveData<List<Int>>().apply {
        val picturesDirectoryDefault =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val picturesDirectory = File(picturesDirectoryDefault, "VinePlantApp")

        if (!picturesDirectory.exists()) {
            if (picturesDirectory.mkdirs()) {
                Log.d("MyApp", "Directory created")
            } else {
                Log.d("MyApp", "Failed to create directory")
            }
        }
        val imageFiles = picturesDirectory.listFiles()
        val list= arrayListOf<Int>()
        if (imageFiles != null && imageFiles.isNotEmpty()) {
            val numOfPlants = imageFiles.size
            var numHealthy = 0
            val imagesList = mainUtils.createArrayImages(imageFiles)
            for (image in imagesList){
                println(image.plantStatus)
                if(image.plantStatus=="HEALTHY"){
                    numHealthy++
                }
            }
            list.add(numOfPlants)
            list.add(numHealthy)
            list.add(numOfPlants-numHealthy)
        }
        else{
            list.add(0)
            list.add(0)
            list.add(0)
        }
        value= list

    }
    private val _statsText = MutableLiveData<String>().apply {

        value= "Stats\n "+
                "photos: " + _statsGallery.value?.get(0).toString() +
               " healthy: " +_statsGallery.value?.get(1).toString()

    }

    val contactText: LiveData<String> = _contactText
    val text: LiveData<String> = _text
    val statsText: LiveData<String> = _statsText
}