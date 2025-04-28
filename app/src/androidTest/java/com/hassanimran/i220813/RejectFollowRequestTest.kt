package com.hassanimran.i220813

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RejectFollowRequestTest {

    @Test
    fun testRejectFollowRequest() {
        // Launch Contacts activity
        ActivityScenario.launch(Contacts::class.java)

        // Verify user2's follow request is displayed
        onView(withId(R.id.followRequestsRecyclerView))
            .check(matches(hasDescendant(withText("user2"))))

        // Click the Reject button
        onView(withId(R.id.rejectButton)).perform(click())

        // Verify the request is removed
        onView(withText("user2")).check(doesNotExist())
    }
}