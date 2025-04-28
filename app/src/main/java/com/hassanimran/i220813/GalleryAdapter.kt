package com.hassanimran.i220813

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GalleryAdapter(
    private val imageList: List<Bitmap>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.galleryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.imageView.setImageBitmap(imageList[position])
        holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount(): Int = imageList.size
}