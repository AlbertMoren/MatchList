package com.example.matchlist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import android.content.Context
import android.content.res.Configuration

private val DarkColorScheme = darkColorScheme(
    //primary = InternationalOrange,
    primary = Rust,
    onPrimary = White,
    secondary = Rust,
    onSecondary = White,
    background = GrayDark,
    onBackground = Whitish,
    surface = GrayDark,
    onSurface = Whitish
)


private val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdSurface,
    secondary = MdPrimaryDark,
    onSecondary = MdSurface,
    background = MdBackground,
    onBackground = MdTextPrimary,
    surface = MdSurface,
    onSurface = MdTextPrimary,
)

@Composable
fun MatchListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

fun isDarkModeEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("CONFIG_APP", Context.MODE_PRIVATE)
    val modoSalvo = prefs.getString("MODO_USER", "AUTO") ?: "AUTO"

    return when (modoSalvo) {
        "ESCURO" -> true
        "CLARO" -> false
        else -> (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}