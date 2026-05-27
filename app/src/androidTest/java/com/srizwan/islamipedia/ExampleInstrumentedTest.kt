package com.srizwan.islamipedia

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // ✅ টেস্ট ১: সঠিক package name চেক
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.srizwan.islamipedia", appContext.packageName)
    }

    // ✅ টেস্ট ২: MainActivity লঞ্চ হয় কিনা চেক (স্ক্রিনে অ্যাপ দেখাবে)
    @Test
    fun appLaunchesSuccessfully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // অ্যাপ RESUMED state-এ আছে কিনা চেক
            scenario.onActivity { activity ->
                assertNotNull("Activity should not be null", activity)
            }
        }
    }
}
