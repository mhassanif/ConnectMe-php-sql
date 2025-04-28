package com.hassanimran.i220813

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0
)