package com.hassanimran.i220813

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class PostsAdapter(
    private val onCommentClick: (String) -> Unit,
    private val onLikeClick: (String, Boolean) -> Unit
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class PostViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
    ) {
        private val userImage = itemView.findViewById<CircleImageView>(R.id.postUserImage)
        private val userName = itemView.findViewById<TextView>(R.id.postUserName)
        private val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        private val postCaption = itemView.findViewById<TextView>(R.id.postCaption)
        private val likeButton = itemView.findViewById<ImageView>(R.id.like)
        private val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
        private val commentButton = itemView.findViewById<ImageView>(R.id.comment)
        private val commentCount = itemView.findViewById<TextView>(R.id.commentCount)

        fun bind(post: Post) {
            // Fetch the current profile picture from the database
            fetchCurrentProfilePicture(post.authorId, userImage)

            if (post.imageUrl.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Glide.with(itemView)
                        .load(bitmap)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.camera_icon)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(postImage)
                } catch (e: Exception) {
                    Log.e("PostsAdapter", "Error decoding Base64 for post ${post.postId}: ${e.message}")
                    Glide.with(itemView)
                        .load(R.drawable.camera_icon)
                        .into(postImage)
                }
            } else {
                Glide.with(itemView)
                    .load(R.drawable.ic_placeholder)
                    .into(postImage)
            }

            userName.text = post.authorUsername
            postCaption.text = post.caption
            likeCount.text = post.likes.size.toString()
            commentCount.text = post.comments.size.toString()

            val isLiked = post.likes.containsKey(auth.currentUser?.uid)
            likeButton.setImageResource(if (isLiked) R.drawable.like else R.drawable.no_like)

            likeButton.setOnClickListener {
                onLikeClick(post.postId, isLiked)
            }
            commentButton.setOnClickListener {
                onCommentClick(post.postId)
            }
        }

        private fun fetchCurrentProfilePicture(authorId: String, userImageView: CircleImageView) {
            // First set the placeholder
            Glide.with(itemView)
                .load(R.drawable.ic_placeholder)
                .into(userImageView)

            // Then fetch the current profile picture from the database
            database.child("users").child(authorId).get()
                .addOnSuccessListener { snapshot ->
                    val userData = snapshot.getValue(User::class.java)
                    val profilePicUrl = userData?.profile_picture


                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(itemView)
                            .load(profilePicUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.camera_icon)
                            .into(userImageView)
                        Log.d("PostsAdapter", "Loaded profile picture for user $authorId")
                    } else {
                        Log.d("PostsAdapter", "No profile picture found for user $authorId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PostsAdapter", "Error fetching profile picture for user $authorId: ${e.message}")
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostViewHolder(parent)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) = holder.bind(getItem(position))
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.postId == newItem.postId
    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}
