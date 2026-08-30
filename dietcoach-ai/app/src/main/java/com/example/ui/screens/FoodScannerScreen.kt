package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FoodLog
import com.example.data.network.FoodAnalysisResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScannerScreen(
    foodLogs: List<FoodLog>,
    isAnalyzing: Boolean,
    analysisResult: FoodAnalysisResult?,
    onAnalyzeImage: (Bitmap, String) -> Unit,
    onSaveAnalyzedFood: (String, String?) -> Unit,
    onSaveManualFood: (String, String, String, Int, Double, Double, Double, Double, String) -> Unit,
    onDeleteFood: (FoodLog) -> Unit
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var userNoteText by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Makan Siang") }
    var showManualDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
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
                    onAnalyzeImage(bmp, userNoteText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedImageUri = null
            onAnalyzeImage(bitmap, userNoteText)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload / Camera Action Header Card
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = PrimaryGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Scan Nutrisi Makanan Otomatis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Gemini Vision AI menganalisis kalori & makronutrisi",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = userNoteText,
                        onValueChange = { userNoteText = it },
                        placeholder = { Text("Catatan porsi/masakan (opsional, misal: tanpa nasi / minyak sedikit)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kamera", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galeri", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Atau Catat Manual Tanpa Foto", color = PrimaryGreen, fontSize = 12.sp)
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
                        CircularProgressIndicator(color = PrimaryGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Coach AI sedang menganalisis foto makanan...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mendeteksi bahan, menghitung kalori, protein, karbo, lemak & serat...",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Analysis Result Display Card
        if (analysisResult != null && !isAnalyzing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hasil Analisis Nutrisi AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryGreenDark
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryGreenLight
                            ) {
                                Text(
                                    text = "Skor Kesehatan: ${analysisResult.healthScore}/10",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Foto Makanan",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = analysisResult.foodName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Estimasi Porsi: ${analysisResult.portion}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Macro Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutrientBadge("Kalori", "${analysisResult.caloriesKcal} kcal", CoralOrange)
                            NutrientBadge("Protein", "${analysisResult.proteinGrams}g", PrimaryGreen)
                            NutrientBadge("Karbo", "${analysisResult.carbsGrams}g", WaterBlue)
                            NutrientBadge("Lemak", "${analysisResult.fatGrams}g", WarningColor)
                            NutrientBadge("Serat", "${analysisResult.fiberGrams}g", PurpleAccent)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Coach Verdict
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.TipsAndUpdates, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Saran Coach Diet:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryGreenDark)
                                }
                                Text(
                                    text = analysisResult.coachVerdict,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Meal Type Choice
                        Text(text = "Kategori Waktu Makan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val mealTypes = listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan")
                            mealTypes.forEach { type ->
                                val isSelected = selectedMealType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMealType = type },
                                    label = { Text(type, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onSaveAnalyzedFood(selectedMealType, selectedImageUri?.toString())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan ke Catatan Makanan Hari Ini")
                        }
                    }
                }
            }
        }

        // List of logged food
        item {
            Text(
                text = "Daftar Makanan Hari Ini (${foodLogs.sumOf { it.caloriesKcal }} kcal)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (foodLogs.isEmpty()) {
            item {
                Text(
                    text = "Belum ada makanan yang dicatat.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(foodLogs) { food ->
                FoodLogItemCard(foodLog = food, onDelete = { onDeleteFood(food) })
            }
        }
    }

    if (showManualDialog) {
        ManualFoodEntryDialog(
            onDismiss = { showManualDialog = false },
            onSave = { name, type, portion, cal, p, c, f, fib, verdict ->
                onSaveManualFood(name, type, portion, cal, p, c, f, fib, verdict)
                showManualDialog = false
            }
        )
    }
}

@Composable
fun NutrientBadge(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ManualFoodEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Makan Siang") }
    var portion by remember { mutableStateOf("1 porsi") }
    var calories by remember { mutableStateOf("300") }
    var protein by remember { mutableStateOf("20") }
    var carbs by remember { mutableStateOf("30") }
    var fat by remember { mutableStateOf("8") }
    var fiber by remember { mutableStateOf("4") }
    var notes by remember { mutableStateOf("Makanan diet seimbang") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Makanan Manual", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Makanan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Kalori (kcal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Karbo (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Lemak (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Serat (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            mealType,
                            portion,
                            calories.toIntOrNull() ?: 250,
                            protein.toDoubleOrNull() ?: 15.0,
                            carbs.toDoubleOrNull() ?: 25.0,
                            fat.toDoubleOrNull() ?: 6.0,
                            fiber.toDoubleOrNull() ?: 3.0,
                            notes
                        )
                    }
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
