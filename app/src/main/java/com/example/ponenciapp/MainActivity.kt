package com.example.ponenciapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ponenciapp.navigation.AppNavigation
import com.example.ponenciapp.screens.utilidad.ThemeViewModel
import com.example.ponenciapp.screens.utilidad.ThemeViewModelFactory
import com.example.ponenciapp.ui.theme.PonenciAppTheme

class MainActivity : ComponentActivity() {
    // Lanzador para pedir permiso de notificaciones
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Si el usuario lo deniega las notificaciones simplemente no se mostrarán
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pedir permiso solo en Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val destinoNotificacion = intent?.getStringExtra("destino")

        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(context)
            )
            PonenciAppTheme(darkTheme = themeViewModel.isDarkTheme) {
                AppNavigation(destinoNotificacion = destinoNotificacion, themeViewModel = themeViewModel)
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
    PonenciAppTheme(darkTheme = false) {
        Greeting("Android")
    }
}