package com.hassanimran.i220813

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class StoriesAdapter(
    private val onStoryClick: (String, List<Story>) -> Unit
) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var userStoriesMap: Map<String, List<Story>> = emptyMap()
    private var userIds: List<String> = emptyList()

    fun setStories(stories: Map<String, List<Story>>) {
        this.userStoriesMap = stories

        val mutableUserIds = stories.keys.toMutableList()
        if (currentUserId != null && mutableUserIds.contains(currentUserId)) {
            mutableUserIds.remove(currentUserId)
            mutableUserIds.add(0, currentUserId)
        }
        this.userIds = mutableUserIds

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val userId = userIds[position]
        val stories = userStoriesMap[userId] ?: return

        val firstStory = stories.first()

        if (firstStory.userProfilePic.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(firstStory.userProfilePic)
                .placeholder(R.drawable.default_pfp)
                .error(R.drawable.default_pfp)
                .into(holder.storyImage)
        } else {
            holder.storyImage.setImageResource(R.drawable.default_pfp)
        }

        holder.username.text = if (userId == currentUserId) "Your Story" else firstStory.username

        val hasUnviewedStory = stories.any { story ->
            currentUserId != null && !story.viewedBy.containsKey(currentUserId)
        }

        holder.storyImage.borderColor = holder.itemView.context.getColor(
            if (hasUnviewedStory) R.color.story_border_unviewed else R.color.story_border_viewed
        )

        holder.itemView.setOnClickListener {
            onStoryClick(userId, stories)
        }
    }

    override fun getItemCount(): Int = userIds.size

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: CircleImageView = itemView.findViewById(R.id.storyImage)
        val username: TextView = itemView.findViewById(R.id.storyUsername)
    }
}