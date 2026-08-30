package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutGuideData
import com.example.data.model.WorkoutLog
import com.example.data.network.HuaweiHealthResult
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuaweiHealthScreen(
    workoutLogs: List<WorkoutLog>,
    isAnalyzing: Boolean,
    analysisResult: HuaweiHealthResult?,
    onAnalyzeScreenshot: (Bitmap) -> Unit,
    onSaveAnalyzedWorkout: (String?) -> Unit,
    onSaveManualWorkout: (String, Int, Int, Int, Double, Int, String) -> Unit,
    onDeleteWorkout: (WorkoutLog) -> Unit
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showManualDialog by remember { mutableStateOf(false) }

    val screenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bmp
                inputStream?.close()
                if (bmp != null) {
                    onAnalyzeScreenshot(bmp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Huawei Health Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CoralOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = CoralOrange)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sinkronisasi Huawei Health",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Upload screenshot aplikasi Huawei Health harian",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Gemini Vision otomatis mendeteksi: Langkah kaki, Kalori terbakar (Active Calories), Durasi olahraga, Jarak (km), dan Rata-rata denyut jantung (bpm).",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { screenshotLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Foto Screenshot Huawei Health")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = CoralOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Olahraga Manual", color = CoralOrange, fontSize = 12.sp)
                    }
                }
            }
        }

        // Loading State
        if (isAnalyzing) {
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
                        CircularProgressIndicator(color = CoralOrange)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mengekstrak data dari screenshot Huawei Health...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Membaca OCR langkah, kalori terbakar, dan detak jantung...",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Analysis Result Card
        if (analysisResult != null && !isAnalyzing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoralOrange))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hasil Ekstraksi Huawei Health",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CoralOrange
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CoralOrangeLight
                            ) {
                                Text(
                                    text = "Huawei Health OCR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = CoralOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Screenshot Huawei Health",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = analysisResult.workoutType,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutrientBadge("Kalori Terbakar", "${analysisResult.caloriesBurnedKcal} kcal", CoralOrange)
                            NutrientBadge("Langkah", "${analysisResult.steps}", PrimaryGreen)
                            NutrientBadge("Durasi", "${analysisResult.durationMinutes} mnt", WaterBlue)
                            NutrientBadge("Jarak", "${String.format(Locale.US, "%.1f", analysisResult.distanceKm)} km", PurpleAccent)
                            NutrientBadge("Detak Jantung", "${analysisResult.avgHeartRateBpm} bpm", WarningColor)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Coach Feedback
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralOrangeLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Ulasan Coach Kebugaran:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CoralOrange)
                                }
                                Text(
                                    text = analysisResult.coachFeedback,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onSaveAnalyzedWorkout(selectedImageUri?.toString()) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan ke Catatan Olahraga Hari Ini")
                        }
                    }
                }
            }
        }

        // Synced Workouts List
        item {
            Text(
                text = "Catatan Olahraga Hari Ini (${workoutLogs.sumOf { it.caloriesBurnedKcal }} kcal terbakar)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (workoutLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Belum ada catatan aktivitas Huawei Health hari ini.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(workoutLogs) { log ->
                WorkoutLogCard(log = log, onDelete = { onDeleteWorkout(log) })
            }
        }
    }

    if (showManualDialog) {
        ManualWorkoutEntryDialog(
            onDismiss = { showManualDialog = false },
            onSave = { type, dur, cal, steps, dist, hr, notes ->
                onSaveManualWorkout(type, dur, cal, steps, dist, hr, notes)
                showManualDialog = false
            }
        )
    }
}

@Composable
fun WorkoutLogCard(
    log: WorkoutLog,
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
                    .background(CoralOrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = CoralOrange)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.workoutType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CoralOrangeContainer
                    ) {
                        Text(
                            text = log.source,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralOrange,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "${log.caloriesBurnedKcal} kcal • ${log.durationMinutes} mnt • ${log.steps} langkah",
                    fontSize = 12.sp,
                    color = CoralOrange,
                    fontWeight = FontWeight.SemiBold
                )

                if (log.coachFeedback.isNotBlank()) {
                    Text(
                        text = log.coachFeedback,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

val CoralOrangeContainer = Color(0xFFFFE0D6)

@Composable
fun ManualWorkoutEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int, Double, Int, String) -> Unit
) {
    var workoutType by remember { mutableStateOf("Jalan Kaki Cepat") }
    var duration by remember { mutableStateOf("30") }
    var calories by remember { mutableStateOf("180") }
    var steps by remember { mutableStateOf("4500") }
    var distance by remember { mutableStateOf("2.8") }
    var heartRate by remember { mutableStateOf("120") }
    var notes by remember { mutableStateOf("Sesi olahraga rutin harian") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Olahraga Manual", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = workoutType,
                    onValueChange = { workoutType = it },
                    label = { Text("Jenis Aktivitas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Durasi (menit)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Kalori (kcal)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = steps,
                        onValueChange = { steps = it },
                        label = { Text("Langkah") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Jarak (km)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        workoutType,
                        duration.toIntOrNull() ?: 30,
                        calories.toIntOrNull() ?: 150,
                        steps.toIntOrNull() ?: 4000,
                        distance.toDoubleOrNull() ?: 2.5,
                        heartRate.toIntOrNull() ?: 120,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange)
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
