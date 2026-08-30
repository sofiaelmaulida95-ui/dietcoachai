package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator(private val context: Context) {

    fun generateAndSharePdf(
        reportType: String, // "Harian", "Mingguan", "Bulanan"
        startDate: String,
        endDate: String,
        userProfile: UserProfile,
        foodLogs: List<FoodLog>,
        workoutLogs: List<WorkoutLog>,
        waterTotalMl: Int,
        weightLogs: List<WeightLog>,
        coachInsights: List<CoachMessage>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Background Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 130, 76) // Deep Green
        }
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        // Header Accent Strip
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(243, 104, 66) // Coral
        }
        canvas.drawRect(0f, 90f, 595f, 95f, accentPaint)

        // Header Texts
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("DIETCOACH AI - LAPORAN PROGRES $reportType.uppercase()", 24f, 40f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(220, 245, 230)
        val periodText = if (startDate == endDate) "Tanggal: $startDate" else "Periode: $startDate s/d $endDate"
        canvas.drawText("$periodText | Program: 1 Sep - 30 Des 2026 (4 Bulan)", 24f, 60f, paint)
        canvas.drawText("Dicetak pada: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())}", 24f, 75f, paint)

        var y = 115f

        // Profile & Goals Box
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 248, 245)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 225, 215)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 65f), 8f, 8f, boxPaint)
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 65f), 8f, 8f, borderPaint)

        paint.color = Color.rgb(30, 130, 76)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("PROFIL KLIEN & TARGET DIET", 36f, y + 20f, paint)

        paint.color = Color.rgb(40, 40, 40)
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val latestWeight = weightLogs.lastOrNull()?.weightKg ?: userProfile.currentWeightKg
        val weightDiff = latestWeight - userProfile.initialWeightKg
        val weightDiffStr = if (weightDiff <= 0) "${String.format(Locale.US, "%.1f", weightDiff)} kg" else "+${String.format(Locale.US, "%.1f", weightDiff)} kg"

        canvas.drawText("Nama: ${userProfile.name} (Wanita, ${userProfile.age} th)  |  Tinggi Badan: ${userProfile.heightCm.toInt()} cm", 36f, y + 36f, paint)
        canvas.drawText("BB Awal: ${userProfile.initialWeightKg} kg  ->  BB Sekarang: $latestWeight kg ($weightDiffStr)  |  Target BB: ${userProfile.targetWeightKg} kg", 36f, y + 50f, paint)

        y += 80f

        // Stats Summary 4 Grid
        val totalFoodCalories = foodLogs.sumOf { it.caloriesKcal }
        val totalWorkoutCalories = workoutLogs.sumOf { it.caloriesBurnedKcal }
        val totalSteps = workoutLogs.sumOf { it.steps }

        val cardWidth = 125f
        val cardHeight = 45f
        val cardSpacing = 11f

        drawSummaryCard(canvas, 24f, y, cardWidth, cardHeight, "Total Kalori Masuk", "$totalFoodCalories kcal", Color.rgb(230, 245, 235), Color.rgb(30, 130, 76))
        drawSummaryCard(canvas, 24f + (cardWidth + cardSpacing), y, cardWidth, cardHeight, "Kalori Huawei Health", "$totalWorkoutCalories kcal", Color.rgb(255, 240, 235), Color.rgb(243, 104, 66))
        drawSummaryCard(canvas, 24f + (cardWidth + cardSpacing) * 2, y, cardWidth, cardHeight, "Total Langkah Kaki", "$totalSteps langkah", Color.rgb(240, 248, 255), Color.rgb(2, 136, 209))
        drawSummaryCard(canvas, 24f + (cardWidth + cardSpacing) * 3, y, cardWidth, cardHeight, "Total Minum Air", "${waterTotalMl} ml", Color.rgb(235, 250, 255), Color.rgb(0, 150, 136))

        y += 60f

        // Section: Rekam Makanan & Nutrisi
        paint.color = Color.rgb(30, 130, 76)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("1. REKAP MAKANAN & ANALISIS NUTRISI", 24f, y, paint)
        y += 12f

        // Food Table Header
        drawTableRow(canvas, y, isHeader = true, listOf("Waktu", "Tipe", "Nama Makanan", "Kalori", "Protein", "Karbo", "Lemak", "Skor"))
        y += 18f

        if (foodLogs.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText("(Belum ada catatan makanan pada periode ini)", 36f, y + 10f, paint)
            y += 20f
        } else {
            for (food in foodLogs.take(8)) {
                drawTableRow(
                    canvas, y, isHeader = false,
                    listOf(
                        food.date.takeLast(5),
                        food.mealType.take(8),
                        food.foodName.take(22),
                        "${food.caloriesKcal} k",
                        "${food.proteinGrams.toInt()}g",
                        "${food.carbsGrams.toInt()}g",
                        "${food.fatGrams.toInt()}g",
                        "${food.healthScore}/10"
                    )
                )
                y += 16f
            }
        }

        y += 12f

        // Section: Rekam Olahraga Huawei Health
        paint.color = Color.rgb(243, 104, 66)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("2. REKAP AKTIVITAS OLAHRAGA (HUAWEI HEALTH SYNC)", 24f, y, paint)
        y += 12f

        drawTableRow(canvas, y, isHeader = true, listOf("Tanggal", "Aktivitas", "Durasi", "Kalori Terbakar", "Langkah", "Jarak", "Detak Jantung", "Sumber"))
        y += 18f

        if (workoutLogs.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText("(Belum ada catatan olahraga pada periode ini)", 36f, y + 10f, paint)
            y += 20f
        } else {
            for (w in workoutLogs.take(5)) {
                drawTableRow(
                    canvas, y, isHeader = false,
                    listOf(
                        w.date.takeLast(5),
                        w.workoutType.take(16),
                        "${w.durationMinutes} mnt",
                        "${w.caloriesBurnedKcal} kcal",
                        "${w.steps}",
                        "${String.format(Locale.US, "%.1f", w.distanceKm)} km",
                        "${w.avgHeartRateBpm} bpm",
                        "Huawei"
                    )
                )
                y += 16f
            }
        }

        y += 12f

        // Section: Catatan & Insight Coach Profesional
        paint.color = Color.rgb(142, 68, 173) // Purple
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("3. INSIGHT & REKOMENDASI COACH PROFESIONAL", 24f, y, paint)
        y += 15f

        val insightBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(252, 248, 255)
        }
        val insightBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(225, 205, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val boxHeight = 90f
        canvas.drawRoundRect(RectF(24f, y, 571f, y + boxHeight), 6f, 6f, insightBoxPaint)
        canvas.drawRoundRect(RectF(24f, y, 571f, y + boxHeight), 6f, 6f, insightBorderPaint)

        paint.color = Color.rgb(60, 40, 70)
        paint.textSize = 9f
        paint.isFakeBoldText = false

        val defaultAdvice = listOf(
            "• Pertahankan asupan kalori defisit harian rata-rata 1350-1400 kcal untuk penurunan aman 0.5-0.75 kg/minggu.",
            "• Pastikan porsi protein minimal 1.2g per kg BB (~75g protein/hari) untuk menjaga massa otot saat penurunan lemak.",
            "• Rutin capai target langkah Huawei Health (minimal 7000-8000 langkah) dan 2200 ml air putih setiap hari.",
            "• Tetap konsisten dan semangat dalam periode program 4 bulan (1 September - 30 Desember 2026)!"
        )

        var insightY = y + 18f
        for (line in defaultAdvice) {
            canvas.drawText(line, 36f, insightY, paint)
            insightY += 16f
        }

        // Footer
        paint.color = Color.rgb(140, 150, 145)
        paint.textSize = 8.5f
        canvas.drawText("DietCoach AI • Laporan Otomatis Resmi • Dikembangkan untuk Sofia (32 th) • 2026", 24f, 820f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache or external files directory
        return try {
            val fileName = "DietReport_${reportType}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null) ?: context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        bgColor: Int,
        textColor: Int
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 100, 95)
            textSize = 8f
        }
        canvas.drawText(title, x + 8f, y + 16f, textPaint)

        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 12f
            isFakeBoldText = true
        }
        canvas.drawText(value, x + 8f, y + 34f, valPaint)
    }

    private fun drawTableRow(canvas: Canvas, y: Float, isHeader: Boolean, columns: List<String>) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colWidths = floatArrayOf(50f, 60f, 130f, 55f, 45f, 45f, 45f, 60f)

        if (isHeader) {
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(235, 242, 238) }
            canvas.drawRect(24f, y - 10f, 571f, y + 6f, bg)
            paint.isFakeBoldText = true
            paint.color = Color.rgb(40, 60, 50)
            paint.textSize = 8.5f
        } else {
            paint.isFakeBoldText = false
            paint.color = Color.rgb(50, 50, 50)
            paint.textSize = 8.5f
        }

        var currentX = 30f
        for (i in columns.indices) {
            val text = columns[i]
            canvas.drawText(text, currentX, y, paint)
            currentX += colWidths.getOrElse(i) { 50f }
        }
    }

    fun sharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Progres Diet & Kebugaran")
                putExtra(Intent.EXTRA_TEXT, "Berikut adalah laporan progres diet dan kebugaran saya dari DietCoach AI.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
