package com.hassanimran.i220813

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class SendFollowRequestTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDatabase: FirebaseDatabase

    @Before
    fun setup() {
        // Mock FirebaseAuth
        mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("user1")

        // Mock FirebaseDatabase (simplified for test)
        mockDatabase = mock(FirebaseDatabase::class.java)

        // Launch Contacts activity
        val intent = Intent()
        ActivityScenario.launch<Contacts>(intent)
    }

    @Test
    fun testSendFollowRequest() {
        // Assume user2 is displayed in the explore users RecyclerView
        onView(withId(R.id.exploreUsersRecyclerView))
            .check(matches(hasDescendant(withText("user2"))))

        // Click the Follow button for user2
        onView(withId(R.id.followButton)).perform(click())

        // Verify the button text changes to "Requested"
        onView(withId(R.id.followButton))
            .check(matches(withText("Requested")))

        // Verify the button is disabled
        onView(withId(R.id.followButton))
            .check(matches(isNotEnabled()))
    }
}