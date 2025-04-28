//package com.hassanimran.i220813
//
//data class Message(
//    val messageId: String = "",
//    val senderId: String = "",
//    val receiverId: String = "",
//    val message: String = "",
//    val timestamp: Long = 0
//)
package com.hassanimran.i220813

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    var message: String = "",
    val timestamp: Long = 0L,
    var isEdited: Boolean = false,
    var imageUrl: String? = null,
    var isDeleted: Boolean = false
)
