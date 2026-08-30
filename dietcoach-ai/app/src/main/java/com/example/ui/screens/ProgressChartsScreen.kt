package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutLog
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressChartsScreen(
    userProfile: UserProfile,
    weightLogs: List<WeightLog>,
    foodLogs: List<FoodLog>,
    workoutLogs: List<WorkoutLog>,
    isGeneratingPdf: Boolean,
    generatedPdf: File?,
    onGeneratePdf: (String) -> Unit,
    onSharePdf: () -> Unit
) {
    var selectedPdfType by remember { mutableStateOf("Mingguan") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PDF Export Banner Card
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
                                .background(PrimaryGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryGreenDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ekspor Laporan PDF Otomatis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Laporan komprehensif harian, mingguan, & bulanan",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Format PDF mencakup rekap makanan, kalori Huawei Health, grafik timbangan, hidrasi air, dan rekomendasi coach.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pdfTypes = listOf("Harian", "Mingguan", "Bulanan")
                        pdfTypes.forEach { type ->
                            val isSelected = selectedPdfType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPdfType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onGeneratePdf(selectedPdfType) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyusun Dokumen PDF...")
                        } else {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat & Bagikan PDF $selectedPdfType")
                        }
                    }
                }
            }
        }

        // Weight Trend Line Chart Card
        item {
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
                        Column {
                            Text(
                                text = "Grafik Progres Penurunan Berat Badan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Awal: 67.0 kg  ->  Target: 52.0 kg",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryGreenContainer
                        ) {
                            val latest = weightLogs.lastOrNull()?.weightKg ?: 67.0
                            Text(
                                text = "Saat Ini: $latest kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreenDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    WeightTrendCanvasChart(
                        weightLogs = weightLogs,
                        initialWeight = userProfile.initialWeightKg,
                        targetWeight = userProfile.targetWeightKg
                    )
                }
            }
        }

        // 4-Month Milestone Roadmap
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = PurpleAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Roadmap Target 4 Bulan (1 Sep - 30 Des 2026)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MilestoneRow(
                        phase = "Fase 1: September 2026",
                        title = "Adaptasi Defisit & Detox Air",
                        target = "67.0 kg -> 63.5 kg (-3.5 kg)",
                        desc = "Fokus konsistensi 1400 kcal, minum 2.2L air, dan jalan 7000 langkah.",
                        color = PrimaryGreen,
                        isCurrent = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MilestoneRow(
                        phase = "Fase 2: Oktober 2026",
                        title = "Akselerasi Pembakaran Lemak",
                        target = "63.5 kg -> 59.5 kg (-4.0 kg)",
                        desc = "Tingkatkan intensitas kardio interval & catat Huawei Health teratur.",
                        color = CoralOrange,
                        isCurrent = false
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MilestoneRow(
                        phase = "Fase 3: November 2026",
                        title = "Pembentukan Otot & Tone Tubuh",
                        target = "59.5 kg -> 55.5 kg (-4.0 kg)",
                        desc = "Fokus kekuatan tubuh bawah & core, tingkatkan porsi protein tanpa lemak.",
                        color = PurpleAccent,
                        isCurrent = false
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MilestoneRow(
                        phase = "Fase 4: Desember 2026",
                        title = "Mencapai Target Ideal & Stabilisasi",
                        target = "55.5 kg -> 52.0 kg (-3.5 kg)",
                        desc = "Target akhir 52 kg tercapai! Pola makan sehat telah menjadi gaya hidup permanen.",
                        color = WaterBlue,
                        isCurrent = false
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WeightTrendCanvasChart(
    weightLogs: List<WeightLog>,
    initialWeight: Double,
    targetWeight: Double
) {
    // Generate data points for chart
    val points = if (weightLogs.size < 2) {
        listOf(
            Pair("1 Sep", 67.0),
            Pair("8 Sep", 66.2),
            Pair("15 Sep", 65.5),
            Pair("22 Sep", 64.8),
            Pair("30 Sep", 63.5),
            Pair("31 Okt", 59.5),
            Pair("30 Nov", 55.5),
            Pair("30 Des", 52.0)
        )
    } else {
        weightLogs.map { Pair(it.date.takeLast(5), it.weightKg) }
    }

    val maxWeight = 70.0
    val minWeight = 50.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw Grid Lines
                val gridPaint = Color.LightGray.copy(alpha = 0.4f)
                val targetPaint = CoralOrange.copy(alpha = 0.5f)

                // 67kg line
                val y67 = height - (((67.0 - minWeight) / (maxWeight - minWeight)) * height).toFloat()
                drawLine(gridPaint, Offset(0f, y67), Offset(width, y67), strokeWidth = 1.dp.toPx())

                // 52kg Target line
                val y52 = height - (((52.0 - minWeight) / (maxWeight - minWeight)) * height).toFloat()
                drawLine(targetPaint, Offset(0f, y52), Offset(width, y52), strokeWidth = 2.dp.toPx())

                // Draw Smooth Curve
                if (points.size >= 2) {
                    val path = Path()
                    val stepX = width / (points.size - 1)

                    points.forEachIndexed { index, pair ->
                        val x = index * stepX
                        val weight = pair.second
                        val y = height - (((weight - minWeight) / (maxWeight - minWeight)) * height).toFloat()

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevWeight = points[index - 1].second
                            val prevY = height - (((prevWeight - minWeight) / (maxWeight - minWeight)) * height).toFloat()

                            val controlX1 = prevX + (x - prevX) / 2
                            val controlX2 = prevX + (x - prevX) / 2
                            path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = PrimaryGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circles on points
                    points.forEachIndexed { index, pair ->
                        val x = index * stepX
                        val weight = pair.second
                        val y = height - (((weight - minWeight) / (maxWeight - minWeight)) * height).toFloat()

                        drawCircle(
                            color = PrimaryGreenDark,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1 Sep (67kg)", fontSize = 10.sp, color = TextSecondary)
            Text(text = "Target 30 Des (52kg)", fontSize = 10.sp, color = CoralOrange, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MilestoneRow(
    phase: String,
    title: String,
    target: String,
    desc: String,
    color: Color,
    isCurrent: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isCurrent) color.copy(alpha = 0.1f) else SurfaceVariantColor,
        border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = phase, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color
                    ) {
                        Text(
                            text = "Fase Berjalan",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(top = 2.dp))
            Text(text = "Target: $target", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CoralOrange, modifier = Modifier.padding(top = 2.dp))
            Text(text = desc, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
