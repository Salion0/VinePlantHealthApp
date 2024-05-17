package it.unipi.mobile.vineplanthealthapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.unipi.mobile.vineplanthealthapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        val contactText: TextView = binding.textContacts
        val statText: TextView = binding.textStats
        val buttonContacts: Button = binding.buttonContacts
        val buttonStats: Button = binding.buttonStats
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        homeViewModel.contactTextButton.observe(viewLifecycleOwner){
            buttonContacts.text = it
        }
        homeViewModel.statsTextButton.observe(viewLifecycleOwner){
            buttonStats.text = it
        }
        // Initially hide the second TextView
        contactText.visibility = View.GONE
        statText.visibility = View.GONE
        // Setting up the button click listener
        buttonContacts.setOnClickListener {
            // Toggle the visibility of the additional TextView
            if (contactText.visibility == View.VISIBLE) {
                contactText.visibility = View.GONE
            } else {
                contactText.visibility = View.VISIBLE
                homeViewModel.contactText.observe(viewLifecycleOwner) {
                    contactText.text = it
                }
            }
        }
        buttonStats.setOnClickListener {
            if(statText.visibility == View.VISIBLE){
                statText.visibility = View.GONE
            } else {
                statText.visibility = View.VISIBLE
                homeViewModel.statsText.observe(viewLifecycleOwner){
                    statText.text = it
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}