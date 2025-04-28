//package com.hassanimran.i220813
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.IOException
//
//class NewPost : AppCompatActivity() {
//    private lateinit var selectedImageView: ImageView
//    private lateinit var galleryRecyclerView: RecyclerView
//    private lateinit var galleryAdapter: GalleryAdapter
//    private var selectedImageUri: Uri? = null
//    private val scope = CoroutineScope(Dispatchers.Main)
//    private val imageList = mutableListOf<Pair<Bitmap, Uri>>()
//
//    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            loadGalleryImages()
//        } else {
//            Toast.makeText(this, "Permission needed to access gallery", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_new_post)
//
//        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }
//
//        initViews()
//        checkPermissionsAndLoadImages()
//    }
//
//    private fun initViews() {
//        selectedImageView = findViewById(R.id.selectedImage)
//        galleryRecyclerView = findViewById(R.id.galleryRecyclerView)
//        val nextButton: TextView = findViewById(R.id.next_button)
//
//        galleryRecyclerView.layoutManager = GridLayoutManager(this, 4)
//
//        nextButton.setOnClickListener {
//            selectedImageUri?.let { uri ->
//                val intent = Intent(this, ConfirmPost::class.java).apply {
//                    putExtra("imageUri", uri.toString())
//                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                }
//                startActivity(intent)
//            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun checkPermissionsAndLoadImages() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
//            loadGalleryImages()
//        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            loadGalleryImages()
//        } else {
//            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//    }
//
//    private fun loadGalleryImages() {
//        scope.launch {
//            try {
//                val images = GalleryHelper.fetchGalleryImages(this@NewPost)
//                imageList.clear()
//
//                images.forEach { (originalBitmap, path) ->
//                    val uri = Uri.fromFile(File(path))
//                    // Create a scaled-down version of the bitmap for preview
//                    val scaledBitmap = createScaledBitmap(originalBitmap, path)
//                    imageList.add(Pair(scaledBitmap, uri))
//                    // Recycle the original bitmap if it's different from scaled
//                    if (originalBitmap != scaledBitmap && !originalBitmap.isRecycled) {
//                        originalBitmap.recycle()
//                    }
//                }
//
//                galleryAdapter = GalleryAdapter(imageList.map { it.first }) { position ->
//                    selectedImageUri = imageList[position].second
//                    // Load higher quality version when selected
//                    loadFullQualityImage(selectedImageUri)?.let { bitmap ->
//                        selectedImageView.setImageBitmap(bitmap)
//                    }
//                    Log.d("NewPost", "Selected URI: $selectedImageUri")
//                }
//
//                galleryRecyclerView.adapter = galleryAdapter
//            } catch (e: Exception) {
//                Toast.makeText(this@NewPost, "Error loading images", Toast.LENGTH_SHORT).show()
//                Log.e("NewPost", "Error loading images", e)
//            }
//        }
//    }
//
//    private fun createScaledBitmap(bitmap: Bitmap, path: String): Bitmap {
//        return try {
//            // Calculate inSampleSize for efficient memory usage
//            val options = BitmapFactory.Options().apply {
//                inJustDecodeBounds = true
//                BitmapFactory.decodeFile(path, this)
//                inSampleSize = calculateInSampleSize(this, 150, 150) // Target size for thumbnails
//                inJustDecodeBounds = false
//            }
//
//            BitmapFactory.decodeFile(path, options) ?: bitmap
//        } catch (e: Exception) {
//            Log.e("NewPost", "Error scaling bitmap", e)
//            bitmap
//        }
//    }
//
//    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
//        val height = options.outHeight
//        val width = options.outWidth
//        var inSampleSize = 1
//
//        if (height > reqHeight || width > reqWidth) {
//            val halfHeight = height / 2
//            val halfWidth = width / 2
//
//            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
//                inSampleSize *= 2
//            }
//        }
//        return inSampleSize
//    }
//
//    private fun loadFullQualityImage(uri: Uri?): Bitmap? {
//        return try {
//            uri?.let {
//                contentResolver.openInputStream(it)?.use { inputStream ->
//                    BitmapFactory.decodeStream(inputStream)
//                }
//            }
//        } catch (e: IOException) {
//            Log.e("NewPost", "Error loading full quality image", e)
//            null
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        imageList.forEach { (bitmap, _) ->
//            if (!bitmap.isRecycled) {
//                bitmap.recycle()
//            }
//        }
//        imageList.clear()
//    }
//}

package com.hassanimran.i220813

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class NewPost : AppCompatActivity() {
    private lateinit var selectedImageView: ImageView
    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter
    private var selectedImageUri: Uri? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private val imageList = mutableListOf<Pair<Bitmap, Uri>>()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            loadGalleryImages()
        } else {
            Toast.makeText(this, "Permission needed to access gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }

        initViews()
        checkPermissionsAndLoadImages()
    }

    private fun initViews() {
        selectedImageView = findViewById(R.id.selectedImage)
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView)
        val nextButton: TextView = findViewById(R.id.next_button)

        galleryRecyclerView.layoutManager = GridLayoutManager(this, 4)

        nextButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                val intent = Intent(this, ConfirmPost::class.java).apply {
                    putExtra("imageUri", uri.toString())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            loadGalleryImages()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadGalleryImages()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadGalleryImages() {
        scope.launch {
            try {
                val images = GalleryHelper.fetchGalleryImages(this@NewPost)
                imageList.clear()

                images.forEach { (originalBitmap, path) ->
                    val uri = Uri.fromFile(File(path))
                    val scaledBitmap = createScaledBitmap(originalBitmap, path)
                    imageList.add(Pair(scaledBitmap, uri))
                    if (originalBitmap != scaledBitmap && !originalBitmap.isRecycled) {
                        originalBitmap.recycle()
                    }
                }

                galleryAdapter = GalleryAdapter(imageList.map { it.first }) { position ->
                    selectedImageUri = imageList[position].second
                    loadFullQualityImage(selectedImageUri)?.let { bitmap ->
                        selectedImageView.setImageBitmap(bitmap)
                    }
                    Log.d("NewPost", "Selected URI: $selectedImageUri")
                }

                galleryRecyclerView.adapter = galleryAdapter
            } catch (e: Exception) {
                Toast.makeText(this@NewPost, "Error loading images: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("NewPost", "Error loading images", e)
            }
        }
    }

    private fun createScaledBitmap(bitmap: Bitmap, path: String): Bitmap {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, this)
                inSampleSize = calculateInSampleSize(this, 150, 150)
                inJustDecodeBounds = false
            }
            BitmapFactory.decodeFile(path, options) ?: bitmap
        } catch (e: Exception) {
            Log.e("NewPost", "Error scaling bitmap", e)
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun loadFullQualityImage(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: IOException) {
            Log.e("NewPost", "Error loading full quality image", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageList.forEach { (bitmap, _) ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        imageList.clear()
    }
}