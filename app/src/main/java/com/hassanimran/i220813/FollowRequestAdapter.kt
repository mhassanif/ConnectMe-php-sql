package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FollowRequestAdapter(
    private val requests: MutableList<FollowRequest>,
    private val onAcceptClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit
) : RecyclerView.Adapter<FollowRequestAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follow_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.usernameTextView.text = request.username

        holder.acceptButton.setOnClickListener {
            onAcceptClick(request.userId)
        }
        holder.rejectButton.setOnClickListener {
            onRejectClick(request.userId)
        }
    }

    override fun getItemCount(): Int = requests.size
}