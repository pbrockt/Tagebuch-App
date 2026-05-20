package com.pbrockt.tagebuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pbrockt.tagebuch.navigation.AppNavigation
import com.pbrockt.tagebuch.ui.theme.TagebuchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TagebuchTheme {
                AppNavigation()
            }
        }
    }
}
