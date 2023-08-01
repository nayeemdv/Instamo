package com.example.instamo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.R
import com.example.instamo.databinding.UserItemLayoutBinding
import com.example.instamo.fragments.ProfileFragment
import com.example.instamo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    private var context: Context,
    private var userList: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        return UserItemViewHolder(
            UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        //to display the user data
        val user = userList[position]

        holder.binding.apply {

            userItemSearchUsername.text = user.getUsername()
            userItemSearchFullname.text = user.getFullname()
            Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                .into(userItemImage) //add picasso dependency for image caching and downloading

            checkFollowingStatus(user.getUid(), userItemFollow)

            //to go to searched user's profile

            userItem.setOnClickListener {
                val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.getUid())
                pref.apply()

                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }

            holder.binding.userItemFollow.setOnClickListener {
                if (userItemFollow.text.toString() == "Follow") {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.getUid())
                            .setValue(true).addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    firebaseUser?.uid.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.getUid())
                                            .child("Followers").child(it1.toString())
                                            .setValue(true).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    pushNotification(user.getUid())
                                                }
                                            }
                                    }
                                }
                            }
                    }
                } else {
                    if (userItemFollow.text.toString() == "Following") {
                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(user.getUid())
                                .removeValue()
                                .addOnCompleteListener { task -> //reversing following action
                                    if (task.isSuccessful) {
                                        firebaseUser?.uid.let { it1 ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("Follow").child(user.getUid())
                                                .child("Followers").child(it1.toString())
                                                .removeValue().addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                    }
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    class UserItemViewHolder(val binding: UserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


    private fun pushNotification(userId: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userId)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "started following you"
        notifyMap["postId"] = ""
        notifyMap["isPost"] = false

        ref.push().setValue(notifyMap)
    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(uid).exists()) {
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }
            }
        })
    }
}