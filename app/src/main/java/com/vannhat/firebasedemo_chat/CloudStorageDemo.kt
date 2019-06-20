package com.vannhat.firebasedemo_chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_cloud_storage_demo.*
import java.io.ByteArrayOutputStream

class CloudStorageDemo : AppCompatActivity() {

    private val storage = FirebaseStorage.getInstance().reference
    private var imgPath: Uri? = null
    private var uploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_storage_demo)
        handleClick()


    }

    private fun handleClick() {
        btn_upload_bytes.setOnClickListener {
            uploadBytes()
        }
        img_upload.setOnClickListener {
            if (isPermissionGranted(this)) requestPermissionW(this)
            else pickImageUriFromDevice()
        }
        btn_upload_file.setOnClickListener {
            imgPath?.let { uri ->
                uploadLocalFile(uri)
                btn_upload_pause.text = getString(R.string.pause)
            }
        }

        btn_upload_pause.setOnClickListener {
            if (uploadTask?.isPaused == true) {
                btn_upload_pause.text = getString(R.string.pause)
                uploadTask?.resume()
            } else {
                uploadTask?.pause()
                btn_upload_pause.text = getString(R.string.resume)
            }
        }

        btn_upload_cancel.setOnClickListener {
            uploadTask?.cancel()
        }


//        btn_upload_stream.setOnClickListener {
//            imgPath?.path?.split(":")?.get(1)?.let { it1 -> uploadStream(it1) }
//        }
    }

    private fun uploadBytes() {
        val imageRef = storage.child("images/girls/TamTit2.jpg")

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

//    private fun uploadStream(url: String) {
//        val stream = FileInputStream(File(url))
//        val imageRef = storage.child("images/girls/TamtitUrl.jpg")
//
//        imageRef.putStream(stream)
//            .addOnSuccessListener {
//                createToast(this, "stream upload success!")
//            }.addOnFailureListener {
//                createToast(this, "stream upload fail!")
//            }
//    }

    private fun uploadLocalFile(path: Uri) {
        val imageRef = storage.child("images/girls/video.jpg")
        uploadTask = imageRef.putFile(path)
            .addOnSuccessListener {
                createToast(this, "file upload success!")
            }.addOnFailureListener {
                createToast(this, "file upload fail!")
            }

        // track uploading process
        uploadTask?.addOnProgressListener { taskSnapshot ->
            val sum = taskSnapshot.totalByteCount
            val pr = taskSnapshot.bytesTransferred
            val percent = (pr * 100) / sum
            pr_upload.progress = percent.toInt()
            createLog(percent.toString())
        }?.addOnPausedListener {
            createToast(this, "Paused!")
        }?.addOnCanceledListener {
            createToast(this, "Canceled!")
            pr_upload.progress = 0
        }

        // get Link download
        uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful)
                task.exception?.let {
                    throw it
                }
            return@Continuation imageRef.downloadUrl
        })?.addOnSuccessListener {
            Glide.with(this).load(it).into(img_downloaded)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putString("reference", storage.toString())
    }

    private fun pickImageUriFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType(IMAGE_TYPE).setType(VIDEO_TYPE)
            .addCategory(
                Intent.CATEGORY_OPENABLE
            )
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imgPath = data.data
            Glide.with(this).load(data.data).into(img_upload)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val IMAGE_TYPE = "image/*"
        private const val VIDEO_TYPE = "video/*"
    }

}
