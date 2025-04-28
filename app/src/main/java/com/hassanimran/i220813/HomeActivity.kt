package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var storiesAdapter: StoriesAdapter
    private lateinit var storyViewModel: StoryViewModel
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database.reference
    private lateinit var storyAdd: ImageView
    private lateinit var storyImage: CircleImageView

    private val storyAddLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            storyViewModel.loadStories() // Refresh stories after adding
            loadPosts()
        }
    }

    private fun storeFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseDatabase.getInstance().reference.child("users").child(userId).child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM token stored for user $userId: $token")
                    }
                    .addOnFailureListener { error ->
                        Log.e("FCM", "Failed to store FCM token: ${error.message}")
                    }
            } else {
                Log.e("FCM", "Failed to retrieve FCM token: ${task.exception?.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUserId = auth.currentUser?.uid ?: return
        database.child("users").child(currentUserId).child("isOnline").setValue(true)

        // Set up a disconnection handler to set isOnline to false when the app disconnects
        database.child("users").child(currentUserId).child("isOnline")
            .onDisconnect().setValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentUserId = auth.currentUser?.uid ?: return
        database.child("users").child(currentUserId).child("isOnline").setValue(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            storeFcmToken(currentUser.uid)
        }

        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        storiesRecyclerView = findViewById(R.id.storiesRecyclerView)
        storyAdd = findViewById(R.id.storyAdd)
        storyImage = findViewById(R.id.storyImage)

        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]

        setupPostsRecyclerView()
        setupStoriesRecyclerView()

        loadPosts()

        storyViewModel.userStories.observe(this) { stories ->
            storiesAdapter.setStories(stories)
        }

        swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
            storyViewModel.loadStories()
        }

        storyAdd.setOnClickListener {
            storyAddLauncher.launch(Intent(this, StoryAddActivity::class.java))
        }

        storyImage.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userStories = storyViewModel.getStoriesByUserId(currentUser.uid)
                if (userStories.isNotEmpty()) {
                    openStoryView(currentUser.uid, userStories)
                } else {
                    storyAddLauncher.launch(Intent(this, StoryAddActivity::class.java))
                }
            }
        }

        loadPosts()
        storyViewModel.loadStories()

        val navBar = NavBar(this)
        navBar.setupNavBar()

        val dm = findViewById<ImageButton>(R.id.dm)
        dm.setOnClickListener {
            startActivity(Intent(this, Dm::class.java))
        }
    }

    //    private fun setupPostsRecyclerView() {
//        postsRecyclerView.layoutManager = LinearLayoutManager(this)
//        postsAdapter = PostsAdapter(
//            onCommentClick = { postId ->
//                startActivity(Intent(this, HomeActivity::class.java).apply {
//                    //putExtra("postId", postId)
//                })
//            },
//            onLikeClick = { postId, isLiked ->
//                toggleLike(postId, isLiked)
//            }
//        )
//        postsRecyclerView.adapter = postsAdapter
//    }
    private fun setupPostsRecyclerView() {
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsAdapter = PostsAdapter(
            onCommentClick = { postId ->
                Log.d("HomeActivity", "Opening CommentsActivity for postId: $postId")
                try {
                    val intent = Intent(this, CommentsActivity::class.java)
                    intent.putExtra("postId", postId)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error starting CommentsActivity: ${e.message}", e)
                    Toast.makeText(this, "Error opening comments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onLikeClick = { postId, isLiked ->
                toggleLike(postId, isLiked)
            }
        )
        postsRecyclerView.adapter = postsAdapter
    }
    private fun setupStoriesRecyclerView() {
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storiesAdapter = StoriesAdapter { userId, stories ->
            openStoryView(userId, stories)
        }
        storiesRecyclerView.adapter = storiesAdapter
    }

    private fun loadPosts() {
        swipeRefreshLayout.isRefreshing = true
        database.child("posts")
            .orderByChild("timestamp")
            .limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postList = mutableListOf<Post>()
                    for (postSnapshot in snapshot.children) {
                        postSnapshot.getValue(Post::class.java)?.let {
                            Log.d("HomeActivity", "Loaded post ${it.postId} with imageUrl length: ${it.imageUrl.length}")
                            postList.add(0, it)
                        }
                    }
                    postsAdapter.submitList(if (postList.isEmpty()) dummyPosts else postList)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    postsAdapter.submitList(dummyPosts)
                    swipeRefreshLayout.isRefreshing = false
                    Log.e("HomeActivity", "Failed to load posts: ${error.message}")
                }
            })
    }

    private fun toggleLike(postId: String, isLiked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val likeRef = database.child("posts").child(postId).child("likes").child(userId)
        if (isLiked) likeRef.removeValue() else likeRef.setValue(true)
    }

    private fun openStoryView(userId: String, stories: List<Story>) {
        val intent = Intent(this, StoryViewActivity::class.java).apply {
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        storyViewModel.loadStories()
        loadPosts()
    }

    private val dummyPosts = listOf(
        Post(
            postId = "1",
            authorId = "user_001",
            authorName = "John Doe",
            authorUsername = "@johndoe",
            authorProfilePic = "https://example.com/profile1.jpg",
            imageUrl = "",
            caption = "Exploring the beauty of nature! 🌿🌄",
            timestamp = System.currentTimeMillis(),
            likes = mapOf("user_002" to true, "user_003" to true),
            comments = mapOf(
                "comment_001" to Comment(
                    userId = "user_002",
                    username = "@janesmith",
                    text = "Absolutely stunning!",
                    timestamp = System.currentTimeMillis()
                ),
                "comment_002" to Comment(
                    userId = "user_003",
                    username = "@michael",
                    text = "Where is this?",
                    timestamp = System.currentTimeMillis()
                )
            )
        ),
        Post(
            postId = "2",
            authorId = "user_004",
            authorName = "Alice Johnson",
            authorUsername = "@alicej",
            authorProfilePic = "https://example.com/profile2.jpg",
            imageUrl = "",
            caption = "Delicious homemade pasta 🍝",
            timestamp = System.currentTimeMillis(),
            likes = mapOf("user_001" to true, "user_002" to true, "user_005" to true),
            comments = emptyMap()
        )
    )
}
