package com.theveloper.pixelplay.presentation.components.scoped

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controls queue sheet visibility, drag and snapping decisions.
 * Behavior mirrors the previous inline logic in UnifiedPlayerSheet.
 */
internal class QueueSheetController(
    private val scope: CoroutineScope,
    private val queueSheetOffset: Animatable<Float, AnimationVector1D>,
    private val hiddenOffsetProvider: () -> Float,
    private val allowInteractionProvider: () -> Boolean,
    private val minFlingTravelPxProvider: () -> Float,
    private val dragThresholdPxProvider: () -> Float,
    private val showQueueSheetProvider: () -> Boolean,
    private val onShowQueueSheetChange: (Boolean) -> Unit
) {
    suspend fun syncOffsetToVisibility() {
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset <= 0f) return
        val targetOffset = if (showQueueSheetProvider()) {
            queueSheetOffset.value.coerceIn(0f, hiddenOffset)
        } else {
            hiddenOffset
        }
        queueSheetOffset.snapTo(targetOffset)
    }

    suspend fun syncCollapsedWhenHidden() {
        val hiddenOffset = hiddenOffsetProvider()
        if (!showQueueSheetProvider() && hiddenOffset > 0f && queueSheetOffset.value != hiddenOffset) {
            queueSheetOffset.snapTo(hiddenOffset)
        }
    }

    suspend fun forceCollapseIfInteractionDisabled() {
        if (allowInteractionProvider()) return
        onShowQueueSheetChange(false)
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset > 0f) {
            queueSheetOffset.snapTo(hiddenOffset)
        }
    }

    suspend fun animateTo(targetExpanded: Boolean) {
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset == 0f) {
            onShowQueueSheetChange(targetExpanded)
            return
        }
        val target = if (targetExpanded) 0f else hiddenOffset
        onShowQueueSheetChange(true)
        queueSheetOffset.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 255,
                easing = FastOutSlowInEasing
            )
        )
        onShowQueueSheetChange(targetExpanded)
    }

    fun animate(targetExpanded: Boolean) {
        if (!allowInteractionProvider() && targetExpanded) return
        scope.launch { animateTo(targetExpanded && allowInteractionProvider()) }
    }

    fun beginDrag() {
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset == 0f || !allowInteractionProvider()) return
        onShowQueueSheetChange(true)
        scope.launch { queueSheetOffset.stop() }
    }

    fun dragBy(dragAmount: Float) {
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset == 0f || !allowInteractionProvider()) return
        val newOffset = (queueSheetOffset.value + dragAmount).coerceIn(0f, hiddenOffset)
        scope.launch { queueSheetOffset.snapTo(newOffset) }
    }

    fun endDrag(totalDrag: Float, velocity: Float) {
        val hiddenOffset = hiddenOffsetProvider()
        if (hiddenOffset == 0f || !allowInteractionProvider()) return

        val isFastUpward = velocity < -650f
        val isFastDownward = velocity > 650f
        val hasMeaningfulUpwardTravel = totalDrag < -minFlingTravelPxProvider()
        val dragThresholdPx = dragThresholdPxProvider()
        val shouldExpand = (isFastUpward && hasMeaningfulUpwardTravel) ||
            (!isFastDownward && (
                queueSheetOffset.value < hiddenOffset - dragThresholdPx ||
                    totalDrag < -dragThresholdPx
                ))

        animate(shouldExpand)
    }
}
