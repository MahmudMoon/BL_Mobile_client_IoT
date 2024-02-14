package com.example.bl_android_client

import android.content.Context
import android.widget.Toast

class Utils(val context: Context) {
    fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}