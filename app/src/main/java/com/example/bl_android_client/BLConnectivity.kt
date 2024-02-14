package com.example.bl_android_client

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity


private const val TAG = "BLConnectivity"

class BLConnectivity(val context: Context) {

    private var blMmanager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
    private lateinit var blAdapter: BluetoothAdapter;
    private var utils: Utils = Utils(context);

    fun isBLAvailable(): Boolean{
        if(blMmanager==null){
            utils.showToast("BL not supported")
        }else{
            blAdapter = blMmanager.adapter
            if(blAdapter==null){
                utils.showToast("BL not supported")
            }else{
                utils.showToast("BL supported")
            }
            return blAdapter!=null
        }
        return false
    }

    fun isBLEnable(): Boolean{
       return blAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    fun enableBL() {
        context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    @SuppressLint("MissingPermission")
    fun disableBL(){
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    fun stopBLScan(){
        if(!blAdapter.isDiscovering) {
            Log.d(TAG, "startBLScan: Start")
            blAdapter.cancelDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    fun startBLScan(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "startBLScan: PERMISSION DENIED")
            return
        }
        if(!blAdapter.isDiscovering) {
            Log.d(TAG, "startBLScan: Start")
            blAdapter.startDiscovery()
            Log.d(TAG, "startBLScan: Calling pair..")
        }
    }

    @SuppressLint("MissingPermission")
    fun pairedDeviceList(): List<BluetoothDevice>? {
        val list = blAdapter.bondedDevices.toList()
        Log.d(TAG, "pairedDeviceList: size "+ list.size)
        return list

    }

    fun openSettingsScreen(){
        val intent = Intent()
        intent.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        context.startActivity(intent)
    }
}