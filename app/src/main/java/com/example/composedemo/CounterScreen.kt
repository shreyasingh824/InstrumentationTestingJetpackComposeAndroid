package com.example.composedemo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Test Tags ───────────────────────────────────────────────────────────────
// Constants so both the screen AND the test use the exact same string.
// Never type the tag string twice — one typo breaks the test silently.
object Tags {
    const val COUNTER_VALUE    = "counter_value"
    const val INCREMENT_BUTTON = "increment_button"
    const val DECREMENT_BUTTON = "decrement_button"
    const val RESET_BUTTON     = "reset_button"
    const val ERROR_TEXT       = "error_text"
}

// ─── Screen ──────────────────────────────────────────────────────────────────
@Composable
fun CounterScreen(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // The big number
        Text(
            text = count.toString(),
            fontSize = 80.sp,
            modifier = Modifier.testTag(Tags.COUNTER_VALUE)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message — only shown when count goes below 0 (should never happen,
        // but we test this guard)
        if (count < 0) {
            Text(
                text = "Count cannot be negative!",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(Tags.ERROR_TEXT)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            Button(
                onClick = onDecrement,
                enabled = count > 0,                          // disabled at zero
                modifier = Modifier.testTag(Tags.DECREMENT_BUTTON)
            ) {
                Text("−", fontSize = 24.sp)
            }

            Button(
                onClick = onIncrement,
                enabled = count < 10,                         // disabled at 10
                modifier = Modifier.testTag(Tags.INCREMENT_BUTTON)
            ) {
                Text("+", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.testTag(Tags.RESET_BUTTON)
        ) {
            Text("Reset")
        }
    }
}
