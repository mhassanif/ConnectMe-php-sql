package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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

class Dm : AppCompatActivity() {

    private lateinit var dmRecyclerView: RecyclerView
    private lateinit var dmAdapter: DmAdapter
    private val dmList = mutableListOf<DmUser>()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val userListeners = mutableMapOf<String, ValueEventListener>() // Track listeners to avoid duplicates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dmRecyclerView = findViewById(R.id.dmRecyclerView)
        dmRecyclerView.layoutManager = LinearLayoutManager(this)
        dmAdapter = DmAdapter(dmList) { userId ->
            val intent = Intent(this, Chat::class.java).apply {
                putExtra("RECEIVER_ID", userId)
            }
            startActivity(intent)
        }
        dmRecyclerView.adapter = dmAdapter as RecyclerView.Adapter<*>

        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        fetchDmUsers()
    }

    private fun fetchDmUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        database.child("users").child(currentUserId).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear existing listeners to avoid duplicates
                    userListeners.forEach { (userId, listener) ->
                        database.child("users").child(userId).removeEventListener(listener)
                    }
                    userListeners.clear()

                    val followedUserIds = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val userId = child.key!!
                        followedUserIds.add(userId)
                    }

                    // Update the list of followed users
                    val tempList = dmList.toMutableList()
                    tempList.retainAll { followedUserIds.contains(it.userId) } // Remove users not in following
                    val newUserIds = followedUserIds.filter { userId -> tempList.none { it.userId == userId } }

                    // Add new users to the list with default values
                    newUserIds.forEach { userId ->
                        tempList.add(DmUser(userId, "Loading...", false))
                    }

                    // Update the main list
                    dmList.clear()
                    dmList.addAll(tempList)
                    dmAdapter.notifyDataSetChanged()

                    // Set up real-time listeners for each user
                    followedUserIds.forEach { userId ->
                        val listener = object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val username = userSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                                val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false

                                // Find and update the user in the list
                                val index = dmList.indexOfFirst { it.userId == userId }
                                if (index != -1) {
                                    dmList[index] = DmUser(userId, username, isOnline)
                                    dmAdapter.notifyItemChanged(index) // Update only the changed item
                                } else {
                                    // If the user isn't in the list (e.g., added after initial load), add them
                                    dmList.add(DmUser(userId, username, isOnline))
                                    dmAdapter.notifyItemInserted(dmList.size - 1)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error if needed
                            }
                        }
                        userListeners[userId] = listener
                        database.child("users").child(userId).addValueEventListener(listener)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listeners when the activity is destroyed
        userListeners.forEach { (userId, listener) ->
            database.child("users").child(userId).removeEventListener(listener)
        }
        userListeners.clear()
    }
}

data class DmUser(
    val userId: String,
    val username: String,
    val isOnline: Boolean = false
)