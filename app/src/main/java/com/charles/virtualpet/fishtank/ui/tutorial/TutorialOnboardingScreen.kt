package com.charles.virtualpet.fishtank.ui.tutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.ui.theme.PastelBlue
import com.charles.virtualpet.fishtank.ui.theme.PastelGreen
import com.charles.virtualpet.fishtank.ui.theme.PastelPink
import com.charles.virtualpet.fishtank.ui.theme.PastelPurple
import com.charles.virtualpet.fishtank.ui.theme.PastelYellow
import com.charles.virtualpet.fishtank.ui.components.AdMobBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialOnboardingScreen(
    onComplete: () -> Unit,
    isReplay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val slides = TutorialSlides.slides
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == slides.size - 1
    val isFirstPage = currentPage == 0

    // Gradient background based on current page
    val gradientColors = listOf(
        listOf(PastelBlue, PastelGreen),
        listOf(PastelPink, PastelPurple),
        listOf(PastelBlue, PastelGreen),
        listOf(PastelYellow, PastelPink),
        listOf(PastelPurple, PastelBlue),
        listOf(PastelGreen, PastelYellow)
    )
    val currentGradient = gradientColors[currentPage.coerceAtMost(gradientColors.size - 1)]
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = currentGradient
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 58.dp), // Space for banner (50dp) + padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button (top right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onComplete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Skip", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                TutorialSlideContent(slide = slide)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page indicators
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                slides.forEachIndexed { index, _ ->
                    val isSelected = index == currentPage
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = tween(300),
                        label = "indicator_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                            )
                    )
                    if (index < slides.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button (hidden on first page)
                if (!isFirstPage) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color.White, Color.White))
                        )
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next/Finish button
                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (isLastPage) "Get Started!" else "Next",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // AdMob Banner
            AdMobBanner()
        }
    }
}

@Composable
private fun TutorialSlideContent(
    slide: TutorialSlide,
    modifier: Modifier = Modifier
) {
    val imageScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "image_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Icon/Image
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(imageScale),
                contentAlignment = Alignment.Center
            ) {
                if (slide.iconRes != null) {
                    Image(
                        painter = painterResource(id = slide.iconRes),
                        contentDescription = slide.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (slide.emoji != null) {
                    Text(
                        text = slide.emoji,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = slide.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF2C3E50), // Dark color for good contrast
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Description
            Text(
                text = slide.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                color = Color(0xFF2C3E50).copy(alpha = 0.9f), // Dark color for readability
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
