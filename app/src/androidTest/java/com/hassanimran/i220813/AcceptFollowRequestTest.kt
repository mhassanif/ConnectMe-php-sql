package com.hassanimran.i220813
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.TimeUnit
import androidx.test.espresso.Root
import androidx.test.espresso.assertion.ViewAssertions
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

@RunWith(AndroidJUnit4::class)
class AcceptFollowRequestTest {

    @Test
    fun testAcceptFollowRequest() {
        // Launch Contacts activity
        ActivityScenario.launch(Contacts::class.java)

        // Verify user2's follow request is displayed
        onView(withId(R.id.followRequestsRecyclerView))
            .check(matches(hasDescendant(withText("user2"))))

        // Click the Accept button
        onView(withId(R.id.acceptButton)).perform(click())

        // Verify the request is removed
        onView(withText("user2")).check(ViewAssertions.doesNotExist())
    }
}