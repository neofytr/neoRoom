package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatapp.ui.theme.ChatappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // This works together with our theme's status bar handling
        setContent {
            ChatappTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary // Using theme color for text
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun GreetingPreviewLight() {
    ChatappTheme(darkTheme = false) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
            Greeting("Preview", Modifier.padding(innerPadding))
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun GreetingPreviewDark() {
    ChatappTheme(darkTheme = true) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
            Greeting("Preview", Modifier.padding(innerPadding))
        }
    }
}