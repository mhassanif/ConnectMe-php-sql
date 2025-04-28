package com.hassanimran.i220813

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.*


private val database = Firebase.database.reference

class Contacts : AppCompatActivity() {

    private lateinit var followRequestsRecyclerView: RecyclerView
    private lateinit var exploreUsersRecyclerView: RecyclerView
    private lateinit var followRequestAdapter: FollowRequestAdapter
    private lateinit var exploreUserAdapter: ExploreUserAdapter
    private val followRequests = mutableListOf<FollowRequest>()
    private val exploreUsers = mutableListOf<User>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacts)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        followRequestsRecyclerView = findViewById(R.id.followRequestsRecyclerView)
        exploreUsersRecyclerView = findViewById(R.id.exploreUsersRecyclerView)

        followRequestsRecyclerView.layoutManager = LinearLayoutManager(this)
        exploreUsersRecyclerView.layoutManager = LinearLayoutManager(this)

        followRequestAdapter = FollowRequestAdapter(followRequests, { userId ->
            acceptFollowRequest(userId)
        }, { userId ->
            rejectFollowRequest(userId)
        })
        exploreUserAdapter = ExploreUserAdapter(exploreUsers) { userId ->
            sendFollowRequest(userId)
        }

        followRequestsRecyclerView.adapter = followRequestAdapter
        exploreUsersRecyclerView.adapter = exploreUserAdapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Contacts", "Current user ID is null. User might not be logged in.")
            return
        }
        Log.d("Contacts", "Current user ID: $currentUserId")
        fetchAllUsers(currentUserId)
        fetchFollowRequests(currentUserId)

        val navBar = NavBar(this)
        navBar.setupNavBar()
    }

    private fun fetchAllUsers(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        Log.d("Contacts", "Starting to fetch users from database")

        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Contacts", "Snapshot received: ${snapshot.exists()}")
                    Log.d("Contacts", "Number of users in snapshot: ${snapshot.childrenCount}")

                    exploreUsers.clear()
                    if (!snapshot.exists()) {
                        Log.w("Contacts", "No users found in the database")
                        exploreUserAdapter.notifyDataSetChanged()
                        return
                    }

                    for (child in snapshot.children) {
                        val userId = child.key
                        Log.d("Contacts", "Processing user ID: $userId")

                        if (userId != null && userId != currentUserId) {
                            val username = child.child("username").getValue(String::class.java)
                            if (username != null) {
                                Log.d("Contacts", "Found user: $username (ID: $userId)")
                                database.child("users").child(userId).child("reqs").child(currentUserId)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(requestSnapshot: DataSnapshot) {
                                            val hasRequested = requestSnapshot.exists()
                                            val user = User(userId, username, hasRequested = hasRequested)
                                            exploreUsers.add(user)
                                            exploreUserAdapter.notifyDataSetChanged()
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("Contacts", "Error checking request status for user ID $userId: ${error.message}")
                                            val user = User(userId, username)
                                            exploreUsers.add(user)
                                            exploreUserAdapter.notifyDataSetChanged()
                                        }
                                    })
                            } else {
                                Log.w("Contacts", "Username not found for user ID: $userId")
                            }
                        } else {
                            Log.d("Contacts", "Skipping user ID: $userId (matches current user or null)")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Contacts", "Error fetching users: ${error.message}")
                }
            })
    }

    private fun sendFollowRequest(targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Contacts", "Current user ID is null. Cannot send follow request.")
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(targetUserId).child("reqs").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("Contacts", "Follow request already sent to user ID: $targetUserId")
                        Toast.makeText(this@Contacts, "Request already sent", Toast.LENGTH_SHORT).show()
                    } else {
                        database.child("users").child(targetUserId).child("reqs").child(currentUserId)
                            .setValue(true)
                            .addOnSuccessListener {
                                Log.d("Contacts", "Follow request sent to user ID: $targetUserId")
                                Toast.makeText(this@Contacts, "Follow request sent", Toast.LENGTH_SHORT).show()
                                // Fetch sender's username and send FCM notification
                                database.child("users").child(currentUserId).child("username")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val username = snapshot.getValue(String::class.java) ?: "Unknown"
                                            sendFcmNotification(targetUserId, currentUserId, username, "follow_request")
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("Contacts", "Error fetching username: ${error.message}")
                                        }
                                    })
                            }
                            .addOnFailureListener { error ->
                                Log.e("Contacts", "Error sending follow request: ${error.message}")
                                Toast.makeText(this@Contacts, "Error sending request", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Contacts", "Error checking existing request: ${error.message}")
                    Toast.makeText(this@Contacts, "Error checking request status", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendFcmNotification(receiverId: String, senderId: String, username: String, type: String) {
        database.child("users").child(receiverId).child("fcmToken")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val token = snapshot.getValue(String::class.java)
                    if (token != null) {
                        val data = mapOf(
                            "type" to type,
                            "senderId" to senderId,
                            "receiverId" to receiverId,
                            "username" to username
                        )
                        FirebaseMessaging.getInstance().send(
                            RemoteMessage.Builder("$token@gcm.googleapis.com")
                                .setMessageId(UUID.randomUUID().toString())
                                .setData(data)
                                .build()
                        )
                        Log.d("FCM", "Notification sent to $receiverId for $type")
                    } else {
                        Log.w("FCM", "No FCM token found for user $receiverId")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FCM", "Error fetching FCM token: ${error.message}")
                }
            })
    }

    private fun fetchFollowRequests(currentUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("reqs")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Contacts", "Fetched follow requests: ${snapshot.childrenCount} requests found")
                    followRequests.clear()
                    for (child in snapshot.children) {
                        val requesterId = child.key!!
                        fetchUserDetails(requesterId) { username ->
                            val request = FollowRequest(requesterId, username)
                            followRequests.add(request)
                            followRequestAdapter.notifyDataSetChanged()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Contacts", "Error fetching follow requests: ${error.message}")
                }
            })
    }

    private fun fetchUserDetails(userId: String, onResult: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: ""
                    Log.d("Contacts", "Fetched user details for ID $userId: $username")
                    onResult(username)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Contacts", "Error fetching user details: ${error.message}")
                }
            })
    }

    private fun acceptFollowRequest(requesterId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        val updates = mutableMapOf<String, Any?>()
        updates["users/$currentUserId/reqs/$requesterId"] = null
        updates["users/$currentUserId/followers/$requesterId"] = true
        updates["users/$requesterId/following/$currentUserId"] = true
        database.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("Contacts", "Follow request accepted for user ID: $requesterId")
                followRequests.removeIf { it.userId == requesterId }
                followRequestAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Log.e("Contacts", "Error accepting follow request: ${error.message}")
            }
    }

    private fun rejectFollowRequest(requesterId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(currentUserId).child("reqs").child(requesterId).removeValue()
            .addOnSuccessListener {
                Log.d("Contacts", "Follow request rejected for user ID: $requesterId")
                followRequests.removeIf { it.userId == requesterId }
                followRequestAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Log.e("Contacts", "Error rejecting follow request: ${error.message}")
            }
    }
}