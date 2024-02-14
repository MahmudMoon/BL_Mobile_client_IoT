package com.example.bl_android_client

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bl_android_client.Contants.BL_REQUEST_CODE
import com.example.bl_android_client.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), HandleBLStateChange {
    lateinit var blConnectivity: BLConnectivity
    private var utils: Utils = Utils(this);
    private lateinit var blConnectivityReceiver: BLConnectivityReceiver
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var adapter: PairedListAdapter;
    private var pairedList = ArrayList<BluetoothDevice>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        activityMainBinding.viewmodel = viewModel

        blConnectivity = BLConnectivity(this);
        viewModel.setBlConnectivity(blConnectivity)

        adapter = PairedListAdapter(pairedList, this, blConnect = blConnectivity)
        activityMainBinding.rvBlPairedList.layoutManager = LinearLayoutManager(this)
        activityMainBinding.rvBlPairedList.adapter = adapter

        blConnectivityReceiver = BLConnectivityReceiver(this as HandleBLStateChange)

        if(blConnectivity.isBLAvailable()){
            if(!blConnectivity.isBLEnable()){
                utils.showToast("Please enable BL")
                requestToEnableBL()
            }
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(blConnectivityReceiver, filter)
        }

        viewModel.disableBLE.observe(this){ disableNow ->
           if(disableNow && blConnectivity.isBLEnable()){
               blConnectivity.disableBL()
           }else if(!disableNow && !blConnectivity.isBLEnable()){
               blConnectivity.enableBL()
           }
        }

    }


    private fun requestToEnableBL(){
        val permissionList = ArrayList<String>()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        @RequiresApi(S)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        @RequiresApi(S)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        if(permissionList.size>0){
            ActivityCompat.requestPermissions(this@MainActivity, permissionList.toTypedArray(), BL_REQUEST_CODE)
        }else{
           blConnectivity.enableBL()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== BL_REQUEST_CODE && grantResults.any { it != PackageManager.PERMISSION_GRANTED }){
           utils.showToast("Permission denied")
        }else if(requestCode== BL_REQUEST_CODE ){
            utils.showToast("Permission accepted")
            blConnectivity.enableBL()
        }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        viewModel.blStatus.set(blConnectivity.isBLEnable())
        if(blConnectivity.isBLEnable()){
            reloadPairedDeviceList()
        }else{
            activityMainBinding.rvBlPairedList.visibility = View.GONE
        }
    }

    @SuppressLint("MissingPermission")
    fun reloadPairedDeviceList(){
        if(activityMainBinding.rvBlPairedList.visibility==View.GONE) {
            activityMainBinding.rvBlPairedList.visibility = View.VISIBLE
        }
        blConnectivity.startBLScan()
        pairedList.clear();
        blConnectivity.pairedDeviceList()?.let {
            Log.d(TAG, "onResume: Paired list")
            it.forEach {
                Log.d(TAG, "reloadPairedDeviceList: "+it)
                Log.d(TAG, "onResume: "+ it.name + " => "+ it.address)
            }
            pairedList.addAll(it)
            adapter = PairedListAdapter(pairedList, this, blConnect = blConnectivity)
            activityMainBinding.rvBlPairedList.adapter =  adapter
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(blConnectivityReceiver)
    }

    override fun blStateChange(state: Int) {
        if(BluetoothAdapter.STATE_ON == state){
            Log.d(TAG, "blStateChange: BL_ON")
            viewModel.blStatus.set(true)
            if(blConnectivity.isBLEnable()){
                reloadPairedDeviceList()
            }
        }else if(BluetoothAdapter.STATE_OFF == state){
            Log.d(TAG, "blStateChange: BL_OFF")
            viewModel.blStatus.set(false)
            activityMainBinding.rvBlPairedList.visibility = View.GONE
        }
    }


}