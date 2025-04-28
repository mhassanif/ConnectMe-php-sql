package com.hassanimran.i220813

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentsActivity : AppCompatActivity() {
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var postId: String? = null

    // Remove any potential comment limit by using a large number
    private val COMMENT_LOAD_LIMIT = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_comments)
            Log.d("CommentsActivity", "Layout inflated successfully")
        } catch (e: Exception) {
            Log.e("CommentsActivity", "Error inflating layout: ${e.message}", e)
            Toast.makeText(this, "Error loading comments view", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        postId = intent.getStringExtra("postId")

        if (postId == null) {
            Log.e("CommentsActivity", "No postId provided")
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("CommentsActivity", "Received postId: $postId")

        try {
            commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
            commentInput = findViewById(R.id.commentInput)
            sendButton = findViewById(R.id.sendButton)
            Log.d("CommentsActivity", "Views initialized successfully")
        } catch (e: Exception) {
            Log.e("CommentsActivity", "Error finding views: ${e.message}")
            Toast.makeText(this, "Error initializing UI", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadComments()

        sendButton.setOnClickListener {
            addComment()
        }
    }

    private fun setupRecyclerView() {
        try {
            commentsRecyclerView.layoutManager = LinearLayoutManager(this)
            commentsAdapter = CommentsAdapter()
            commentsRecyclerView.adapter = commentsAdapter
            Log.d("CommentsActivity", "RecyclerView set up successfully")
        } catch (e: Exception) {
            Log.e("CommentsActivity", "Error setting up RecyclerView: ${e.message}")
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments() {
        Log.d("CommentsActivity", "Loading comments for postId: $postId")
        // Remove any limit on comments by not using limitToFirst/limitToLast
        database.reference.child("posts").child(postId!!).child("comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (commentSnapshot in snapshot.children) {
                        try {
                            commentSnapshot.getValue(Comment::class.java)?.let {
                                comments.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("CommentsActivity", "Error parsing comment: ${e.message}")
                        }
                    }
                    Log.d("CommentsActivity", "Loaded ${comments.size} comments")
                    commentsAdapter.submitList(comments.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CommentsActivity", "Failed to load comments: ${error.message}")
                    Toast.makeText(this@CommentsActivity, "Failed to load comments", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addComment() {
        val text = commentInput.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        database.reference.child("users").child(user.uid).get()
            .addOnSuccessListener { snapshot ->
                val userData = snapshot.getValue(User::class.java)
                val username = userData?.username ?: "Unknown"
                val profilePic = userData?.profile_picture ?: ""

                val commentId = database.reference.child("posts").child(postId!!).child("comments").push().key ?: run {
                    Toast.makeText(this, "Error generating comment ID", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val comment = Comment(
                    userId = user.uid,
                    username = username,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                database.reference.child("posts").child(postId!!).child("comments").child(commentId).setValue(comment)
                    .addOnSuccessListener {
                        commentInput.text.clear()
                        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                        Log.d("CommentsActivity", "Comment added successfully: $commentId")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add comment: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("CommentsActivity", "Error adding comment: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CommentsActivity", "Error fetching user data: ${e.message}")
            }
    }
}
