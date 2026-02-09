package com.theveloper.pixelplay.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel

/**
 * Rewrite host for the player sheet.
 *
 * For now this delegates to the stable implementation so we can iterate behind
 * an experimental runtime switch without risking production behavior.
 *
 * Migration targets:
 * 1) split state collection from rendering
 * 2) collapse animation state into a single clock
 * 3) precompose full player sections with predictable interaction readiness
 *
 * Current progress:
 * - mini/full/prewarm layers extracted in UnifiedPlayerSheetLayers.kt
 * - queue/cast overlays extracted in UnifiedPlayerOverlaysLayer.kt
 * - song-info/save-queue overlays extracted in UnifiedPlayerOverlaysLayer.kt
 * - queue + song-info callback wiring moved to UnifiedPlayerQueueAndSongInfoHost
 * - motion clock extraction started via scoped SheetMotionController
 * - gesture math/dismiss handlers extracted to scoped controllers
 * - vertical drag pointer input moved to playerSheetVerticalDragGesture modifier
 * - horizontal dismiss pointer input moved to miniPlayerDismissHorizontalGesture modifier
 * - queue sheet open/drag/snap logic moved to QueueSheetController
 * - predictive back lifecycle moved to PlayerSheetPredictiveBackHandler
 * - sheet visual derived state moved to rememberSheetVisualState
 * - album-theme + mini-appearance visual state moved to rememberSheetThemeState
 * - shape + vertical drag interaction state moved to rememberSheetInteractionState
 * - overlay/IME/scrim derived state moved to rememberSheetOverlayState
 * - save-queue + song-info modal state moved to rememberSheetModalOverlayController
 * - queue runtime effects moved to QueueSheetRuntimeEffects
 * - queue state/controller derivation moved to rememberQueueSheetState
 * - mini dismiss handler creation moved to rememberMiniPlayerDismissGestureHandler
 * - drag/back state derivation moved to rememberSheetBackAndDragState
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun UnifiedPlayerSheetV2(
    playerViewModel: PlayerViewModel,
    sheetCollapsedTargetY: Float,
    containerHeight: Dp,
    collapsedStateHorizontalPadding: Dp = 12.dp,
    navController: NavHostController,
    hideMiniPlayer: Boolean = false,
    isNavBarHidden: Boolean = false
) {
    UnifiedPlayerSheetV2Host(
        playerViewModel = playerViewModel,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        containerHeight = containerHeight,
        collapsedStateHorizontalPadding = collapsedStateHorizontalPadding,
        navController = navController,
        hideMiniPlayer = hideMiniPlayer,
        isNavBarHidden = isNavBarHidden
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun UnifiedPlayerSheetV2Host(
    playerViewModel: PlayerViewModel,
    sheetCollapsedTargetY: Float,
    containerHeight: Dp,
    collapsedStateHorizontalPadding: Dp,
    navController: NavHostController,
    hideMiniPlayer: Boolean,
    isNavBarHidden: Boolean
) {
    // Layer composables live in UnifiedPlayerSheetLayers.kt and are ready for V2 host migration.
    // For parity, we still route rendering to the legacy scene until V2 motion/state host lands.
    UnifiedPlayerSheet(
        playerViewModel = playerViewModel,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        containerHeight = containerHeight,
        collapsedStateHorizontalPadding = collapsedStateHorizontalPadding,
        navController = navController,
        hideMiniPlayer = hideMiniPlayer,
        isNavBarHidden = isNavBarHidden
    )
}
