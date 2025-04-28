package com.hassanimran.i220813

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64 // Add this import
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryViewActivity : AppCompatActivity() {

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var storyImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var usernameText: TextView
    private lateinit var timeText: TextView
    private lateinit var closeButton: ImageView

    private var stories: List<Story> = emptyList()
    private var currentStoryIndex = 0
    private var userId: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private val storyDuration = 30000L // 30 seconds

    private var touchStartX = 0f
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_view)

        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]

        progressBar = findViewById(R.id.storyProgressBar)
        storyImage = findViewById(R.id.storyImage)
        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        timeText = findViewById(R.id.timeText)
        closeButton = findViewById(R.id.closeButton)

        userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchStories(userId)

        closeButton.setOnClickListener {
            finish()
        }

        storyImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartX = event.x
                    pauseStory()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val touchEndX = event.x
                    val deltaX = touchEndX - touchStartX

                    when {
                        deltaX < -100 -> showNextStory()
                        deltaX > 100 -> showPreviousStory()
                        else -> resumeStory()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchStories(userId: String) {
        stories = storyViewModel.getStoriesByUserId(userId)
        if (stories.isEmpty()) {
            FirebaseDatabase.getInstance().reference.child("stories").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fetchedStories = mutableListOf<Story>()
                        for (storySnapshot in snapshot.children) {
                            val story = storySnapshot.getValue(Story::class.java)?.takeIf { it.isValid() }
                            if (story != null) {
                                fetchedStories.add(story)
                            }
                        }
                        stories = fetchedStories.sortedByDescending { it.timestamp }
                        if (stories.isEmpty()) {
                            Toast.makeText(this@StoryViewActivity, "No stories available", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            showStory(currentStoryIndex)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@StoryViewActivity, "Error loading stories: ${error.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
        } else {
            showStory(currentStoryIndex)
        }
    }

    private fun showStory(index: Int) {
        if (index < 0 || index >= stories.size) {
            finish()
            return
        }

        currentStoryIndex = index
        val story = stories[index]

        try {
            if (story.imageUrl.isNotEmpty()) {
                val decodedImage = Base64.decode(story.imageUrl, Base64.DEFAULT)
                Glide.with(this)
                    .load(decodedImage)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.error_image)
                    .into(storyImage)
            } else {
                storyImage.setImageResource(R.drawable.ic_placeholder)
            }

            if (story.userProfilePic.isNotEmpty()) {
                Glide.with(this)
                    .load(story.userProfilePic)
                    .placeholder(R.drawable.story_1)
                    .error(R.drawable.default_pfp)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.default_pfp)
            }

            usernameText.text = story.username

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeText.text = sdf.format(Date(story.timestamp))

            storyViewModel.markStoryAsViewed(userId, story.storyId)

            progressBar.progress = 0
            progressBar.max = 100
            startProgressAnimation()
        } catch (e: Exception) {
            Log.e("StoryViewActivity", "Error showing story: ${e.message}", e)
            Toast.makeText(this, "Error showing story", Toast.LENGTH_SHORT).show()
            showNextStory()
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (isPaused) {
                handler.postDelayed(this, 100)
                return
            }

            val currentProgress = progressBar.progress
            val maxProgress = progressBar.max

            if (currentProgress < maxProgress) {
                val increment = (100 * 100 / storyDuration).toInt()
                progressBar.progress = currentProgress + increment
                handler.postDelayed(this, 100)
            } else {
                showNextStory()
            }
        }
    }

    private fun startProgressAnimation() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun pauseStory() {
        isPaused = true
    }

    private fun resumeStory() {
        isPaused = false
    }

    private fun showNextStory() {
        handler.removeCallbacks(progressRunnable)
        if (currentStoryIndex < stories.size - 1) {
            showStory(currentStoryIndex + 1)
        } else {
            finish()
        }
    }

    private fun showPreviousStory() {
        handler.removeCallbacks(progressRunnable)
        if (currentStoryIndex > 0) {
            showStory(currentStoryIndex - 1)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(progressRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
    }
}