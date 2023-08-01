package com.example.instamo.activities

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instamo.databinding.ActivityAddStoryBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageStoryRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

    }

    private fun uploadStory() {
        when (imageUri) {
            null -> {
                Toast.makeText(this,"Please select Image", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait while your story is added")
                progressDialog.show()


                val fileRef=storageStoryRef!!.child(System.currentTimeMillis().toString()+".jpg")

                val uploadTask: StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                })
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val downloadUrl = task.result
                            myUrl = downloadUrl.toString()


                            val ref = FirebaseDatabase.getInstance().reference
                                .child("Story")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)

                            val storyId = (ref.push().key).toString()

                            val timeEnd =
                                System.currentTimeMillis() + 86400000 //864000 is the milliSec conversion for 24hrs//The timeSpan to expire the story

                            val storyMap = HashMap<String, Any>()

                            storyMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
                            storyMap["timeStart"] = ServerValue.TIMESTAMP
                            storyMap["timeEnd"] = timeEnd
                            storyMap["imageUrl"] = myUrl
                            storyMap["storyId"] = storyId

                            ref.child(storyId).updateChildren(storyMap)

                            Toast.makeText(this, "Story Added!!", Toast.LENGTH_SHORT)
                                .show()

                            finish()


                            progressDialog.dismiss()

                        } else {
                            progressDialog.dismiss()
                        }


                    }

            }
        }
    }
}