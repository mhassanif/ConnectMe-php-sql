package com.hassanimran.i220813

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserItemAdapter(
    private val users: List<UserItem>
) : RecyclerView.Adapter<UserItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val chatIcon: ImageView = itemView.findViewById(R.id.chatIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.usernameTextView.text = user.username

        // Decode and display the profile picture
        if (!user.profileImage.isNullOrEmpty()) {
            try {
                val decodedImage = Base64.decode(user.profileImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                holder.profileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Fallback to default image if decoding fails
                holder.profileImageView.setImageResource(R.drawable.my_pfp)
            }
        } else {
            holder.profileImageView.setImageResource(R.drawable.my_pfp)
        }

        holder.chatIcon.setOnClickListener {
            val intent = Intent(holder.itemView.context, Chat::class.java)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = users.size
}