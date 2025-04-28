package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class DmAdapter(
    private val dmList: List<DmUser>,
    private val onUserClick: (String) -> Unit
) : RecyclerView.Adapter<DmAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.dmUsernameTextView)
        val onlineStatusDot: View = itemView.findViewById(R.id.onlineStatusDot)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dmUser = dmList[position]
        holder.usernameTextView.text = dmUser.username
        holder.onlineStatusDot.visibility = if (dmUser.isOnline) View.VISIBLE else View.GONE

        // Assuming profile picture loading is handled elsewhere as per your previous setup
        holder.itemView.setOnClickListener {
            onUserClick(dmUser.userId)
        }
    }

    override fun getItemCount(): Int = dmList.size
}