package com.vannhat.firebasedemo_chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_storage.setOnClickListener {
            startActivity(Intent(this, CloudStorageDemo::class.java))
        }

        var i = 0

        //getRealTimeForMultipleDoc()
        //viewChangeBetweenSnapshots()
        btn_go.setOnClickListener {
            updateElementInArray()
        }

    }

    // ===================== Create,update,delete Data ====================

    private fun initData(): HashMap<String, Any> {
        val arrayVehicle = listOf("toyota", "mec", "yamaha", "moto")

        val user = hashMapOf(
            "First" to "Batched Write created",
            "Last" to "Get",
            "born" to 1969,
            "vehicle" to arrayVehicle
        )

        // create a nested data
        val nestedName = hashMapOf(
            "oldName" to "Old Name",
            "secondName" to "Second Name",
            "thirdName" to "Third Name"
        )

        //assign nestedData to its parent
        user["NestedObject"] = nestedName

        return user
    }

    /**
     * create a documents:  if doc doesn't exist, its content will be created
     * Unless it will be override
     */
    private fun createDocument(data: HashMap<String, Any>, docPath: String) {
        db.collection("users").document(docPath)
            .set(data)
            .addOnSuccessListener {
                Log.d("cccc", "Added !")
            }.addOnFailureListener {
                Log.d("cccc", "Error: ${it.message}")
            }
    }

    /**
     * add a field to a doc
     * if this field doesn't exist, its content will be created
     *  Unless it will be override
     *  SetOptions.merge() : merge data if data was exist, unless data will be overridden
     */
    private fun addFileToDoc() {
        // add file Address to doc
        val address = hashMapOf("Address" to "DaNang-2")
        db.collection("users").document("NHAT").set(address, SetOptions.merge())
    }

    /**
     * update data without override. we have 3 method for this func
     */
    private fun updateDataInDoc(dbParam: FirebaseFirestore? = null, doc: String, content: String) {
        val dbTemp = dbParam ?: db

        dbTemp.collection("users").document(doc).update(
            mapOf(
                "First" to content,
                "NestedObject" to mapOf("oldName" to "updated map oldName")
            )
        ).addOnSuccessListener {
            createLog("Update success!")
        }.addOnFailureListener {
            createLog("Fail : " + it.message)
        }

    }

    /**
     * update element in an array inside a doc
     */
    private fun updateElementInArray() {
        // add an element
        db.collection("users").document("hasList")
            .update("vehicle", FieldValue.arrayUnion("air blade 1"))

        // remove an element by value
        db.collection("users").document("hasList")
            .update("vehicle", FieldValue.arrayRemove("mec"))
    }

    /**
     * transaction : edit a document when you need to read its old Data
     * This method is faster than "Batched write" method
     */
    private fun editDocWithTransaction() {
        val doc = db.collection("users").document("Transaction")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(doc)
            val newBorn = snapshot.getDouble("born")?.plus(3)
            snapshot.getDouble("born")?.toString()?.let { it1 -> createLog(it1) }
            transaction.update(doc, "born", newBorn)
        }.addOnSuccessListener {
            createToast(this, "The age has been added!")
        }
    }

    /**
     *Batched write : when you don't need get it's old data
     * using when you wanna update multiple docs
     */
    private fun editDocumentWithBatchedWrite() {
        // get a new write batch
        val batch = db.batch()

        // create a new doc
        val batchedWriteRef = db.collection("users").document("batchedWrite")
        batch.set(batchedWriteRef, initData())

        // update a property in NHAT
        val nhatWriteRef = db.collection("users").document("NHAT")
        batch.update(nhatWriteRef, "First", "Nhat update by Batched Write")

        // delete a doc: aaaa , if this doc not exist, delete will be not execute
        val aaaaWriteRef = db.collection("users").document("aaaa")
        batch.delete(aaaaWriteRef)

        // => Commit the batch -> execute in firebase
        batch.commit().addOnSuccessListener {
            createToast(this, "Commit is successful")
        }.addOnFailureListener {
            createToast(this, "Fail: " + it.message)
        }

    }

    private fun deleteDocs(docName: String) {
        db.collection("users").document(docName).delete().addOnSuccessListener {
            createToast(this, "Deleted!")
        }.addOnFailureListener {
            createToast(this, "Failed: " + it.message)
        }
    }

    private fun deleteField(docName: String, field: String) {
        db.collection("users").document(docName).update(
            mapOf(field to FieldValue.delete())
        ).addOnSuccessListener {
            createToast(this, "Deleted!")
        }.addOnFailureListener {
            createToast(this, "Failed: " + it.message)
        }
    }

    // ===================== Read Data ====================

    /**
     * Source could be:
     *   - DEFAULT : if app can't reach to server, data will be fetched from CACHE instead
     *   - CACHE : Data only can be fetched from CACHE (local) which place data has been saved before from sever
     *   - SERVER : Data only can be fetched from SERVER
     */
    private fun getSingleDocFromSource() {
        val docRef = db.collection("users").document("NHAT")
        val cacheSource = Source.DEFAULT

        docRef.get(cacheSource).addOnSuccessListener {
            val userData = it.toObject(User::class.java)
            createLog("Document data: ${it?.data}")
            createLog("Object document data: ${userData?.First}")
        }.addOnFailureListener {
            createLog("Cached document data: ${it.message}")
        }
    }

    /**
     * We can using WHERE to refine result, unless get all docs in collection
     */
    private fun getMutilpleDocs() {
        db.collection("users")
            .whereEqualTo("born", 1975) // where
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val userData = doc.toObject(User::class.java)
                    createLog("user: " + userData.First)
                }
            }

    }


    /**
     * Receive realtime data, get data whenever data has change
     * snapshot.metadata.hasPendingWrites() : indicates whether the document has changed by local
     *      that haven't been write to Server yet. It == true when new data (from CACHE) hasn't been updated to               Server yet.
     *
     * We can pass param MetadataChanges.INCLUDE : update data when CACHE changes and SERVER changes
     *    MetadataChanges.EXCLUDE : (default)  only show change nearest realtime
     */
    private fun getRealTimeData() {
        val nhatRef = db.collection("users").document("NHAT")
        nhatRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                createLog("Listener null: " + e.message)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                " Local "
            else
                "Server "

            if (snapshot != null && snapshot.exists())
                createLog("$source, Current data: $snapshot")
            else
                createLog("Current data : null")

        }
    }

    /**
     * We can use isFromCache() method to check whether fetched data which is from Server or Local (CACHE)
     */
    private fun getRealTimeForMultipleDoc() {
        db.collection("users")
            .whereEqualTo("born", 1969)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    createLog("Listener null: " + e.message)
                    return@addSnapshotListener
                }

                val source = if (value != null && value.metadata.isFromCache)
                    " Local "
                else
                    "Server "

                val refinedUsers = mutableListOf<String>()
                if (value != null) {
                    for (user in value)
                        user.getString("First")?.let { refinedUsers.add(it) }
                }

                createLog("From $source,  Current users: $refinedUsers")
            }
    }

    /**
     * Only fetch data which has been change include ADDED, MODIFIED, REMOVED
     *
     */
    private fun viewChangeBetweenSnapshots() {
        db.collection("users")
            /*.whereEqualTo("First", "update BB 1")*/
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    createLog("Listener null: " + e.message)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> createLog("ADDED user : ${dc.document.data}")
                        DocumentChange.Type.MODIFIED -> createLog(
                            "MODIFIED user : ${dc.document.data}"
                        )
                        DocumentChange.Type.REMOVED -> createLog(
                            "REMOVED user : ${dc.document.data}"
                        )
                    }

            }
    }

    /**
     * We can query offline with Firebase, data will be saved in local (CACHE area)
     * With android and IOS, query data from local is enable default
     * We can disable query data from local with PersistenceEnabled option to false
     *
     * By the way, we can set size for CACHE with setCacheSizeBytes().
     *      When CACHE is full, Firebase will try to clear up documents which are older or unused
     */
    private fun queryOfflineData() {
        try {
            db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // set enable = false
                .setCacheSizeBytes(19001569) // set size for CACHE
                .build()
            updateDataInDoc(db, "NHAT", "new content4")
        } catch (e: IllegalStateException) { // will be crashed without try-catch
            createLog("Error: " + e.message)
        }

    }

    /**
     * When disableNetwork():
     *      all queried data will be take from local (CACHE) even device has network access
     */
    private fun disableFirebaseAccess() {
        db.disableNetwork().addOnFailureListener {
            //
        }

        db.enableNetwork().addOnFailureListener {
            //
        }
    }

    private fun detachListener() {
        val query = db.collection("users")
        val registration = query.addSnapshotListener { _, _ ->
            // Dosome thing
        }
        // ...
        // Stop listener
        registration.remove()
    }
}
