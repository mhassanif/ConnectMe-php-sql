package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 0) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.message
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 0 else 1
    }
}