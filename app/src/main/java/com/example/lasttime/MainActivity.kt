package com.example.lasttime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.lasttime.presentation.navigation.AppNavigation
import com.example.lasttime.ui.theme.LastTimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LastTimeTheme {
                AppNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}