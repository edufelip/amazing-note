package com.edufelip.shared

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun GreetingView() {
    Text(Greeting().greet())
}

