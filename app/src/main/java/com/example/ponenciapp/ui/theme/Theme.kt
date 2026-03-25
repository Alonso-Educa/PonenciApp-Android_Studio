package com.example.ponenciapp.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

val MyLightColorScheme = lightColorScheme(
    primary = Color(0xFF475D92),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF575E71),
    onSecondary = Color(0xFFFFFFFF),

    tertiary = Color(0xFF725577),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFAF8FF),
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFE3E2EC),
    onSurfaceVariant = Color(0xFF46464F),

    outline = Color(0xFF777680),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

val MyDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FA8FF),        // azul suave
    secondary = Color(0xFFB0B8CC),      // gris azulado
    tertiary = Color(0xFFC7A6FF),       // toque violeta

//    background = Color(0xFF121212)  // o incluso 0xFF1A1A1A surface: 0xFF242424
    background = Color(0xFF1A1A1A),     // fondo oscuro clásico
    surface = Color(0xFF1A1A1A),        // tarjetas
    surfaceVariant = Color(0xFF2A2A2A), // variantes

    onPrimary = Color(0xFF000000),
    onBackground = Color(0xFFEAEAEA),
    onSurface = Color(0xFFEAEAEA)
)

@Composable
fun PonenciAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MyDarkColorScheme else MyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}