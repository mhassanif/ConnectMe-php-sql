package com.hassanimran.i220813

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView  // Add this import

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var profileImageView: CircleImageView  // Change to CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize UI components
        profileImageView = findViewById(R.id.profile_image)
        val followers: LinearLayout = findViewById(R.id.followers)
        val following: LinearLayout = findViewById(R.id.following)
        val editProfile: ImageView = findViewById(R.id.edit_profile)
        val logout: Button = findViewById(R.id.logout_button)
        val userName: TextView = findViewById(R.id.user_name)  // Add this
        val userBio: TextView = findViewById(R.id.user_bio)    // Add this

        // Populate profile data
        populateProfileData()

        // Add button to lead to follower and following page
        followers.setOnClickListener {
            val intent = Intent(this, Followers::class.java)
            startActivity(intent)
        }

        following.setOnClickListener {
            val intent = Intent(this, Following::class.java)
            startActivity(intent)
        }

        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        // Initialize and set up the NavBar
        val navBar = NavBar(this)
        navBar.setupNavBar()

        logout.setOnClickListener {
            Firebase.auth.signOut()
            val sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // Add this function
    private fun populateProfileData() {
        val userId = auth.currentUser?.uid ?: return
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val bio = snapshot.child("bio").value.toString()
                    val profilePicture = snapshot.child("profile_picture").value?.toString() ?: ""

                    findViewById<TextView>(R.id.user_name).text = name
                    findViewById<TextView>(R.id.user_bio).text = bio

                    if (profilePicture.isNotEmpty()) {
                        try {
                            val decodedImage = Base64.decode(profilePicture, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            profileImageView.setImageResource(R.drawable.my_pfp) // Fallback to default
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.my_pfp) // Default image
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                profileImageView.setImageResource(R.drawable.my_pfp)
                findViewById<TextView>(R.id.user_name).text = "User" // Fallback name
                findViewById<TextView>(R.id.user_bio).text = ""      // Clear bio on error
            }
        })
    }
}