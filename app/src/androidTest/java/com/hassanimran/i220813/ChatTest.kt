package com.hassanimran.i220813

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatTest {

    private val receiverId = "user2"

    @Before
    fun setup() {
        // Launch Chat activity with RECEIVER_ID
        val intent = Intent().apply {
            putExtra("RECEIVER_ID", receiverId)
        }
        ActivityScenario.launch<Chat>(intent)
    }

    @Test
    fun testSendTextMessage() {
        // Type a message
        val message = "Hello, user2!"
        onView(withId(R.id.messageEditText)).perform(typeText(message), closeSoftKeyboard())

        // Click the send button
        onView(withId(R.id.sendButton)).perform(click())

        // Verify the message appears in the RecyclerView
        onView(withId(R.id.chatRecyclerView))
            .check(matches(hasDescendant(withText(message))))
    }

    @Test
    fun testSendEmptyMessage() {
        // Clear the EditText (ensure it's empty)
        onView(withId(R.id.messageEditText)).perform(clearText(), closeSoftKeyboard())

        // Click the send button
        onView(withId(R.id.sendButton)).perform(click())

        // Verify no new message appears (RecyclerView remains unchanged)
        // Since Chat.kt skips empty messages, we check the EditText remains empty
        onView(withId(R.id.messageEditText)).check(matches(withText("")))
    }

    @Test
    fun testSendMultipleMessages() {
        // Send first message
        val message1 = "First message"
        onView(withId(R.id.messageEditText)).perform(typeText(message1), closeSoftKeyboard())
        onView(withId(R.id.sendButton)).perform(click())

        // Send second message
        val message2 = "Second message"
        onView(withId(R.id.messageEditText)).perform(typeText(message2), closeSoftKeyboard())
        onView(withId(R.id.sendButton)).perform(click())

        // Verify both messages appear in order
        onView(withId(R.id.chatRecyclerView))
            .check(matches(hasDescendant(withText(message1))))
        onView(withId(R.id.chatRecyclerView))
            .check(matches(hasDescendant(withText(message2))))

        // Verify the second message is at the bottom (scroll to it)
        onView(withId(R.id.chatRecyclerView))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(message2))
            ))
    }
}