package it.unipi.mobile.vineplanthealthapp.ui.home

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.unipi.mobile.vineplanthealthapp.Config
import it.unipi.mobile.vineplanthealthapp.R
import it.unipi.mobile.vineplanthealthapp.databinding.FragmentHomeBinding
import it.unipi.mobile.vineplanthealthapp.ui.gallery.Image
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var photosValue = 0
    private var healthyValue = 0
    private val mainUtils = MainUtils()
    private var isFirstViewCreation = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.textHome.text = getString(R.string.home_presentation)
        binding.textContacts.text = getString(R.string.home_contacts)
        updateStats()
        isFirstViewCreation = true
        binding.textStats.text = getString(R.string.stat_text, photosValue, healthyValue)
        return root
    }

    override fun onResume() {
        super.onResume()
        Log.e("HomeFragment", "onResume")
        if (!isFirstViewCreation) {
            updateStats()
        }
        else
            isFirstViewCreation = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("HomeFragment", "onDestroyView")
        _binding = null
    }
    // function to update the number of images number
    private fun updateStats(){
        var images = mutableListOf<Image>()
        val picturesDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"VinePlantApp")
        val imageFiles = picturesDirectory.listFiles()

        if (imageFiles == null) {
            photosValue = 0
            healthyValue = 0
        }
        else {
            photosValue = imageFiles.size
            // print the number of images
            Log.d("MyApp", "Number of images apply: $photosValue")
            images = mainUtils.createArrayImages(imageFiles)
            Log.d("MyApp", "Number of images: ${images.size}")

            for (image in images){
                if(image.plantStatus == Config.HEALTHY_LABEL){
                    healthyValue++
                }
            }
        }
        binding.textStats.text = getString(R.string.stat_text, photosValue, healthyValue)
    }
}