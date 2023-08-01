package com.example.instamo.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.activities.AddStoryActivity
import com.example.instamo.R
import com.example.instamo.activities.StoryActivity
import com.example.instamo.model.Story
import com.example.instamo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(private val context: Context, private val mStory: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType == 0) {
            val view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val story = mStory[position]

        userInfo(holder, story.getUserId(), position)

        if (holder.adapterPosition != 0) {
            seenStory(holder, story.getUserId())
        }
        if (holder.adapterPosition == 0) {
            myStory(holder.addStoryText!!, holder.storyPlusBtn!!, false)
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == 0) {
                myStory(holder.addStoryText!!, holder.storyPlusBtn!!, true)

            } else {
                val intent = Intent(context, StoryActivity::class.java)
                intent.putExtra("userId", story.getUserId())
                context.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Story Item
        var storyImageSeen: CircleImageView? = null
        var storyImage: CircleImageView? = null
        var storyUserName: TextView? = null

        //Add Story Item layout
        var storyPlusBtn: ImageView? = null
        var addStoryText: TextView? = null

        init {
            //Story Item
            storyImageSeen = itemView.findViewById(R.id.story_image_seen)
            storyImage = itemView.findViewById(R.id.story_image)
            storyUserName = itemView.findViewById(R.id.story_username)

            //Add Story Item layout
            storyPlusBtn = itemView.findViewById(R.id.story_add)
            addStoryText = itemView.findViewById(R.id.add_story_text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    private fun userInfo(viewHolder: ViewHolder, userid: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(viewHolder.storyImage)

                    if (position != 0) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(viewHolder.storyImageSeen)
                        viewHolder.storyUserName!!.text = user.getUsername()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun myStory(
        textView: TextView, imageView: ImageView, click: Boolean
    )//to differentiate between story of online users and following users
    {

        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var counter = 0
                val timeCurrent = System.currentTimeMillis()

                for (snapshot in datasnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        counter++
                    }
                }

                if (click) {
                    if (counter > 0) {
                        val alertDialog = AlertDialog.Builder(context).create()
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL, "View Story"
                        ) { dialog, _ ->
                            val intent = Intent(context, StoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            context.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE, "Add Story"
                        ) { dialog, _ ->
                            val intent = Intent(context, AddStoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            context.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(context, AddStoryActivity::class.java)
                        intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                        context.startActivity(intent)
                    }
                } else {
                    if (counter > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    } else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun seenStory(
        viewHolder: ViewHolder, userId: String
    ) //to check whether story is seen or not
    {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story").child(userId)

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var i = 0

                for (snapshot in datasnapshot.children) {
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story::class.java)!!
                            .getTimeEnd()
                    ) //checking if not seen and not expired
                    {
                        i++


                    }
                }

                if (i > 0) {
                    viewHolder.storyImage!!.visibility = View.VISIBLE
                    viewHolder.storyImageSeen!!.visibility = View.GONE
                } else {
                    viewHolder.storyImage!!.visibility = View.GONE
                    viewHolder.storyImageSeen!!.visibility = View.VISIBLE

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}