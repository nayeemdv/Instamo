package com.example.instamo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.activities.MainActivity
import com.example.instamo.R
import com.example.instamo.databinding.CommentItemLayoutBinding
import com.example.instamo.model.Comment
import com.example.instamo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private var context: Context, private var commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {

        return CommentViewHolder(
            CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = commentList[position]

        holder.binding.apply {

            if (comment.getComment() != "") publisherCaption.text = (comment.getComment())

            publisherInfo(publisherImageProfile, publisherUsername, comment.getPublisher())

            publisherUsername.setOnClickListener {

                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("PUBLISHER_ID", comment.getPublisher())
                }
                context.startActivity(intent)
            }

            holder.binding.publisherImageProfile.setOnClickListener {

                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("PUBLISHER_ID", comment.getPublisher())
                }
                context.startActivity(intent)
            }

        }

    }

    inner class CommentViewHolder(val binding: CommentItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun publisherInfo(
        profileImage: CircleImageView,
        username: TextView,
        publisherID: String
    ) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    username.text = (user.getUsername())
                }
            }

        })
    }
}