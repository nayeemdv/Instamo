package com.example.instamo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamo.adapter.PostAdapter
import com.example.instamo.adapter.StoryAdapter
import com.example.instamo.databinding.FragmentHomeBinding
import com.example.instamo.model.Post
import com.example.instamo.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null
    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.recyclerViewHome.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true).also {
                it.stackFromEnd = true
            }
            //For Posts
            postList = ArrayList()
            postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
            adapter = postAdapter
        }

        binding.recyclerViewStory.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).also {
                    it.stackFromEnd = true
                }
            storyList = ArrayList()
            storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
            adapter = storyAdapter
        }
        checkFollowings()

        return binding.root
    }

    //to get the following List of logged-in user
    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear() //to get previous data
                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                    retrieveStories()
                }
            }
        })
    }

    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    postList?.clear()
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)

                        for (id in (followingList as ArrayList<String>)) {
                            if (post!!.getPublisher() == id) {
                                postList!!.add(post)
                            }
                            postAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }

        })
    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()

                (storyList as ArrayList<Story>).clear()

                (storyList as ArrayList<Story>).add(
                    Story(
                        "",
                        0,
                        0,
                        "",
                        FirebaseAuth.getInstance().currentUser!!.uid
                    )
                )

                for (id in followingList!!) {
                    var countStory = 0

                    var story: Story? = null

                    for (snapshot in datasnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)

                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}
