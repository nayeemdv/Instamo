package com.example.instamo.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instamo.activities.AccountSettings
import com.example.instamo.adapter.MyPostAdapter
import com.example.instamo.model.Post
import com.example.instamo.model.User
import com.example.instamo.R
import com.example.instamo.activities.ShowUsersActivity
import com.example.instamo.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Collections
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    var postList: List<Post>? = null
    var myPostAdapter: MyPostAdapter? = null
    var postListSaved: List<Post>? = null
    var myImagesAdapterSavedImg: MyPostAdapter? = null
    var mySavedImg: List<String>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            binding.editProfileButton.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowOrFollowingButtonStatus()
        }
        //to call account profile setting activity
        binding.editProfileButton.setOnClickListener {
            when (binding.editProfileButton.text.toString()) {
                "Edit Profile" -> startActivity(
                    Intent(
                        context, AccountSettings::class.java
                    )
                )

                "Follow" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow").child(it1)
                            .child("Following").child(profileId).setValue(true)
                        pushNotification()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
                            .child("Followers").child(it1).setValue(true)
                    }
                }

                "Following" -> {

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow").child(it1)
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
                            .child("Followers").child(it1).removeValue()
                    }
                }
            }
        }
        binding.totalFollowers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }
        binding.totalFollowing.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        //to get own feeds
        binding.recyclerviewProfile.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            postList = ArrayList()
            myPostAdapter = context?.let { MyPostAdapter(it, postList as ArrayList<Post>) }
            adapter = myPostAdapter
        }

        binding.rvSavedImage.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            postListSaved = ArrayList()
            myImagesAdapterSavedImg =
                context?.let { MyPostAdapter(it, postListSaved as ArrayList<Post>) }
            adapter = myImagesAdapterSavedImg
        }

        //Default
        binding.rvSavedImage.visibility = View.GONE
        binding.recyclerviewProfile.visibility = View.VISIBLE

        //To view savedImages button function
        binding.btnPostGrid.setOnClickListener {
            binding.rvSavedImage.visibility = View.GONE
            binding.recyclerviewProfile.visibility = View.VISIBLE
        }

        //To view uploadedImages button function
        binding.rvSavedImage.setOnClickListener {
            binding.rvSavedImage.visibility = View.VISIBLE
            binding.recyclerviewProfile.visibility = View.GONE
        }

        //to fill in data in profile page
        getFollowers()
        getFollowing()
        getNoOfPosts()
        getUserInfo(binding.root)
        myPosts()
        mySaves()

        return binding.root
    }

    private fun mySaves() {

        mySavedImg = ArrayList()
        val savesRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (pO in snapshot.children) {
                        (mySavedImg as ArrayList<String>).add(pO.key!!)
                    }
                    readSavedImagesData()//Following is thr function to get the details of the saved posts
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun readSavedImagesData() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.exists()) {
                    (postListSaved as ArrayList<Post>).clear()

                    for (snapshot in datasnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)

                        for (key in mySavedImg!!) {
                            if (post!!.getPostId() == key) {
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun checkFollowOrFollowingButtonStatus() {

        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Follow").child(it1).child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.child(profileId).exists()) {
                    binding.editProfileButton.text = "Following"
                } else {
                    binding.editProfileButton.text = "Follow"
                }
            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    binding.totalFollowers.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getFollowing() {
        val followingsRef =
            FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    binding.totalFollowing.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getNoOfPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var i = 0
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.getPublisher() == profileId) {
                        i += 1
                    }
                }
                binding.totalPosts.text = "$i"
            }
        })
    }

    private fun myPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (postList as ArrayList<Post>).clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.getPublisher() == profileId) (postList as ArrayList<Post>).add(post)
                }
                Collections.reverse(postList)
                myPostAdapter!!.notifyDataSetChanged()
            }
        })
    }


    private fun pushNotification() {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(profileId)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "➱Started following you "
        notifyMap["postid"] = ""
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }


    private fun getUserInfo(view: View) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImageProfile)
                    binding.apply {
                        profileToolbarUsername.text = user.getUsername()
                        fullnameInProfile.text = user.getFullname()
                        usernameInProfile.text = user.getUsername()
                        bioProfile.text = user.getBio()
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}