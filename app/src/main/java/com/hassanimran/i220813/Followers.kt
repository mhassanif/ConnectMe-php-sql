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

class Followers : AppCompatActivity() {

    private lateinit var followersRecyclerView: RecyclerView
    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private val followersList = mutableListOf<UserItem>()
    private lateinit var userItemAdapter: UserItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        followersRecyclerView = findViewById(R.id.followersRecyclerView)
        usernameTextView = findViewById(R.id.usernameTextView)
        followersCountTextView = findViewById(R.id.followersCountTextView)

        // Set up RecyclerView
        followersRecyclerView.layoutManager = LinearLayoutManager(this)
        userItemAdapter = UserItemAdapter(followersList)
        followersRecyclerView.adapter = userItemAdapter

        // Fetch data
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Followers", "Current user ID is null. User might not be logged in.")
            return
        }
        fetchUserDetails(currentUserId)
        fetchFollowers(currentUserId)

        // Set up click listeners
        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val tabChange: TextView = findViewById(R.id.following_tab)
        tabChange.setOnClickListener {
            val intent = Intent(this, Following::class.java)
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
                    Log.e("Followers", "Error fetching user details: ${error.message}")
                }
            })
    }

    private fun fetchFollowers(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Followers", "Fetched followers: ${snapshot.childrenCount} followers found")
                    followersList.clear()
                    for (child in snapshot.children) {
                        val followerId = child.key!!
                        // Fetch the follower's username and profile picture
                        database.child("users").child(followerId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val username = userSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                                    val profileImage = userSnapshot.child("profile_picture").getValue(String::class.java)
                                    val user = UserItem(followerId, username, profileImage)
                                    followersList.add(user)
                                    userItemAdapter.notifyDataSetChanged()
                                    // Update the follower count
                                    followersCountTextView.text = "${followersList.size} Followers"
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Followers", "Error fetching follower details: ${error.message}")
                                }
                            })
                    }
                    if (followersList.isEmpty()) {
                        followersCountTextView.text = "0 Followers"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Followers", "Error fetching followers: ${error.message}")
                }
            })
    }
}