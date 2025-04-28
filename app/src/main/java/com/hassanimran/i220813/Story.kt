package com.hassanimran.i220813

import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class Story(
    val storyId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val username: String = "",
    val userProfilePic: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val viewedBy: Map<String, Boolean> = HashMap(),
    val imageRes: Int = 0  // Added for compatibility with your existing code
) {
    // Empty constructor required for Firebase
    constructor() : this(
        storyId = UUID.randomUUID().toString(),
        userId = "",
        username = "",
        userProfilePic = "",
        imageUrl = "",
        timestamp = System.currentTimeMillis()
    )

    // Constructor that only takes imageRes (for compatibility with your existing code)
    constructor(imageRes: Int) : this(
        storyId = UUID.randomUUID().toString(),
        userId = "",
        username = "",
        userProfilePic = "",
        imageUrl = "",
        timestamp = System.currentTimeMillis(),
        imageRes = imageRes
    )

    // Check if story is still valid (less than 24 hours old)
    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val storyAge = currentTime - timestamp
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return storyAge <= oneDayInMillis
    }
}

