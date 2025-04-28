package com.hassanimran.i220813

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import android.util.Base64
import java.util.UUID

class StoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _userStories = MutableLiveData<Map<String, List<Story>>>()
    val userStories: LiveData<Map<String, List<Story>>> = _userStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus

    private val allStoriesById = mutableMapOf<String, Story>()

    init {
        loadStories()
    }

    fun loadStories() {
        _isLoading.value = true
        database.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storiesMap = mutableMapOf<String, MutableList<Story>>()
                allStoriesById.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val userStoriesList = mutableListOf<Story>()

                    for (storySnapshot in userSnapshot.children) {
                        val story = storySnapshot.getValue(Story::class.java) ?: continue
                        if (story.isValid()) {
                            userStoriesList.add(story)
                            allStoriesById[story.storyId] = story
                        } else {
                            storySnapshot.ref.removeValue() // Clean up expired stories
                        }
                    }

                    userStoriesList.sortByDescending { it.timestamp }
                    if (userStoriesList.isNotEmpty()) {
                        storiesMap[userId] = userStoriesList
                    }
                }

                _userStories.value = storiesMap
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _userStories.value = emptyMap()
                _isLoading.value = false
            }
        })
    }

    fun getStoryById(storyId: String): Story? = allStoriesById[storyId]

    fun getStoriesByUserId(userId: String): List<Story> {
        return _userStories.value?.get(userId)?.filter { it.isValid() } ?: emptyList()
    }

    fun uploadStory(bitmap: Bitmap) {
        _uploadStatus.value = UploadStatus.UPLOADING

        val currentUser = auth.currentUser ?: run {
            _uploadStatus.value = UploadStatus.ERROR("User not authenticated")
            return
        }

        val encodedImage = encodeImage(bitmap)

        database.child("users").child(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java) ?: run {
                    _uploadStatus.value = UploadStatus.ERROR("User data not found")
                    return@addOnSuccessListener
                }

                val storyId = UUID.randomUUID().toString()
                val story = Story(
                    storyId = storyId,
                    userId = currentUser.uid,
                    username = user.username ?: "Unknown",
                    userProfilePic = user.profile_picture ?: "",
                    imageUrl = encodedImage,
                    timestamp = System.currentTimeMillis(),
                    viewedBy = emptyMap()
                )

                database.child("stories").child(currentUser.uid).child(storyId).setValue(story)
                    .addOnSuccessListener {
                        _uploadStatus.value = UploadStatus.SUCCESS
                        // No need to call loadStories() here; the ValueEventListener will handle updates
                    }
                    .addOnFailureListener { e ->
                        _uploadStatus.value = UploadStatus.ERROR("Failed to save story: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _uploadStatus.value = UploadStatus.ERROR("Failed to get user data: ${e.message}")
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Reduced quality to manage size
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun markStoryAsViewed(userId: String, storyId: String) {
        val currentUser = auth.currentUser?.uid ?: return
        database.child("stories").child(userId).child(storyId).child("viewedBy")
            .child(currentUser).setValue(true)
    }
}

sealed class UploadStatus {
    object UPLOADING : UploadStatus()
    object SUCCESS : UploadStatus()
    data class ERROR(val message: String) : UploadStatus()
}