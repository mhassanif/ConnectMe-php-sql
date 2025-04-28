//package com.hassanimran.i220813
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class ChatAdapter(
//    private val messages: List<Message>,
//    private val currentUserId: String
//) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val layout = if (viewType == 0) R.layout.item_message_sent else R.layout.item_message_received
//        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val message = messages[position]
//        holder.messageTextView.text = message.message
//    }
//
//    override fun getItemCount(): Int = messages.size
//
//    override fun getItemViewType(position: Int): Int {
//        return if (messages[position].senderId == currentUserId) 0 else 1
//    }
//}
package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserId: String,
    private val onMessageEdit: (Message) -> Unit,
    private val onMessageDelete: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder.itemViewType == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.sentMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.sentMessageTime)
        private val editedIndicator: TextView = itemView.findViewById(R.id.sentEditedIndicator)
        private val messageImage: ImageView = itemView.findViewById(R.id.sentMessageImage)

        fun bind(message: Message) {
            if (message.isDeleted) {
                messageText.text = "This message was deleted"
                messageText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                messageImage.visibility = View.GONE
                editedIndicator.visibility = View.GONE
            } else {
                messageText.text = message.message
                messageText.setTextColor(itemView.context.getColor(android.R.color.black))
                editedIndicator.visibility = if (message.isEdited) View.VISIBLE else View.GONE

                // Handle image if present
                if (message.imageUrl != null) {
                    messageImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.camera_icon)
                        .into(messageImage)
                } else {
                    messageImage.visibility = View.GONE
                }

                // Set up long click for edit/delete options
                itemView.setOnLongClickListener {
                    showPopupMenu(it, message)
                    true
                }
            }

            // Format and display time
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeText.text = sdf.format(Date(message.timestamp))
        }

        private fun showPopupMenu(view: View, message: Message) {
            if (!message.isDeleted) {
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.inflate(R.menu.message_options_menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onMessageEdit(message)
                            true
                        }
                        R.id.action_delete -> {
                            onMessageDelete(message)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.receivedMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.receivedMessageTime)
        private val editedIndicator: TextView = itemView.findViewById(R.id.receivedEditedIndicator)
        private val messageImage: ImageView = itemView.findViewById(R.id.receivedMessageImage)

        fun bind(message: Message) {
            if (message.isDeleted) {
                messageText.text = "This message was deleted"
                messageText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                messageImage.visibility = View.GONE
                editedIndicator.visibility = View.GONE
            } else {
                messageText.text = message.message
                messageText.setTextColor(itemView.context.getColor(android.R.color.black))
                editedIndicator.visibility = if (message.isEdited) View.VISIBLE else View.GONE

                // Handle image if present
                if (message.imageUrl != null) {
                    messageImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.camera_icon)
                        .into(messageImage)
                } else {
                    messageImage.visibility = View.GONE
                }
            }

            // Format and display time
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeText.text = sdf.format(Date(message.timestamp))
        }
    }
}
