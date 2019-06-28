package com.vannhat.firebasedemo_chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


fun createToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun createLog(message: String) {
    Log.d("ccccc", message)
}

fun isPermissionGranted(activity: AppCompatActivity): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

    return ContextCompat.checkSelfPermission(activity,
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
}


fun requestPermissionW(activity: AppCompatActivity) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        createLog("explain permission")
    } else {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }
}

const val PERMISSION_REQUEST_CODE = 1