package com.hassanimran.i220813

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class ConfirmPost : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var selectedImageView: ImageView
    private lateinit var captionInput: EditText
    private lateinit var shareButton: Button
    private var selectedBitmap: Bitmap? = null
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_post)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        selectedImageView = findViewById(R.id.selectedImage)
        captionInput = findViewById(R.id.caption_input)
        shareButton = findViewById(R.id.share_button)
        findViewById<ImageView>(R.id.close_button).setOnClickListener { finish() }

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        if (imageUri == null) {
            showError("No image selected")
            finish()
            return
        }

        selectedBitmap = loadBitmapFromUri(imageUri)
        selectedBitmap?.let {
            selectedImageView.setImageBitmap(it)
            encodeImage(it)
            Log.d("ConfirmPost", "Encoded Image Length: ${encodedImage?.length}")
        } ?: run {
            showError("Failed to load image")
            finish()
            return
        }

        shareButton.setOnClickListener {
            val caption = captionInput.text.toString().trim()
            selectedBitmap?.let {
                if (encodedImage != null) {
                    uploadPost(caption)
                } else {
                    showError("Image encoding failed")
                }
            } ?: showError("No image available to upload")
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ConfirmPost", "Error loading bitmap: ${e.message}")
            null
        }
    }

    private fun encodeImage(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Reduced quality to 80 to manage size
        val byteArray = outputStream.toByteArray()
        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadPost(caption: String) {
        shareButton.isEnabled = false
        shareButton.text = "Uploading..."

        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("User not authenticated")
            resetShareButton()
            return
        }

        val postId = database.reference.child("posts").push().key ?: run {
            showError("Could not generate post ID")
            resetShareButton()
            return
        }

        database.reference.child("users").child(currentUser.uid).get()
            .addOnSuccessListener { userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                if (user == null) {
                    showError("User data not found in database")
                    resetShareButton()
                    return@addOnSuccessListener
                }

                val post = Post(
                    postId = postId,
                    authorId = currentUser.uid,
                    authorName = user.username ?: "Unknown",
                    authorUsername = user.username ?: "Unknown",
                    authorProfilePic = user.profile_picture ?: "",
                    imageUrl = encodedImage ?: "",
                    caption = caption,
                    timestamp = System.currentTimeMillis(),
                    likes = emptyMap(),
                    comments = emptyMap()
                )

                database.reference.child("posts").child(postId).setValue(post)
                    .addOnSuccessListener {
                        Log.d("ConfirmPost", "Post uploaded with imageUrl length: ${encodedImage?.length}")
                        Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showError("Failed to upload post: ${e.message}")
                        resetShareButton()
                    }
            }
            .addOnFailureListener { e ->
                showError("Failed to fetch user data: ${e.message}")
                resetShareButton()
            }
    }

    private fun resetShareButton() {
        shareButton.isEnabled = true
        shareButton.text = "Share"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("ConfirmPost", message)
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        selectedBitmap = null
    }
}