package com.theveloper.pixelplay.presentation.components.scoped

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.util.lerp
import com.theveloper.pixelplay.presentation.viewmodel.PlayerSheetState
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal fun PlayerSheetPredictiveBackHandler(
    enabled: Boolean,
    playerViewModel: PlayerViewModel,
    sheetCollapsedTargetY: Float,
    sheetExpandedTargetY: Float,
    sheetMotionController: SheetMotionController,
    animationDurationMs: Int
) {
    val scope = rememberCoroutineScope()
    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                playerViewModel.updatePredictiveBackCollapseFraction(backEvent.progress)
            }
            scope.launch {
                val progressAtRelease = playerViewModel.predictiveBackCollapseFraction.value
                val currentVisualY = lerp(sheetExpandedTargetY, sheetCollapsedTargetY, progressAtRelease)
                val currentVisualExpansionFraction = (1f - progressAtRelease).coerceIn(0f, 1f)
                sheetMotionController.snapTo(
                    translationYValue = currentVisualY,
                    expansionFractionValue = currentVisualExpansionFraction
                )
                playerViewModel.updatePredictiveBackCollapseFraction(1f)
                playerViewModel.collapsePlayerSheet()
                playerViewModel.updatePredictiveBackCollapseFraction(0f)
            }
        } catch (_: CancellationException) {
            scope.launch {
                Animatable(playerViewModel.predictiveBackCollapseFraction.value).animateTo(
                    targetValue = 0f,
                    animationSpec = tween(animationDurationMs)
                ) {
                    playerViewModel.updatePredictiveBackCollapseFraction(this.value)
                }

                if (playerViewModel.sheetState.value == PlayerSheetState.EXPANDED) {
                    playerViewModel.expandPlayerSheet()
                } else {
                    playerViewModel.collapsePlayerSheet()
                }
            }
        }
    }
}
