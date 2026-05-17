package com.example.composedemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// ═════════════════════════════════════════════════════════════════════════════
//  INSTRUMENTED TEST
//
//  What makes this an instrumented test — 3 things together:
//
//  1. @RunWith(AndroidJUnit4::class)   ← uses Android's test runner, not JVM
//  2. This file lives in  src/androidTest/  ← not src/test/
//  3. createComposeRule()              ← spins up a real Activity on the device
//
//  How to run:
//    ./gradlew connectedDebugAndroidTest
//    (device or emulator must be connected)
// ═════════════════════════════════════════════════════════════════════════════

@RunWith(AndroidJUnit4::class)          // ← THIS is what makes it instrumented
class CounterScreenTest {

    // Creates a blank Activity on the real device to host our Composable
    @get:Rule
    val composeTestRule = createComposeRule()

    // ── helper — reduces copy-paste in every test ─────────────────────────
    private fun renderScreen(
        count: Int = 0,
        onIncrement: () -> Unit = {},
        onDecrement: () -> Unit = {},
        onReset: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            CounterScreen(
                count = count,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onReset = onReset,
            )
        }
    }

    // ── 1. Does the screen show the right number? ─────────────────────────

    @Test
    fun shows_initial_count_value() {
        renderScreen(count = 0)

        composeTestRule
            .onNodeWithTag(Tags.COUNTER_VALUE)
            .assertTextEquals("0")
    }

    @Test
    fun shows_non_zero_count_correctly() {
        renderScreen(count = 7)

        composeTestRule
            .onNodeWithTag(Tags.COUNTER_VALUE)
            .assertTextEquals("7")
    }

    // ── 2. Button enabled / disabled state ───────────────────────────────

    @Test
    fun decrement_button_disabled_when_count_is_zero() {
        renderScreen(count = 0)

        composeTestRule
            .onNodeWithTag(Tags.DECREMENT_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun decrement_button_enabled_when_count_is_above_zero() {
        renderScreen(count = 5)

        composeTestRule
            .onNodeWithTag(Tags.DECREMENT_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun increment_button_disabled_when_count_is_10() {
        renderScreen(count = 10)

        composeTestRule
            .onNodeWithTag(Tags.INCREMENT_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun increment_button_enabled_when_count_is_below_10() {
        renderScreen(count = 5)

        composeTestRule
            .onNodeWithTag(Tags.INCREMENT_BUTTON)
            .assertIsEnabled()
    }

    // ── 3. Callbacks fire when buttons are tapped ─────────────────────────

    @Test
    fun tapping_increment_fires_onIncrement_callback() {
        var wasCalled = false

        renderScreen(
            count = 5,
            onIncrement = { wasCalled = true }
        )

        composeTestRule
            .onNodeWithTag(Tags.INCREMENT_BUTTON)
            .performClick()

        assertTrue("onIncrement should have been called", wasCalled)
    }

    @Test
    fun tapping_decrement_fires_onDecrement_callback() {
        var wasCalled = false

        renderScreen(
            count = 5,
            onDecrement = { wasCalled = true }
        )

        composeTestRule
            .onNodeWithTag(Tags.DECREMENT_BUTTON)
            .performClick()

        assertTrue(wasCalled)
    }

    @Test
    fun tapping_reset_fires_onReset_callback() {
        var wasCalled = false

        renderScreen(
            count = 5,
            onReset = { wasCalled = true }
        )

        composeTestRule
            .onNodeWithTag(Tags.RESET_BUTTON)
            .performClick()

        assertTrue(wasCalled)
    }

    // ── 4. Disabled button does NOT fire callback ─────────────────────────

    @Test
    fun tapping_disabled_decrement_does_NOT_fire_callback() {
        var wasCalled = false

        renderScreen(
            count = 0,                              // decrement disabled at 0
            onDecrement = { wasCalled = true }
        )

        composeTestRule
            .onNodeWithTag(Tags.DECREMENT_BUTTON)
            .performClick()

        assertTrue("Disabled button must not fire callback", !wasCalled)
    }

    // ── 5. Conditional UI ─────────────────────────────────────────────────

    @Test
    fun error_text_not_shown_for_valid_count() {
        renderScreen(count = 0)

        composeTestRule
            .onNodeWithTag(Tags.ERROR_TEXT)
            .assertDoesNotExist()
    }

    @Test
    fun error_text_shown_when_count_is_negative() {
        // This would only happen if our guard fails — tests the safety net
        renderScreen(count = -1)

        composeTestRule
            .onNodeWithTag(Tags.ERROR_TEXT)
            .assertIsDisplayed()
    }

    // ── 6. All main nodes exist on screen ────────────────────────────────

    @Test
    fun all_main_ui_elements_are_visible() {
        renderScreen(count = 5)

        composeTestRule.onNodeWithTag(Tags.COUNTER_VALUE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(Tags.INCREMENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(Tags.DECREMENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(Tags.RESET_BUTTON).assertIsDisplayed()
    }
}
