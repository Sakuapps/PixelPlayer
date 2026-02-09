package com.theveloper.pixelplay.presentation.components.scoped

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Density
import com.theveloper.pixelplay.presentation.viewmodel.PlayerSheetState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.max

internal data class SheetOverlayState(
    val internalIsKeyboardVisible: Boolean,
    val actuallyShowSheetContent: Boolean,
    val isQueueVisible: Boolean,
    val queueVisualOpenFraction: Float,
    val bottomSheetOpenFraction: Float,
    val queueScrimAlpha: Float
)

@Composable
internal fun rememberSheetOverlayState(
    density: Density,
    showPlayerContentArea: Boolean,
    hideMiniPlayer: Boolean,
    currentSheetContentState: PlayerSheetState,
    hasPendingSaveQueueOverlay: Boolean,
    hasSelectedSongForInfo: Boolean,
    showQueueSheet: Boolean,
    queueHiddenOffsetPx: Float,
    queueSheetOffset: Animatable<Float, AnimationVector1D>,
    screenHeightPx: Float,
    castSheetOpenFraction: Float
): SheetOverlayState {
    var internalIsKeyboardVisible by remember { mutableStateOf(false) }

    val imeInsets = WindowInsets.ime
    LaunchedEffect(imeInsets, density) {
        snapshotFlow { imeInsets.getBottom(density) > 0 }
            .distinctUntilChanged()
            .collectLatest { isVisible ->
                if (internalIsKeyboardVisible != isVisible) {
                    internalIsKeyboardVisible = isVisible
                }
            }
    }

    val shouldShowSheet by remember(showPlayerContentArea, hideMiniPlayer) {
        derivedStateOf { showPlayerContentArea && !hideMiniPlayer }
    }

    val actuallyShowSheetContent by remember(
        shouldShowSheet,
        internalIsKeyboardVisible,
        currentSheetContentState,
        hasPendingSaveQueueOverlay,
        hasSelectedSongForInfo
    ) {
        derivedStateOf {
            shouldShowSheet && (
                !internalIsKeyboardVisible ||
                    currentSheetContentState == PlayerSheetState.EXPANDED ||
                    hasPendingSaveQueueOverlay ||
                    hasSelectedSongForInfo
                )
        }
    }

    val isQueueVisible by remember(showQueueSheet, queueHiddenOffsetPx, queueSheetOffset) {
        derivedStateOf {
            showQueueSheet &&
                queueHiddenOffsetPx > 0f &&
                queueSheetOffset.value < queueHiddenOffsetPx
        }
    }

    val queueVisualOpenFraction by remember(queueSheetOffset, showQueueSheet, screenHeightPx) {
        derivedStateOf {
            if (!showQueueSheet || screenHeightPx <= 0f) {
                0f
            } else {
                val revealPx = (screenHeightPx - queueSheetOffset.value).coerceAtLeast(0f)
                (revealPx / screenHeightPx).coerceIn(0f, 1f)
            }
        }
    }

    val bottomSheetOpenFraction by remember(queueVisualOpenFraction, castSheetOpenFraction) {
        derivedStateOf { max(queueVisualOpenFraction, castSheetOpenFraction) }
    }

    val queueScrimAlpha by remember(queueVisualOpenFraction) {
        derivedStateOf { (queueVisualOpenFraction * 0.45f).coerceIn(0f, 0.45f) }
    }

    return SheetOverlayState(
        internalIsKeyboardVisible = internalIsKeyboardVisible,
        actuallyShowSheetContent = actuallyShowSheetContent,
        isQueueVisible = isQueueVisible,
        queueVisualOpenFraction = queueVisualOpenFraction,
        bottomSheetOpenFraction = bottomSheetOpenFraction,
        queueScrimAlpha = queueScrimAlpha
    )
}
