package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.Glide
import android.util.Log

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val userImage: CircleImageView = view.findViewById(R.id.commentUserImage)
        private val username: TextView = view.findViewById(R.id.commentUsername)
        private val commentText: TextView = view.findViewById(R.id.commentText)

        fun bind(comment: Comment) {
            username.text = comment.username
            commentText.text = comment.text

            // Load user profile picture with better error handling
            try {
                Glide.with(itemView.context)
                    .load(R.drawable.me) // Default to placeholder
                    .placeholder(R.drawable.me)
                    .error(R.drawable.me)
                    .into(userImage)
            } catch (e: Exception) {
                Log.e("CommentsAdapter", "Error loading profile image: ${e.message}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.timestamp == newItem.timestamp && oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}