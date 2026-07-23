package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }

    val partinoLetters = listOf('P', 'A', 'R', 'T', 'I', 'N', 'O')
    val letterOffsets = remember { partinoLetters.map { Animatable(-100f) } }
    val letterAlphas = remember { partinoLetters.map { Animatable(0f) } }

    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Logo Fade-in & Scale Animation
        logoAlpha.animateTo(1f, animationSpec = tween(800))
        logoScale.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f))

        // 2. PARTINO letter by letter dropping from top (TranslateAnimation)
        partinoLetters.indices.forEach { index ->
            launch {
                delay(index * 120L)
                letterAlphas[index].animateTo(1f, tween(300))
                letterOffsets[index].animateTo(0f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f))
            }
        }

        delay(800)
        // 3. Subtitle Fade-in
        subtitleAlpha.animateTo(1f, animationSpec = tween(500))

        // Total 3-second splash screen duration
        delay(1200)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo from URL with Coil & fallback to XML vector
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://cdn.imgurl.ir/uploads/h929475_logo.png",
                    contentDescription = "Partino Logo",
                    modifier = Modifier.size(110.dp),
                    error = painterResource(id = R.drawable.ic_logo),
                    placeholder = painterResource(id = R.drawable.ic_logo)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PARTINO Animated dropping letters
            Row(horizontalArrangement = Arrangement.Center) {
                partinoLetters.forEachIndexed { index, char ->
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .offset { IntOffset(0, letterOffsets[index].value.toInt()) }
                            .alpha(letterAlphas[index].value)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "مدیریت هوشمند مطالعه",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp
                ),
                color = Color.Gray,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}
