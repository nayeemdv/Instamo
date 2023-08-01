package com.example.instamo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.R
import com.example.instamo.databinding.MypostLayoutBinding
import com.example.instamo.fragments.PostDetailFragment
import com.example.instamo.model.Post
import com.squareup.picasso.Picasso

class MyPostAdapter(private val context: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<MyPostAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MypostLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(MypostLayoutBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]

        Picasso.get().load(post.getPostImage()).into(holder.binding.myPostedPicture)
        holder.binding.myPostedPicture.setOnClickListener {

            val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("postId", post.getPostId())
            pref.apply()

            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailFragment()).commit()
        }
    }
}