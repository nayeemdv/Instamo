package com.example.instamo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.model.Post
import com.example.instamo.R
import com.example.instamo.databinding.NotificationItemBinding
import com.example.instamo.fragments.PostDetailFragment
import com.example.instamo.fragments.ProfileFragment
import com.example.instamo.model.Notification
import com.example.instamo.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(
    private var context: Context,
    private var mNotification: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {


    class NotificationViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {

        return NotificationViewHolder(NotificationItemBinding.inflate(LayoutInflater.from(parent.context)))

    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = mNotification[position]

        holder.binding.apply {

            holder.binding.notificationText.text = notification.getText()

            publisherInfo(notificationImageProfile, notificationUsername, notification.getUserId())
            if (notification.getIsPost()) {
                postedImage.visibility = View.VISIBLE
                getPostedImg(postedImage, notification.getPostId())
            } else {
                postedImage.visibility = View.GONE
            }

            postedImage.setOnClickListener {
                if (notification.getIsPost()) {
                    val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("postId", notification.getPostId())
                    pref.apply()

                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostDetailFragment()).commit()
                } else {

                    val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", notification.getUserId())
                    pref.apply()

                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                }
            }

        }
    }

    private fun publisherInfo(imgView: CircleImageView, username: TextView, publisherId: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imgView)
                username.text = (user.getUsername())
            }
        })
    }

    private fun getPostedImg(postImg: ImageView, postId: String?) {

        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                Picasso.get().load(post!!.getPostImage()).into(postImg)
            }
        })
    }
}
