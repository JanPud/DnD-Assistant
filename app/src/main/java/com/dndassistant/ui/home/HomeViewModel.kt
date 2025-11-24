package com.dndassistant.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _uiState = MutableLiveData(HomeUIState())
    val uiState: LiveData<HomeUIState> = _uiState

    val connectingDone = MutableStateFlow(false)

    private val _startAdvertising = MutableLiveData<Unit>()
    val startAdvertising: LiveData<Unit> = _startAdvertising
    private val _startDiscovery = MutableLiveData<Unit>()
    val startDiscovery: LiveData<Unit> = _startDiscovery
    private val _stopAll = MutableLiveData<Unit>()
    val stopAll: LiveData<Unit> = _stopAll

    data class HomeUIState(
        val hostButtonEnabled: Boolean = false,
        val hostButtonState: Boolean = false,
        val hostButtonInfo: String = "Not hosting",

        val discoveryButtonEnabled: Boolean = false,
        val discoveryButtonState: Boolean = false,
        val discoveryButtonInfo: String = "Not discovering",

        val hostTagText: String = "",
        val yourTagText: String = "",
        val otherTagsText: String = "",
        val infoText: String = ""
    )

    fun enableHostButton(enable: Boolean){
        _uiState.value = _uiState.value!!.copy(hostButtonEnabled = enable)
    }
    fun setHostButton(state: Boolean){
        _uiState.value = _uiState.value!!.copy(hostButtonState = state)
        if (state){
            _startAdvertising.value = Unit
            setInfo("Looking for clients")
        } else {
            _stopAll.value = Unit
        }

    }
    fun setHostButtonInfo(info: String){
        _uiState.value = _uiState.value!!.copy(hostButtonInfo = info)
    }

    fun enableDiscoveryButton(enable: Boolean){
        _uiState.value = _uiState.value!!.copy(discoveryButtonEnabled = enable)
    }
    fun setDiscoveryButton(state: Boolean){
        _uiState.value = _uiState.value!!.copy(discoveryButtonState = state)
        if (state){
            _startDiscovery.value = Unit
            setInfo("Looking for host")
        } else {
            _stopAll.value = Unit
        }
    }
    fun setDiscoveryButtonInfo(info: String){
        _uiState.value = _uiState.value!!.copy(discoveryButtonInfo = info)
    }

    fun setHostTag(tag: String){
        _uiState.value = _uiState.value!!.copy(hostTagText = tag)
    }
    fun setYourTag(tag: String){
        _uiState.value = _uiState.value!!.copy(yourTagText = tag)
    }
    fun setOtherTags(tags: List<String>){
        val stringOfTags = tags.joinToString("\n")
        _uiState.value = _uiState.value!!.copy(otherTagsText = stringOfTags)
    }
    fun setInfo(info: String){
        _uiState.value = _uiState.value!!.copy(infoText = info)
    }

    fun resetConnectingAnimation(){
        connectingDone.value = true
        connectingDone.value = false
    }

}