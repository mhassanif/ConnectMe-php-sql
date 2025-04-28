package com.hassanimran.i220813

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

object GalleryHelper {
    // Update the fetchGalleryThumbnails function in GalleryHelper.kt
    suspend fun fetchGalleryImages(context: Context): List<Pair<Bitmap, String>> = withContext(Dispatchers.IO) {
        val imageList = mutableListOf<Pair<Bitmap, String>>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext() && imageList.size < 50) {
                    val id = cursor.getLong(idColumn)
                    val path = cursor.getString(dataColumn)
                    loadThumbnail(context, id)?.let { bitmap ->
                        imageList.add(Pair(bitmap, path))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext imageList
    }

    private fun loadThumbnail(context: Context, imageId: Long): Bitmap? {
        return try {
            // Load thumbnail with target size
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId),
                    Size(256, 256),
                    null
                )
            } else {
                MediaStore.Images.Thumbnails.getThumbnail(
                    context.contentResolver,
                    imageId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565 // More memory efficient
                    }
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}