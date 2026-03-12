package com.example.ponenciapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ponenciapp.navigation.AppNavigation
import com.example.ponenciapp.ui.theme.PonenciAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val destinoNotificacion = intent?.getStringExtra("destino")
        setContent {
            PonenciAppTheme(darkTheme = false) {
                AppNavigation(destinoNotificacion = destinoNotificacion)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val destino = intent.getStringExtra("destino")
        if (destino != null) setIntent(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PonenciAppTheme {
        Greeting("Android")
    }
}