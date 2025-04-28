//package com.hassanimran.i220813
//
//import android.os.Bundle
//import android.widget.EditText
//import android.widget.ImageView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//
//class Chat : AppCompatActivity() {
//
//    private lateinit var chatRecyclerView: RecyclerView
//    private lateinit var messageEditText: EditText
//    private lateinit var sendButton: ImageView
//    private lateinit var chatAdapter: ChatAdapter
//    private val messageList = mutableListOf<Message>()
//    private val auth = FirebaseAuth.getInstance()
//    private val database = FirebaseDatabase.getInstance().reference
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_chat)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        chatRecyclerView = findViewById(R.id.chatRecyclerView)
//        messageEditText = findViewById(R.id.messageEditText)
//        sendButton = findViewById(R.id.sendButton)
//
//        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: return
//        val senderId = auth.currentUser?.uid ?: return
//        val conversationId = getConversationId(senderId, receiverId)
//
//        chatRecyclerView.layoutManager = LinearLayoutManager(this)
//        chatAdapter = ChatAdapter(messageList, senderId)
//        chatRecyclerView.adapter = chatAdapter
//
//        val back: ImageView = findViewById(R.id.back_button)
//        back.setOnClickListener {
//            finish()
//        }
//
//        sendButton.setOnClickListener {
//            val messageText = messageEditText.text.toString().trim()
//            if (messageText.isNotEmpty()) {
//                sendMessage(senderId, receiverId, messageText, conversationId)
//                messageEditText.text.clear()
//            }
//        }
//
//        listenForMessages(conversationId)
//    }
//
//    private fun getConversationId(senderId: String, receiverId: String): String {
//        return if (senderId < receiverId) "conversation_${senderId}_${receiverId}"
//        else "conversation_${receiverId}_${senderId}"
//    }
//
//    private fun sendMessage(senderId: String, receiverId: String, messageText: String, conversationId: String) {
//        val messageRef = database.child("messages").child(conversationId).push()
//        val message = Message(
//            messageId = messageRef.key!!,
//            senderId = senderId,
//            receiverId = receiverId,
//            message = messageText,
//            timestamp = System.currentTimeMillis()
//        )
//        messageRef.setValue(message)
//    }
//
//    private fun listenForMessages(conversationId: String) {
//        database.child("messages").child(conversationId)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messageList.clear()
//                    for (child in snapshot.children) {
//                        val message = child.getValue(Message::class.java)
//                        message?.let { messageList.add(it) }
//                    }
//                    chatAdapter.notifyDataSetChanged()
//                    chatRecyclerView.scrollToPosition(messageList.size - 1)
//                }
//                override fun onCancelled(error: DatabaseError) {}
//            })
//    }
////}
//package com.hassanimran.i220813
//
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Base64
//import android.util.Log
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.google.firebase.storage.FirebaseStorage
//import java.io.ByteArrayOutputStream
//import java.util.UUID
//
//class Chat : AppCompatActivity() {
//
//    private lateinit var chatRecyclerView: RecyclerView
//    private lateinit var messageEditText: EditText
//    private lateinit var sendButton: ImageView
//    private lateinit var attachButton: ImageView
//    private lateinit var chatAdapter: ChatAdapter
//    private val messageList = mutableListOf<Message>()
//    private val auth = FirebaseAuth.getInstance()
//    private val database = FirebaseDatabase.getInstance().reference
//    private val storage = FirebaseStorage.getInstance().reference
//    private var currentEditingMessage: Message? = null
//    private var receiverId: String = ""
//    private var senderId: String = ""
//    private var conversationId: String = ""
//
//    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            result.data?.data?.let { uri ->
//                uploadImage(uri)
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_chat)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        chatRecyclerView = findViewById(R.id.chatRecyclerView)
//        messageEditText = findViewById(R.id.messageEditText)
//        sendButton = findViewById(R.id.sendButton)
//        attachButton = findViewById(R.id.attachButton)
//
//        receiverId = intent.getStringExtra("RECEIVER_ID") ?: return
//        senderId = auth.currentUser?.uid ?: return
//        conversationId = getConversationId(senderId, receiverId)
//
//        chatRecyclerView.layoutManager = LinearLayoutManager(this)
//        chatAdapter = ChatAdapter(
//            messageList,
//            senderId,
//            onMessageEdit = { message -> showEditDialog(message) },
//            onMessageDelete = { message -> deleteMessage(message) }
//        )
//        chatRecyclerView.adapter = chatAdapter
//
//        val back: ImageView = findViewById(R.id.back_button)
//        back.setOnClickListener {
//            finish()
//        }
//
//        sendButton.setOnClickListener {
//            val messageText = messageEditText.text.toString().trim()
//            if (messageText.isNotEmpty()) {
//                if (currentEditingMessage != null) {
//                    updateMessage(currentEditingMessage!!, messageText)
//                    currentEditingMessage = null
//                    messageEditText.setText("")
//                    sendButton.setImageResource(R.drawable.send_icon)
//                } else {
//                    sendMessage(senderId, receiverId, messageText, conversationId)
//                    messageEditText.text.clear()
//                }
//            }
//        }
//
//        attachButton.setOnClickListener {
//            openImagePicker()
//        }
//
//        listenForMessages(conversationId)
//    }
//
//    private fun getConversationId(senderId: String, receiverId: String): String {
//        return if (senderId < receiverId) "conversation_${senderId}_${receiverId}"
//        else "conversation_${receiverId}_${senderId}"
//    }
//
//    private fun sendMessage(senderId: String, receiverId: String, messageText: String, conversationId: String) {
//        val messageRef = database.child("messages").child(conversationId).push()
//        val message = Message(
//            messageId = messageRef.key!!,
//            senderId = senderId,
//            receiverId = receiverId,
//            message = messageText,
//            timestamp = System.currentTimeMillis()
//        )
//        messageRef.setValue(message)
//    }
//
//    private fun sendImageMessage(senderId: String, receiverId: String, imageUrl: String, conversationId: String) {
//        val messageRef = database.child("messages").child(conversationId).push()
//        val message = Message(
//            messageId = messageRef.key!!,
//            senderId = senderId,
//            receiverId = receiverId,
//            message = "📷 Photo",
//            timestamp = System.currentTimeMillis(),
//            imageUrl = imageUrl
//        )
//        messageRef.setValue(message)
//    }
//
//    private fun updateMessage(message: Message, newText: String) {
//        message.message = newText
//        message.isEdited = true
//        database.child("messages").child(conversationId).child(message.messageId).setValue(message)
//    }
//
//    private fun deleteMessage(message: Message) {
//        AlertDialog.Builder(this)
//            .setTitle("Delete Message")
//            .setMessage("Are you sure you want to delete this message?")
//            .setPositiveButton("Delete") { _, _ ->
//                message.isDeleted = true
//                database.child("messages").child(conversationId).child(message.messageId).setValue(message)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun showEditDialog(message: Message) {
//        currentEditingMessage = message
//        messageEditText.setText(message.message)
//        messageEditText.setSelection(message.message.length)
//        messageEditText.requestFocus()
//        sendButton.setImageResource(R.drawable.edit_icon)
//    }
//
//    private fun openImagePicker() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        pickImageLauncher.launch(intent)
//    }
//
//    private fun uploadImage(imageUri: Uri) {
//        val imageRef = storage.child("chat_images/${UUID.randomUUID()}")
//
//        try {
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//            val baos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
//            val data = baos.toByteArray()
//
//            val uploadTask = imageRef.putBytes(data)
//            uploadTask.addOnSuccessListener {
//                imageRef.downloadUrl.addOnSuccessListener { uri ->
//                    sendImageMessage(senderId, receiverId, uri.toString(), conversationId)
//                }
//            }.addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
//                Log.e("Chat", "Image upload failed", e)
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
//            Log.e("Chat", "Error processing image", e)
//        }
//    }
//
//    private fun listenForMessages(conversationId: String) {
//        database.child("messages").child(conversationId)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messageList.clear()
//                    for (child in snapshot.children) {
//                        val message = child.getValue(Message::class.java)
//                        message?.let { messageList.add(it) }
//                    }
//                    chatAdapter.notifyDataSetChanged()
//                    if (messageList.isNotEmpty()) {
//                        chatRecyclerView.scrollToPosition(messageList.size - 1)
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {}
//            })
//    }
//}
package com.hassanimran.i220813

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.util.UUID

class Chat : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var attachButton: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: CircleImageView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private var currentEditingMessage: Message? = null
    private var receiverId: String = ""
    private var receiverName: String = ""
    private var senderId: String = ""
    private var conversationId: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        attachButton = findViewById(R.id.attachButton)
        nameTextView = findViewById(R.id.name)
        profileImageView = findViewById(R.id.profileImageLarge)

        receiverId = intent.getStringExtra("RECEIVER_ID") ?: return
        receiverName = intent.getStringExtra("RECEIVER_NAME") ?: "Chat"
        senderId = auth.currentUser?.uid ?: return
        conversationId = getConversationId(senderId, receiverId)

        // Update the UI with receiver's name
        nameTextView.text = receiverName

        // Fetch and display receiver's profile picture
        fetchReceiverProfilePicture()

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(
            messageList,
            senderId,
            onMessageEdit = { message -> showEditDialog(message) },
            onMessageDelete = { message -> deleteMessage(message) }
        )
        chatRecyclerView.adapter = chatAdapter

        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (currentEditingMessage != null) {
                    updateMessage(currentEditingMessage!!, messageText)
                    currentEditingMessage = null
                    messageEditText.setText("")
                    sendButton.setImageResource(R.drawable.send_icon)
                } else {
                    sendMessage(senderId, receiverId, messageText, conversationId)
                    messageEditText.text.clear()
                }
            }
        }

        attachButton.setOnClickListener {
            openImagePicker()
        }

        listenForMessages(conversationId)
    }

    private fun fetchReceiverProfilePicture() {
        database.child("users").child(receiverId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null && !user.profile_picture.isNullOrEmpty()) {
                    try {
                        // Decode Base64 profile picture
                        val decodedImage = Base64.decode(user.profile_picture, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                        profileImageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("Chat", "Error decoding profile picture", e)
                        profileImageView.setImageResource(R.drawable.default_pfp)
                    }
                } else {
                    profileImageView.setImageResource(R.drawable.default_pfp)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Chat", "Failed to fetch receiver profile", error.toException())
                profileImageView.setImageResource(R.drawable.default_pfp)
            }
        })
    }

    private fun getConversationId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) "conversation_${senderId}_${receiverId}"
        else "conversation_${receiverId}_${senderId}"
    }

    private fun sendMessage(senderId: String, receiverId: String, messageText: String, conversationId: String) {
        val messageRef = database.child("messages").child(conversationId).push()
        val message = Message(
            messageId = messageRef.key!!,
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        messageRef.setValue(message)
    }

    private fun sendImageMessage(senderId: String, receiverId: String, imageUrl: String, conversationId: String) {
        val messageRef = database.child("messages").child(conversationId).push()
        val message = Message(
            messageId = messageRef.key!!,
            senderId = senderId,
            receiverId = receiverId,
            message = "📷 Photo",
            timestamp = System.currentTimeMillis(),
            imageUrl = imageUrl
        )
        messageRef.setValue(message)
    }

    private fun updateMessage(message: Message, newText: String) {
        message.message = newText
        message.isEdited = true
        database.child("messages").child(conversationId).child(message.messageId).setValue(message)
    }

    private fun deleteMessage(message: Message) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                message.isDeleted = true
                database.child("messages").child(conversationId).child(message.messageId).setValue(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(message: Message) {
        currentEditingMessage = message
        messageEditText.setText(message.message)
        messageEditText.setSelection(message.message.length)
        messageEditText.requestFocus()
        sendButton.setImageResource(R.drawable.edit_icon)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadImage(imageUri: Uri) {
        val imageRef = storage.child("chat_images/${UUID.randomUUID()}")

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    sendImageMessage(senderId, receiverId, uri.toString(), conversationId)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Chat", "Image upload failed", e)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Chat", "Error processing image", e)
        }
    }

    private fun listenForMessages(conversationId: String) {
        database.child("messages").child(conversationId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (child in snapshot.children) {
                        val message = child.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    chatAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        chatRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}