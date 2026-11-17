package com.example.habittracker.ui.mascot

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Looping mascot. Changing [mascot] cross-fades to the new mood instead of cutting,
 * so the character feels like one creature changing expression.
 */
@Composable
fun MascotView(
    mascot: Mascot,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    speed: Float = 1f,
) {
    val animate = rememberAnimationsEnabled()
    AnimatedContent(
        targetState = mascot,
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(200)) },
        label = "mascot-mood",
        modifier = modifier,
    ) { mood ->
        MascotFrames(
            mascot = mood,
            size = size,
            iterations = if (animate) LottieConstants.IterateForever else 1,
            speed = if (animate) speed else 0f,
        )
    }
}

/**
 * Plays the mascot once and reports back, for celebratory reactions.
 * With system animations switched off it just holds a still frame.
 */
@Composable
fun MascotBurst(
    mascot: Mascot,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    speed: Float = 1.2f,
    onFinished: () -> Unit = {},
) {
    val animate = rememberAnimationsEnabled()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(mascot.rawRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = if (animate) speed else 0f,
    )

    LaunchedEffect(composition, progress) {
        if (composition != null && progress >= 1f) onFinished()
    }

    Box(modifier = modifier.size(size)) {
        LottieAnimation(
            composition = composition,
            progress = { if (animate) progress else STILL_FRAME },
            modifier = Modifier
                .size(size)
                .semantics { contentDescription = mascot.description },
        )
    }
}

@Composable
private fun MascotFrames(mascot: Mascot, size: Dp, iterations: Int, speed: Float) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(mascot.rawRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        restartOnPlay = false,
    )
    LottieAnimation(
        composition = composition,
        progress = { if (speed == 0f) STILL_FRAME else progress },
        modifier = Modifier
            .size(size)
            .semantics { contentDescription = mascot.description },
    )
}

/** Frame shown when the user has animations switched off - mid-loop reads best. */
private const val STILL_FRAME = 0.35f

/** False when the user turned animations off in developer or accessibility settings. */
@Composable
private fun rememberAnimationsEnabled(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) != 0f
        }.getOrDefault(true)
    }
}
