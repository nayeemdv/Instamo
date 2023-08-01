package com.example.instamo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instamo.adapter.NotificationAdapter
import com.example.instamo.databinding.FragmentNotificationBinding
import com.example.instamo.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private var notificationAdapter: NotificationAdapter? = null
    private var notificationList: MutableList<Notification>? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.recyclerviewNotification.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            notificationList = ArrayList()
            notificationAdapter = context?.let {
                NotificationAdapter(
                    it,
                    notificationList as ArrayList<Notification>
                )
            }
            adapter = notificationAdapter
        }
        readNotification()
        return binding.root
    }

    private fun readNotification() {

        val postRef =
            FirebaseDatabase.getInstance().reference.child("Notification").child(firebaseUser!!.uid)
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                notificationList?.clear()
                for (snapshot in p0.children) {
                    val notification: Notification? = snapshot.getValue(Notification::class.java)
                    notificationList!!.add(notification!!)
                }
                notificationList?.reverse()
                notificationAdapter!!.notifyDataSetChanged()

            }
        })
    }
}