package com.example.bl_android_client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.Method
import java.util.*


private const val TAG = "PairedListAdapter"

class PairedListAdapter(val pairedList: List<BluetoothDevice> , val context: Context, val blConnect: BLConnectivity):
    RecyclerView.Adapter<PairedListAdapter.PairedViewHolder>() {

    inner class PairedViewHolder(itemView: View): ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceNameTextView)
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.deviceAddressTextView)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.rv_item, parent, false)
       return PairedViewHolder(view)
    }

    override fun getItemCount(): Int {
       return pairedList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: PairedViewHolder, position: Int) {
        holder.deviceNameTextView.text = pairedList[position].name
        holder.deviceAddressTextView.text = pairedList[position].address

        holder.itemView.setOnClickListener {
            holder.progressBar.visibility = View.VISIBLE
            Log.d(TAG, "onBindViewHolder: ready to connect")
            blConnect.stopBLScan()
            val uuid: UUID =
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            CoroutineScope(Dispatchers.IO).launch {
                connect(pairedList[position], uuid, holder.progressBar, holder.deviceNameTextView)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(bluetoothDevice: BluetoothDevice, uuid: UUID, process: ProgressBar, title: TextView) {

        var mBluetoothSocket : BluetoothSocket? = null
        try {
            val sdk = Build.VERSION.SDK_INT
            if (sdk >= 10) {
                mBluetoothSocket =
                    bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            } else {
                mBluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            }
            if (mBluetoothSocket != null) {
                if (!mBluetoothSocket.isConnected()) {
                    mBluetoothSocket.connect()
                    mBluetoothSocket.outputStream?.write("Hello BL".toByteArray())
                    mBluetoothSocket.close()

                    //for reading
//                    mBluetoothSocket.inputStream?.read()
//                    mBluetoothSocket.close()

                    Log.d(TAG, "connect: Connected")
                    CoroutineScope(Dispatchers.Main).launch {
                        process.visibility = View.GONE
                        title.setTextColor(Color.GREEN)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (connectException: IOException) {
            Log.d(TAG, "connect: failed "+ connectException.localizedMessage)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
            }
            connectException.printStackTrace()
            try {
                val m: Method = bluetoothDevice.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                )
                mBluetoothSocket = m.invoke(bluetoothDevice, 1) as BluetoothSocket
                mBluetoothSocket.connect()
                CoroutineScope(Dispatchers.Main).launch {
                    process.visibility = View.GONE
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(TAG, "connect: Connected")
            } catch (e: Exception) {
                Log.d(TAG, "connect: failed "+ e.localizedMessage)
                Log.e("BLUE", e.toString())
                try {
                    mBluetoothSocket?.close()
                    CoroutineScope(Dispatchers.Main).launch {
                        process.visibility = View.GONE
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d(TAG, "connect: closed ")
                } catch (ie: IOException) {
                    ie.printStackTrace()
                    Log.d(TAG, "connect: failed "+ ie.localizedMessage)
                    CoroutineScope(Dispatchers.Main).launch {
                        process.visibility = View.GONE
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}