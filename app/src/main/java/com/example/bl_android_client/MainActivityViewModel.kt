package com.example.bl_android_client

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val TAG = "MainActivityViewModel"

class MainActivityViewModel: ViewModel() {
    private var _disableBL = MutableLiveData<Boolean>()
    val disableBLE : LiveData<Boolean>
    get() = _disableBL

    var blStatus = ObservableBoolean(false)
    private var blConnectivity: BLConnectivity? = null

    fun setBlConnectivity(blCon: BLConnectivity?){
        this.blConnectivity = blCon
    }

    fun openBlSettings(){
        blConnectivity?.openSettingsScreen()
    }

    fun changeStatus(){
        Log.d(TAG, "changeStatus: ")
        if(blStatus.get()){
            blStatus.set(false)
            _disableBL.postValue(true)
            Log.d(TAG, "changeStatus: disbale done")
        }else{
            blStatus.set(true)
            Log.d(TAG, "changeStatus: enable")
            _disableBL.postValue(false)
        }
    }
}
