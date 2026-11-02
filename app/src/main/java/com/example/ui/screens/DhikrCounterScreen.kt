package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrCounterScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val count by viewModel.dhikrCount.collectAsState()
    val target by viewModel.dhikrTarget.collectAsState()
    val currentPhrase by viewModel.dhikrPhrase.collectAsState()

    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(),
        label = "scale"
    )

    fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sacred Tasbih Counter", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetDhikr() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Counter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .testTag("dhikr_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Preset Phrases Chips
            val presets = listOf(
                "SubhanAllah (سبحان الله)",
                "Alhamdulillah (الحمد لله)",
                "Allahu Akbar (الله أكبر)",
                "Astaghfirullah (أستغفر الله)",
                "La ilaha illallah (لا إله إلا الله)"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select Dhikr:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    presets.take(3).forEach { phrase ->
                        val short = phrase.substringBefore(" ")
                        FilterChip(
                            selected = currentPhrase == phrase,
                            onClick = { viewModel.dhikrPhrase.value = phrase },
                            label = { Text(short, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Central Bead Counter Disc
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = currentPhrase,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmeraldLight.copy(alpha = 0.9f),
                                    EmeraldPrimary,
                                    EmeraldDark
                                )
                            )
                        )
                        .clickable {
                            isPressed = true
                            vibrate()
                            viewModel.incrementDhikr()
                            isPressed = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "/ $target",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TAP TO COUNT",
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Target Selector & Tributes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(33, 100, 1000).forEach { t ->
                        FilterChip(
                            selected = target == t,
                            onClick = { viewModel.dhikrTarget.value = t },
                            label = { Text("$t", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Created by Hamza Majeed • Pray for My Parents",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
