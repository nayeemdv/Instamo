package com.example.instamo.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.instamo.R
import com.example.instamo.databinding.ActivityAccountSettingBinding
import com.example.instamo.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso


class AccountSettings : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingBinding
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        getUserInfo()

        binding.eProfileBtnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettings, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.eProfileBtnClose.setOnClickListener {
            val intent = Intent(this@AccountSettings, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        binding.eProfileBtnChangeImage.setOnClickListener {
            checker = "clicked"
        }

        binding.eProfileBtnSave.setOnClickListener {
            if (checker == "clicked") uploadProfileImageAndInfo() else updateUserInfoOnly()
        }
    }

    private fun uploadProfileImageAndInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT)
                .show()

            TextUtils.isEmpty(binding.eProfileEtFullname.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }

            TextUtils.isEmpty(binding.eProfileEtUsername.text.toString()) -> {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Profile Settings")
                progressDialog.setMessage("Please wait! Updating...")
                progressDialog.show()

                val fileRef = storageProfileRef!!.child(firebaseUser.uid + ".png")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Toast.makeText(this, "exception:-- $it", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = binding.eProfileEtFullname.text.toString()
                        userMap["username"] =
                            binding.eProfileEtUsername.text.toString().lowercase()
                        userMap["bio"] = binding.eProfileEtBio.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@AccountSettings, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(binding.eProfileEtFullname.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }

            TextUtils.isEmpty(binding.eProfileEtUsername.text.toString()) -> {
                Toast.makeText(this, "username is required", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val userRef: DatabaseReference =
                    FirebaseDatabase.getInstance().reference.child("Users")
                //using hashmap to store values
                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.eProfileEtFullname.text.toString()
                userMap["username"] =
                    binding.eProfileEtUsername.text.toString().lowercase()
                userMap["bio"] = binding.eProfileEtBio.text.toString()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AccountSettings, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getUserInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.eProfileIvProfileImage)
                    binding.eProfileEtFullname.setText(user.getFullname())
                    binding.eProfileEtUsername.setText(user.getUsername())
                    binding.eProfileEtBio.setText(user.getBio())
                }
            }
        })
    }
}