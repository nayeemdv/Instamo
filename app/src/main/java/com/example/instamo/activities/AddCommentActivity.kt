package com.example.instamo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamo.R
import com.example.instamo.adapter.CommentAdapter
import com.example.instamo.databinding.ActivityAddCommentBinding
import com.example.instamo.model.Comment
import com.example.instamo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class AddCommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommentBinding
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentAdapter? = null
    private var commentList: MutableList<Comment>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.comments_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Comments"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        binding.recyclerviewComments.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)

            commentList = ArrayList()
            commentAdapter = CommentAdapter(context, commentList as ArrayList<Comment>)
            adapter = commentAdapter
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val postId = intent.getStringExtra("POST_ID")

        getImage()
        readComments(postId!!)
        getPostImage(postId)


        binding.postComment.setOnClickListener {
            if (binding.addComment.text.toString() == "") {
                Toast.makeText(this, "You can't send an empty comment", Toast.LENGTH_SHORT).show()
            } else {
                postComment(postId)
            }
        }

    }

    private fun postComment(postId: String) {

        val commentRef: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        val commentMap = HashMap<String, Any>()
        commentMap["publisher"] = firebaseUser!!.uid
        commentMap["comment"] = binding.addComment.text.toString()

        commentRef.push().setValue(commentMap)
        pushNotification(postId)
        binding.addComment.setText("")
        Toast.makeText(this, "posted!!", Toast.LENGTH_LONG).show()
    }

    private fun getImage() {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.userProfileImage)
                }
            }
        })
    }

    private fun pushNotification(postID: String) {

        val ref = FirebaseDatabase.getInstance()
            .reference.child("Notification")
            .child(firebaseUser!!.uid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "commented : ${binding.addComment.text}"
        notifyMap["postId"] = postID
        notifyMap["isPost"] = true

        ref.push().setValue(notifyMap)
    }

    private fun readComments(postId: String) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                commentList?.clear()
                for (snapshot in p0.children) {
                    val cmnt: Comment? = snapshot.getValue(Comment::class.java)
                    commentList!!.add(cmnt!!)
                }
                commentAdapter!!.notifyDataSetChanged()
            }
        })
    }

    private fun getPostImage(postId: String) {
        val postRef = FirebaseDatabase.getInstance()
            .reference.child("Posts")
            .child(postId).child("postImage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val image = p0.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(binding.postImageComment)
                }
            }
        })
    }
}