package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExploreUserAdapter(
    private val users: MutableList<User>,
    private val onFollowClick: (String) -> Unit
) : RecyclerView.Adapter<ExploreUserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val followButton: Button = itemView.findViewById(R.id.followButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.usernameTextView.text = user.username

        holder.followButton.text = if (user.hasRequested) "Requested" else "Follow"
        holder.followButton.isEnabled = !user.hasRequested

        holder.followButton.setOnClickListener {
            onFollowClick(user.userId)
            user.hasRequested = true
            holder.followButton.text = "Requested"
            holder.followButton.isEnabled = false
        }
    }

    override fun getItemCount(): Int = users.size
}