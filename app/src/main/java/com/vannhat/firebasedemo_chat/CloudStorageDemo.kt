package com.vannhat.firebasedemo_chat

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_cloud_storage_demo.*
import java.io.ByteArrayOutputStream

class CloudStorageDemo : AppCompatActivity() {

    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_storage_demo)

        btn_upload_bytes.setOnClickListener {
            createReference()
        }
    }

    private fun createReference() {
        val storageDef = storage.getReferenceFromUrl("gs://chatapp-firebasedemo.appspot.com/")
        val imageRef = storageDef.child("images/girls/TamTit.jpg")

        img_girl.isDrawingCacheEnabled = true
        img_girl.buildDrawingCache()
        val bitmap = (img_girl.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data).addOnSuccessListener {
            createLog("Uploading successful")
        }.addOnFailureListener {
            createLog("Fail : " + it.message)
        }
    }
}
