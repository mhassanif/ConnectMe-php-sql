package com.hassanimran.i220813

data class User(
    val userId: String = "",
    val username: String = "",
    val profile_picture: String? = null,
    var hasRequested: Boolean = false
)

data class FollowRequest(
    val userId: String,
    val username: String
)

data class UserItem(
    val userId: String,
    val username: String,
    val profileImage: String?
)

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorProfilePic: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0L,
    val likes: Map<String, Boolean> = emptyMap(),
    val comments: Map<String, Comment> = emptyMap()
)

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
