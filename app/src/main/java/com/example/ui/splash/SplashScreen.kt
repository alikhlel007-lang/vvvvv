package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.CafeCardBorder
import com.example.ui.theme.CafeCyanPrimary
import com.example.ui.theme.CafeDarkBg
import com.example.ui.theme.CafeGold
import com.example.ui.theme.CafeTextPrimary
import com.example.ui.theme.CafeTextSecondary

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    val glowPulse = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Smooth entrance
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
        // Subtle pulse effect
        glowPulse.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF042024),
                        CafeDarkBg,
                        Color(0xFF000000)
                    )
                )
            )
            .clickable {
                // Allow tapping to skip immediately
                viewModel.navigateTo(if (currentUser != null) AppScreen.CASHIER_ORDERS else AppScreen.LOGIN)
            }
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Main Q-Cafe App Icon Box
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value * glowPulse.value)
                    .alpha(alpha.value)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.radialGradient(
                            listOf(CafeCyanPrimary, CafeGold, CafeCardBorder)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cafe_logo),
                    contentDescription = "Q-Cafe Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .testTag("splash_logo_image")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Q-Cafe",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CafeTextPrimary,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle in Arabic
            Text(
                text = "نظام المنيو الإلكتروني الذكي وإدارة الكاشير",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = CafeTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Subtle Loading Spinner
            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .alpha(alpha.value),
                color = CafeCyanPrimary,
                strokeWidth = 2.5.dp
            )
        }
    }
}
