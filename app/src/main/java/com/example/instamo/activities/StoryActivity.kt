package com.example.instamo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.instamo.R
import com.example.instamo.databinding.ActivityStoryBinding
import com.example.instamo.model.Story
import com.example.instamo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding

    private var currentUserId: String = ""
    private var userId: String = ""
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L

    var imagesList: List<String>? = null
    var storyIdsList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId").toString()

        binding.layoutSeen.visibility = View.GONE
        binding.storyDelete.visibility = View.GONE

        if (userId == currentUserId) {
            binding.layoutSeen.visibility = View.VISIBLE
            binding.storyDelete.visibility = View.VISIBLE
        }
        getStories(userId)
        userInfo(userId)

        binding.seenNumber.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyId", storyIdsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        binding.storyDelete.setOnClickListener {

            val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)
                .child(storyIdsList!![counter])

            ref.removeValue().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addViewToStory(storyId: String) {
        val ref =
            FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyId)
                .child("views").child(currentUserId).setValue(true)
    }

    private fun getStories(useId: String) {

        imagesList = ArrayList()
        storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {

                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (snapshot in datasnapshot.children) {
                    val story: Story? = snapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())
                    }
                }


                addViewToStory(storyIdsList!![counter])
                seenNumber(storyIdsList!![counter])
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun seenNumber(storyId: String) {
        val ref =
            FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyId)
                .child("views")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.seenNumber.text = " ${snapshot.childrenCount}"
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo(userid: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.storyProfileImage)

                    binding.storyUsername.text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}

