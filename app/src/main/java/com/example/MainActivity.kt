package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.RadioDashboard
import com.example.ui.RadioViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[RadioViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                RadioDashboard(viewModel = viewModel)
            }
        }
    }
}
