package com.hassanimran.i220813

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

class StoryAddActivity : AppCompatActivity() {

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var imagePreview: ImageView
    private lateinit var uploadButton: Button
    private lateinit var cancelButton: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imagePreview.setImageURI(uri)
                imagePreview.visibility = View.VISIBLE
                uploadButton.isEnabled = true
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_add)

        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]

        imagePreview = findViewById(R.id.imagePreview)
        uploadButton = findViewById(R.id.uploadButton)
        cancelButton = findViewById(R.id.cancelButton)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        uploadButton.setOnClickListener {
            uploadStory()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        storyViewModel.uploadStatus.observe(this) { status ->
            when (status) {
                is UploadStatus.UPLOADING -> {
                    progressBar.visibility = View.VISIBLE
                    uploadButton.isEnabled = false
                }
                is UploadStatus.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Signal to HomeActivity to refresh
                    finish()
                }
                is UploadStatus.ERROR -> {
                    progressBar.visibility = View.GONE
                    uploadButton.isEnabled = true
                    Toast.makeText(this, "Error: ${status.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissionAndOpenGallery() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Toast.makeText(
                    this,
                    "Storage permission is needed to select images",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadStory() {
        val imageUri = selectedImageUri
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val bitmap = contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            if (bitmap != null) {
                storyViewModel.uploadStory(bitmap)
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}