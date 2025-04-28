package com.hassanimran.i220813

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
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

class Chat : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

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

        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: return
        val senderId = auth.currentUser?.uid ?: return
        val conversationId = getConversationId(senderId, receiverId)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messageList, senderId)
        chatRecyclerView.adapter = chatAdapter

        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(senderId, receiverId, messageText, conversationId)
                messageEditText.text.clear()
            }
        }

        listenForMessages(conversationId)
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
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}