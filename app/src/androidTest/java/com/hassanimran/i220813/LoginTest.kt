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
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

object ToastMatcher {
    fun isToast(): TypeSafeMatcher<Root> {
        return object : TypeSafeMatcher<Root>() {
            override fun describeTo(description: Description) {
                description.appendText("is toast")
            }
            override fun matchesSafely(root: Root): Boolean {
                val type = root.windowLayoutParams.get().type
                if (type == android.view.WindowManager.LayoutParams.TYPE_TOAST) {
                    val windowToken = root.decorView.windowToken
                    val appToken = root.decorView.applicationWindowToken
                    return windowToken == appToken
                }
                return false
            }
        }
    }
}

class FirebaseIdlingResource : IdlingResource {
    private var isIdle = true
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "FirebaseIdlingResource"
    override fun isIdleNow(): Boolean = isIdle
    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    fun setBusy() { isIdle = false }
    fun setIdle() {
        isIdle = true
        callback?.onTransitionToIdle()
    }
}

@RunWith(AndroidJUnit4::class)
class LoginTest {

    private val testEmail = "user1_${System.currentTimeMillis()}@test.com"
    private val testPassword = "password123"
    private lateinit var auth: FirebaseAuth
    private val firebaseIdlingResource = FirebaseIdlingResource()

    @Before
    fun setup() {
        IdlingRegistry.getInstance().register(firebaseIdlingResource)
        firebaseIdlingResource.setBusy()
        auth = Firebase.auth
        auth.useEmulator("10.0.2.2", 9099)
        auth.signOut()
        try {
            val signInTask = auth.signInWithEmailAndPassword(testEmail, testPassword)
            Tasks.await(signInTask, 5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Tasks.await(auth.createUserWithEmailAndPassword(testEmail, testPassword), 5, TimeUnit.SECONDS)
        }
        firebaseIdlingResource.setIdle()
        val intent = Intent(ApplicationProvider.getApplicationContext(), Login::class.java)
        ActivityScenario.launch<Login>(intent)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(firebaseIdlingResource)
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            try {
                Tasks.await(user.delete(), 5, TimeUnit.SECONDS)
            } catch (e: Exception) { }
        }
        auth.signOut()
    }

    @Test
    fun testSuccessfulLogin() {
        onView(withId(R.id.username_field)).perform(clearText(), typeText(testEmail), closeSoftKeyboard())
        onView(withId(R.id.password_field)).perform(clearText(), typeText(testPassword), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())
        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyFields() {
        onView(withId(R.id.username_field)).perform(clearText())
        onView(withId(R.id.password_field)).perform(clearText())
        onView(withId(R.id.login_button)).perform(click())
        onView(withText("Please fill out all fields"))
            .inRoot(ToastMatcher.isToast())
            .check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testInvalidCredentials() {
        onView(withId(R.id.username_field)).perform(clearText(), typeText("wrong@test.com"), closeSoftKeyboard())
        onView(withId(R.id.password_field)).perform(clearText(), typeText("wrongpassword"), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())
        onView(withText(containsString("Login failed")))
            .inRoot(ToastMatcher.isToast())
            .check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).check(matches(isDisplayed()))
    }
}