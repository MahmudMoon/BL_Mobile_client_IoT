package com.example.bl_android_client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "BLConnectivityReceiver"

class BLConnectivityReceiver(val handleBLStateChange: HandleBLStateChange): BroadcastReceiver() {
    init {
        Log.d(TAG,  "Reveiver started")
    }
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ")
        val deviceList = ArrayList<String>()
        intent?.action?.let {
            if(BluetoothDevice.ACTION_FOUND==it){
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    val deviceName = device.name ?: "Unknown"
                    val deviceAddress = device.address
                    val deviceString = "$deviceName - $deviceAddress"
                    if (!deviceList.contains(deviceString)) {
                        deviceList.add(deviceString)
                        //arrayAdapter.notifyDataSetChanged()
                        Log.d(TAG, "onReceive: device name: $deviceString")
                    }
                }
            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED == it){
               when(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)){
                   BluetoothAdapter.STATE_OFF -> {
                       // Bluetooth is turned off
                       Log.d(TAG, "onReceive: BL_TURNED_OFF")
                       handleBLStateChange.blStateChange(BluetoothAdapter.STATE_OFF)
                   }
                   BluetoothAdapter.STATE_TURNING_OFF -> {
                       // Bluetooth is turning off
                       Log.d(TAG, "onReceive: BL_TURNING_OFF")
                       handleBLStateChange.blStateChange(BluetoothAdapter.STATE_TURNING_OFF)
                   }
                   BluetoothAdapter.STATE_ON -> {
                       // Bluetooth is turned on
                       Log.d(TAG, "onReceive: BL_TURNED_ON")
                       handleBLStateChange.blStateChange(BluetoothAdapter.STATE_ON)
                   }
                   BluetoothAdapter.STATE_TURNING_ON -> {
                       // Bluetooth is turning on
                       Log.d(TAG, "onReceive: BL_TURNING_ON")
                       handleBLStateChange.blStateChange(BluetoothAdapter.STATE_TURNING_ON)
                   }
                   else ->{
                       Log.d(TAG, "onReceive: OTHER STATE")
                   }
               }
            }
        }
    }
}