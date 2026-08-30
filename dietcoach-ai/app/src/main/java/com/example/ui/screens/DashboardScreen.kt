package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(
    userProfile: UserProfile,
    selectedDate: String,
    foodLogs: List<FoodLog>,
    workoutLogs: List<WorkoutLog>,
    waterTotalMl: Int,
    currentWeight: Double,
    onNavigateToTab: (Int) -> Unit,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onLogWeight: (Double, Double?, String) -> Unit,
    onDeleteFoodLog: (FoodLog) -> Unit
) {
    var showWeightDialog by remember { mutableStateOf(false) }

    val totalFoodCalories = foodLogs.sumOf { it.caloriesKcal }
    val totalBurnedCalories = workoutLogs.sumOf { it.caloriesBurnedKcal }
    val totalSteps = workoutLogs.sumOf { it.steps }

    val calorieTarget = userProfile.dailyCalorieTargetKcal
    val netCalories = totalFoodCalories - totalBurnedCalories

    val waterGoal = userProfile.dailyWaterTargetMl
    val waterProgress = (waterTotalMl.toFloat() / waterGoal).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card with Profile & Coach Banner
        item {
            HeroProfileBanner(
                userProfile = userProfile,
                currentWeight = currentWeight,
                onWeighInClick = { showWeightDialog = true }
            )
        }

        // 4-Month Countdown & Journey Roadmap Card
        item {
            FourMonthJourneyCard(
                selectedDate = selectedDate,
                userProfile = userProfile,
                onExploreRoadmap = { onNavigateToTab(5) } // Goes to charts/milestones
            )
        }

        // Calorie & Deficit Summary Card
        item {
            CalorieBalanceCard(
                consumedKcal = totalFoodCalories,
                burnedKcal = totalBurnedCalories,
                targetKcal = calorieTarget,
                onScanFoodClick = { onNavigateToTab(1) },
                onSyncHuaweiClick = { onNavigateToTab(2) }
            )
        }

        // Hydration & Water Card
        item {
            HydrationCard(
                currentMl = waterTotalMl,
                goalMl = waterGoal,
                progress = waterProgress,
                onAddWater = onAddWater,
                onResetWater = onResetWater
            )
        }

        // Huawei Health Activity Sync Status Card
        item {
            HuaweiHealthSummaryCard(
                workoutLogs = workoutLogs,
                totalSteps = totalSteps,
                totalBurned = totalBurnedCalories,
                onUploadScreenshot = { onNavigateToTab(2) }
            )
        }

        // Today's Meals Breakdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan Makanan Hari Ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = { onNavigateToTab(1) }) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Scan",
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Scan Foto", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (foodLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada makanan yang dicatat hari ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Foto makananmu untuk analisis kalori & nutrisi otomatis oleh Gemini AI!",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateToTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil / Upload Foto Makanan")
                        }
                    }
                }
            }
        } else {
            items(foodLogs) { food ->
                FoodLogItemCard(
                    foodLog = food,
                    onDelete = { onDeleteFoodLog(food) }
                )
            }
        }

        // Quick Coach Consultation Call to Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTab(3) },
                colors = CardDefaults.cardColors(containerColor = PurpleLight),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PurpleAccent.copy(alpha = 0.3f), PrimaryGreen.copy(alpha = 0.3f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PurpleAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Coach AI",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tanya Coach Diet Pribadimu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Konsultasikan nafsu makan, resep pengganti, atau simpan insight penting.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PurpleAccent
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showWeightDialog) {
        QuickWeightDialog(
            currentWeight = currentWeight,
            onDismiss = { showWeightDialog = false },
            onConfirm = { weight, fat, note ->
                onLogWeight(weight, fat, note)
                showWeightDialog = false
            }
        )
    }
}

@Composable
fun HeroProfileBanner(
    userProfile: UserProfile,
    currentWeight: Double,
    onWeighInClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Banner image
                Image(
                    painter = painterResource(id = R.drawable.diet_coach_banner),
                    contentDescription = "Diet Coach Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Hai, ${userProfile.name}! 👋",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Wanita 32 th • TB ${userProfile.heightCm.toInt()} cm • Target: ${userProfile.targetWeightKg.toInt()} kg",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Timbangan Saat Ini", fontSize = 11.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", currentWeight),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Text(text = " kg", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    val weightLost = userProfile.initialWeightKg - currentWeight
                    val lostStr = if (weightLost >= 0) "Turun ${String.format(Locale.US, "%.1f", weightLost)} kg" else "Naik ${String.format(Locale.US, "%.1f", -weightLost)} kg"
                    Text(
                        text = "$lostStr dari 67.0 kg awal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (weightLost >= 0) SuccessColor else CoralOrange
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreenLight,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "BMI: ${String.format(Locale.US, "%.1f", userProfile.bmi)} • ${userProfile.getBmiCategory()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = onWeighInClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Catat Timbangan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FourMonthJourneyCard(
    selectedDate: String,
    userProfile: UserProfile,
    onExploreRoadmap: () -> Unit
) {
    val totalDays = 121
    val dayNumber = try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = parser.parse("2026-09-01")?.time ?: 0L
        val current = parser.parse(selectedDate)?.time ?: 0L
        val diff = ((current - start) / (1000 * 60 * 60 * 24)).toInt() + 1
        diff.coerceIn(1, totalDays)
    } catch (_: Exception) {
        1
    }

    val progress = (dayNumber.toFloat() / totalDays).coerceIn(0.01f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = WarningColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Program Diet 4 Bulan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "1 Sep - 30 Des 2026",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryGreen,
                trackColor = PrimaryGreenContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hari ke-$dayNumber dari 121 Hari (${(progress * 100).toInt()}%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                val phase = when {
                    dayNumber <= 30 -> "Fase 1: Adaptasi & Detox"
                    dayNumber <= 60 -> "Fase 2: Akselerasi Lemak"
                    dayNumber <= 90 -> "Fase 3: Pembentukan & Toning"
                    else -> "Fase 4: Konsolidasi & Stabil"
                }

                Text(
                    text = phase,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenDark
                )
            }
        }
    }
}

@Composable
fun CalorieBalanceCard(
    consumedKcal: Int,
    burnedKcal: Int,
    targetKcal: Int,
    onScanFoodClick: () -> Unit,
    onSyncHuaweiClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keseimbangan Kalori Harian",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryGreenContainer
                ) {
                    Text(
                        text = "Target: $targetKcal kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieMetricItem(
                    title = "Masuk (Makanan)",
                    value = "$consumedKcal",
                    unit = "kcal",
                    color = PrimaryGreen,
                    icon = Icons.Default.Restaurant
                )

                VerticalDivider(modifier = Modifier.height(40.dp))

                CalorieMetricItem(
                    title = "Terbakar (Huawei)",
                    value = "$burnedKcal",
                    unit = "kcal",
                    color = CoralOrange,
                    icon = Icons.Default.LocalFireDepartment
                )

                VerticalDivider(modifier = Modifier.height(40.dp))

                val remaining = targetKcal - consumedKcal
                CalorieMetricItem(
                    title = "Sisa Kuota",
                    value = "$remaining",
                    unit = "kcal",
                    color = if (remaining >= 0) WaterBlue else CoralOrange,
                    icon = Icons.Default.PieChart
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onScanFoodClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Menu", fontSize = 12.sp, color = PrimaryGreen)
                }

                OutlinedButton(
                    onClick = onSyncHuaweiClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = CoralOrange)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Huawei Health", fontSize = 12.sp, color = CoralOrange)
                }
            }
        }
    }
}

@Composable
fun CalorieMetricItem(
    title: String,
    value: String,
    unit: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = " $unit", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
        }
    }
}

@Composable
fun HydrationCard(
    currentMl: Int,
    goalMl: Int,
    progress: Float,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = WaterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Asupan Air Minum Harian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "$currentMl / $goalMl ml",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = WaterBlue,
                trackColor = WaterBlueLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onAddWater(250) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = WaterBlueLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+250 ml (Gelas)", fontSize = 11.sp, color = WaterBlue, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { onAddWater(500) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = WaterBlueLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+500 ml (Botol)", fontSize = 11.sp, color = WaterBlue, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onResetWater,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantColor)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun HuaweiHealthSummaryCard(
    workoutLogs: List<WorkoutLog>,
    totalSteps: Int,
    totalBurned: Int,
    onUploadScreenshot: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = CoralOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Huawei Health Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CoralOrangeLight
                ) {
                    Text(
                        text = "${workoutLogs.size} Sesi Terdata",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralOrange,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Langkah Hari Ini", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "$totalSteps", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Target: 8.000", fontSize = 10.sp, color = TextMuted)
                }

                VerticalDivider(modifier = Modifier.height(35.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Kalori Terbakar", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "$totalBurned kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CoralOrange)
                    Text(text = "Zona Pembakaran Lemak", fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onUploadScreenshot,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Upload Screenshot Huawei Health Hari Ini", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FoodLogItemCard(
    foodLog: FoodLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryGreenContainer
                    ) {
                        Text(
                            text = foodLog.mealType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${foodLog.caloriesKcal} kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralOrange
                    )
                }

                Text(
                    text = foodLog.foodName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Text(
                    text = "P: ${foodLog.proteinGrams.toInt()}g • K: ${foodLog.carbsGrams.toInt()}g • L: ${foodLog.fatGrams.toInt()}g",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuickWeightDialog(
    currentWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double?, String) -> Unit
) {
    var weightInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentWeight)) }
    var fatInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Catat Timbangan Hari Ini", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Berat Badan (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fatInput,
                    onValueChange = { fatInput = it },
                    label = { Text("Kadar Lemak Tubuh (%) - Opsional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan (misal: setelah BAB pagi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightInput.toDoubleOrNull() ?: currentWeight
                    val f = fatInput.toDoubleOrNull()
                    onConfirm(w, f, notesInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
