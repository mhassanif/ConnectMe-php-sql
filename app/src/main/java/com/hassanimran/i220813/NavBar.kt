package com.hassanimran.i220813

import android.app.Activity
import android.content.Intent
import android.widget.Button

class NavBar(private val activity: Activity) {

    fun setupNavBar() {
        val homeButton: Button = activity.findViewById(R.id.home_button)
        val searchButton: Button = activity.findViewById(R.id.search_button)
        val postButton: Button = activity.findViewById(R.id.post_button)
        val profileButton: Button = activity.findViewById(R.id.profile_button)
        val contactButton: Button = activity.findViewById(R.id.contact_button)

        homeButton.setOnClickListener {
            val intent = Intent(activity, Home::class.java)
            activity.startActivity(intent)
        }

        searchButton.setOnClickListener {
            val intent = Intent(activity, Search::class.java)
            activity.startActivity(intent)
        }

        postButton.setOnClickListener {
            val intent = Intent(activity, Login::class.java)
            activity.startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(activity, Profile::class.java)
            activity.startActivity(intent)
        }

        contactButton.setOnClickListener {
            val intent = Intent(activity, Login::class.java)
            activity.startActivity(intent)
        }
    }
}