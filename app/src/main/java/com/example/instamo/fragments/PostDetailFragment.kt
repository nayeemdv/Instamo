package com.example.instamo.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamo.adapter.PostAdapter
import com.example.instamo.model.Post
import com.example.instamo.databinding.FragmentPostDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailFragment : Fragment() {
    private lateinit var binding: FragmentPostDetailBinding
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var postId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        postId = pref?.getString("postId", "none")

        binding.rvPostDetail.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            postList = ArrayList()
            postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
            adapter = postAdapter
        }
        readPosts(postId)

        return binding.root
    }

    private fun readPosts(postId: String?) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!)

        Log.d("Post id", postId)
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                val post: Post? = p0.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }
        })
    }
}