package com.vannhat.firebasedemo_chat

import android.content.Context
import android.util.Log
import android.widget.Toast


fun createToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun createLog(message: String) {
    Log.d("ccccc", message)
}