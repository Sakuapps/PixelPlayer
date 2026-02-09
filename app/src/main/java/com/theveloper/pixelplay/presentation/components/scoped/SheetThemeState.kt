package com.theveloper.pixelplay.presentation.components.scoped

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.preferences.ThemePreference
import com.theveloper.pixelplay.presentation.viewmodel.ColorSchemePair

internal data class SheetThemeState(
    val albumColorScheme: ColorScheme,
    val miniPlayerScheme: ColorScheme,
    val isPreparingPlayback: Boolean,
    val miniReadyAlpha: Float,
    val miniAppearScale: Float,
    val playerAreaBackground: Color,
    val effectivePlayerAreaElevation: Dp,
    val miniAlpha: Float
)

@Composable
internal fun rememberSheetThemeState(
    activePlayerSchemePair: ColorSchemePair?,
    isDarkTheme: Boolean,
    playerThemePreference: String,
    currentSong: Song?,
    themedAlbumArtUri: String?,
    preparingSongId: String?,
    playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
    systemColorScheme: ColorScheme
): SheetThemeState {
    val isAlbumArtTheme = playerThemePreference == ThemePreference.ALBUM_ART
    val hasAlbumArt = currentSong?.albumArtUriString != null
    val needsAlbumScheme = isAlbumArtTheme && hasAlbumArt

    val activePlayerScheme = remember(activePlayerSchemePair, isDarkTheme) {
        activePlayerSchemePair?.let { if (isDarkTheme) it.dark else it.light }
    }
    val currentSongActiveScheme = remember(
        activePlayerScheme,
        currentSong?.albumArtUriString,
        themedAlbumArtUri
    ) {
        if (
            activePlayerScheme != null &&
            !currentSong?.albumArtUriString.isNullOrBlank() &&
            currentSong?.albumArtUriString == themedAlbumArtUri
        ) {
            activePlayerScheme
        } else {
            null
        }
    }

    var lastAlbumScheme by remember { mutableStateOf<ColorScheme?>(null) }
    var lastAlbumSchemeSongId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentSong?.id) {
        if (currentSong?.id != lastAlbumSchemeSongId) {
            lastAlbumScheme = null
            lastAlbumSchemeSongId = null
        }
    }
    LaunchedEffect(currentSongActiveScheme, currentSong?.id) {
        val currentSongId = currentSong?.id
        if (currentSongId != null && currentSongActiveScheme != null) {
            lastAlbumScheme = currentSongActiveScheme
            lastAlbumSchemeSongId = currentSongId
        }
    }

    val sameSongLastAlbumScheme = remember(currentSong?.id, lastAlbumSchemeSongId, lastAlbumScheme) {
        if (currentSong?.id != null && currentSong?.id == lastAlbumSchemeSongId) {
            lastAlbumScheme
        } else {
            null
        }
    }
    val isPreparingPlayback = remember(preparingSongId, currentSong?.id) {
        preparingSongId != null && preparingSongId == currentSong?.id
    }

    val albumColorScheme = if (isAlbumArtTheme) {
        currentSongActiveScheme ?: sameSongLastAlbumScheme ?: systemColorScheme
    } else {
        systemColorScheme
    }

    val miniPlayerScheme = when {
        !needsAlbumScheme -> systemColorScheme
        currentSongActiveScheme != null -> currentSongActiveScheme
        sameSongLastAlbumScheme != null -> sameSongLastAlbumScheme
        else -> systemColorScheme
    }
    val miniAppearProgress = remember { Animatable(0f) }
    LaunchedEffect(currentSong?.id) {
        if (currentSong == null) {
            miniAppearProgress.snapTo(0f)
        } else if (miniAppearProgress.value < 1f) {
            miniAppearProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            )
        }
    }

    val miniReadyAlpha = miniAppearProgress.value
    val miniAppearScale = lerp(0.985f, 1f, miniAppearProgress.value)
    val playerAreaBackground = miniPlayerScheme.primaryContainer

    val t = rememberExpansionTransition(playerContentExpansionFraction.value)
    val playerAreaElevation by t.animateDp(label = "elev") { expansion ->
        lerp(2.dp, 12.dp, expansion)
    }
    val effectivePlayerAreaElevation = lerp(0.dp, playerAreaElevation, miniReadyAlpha)
    val miniAlpha by t.animateFloat(label = "miniAlpha") { expansion ->
        (1f - expansion * 2f).coerceIn(0f, 1f)
    }

    return SheetThemeState(
        albumColorScheme = albumColorScheme,
        miniPlayerScheme = miniPlayerScheme,
        isPreparingPlayback = isPreparingPlayback,
        miniReadyAlpha = miniReadyAlpha,
        miniAppearScale = miniAppearScale,
        playerAreaBackground = playerAreaBackground,
        effectivePlayerAreaElevation = effectivePlayerAreaElevation,
        miniAlpha = miniAlpha
    )
}
