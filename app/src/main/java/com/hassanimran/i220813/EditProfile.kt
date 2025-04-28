package com.hassanimran.i220813

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

private const val PICK_IMAGE_REQUEST = 1

class EditProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var profileImageView: CircleImageView  // Change to CircleImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize UI components
        profileImageView = findViewById<CircleImageView>(R.id.profile_image)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val phoneInput = findViewById<EditText>(R.id.contact_number_input)
        val bioInput = findViewById<EditText>(R.id.bio_input)
        val doneButton = findViewById<TextView>(R.id.done_button)

        // Populate fields from database
        populateFields()

        // Image selection
        profileImageView.setOnClickListener {
            chooseImage()
        }

        // Save profile updates
        doneButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val bio = bioInput.text.toString().trim()

            if (validateInput(name, username, phone)) {
                saveProfileData(name, username, phone, bio)
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields() {
        val userId = auth.currentUser?.uid ?: return
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val username = snapshot.child("username").value.toString()
                    val phone = snapshot.child("phone").value.toString()
                    val bio = snapshot.child("bio").value.toString()
                    val profilePicture = snapshot.child("profile_picture").value?.toString() ?: ""

                    findViewById<EditText>(R.id.name_input).setText(name)
                    findViewById<EditText>(R.id.username_input).setText(username)
                    findViewById<EditText>(R.id.contact_number_input).setText(phone)
                    findViewById<EditText>(R.id.bio_input).setText(bio)
                    findViewById<TextView>(R.id.user_name).text = name  // Add this line

                    // Load profile picture or default image
                    if (profilePicture.isNotEmpty()) {
                        try {
                            val decodedImage = Base64.decode(profilePicture, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            profileImageView.setImageResource(R.drawable.default_pfp)
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.default_pfp)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfile, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                profileImageView.setImageResource(R.drawable.default_pfp)
                findViewById<TextView>(R.id.user_name).text = "User"  // Add this line
            }
        })
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveProfileData(name: String, username: String, phone: String, bio: String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf<String, Any>(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "bio" to bio
        )

        // Handle image encoding if new image is selected
        if (imageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val encodedImage = encodeImage(bitmap)
            userMap["profile_picture"] = encodedImage
        }

        db.child(userId).updateChildren(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Profile::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(name: String, username: String, phone: String): Boolean {
        return name.isNotEmpty() && username.isNotEmpty() && phone.isNotEmpty()
    }
}