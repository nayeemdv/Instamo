package com.example.instamo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instamo.adapter.UserAdapter
import com.example.instamo.model.User
import com.example.instamo.databinding.FragmentSearchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchBinding.inflate(inflater, container, false)


        binding.recyclerviewSearch.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            mUser = ArrayList()
            userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
            adapter = userAdapter
        }

        //to show a user on search
        binding.searchItems.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.searchItems.text.isNotEmpty()) {
                    recyclerView?.visibility = View.VISIBLE
                    retrieveUser()
                    searchUser(s.toString().lowercase())
                }
            }
        })
        return binding.root
    }

    private fun searchUser(input: String) {

        val query = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username")
            .startAt(input).endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                mUser?.clear()

                for (snapshot in datasnapshot.children) {
                    //searching all users
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }

    private fun retrieveUser() {
        val usersSearchRef =
            FirebaseDatabase.getInstance().reference.child("Users")//table name:Users
        usersSearchRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Could not read from Database", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (binding.searchItems.text.isEmpty()) {
                    mUser?.clear()
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)
                        if (user != null) {
                            mUser?.add(user)
                        }
                        userAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}