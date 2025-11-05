package com.example.galleryapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// -------------------- Цвета --------------------
private val WarmDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFDD835),         // жёлтая кнопка "+"
    onPrimary = Color.Black,
    secondary = Color(0xFFFFF8E1),       // бежевый для кнопок/акцентов
    surface = Color(0xFF3E2723),         // фон карточек
    surfaceVariant = Color(0xFF5D4037),  // фон подписи автора
    background = Color(0xFF212121),
    onBackground = Color.White,
    onSurface = Color.White
)

private val WarmLightColorScheme = lightColorScheme(
    primary = Color(0xFFFDD835),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFF8E1),
    surface = Color(0xFFF5F5DC),
    surfaceVariant = Color(0xFFFFE0B2),
    background = Color(0xFFFFF8E1),
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun GalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WarmDarkColorScheme
        else -> WarmLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
