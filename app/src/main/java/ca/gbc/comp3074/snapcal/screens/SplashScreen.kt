package ca.gbc.comp3074.snapcal.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ca.gbc.comp3074.snapcal.R


@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val bg = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD600),
            Color(0xFFFF9800),
            Color(0xFFFF5A5F),
            Color(0xFF6F53D9)
        )
    )


    val infinite = rememberInfiniteTransition(label = "splash")


    val flameScale by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    val flameAlpha by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameAlpha"
    )


    val logoScale by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )


    LaunchedEffect(Unit) {
        delay(7000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(R.drawable.bg_fire_gradient),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .scale(flameScale)
                .alpha(flameAlpha)
        )


        Image(
            painter = painterResource(R.drawable.ic_snapcal),
            contentDescription = "SnapCal",
            modifier = Modifier
                .size(128.dp)
                .scale(logoScale)
        )
    }
}
