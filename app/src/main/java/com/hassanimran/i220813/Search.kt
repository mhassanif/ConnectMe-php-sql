package com.hassanimran.i220813

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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

class Search : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var followersFilterCheckBox: CheckBox
    private lateinit var followingFilterCheckBox: CheckBox
    private lateinit var searchResultsRecyclerView: RecyclerView
    private val allUsers = mutableListOf<UserItem>()
    private val searchResults = mutableListOf<UserItem>()
    private lateinit var userItemAdapter: UserItemAdapter
    private val followers = mutableSetOf<String>()
    private val following = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize and set up the NavBar
        val navBar = NavBar(this)
        navBar.setupNavBar()

        // Initialize UI elements
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        followersFilterCheckBox = findViewById(R.id.followersFilterCheckBox)
        followingFilterCheckBox = findViewById(R.id.followingFilterCheckBox)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)

        // Set up RecyclerView
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        userItemAdapter = UserItemAdapter(searchResults)
        searchResultsRecyclerView.adapter = userItemAdapter

        // Fetch data
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Search", "Current user ID is null. User might not be logged in.")
            return
        }

        // Fetch all users, followers, and following
        fetchAllUsers(currentUserId)
        fetchFollowers(currentUserId)
        fetchFollowing(currentUserId)

        // Search button click listener
        searchButton.setOnClickListener {
            performSearch()
        }

        // Filter checkbox listeners
        followersFilterCheckBox.setOnCheckedChangeListener { _, _ -> performSearch() }
        followingFilterCheckBox.setOnCheckedChangeListener { _, _ -> performSearch() }
    }

    private fun fetchAllUsers(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Search", "Fetched users snapshot: ${snapshot.childrenCount} users found")
                    allUsers.clear()
                    for (child in snapshot.children) {
                        val userId = child.key!!
                        if (userId != currentUserId) {
                            val username = child.child("username").getValue(String::class.java) ?: ""
                            val profileImage = child.child("profile_picture").getValue(String::class.java)
                            val user = UserItem(userId, username, profileImage)
                            allUsers.add(user)
                        }
                    }
                    Log.d("Search", "Total users loaded: ${allUsers.size}")
                    performSearch() // Perform initial search (show all users if no query)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Search", "Error fetching users: ${error.message}")
                }
            })
    }

    private fun fetchFollowers(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followers.clear()
                    for (child in snapshot.children) {
                        val followerId = child.key!!
                        followers.add(followerId)
                    }
                    Log.d("Search", "Fetched followers: ${followers.size}")
                    performSearch()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Search", "Error fetching followers: ${error.message}")
                }
            })
    }

    private fun fetchFollowing(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    following.clear()
                    for (child in snapshot.children) {
                        val followingId = child.key!!
                        following.add(followingId)
                    }
                    Log.d("Search", "Fetched following: ${following.size}")
                    performSearch()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Search", "Error fetching following: ${error.message}")
                }
            })
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim().lowercase()
        Log.d("Search", "Performing search with query: $query")

        searchResults.clear()

        // Filter users by username
        val filteredByUsername = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { it.username.lowercase().contains(query) }
        }

        // Apply followers/following filters
        val followersFilter = followersFilterCheckBox.isChecked
        val followingFilter = followingFilterCheckBox.isChecked

        val filteredResults = when {
            followersFilter && followingFilter -> {
                filteredByUsername.filter { user ->
                    followers.contains(user.userId) && following.contains(user.userId)
                }
            }
            followersFilter -> {
                filteredByUsername.filter { user -> followers.contains(user.userId) }
            }
            followingFilter -> {
                filteredByUsername.filter { user -> following.contains(user.userId) }
            }
            else -> filteredByUsername
        }

        searchResults.addAll(filteredResults)
        Log.d("Search", "Filtered results: ${searchResults.size}")
        userItemAdapter.notifyDataSetChanged()
    }
}