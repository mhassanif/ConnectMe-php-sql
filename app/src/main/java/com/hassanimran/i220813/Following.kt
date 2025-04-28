package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Following : AppCompatActivity() {

    private lateinit var followingRecyclerView: RecyclerView
    private lateinit var usernameTextView: TextView
    private lateinit var followingCountTextView: TextView
    private val followingList = mutableListOf<UserItem>()
    private lateinit var userItemAdapter: UserItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        followingRecyclerView = findViewById(R.id.followingRecyclerView)
        usernameTextView = findViewById(R.id.usernameTextView)
        followingCountTextView = findViewById(R.id.followingCountTextView)

        // Set up RecyclerView
        followingRecyclerView.layoutManager = LinearLayoutManager(this)
        userItemAdapter = UserItemAdapter(followingList)
        followingRecyclerView.adapter = userItemAdapter

        // Fetch data
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Following", "Current user ID is null. User might not be logged in.")
            return
        }
        fetchUserDetails(currentUserId)
        fetchFollowing(currentUserId)

        // Set up click listeners
        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val tabChange: TextView = findViewById(R.id.followers_tab)
        tabChange.setOnClickListener {
            val intent = Intent(this, Followers::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserDetails(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: "User"
                    usernameTextView.text = username
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Following", "Error fetching user details: ${error.message}")
                }
            })
    }

    private fun fetchFollowing(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Following", "Fetched following: ${snapshot.childrenCount} following found")
                    followingList.clear()
                    for (child in snapshot.children) {
                        val followingId = child.key!!
                        // Fetch the following user's username and profile picture
                        database.child("users").child(followingId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val username = userSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                                    val profileImage = userSnapshot.child("profile_picture").getValue(String::class.java)
                                    val user = UserItem(followingId, username, profileImage)
                                    followingList.add(user)
                                    userItemAdapter.notifyDataSetChanged()
                                    // Update the following count
                                    followingCountTextView.text = "${followingList.size} Following"
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Following", "Error fetching following details: ${error.message}")
                                }
                            })
                    }
                    if (followingList.isEmpty()) {
                        followingCountTextView.text = "0 Following"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Following", "Error fetching following: ${error.message}")
                }
            })
    }
}