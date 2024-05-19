package it.unipi.mobile.vineplanthealthapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Hello! This application is a university project, the aim of this app is to recognize" +
                "diseases in winegrapes, in order to do so there is a menu on the left where you can find all the functionalities"
    }
    private val _contactText = MutableLiveData<String>().apply {
        value = "e.focacci@studenti.unipi.it"
    }
    private val _contactTextButton = MutableLiveData<String>().apply {
        value = "Contacts"
    }
    private val _statsTextButton = MutableLiveData<String>().apply {
        value = "Stats"
    }
    private val _numberOfPlants = MutableLiveData<Int>().apply {
        value = 15
    }
    private val _numberHealty = MutableLiveData<Int>().apply {
        value = 2
    }
    private val _numberDisease = MutableLiveData<Int>().apply {
        value = 3
    }
    private val _statsText = MutableLiveData<String>().apply {
        value= "Plants: " + _numberOfPlants.value +
                " Healty: " + _numberHealty.value  +
                " Diseases: " + _numberDisease.value
    }
    val contactText: LiveData<String> = _contactText
    val text: LiveData<String> = _text
    val contactTextButton: LiveData<String> = _contactTextButton
    val statsTextButton: LiveData<String> = _statsTextButton
    val statsText: LiveData<String> = _statsText
}