package com.example.composedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Simple state — in a real app this would be in a ViewModel
                var count by remember { mutableStateOf(0) }

                CounterScreen(
                    count = count,
                    onIncrement = { if (count < 10) count++ },
                    onDecrement = { if (count > 0) count-- },
                    onReset = { count = 0 }
                )
            }
        }
    }
}
