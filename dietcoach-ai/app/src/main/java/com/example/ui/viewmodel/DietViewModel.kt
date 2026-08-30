package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.network.FoodAnalysisResult
import com.example.data.network.GeminiService
import com.example.data.network.HuaweiHealthResult
import com.example.data.pdf.PdfReportGenerator
import com.example.data.repository.DietRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DietViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DietRepository(AppDatabase.getInstance(application))
    private val geminiService = GeminiService()
    private val pdfReportGenerator = PdfReportGenerator(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Current selected date (default to today, or 2026-09-01 if earlier)
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Navigation Tab
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Loading & AI States
    private val _isAnalyzingFood = MutableStateFlow(false)
    val isAnalyzingFood: StateFlow<Boolean> = _isAnalyzingFood.asStateFlow()

    private val _foodAnalysisResult = MutableStateFlow<FoodAnalysisResult?>(null)
    val foodAnalysisResult: StateFlow<FoodAnalysisResult?> = _foodAnalysisResult.asStateFlow()

    private val _isAnalyzingHuawei = MutableStateFlow(false)
    val isAnalyzingHuawei: StateFlow<Boolean> = _isAnalyzingHuawei.asStateFlow()

    private val _huaweiAnalysisResult = MutableStateFlow<HuaweiHealthResult?>(null)
    val huaweiAnalysisResult: StateFlow<HuaweiHealthResult?> = _huaweiAnalysisResult.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    private val _isGeneratingRecipe = MutableStateFlow(false)
    val isGeneratingRecipe: StateFlow<Boolean> = _isGeneratingRecipe.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _recipeCategoryFilter = MutableStateFlow("Semua")
    val recipeCategoryFilter: StateFlow<String> = _recipeCategoryFilter.asStateFlow()

    // Database reactive streams
    val dailyFoodLogs: StateFlow<List<FoodLog>> = _selectedDate
        .flatMapLatest { date -> repository.getFoodLogsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodLogs: StateFlow<List<FoodLog>> = repository.getAllFoodLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyWorkoutLogs: StateFlow<List<WorkoutLog>> = _selectedDate
        .flatMapLatest { date -> repository.getWorkoutLogsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.getAllWorkoutLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyWaterTotal: StateFlow<Int> = _selectedDate
        .flatMapLatest { date -> repository.getTotalWaterByDate(date) }
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allWeightLogs: StateFlow<List<WeightLog>> = repository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeightLog: StateFlow<WeightLog?> = repository.getLatestWeightLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val coachMessages: StateFlow<List<CoachMessage>> = repository.getAllCoachMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedInsights: StateFlow<List<CoachMessage>> = repository.getSavedInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<CustomRecipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            seedInitialBaselineData()
        }

        // Keep current weight in user profile updated
        viewModelScope.launch {
            latestWeightLog.collect { log ->
                if (log != null) {
                    _userProfile.update { it.copy(currentWeightKg = log.weightKg) }
                }
            }
        }
    }

    private fun getCurrentDateString(): String {
        val now = Date()
        val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
        // If current date is before start date 2026-09-01, default to 2026-09-01 for realism
        return if (formatted < "2026-09-01") "2026-09-01" else formatted
    }

    private suspend fun seedInitialBaselineData() {
        withContext(Dispatchers.IO) {
            // Seed initial weight if empty
            val initialWeight = repository.getWeightLogForDate("2026-09-01")
            if (initialWeight == null) {
                repository.insertWeightLog(
                    WeightLog(
                        date = "2026-09-01",
                        weightKg = 67.0,
                        bodyFatPercentage = 31.5,
                        notes = "Timbangan hari pertama program diet 4 bulan"
                    )
                )
            }

            // Seed initial coach welcome message if chat is empty
            val messages = repository.getAllCoachMessages().first()
            if (messages.isEmpty()) {
                repository.insertCoachMessage(
                    CoachMessage(
                        sender = "coach",
                        text = "Halo Sofia! Selamat datang di program diet 4 bulan (1 September – 30 Desember 2026). Saya adalah Coach Profesional Diet & Nutrisi pribadimu.\n\nDengan profilmu (Perempuan, 32 th, BB 67 kg, TB 155 cm), target ideal kita adalah mencapai ~52 kg secara bertahap dan sehat. Target defisit kita adalah 1.400 kcal/hari dan pembakaran olahraga ~400 kcal.\n\nKamu bisa mengunggah foto makanan untuk analisis kalori otomatis, upload rekam Huawei Health harian, mencatat minum air, serta menimbang badan. Jangan ragu bertanya apa saja!",
                        isSavedAsInsight = true,
                        insightCategory = "Mindset",
                        insightNotes = "Selamat datang & komitmen 4 bulan program diet (Target: 67kg -> 52kg)"
                    )
                )
            }
        }
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setRecipeCategoryFilter(category: String) {
        _recipeCategoryFilter.value = category
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    // --- Water Tracking ---
    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.insertWaterLog(
                WaterLog(
                    date = _selectedDate.value,
                    amountMl = amountMl
                )
            )
            _snackbarMessage.value = "+$amountMl ml air berhasil dicatat! Tetap terhidrasi."
        }
    }

    fun resetWaterForSelectedDate() {
        viewModelScope.launch {
            val db = AppDatabase.getInstance(getApplication())
            db.waterLogDao().clearWaterLogsForDate(_selectedDate.value)
            _snackbarMessage.value = "Catatan air untuk tanggal ${_selectedDate.value} telah direset."
        }
    }

    // --- Weight Tracking ---
    fun logWeight(weightKg: Double, bodyFat: Double?, notes: String) {
        viewModelScope.launch {
            repository.insertWeightLog(
                WeightLog(
                    date = _selectedDate.value,
                    weightKg = weightKg,
                    bodyFatPercentage = bodyFat,
                    notes = notes
                )
            )
            _userProfile.update { it.copy(currentWeightKg = weightKg) }
            _snackbarMessage.value = "Timbangan $weightKg kg berhasil disimpan untuk ${_selectedDate.value}!"
        }
    }

    fun deleteWeightLog(log: WeightLog) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
            _snackbarMessage.value = "Catatan timbangan dihapus."
        }
    }

    // --- Food Analysis with Gemini ---
    fun analyzeFoodImage(bitmap: Bitmap, userNote: String = "") {
        viewModelScope.launch {
            _isAnalyzingFood.value = true
            _foodAnalysisResult.value = null
            val result = geminiService.analyzeFoodPhoto(bitmap, userNote)
            result.onSuccess { data ->
                _foodAnalysisResult.value = data
                _snackbarMessage.value = "Analisis foto makanan selesai!"
            }.onFailure { error ->
                _snackbarMessage.value = "Gagal menganalisis makanan: ${error.message}"
            }
            _isAnalyzingFood.value = false
        }
    }

    fun saveAnalyzedFoodLog(
        mealType: String,
        photoUri: String? = null
    ) {
        val currentResult = _foodAnalysisResult.value ?: return
        viewModelScope.launch {
            repository.insertFoodLog(
                FoodLog(
                    date = _selectedDate.value,
                    mealType = mealType,
                    foodName = currentResult.foodName,
                    portionDescription = currentResult.portion,
                    caloriesKcal = currentResult.caloriesKcal,
                    proteinGrams = currentResult.proteinGrams,
                    carbsGrams = currentResult.carbsGrams,
                    fatGrams = currentResult.fatGrams,
                    fiberGrams = currentResult.fiberGrams,
                    healthScore = currentResult.healthScore,
                    coachVerdict = currentResult.coachVerdict,
                    photoUri = photoUri
                )
            )
            _foodAnalysisResult.value = null
            _snackbarMessage.value = "${currentResult.foodName} (${currentResult.caloriesKcal} kcal) berhasil dicatat!"
        }
    }

    fun saveManualFoodLog(
        foodName: String,
        mealType: String,
        portion: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        verdict: String
    ) {
        viewModelScope.launch {
            repository.insertFoodLog(
                FoodLog(
                    date = _selectedDate.value,
                    mealType = mealType,
                    foodName = foodName,
                    portionDescription = portion,
                    caloriesKcal = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    fiberGrams = fiber,
                    healthScore = 8,
                    coachVerdict = verdict
                )
            )
            _snackbarMessage.value = "$foodName berhasil dicatat ke $mealType."
        }
    }

    fun deleteFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            repository.deleteFoodLog(foodLog)
            _snackbarMessage.value = "Catatan makanan dihapus."
        }
    }

    // --- Huawei Health Screenshot Analysis ---
    fun analyzeHuaweiScreenshot(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingHuawei.value = true
            _huaweiAnalysisResult.value = null
            val result = geminiService.analyzeHuaweiHealthScreenshot(bitmap)
            result.onSuccess { data ->
                _huaweiAnalysisResult.value = data
                _snackbarMessage.value = "Data Huawei Health berhasil dibaca!"
            }.onFailure { error ->
                _snackbarMessage.value = "Gagal membaca Huawei Health: ${error.message}"
            }
            _isAnalyzingHuawei.value = false
        }
    }

    fun saveAnalyzedHuaweiWorkout(photoUri: String? = null) {
        val currentResult = _huaweiAnalysisResult.value ?: return
        viewModelScope.launch {
            repository.insertWorkoutLog(
                WorkoutLog(
                    date = _selectedDate.value,
                    workoutType = currentResult.workoutType,
                    source = "Huawei Health",
                    durationMinutes = currentResult.durationMinutes,
                    caloriesBurnedKcal = currentResult.caloriesBurnedKcal,
                    steps = currentResult.steps,
                    distanceKm = currentResult.distanceKm,
                    avgHeartRateBpm = currentResult.avgHeartRateBpm,
                    coachFeedback = currentResult.coachFeedback,
                    photoUri = photoUri
                )
            )
            _huaweiAnalysisResult.value = null
            _snackbarMessage.value = "Aktivitas Huawei Health (${currentResult.caloriesBurnedKcal} kcal) tersimpan!"
        }
    }

    fun saveManualWorkoutLog(
        workoutType: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        steps: Int,
        distanceKm: Double,
        avgHeartRate: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertWorkoutLog(
                WorkoutLog(
                    date = _selectedDate.value,
                    workoutType = workoutType,
                    source = "Manual",
                    durationMinutes = durationMinutes,
                    caloriesBurnedKcal = caloriesBurned,
                    steps = steps,
                    distanceKm = distanceKm,
                    avgHeartRateBpm = avgHeartRate,
                    coachFeedback = notes
                )
            )
            _snackbarMessage.value = "Olahraga $workoutType berhasil dicatat!"
        }
    }

    fun deleteWorkoutLog(workoutLog: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(workoutLog)
            _snackbarMessage.value = "Catatan olahraga dihapus."
        }
    }

    // --- AI Coach Chat & Insights ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isSendingChat.value = true

            // Insert user message to Room
            repository.insertCoachMessage(
                CoachMessage(
                    sender = "user",
                    text = userText
                )
            )

            val currentHistory = coachMessages.value.map { Pair(it.sender, it.text) }

            val profile = _userProfile.value
            val todayFoods = dailyFoodLogs.value
            val todayWorkouts = dailyWorkoutLogs.value
            val todayWater = dailyWaterTotal.value
            val latestWeight = latestWeightLog.value?.weightKg ?: profile.currentWeightKg

            val systemInstruction = """
                Kamu adalah Coach Profesional Diet & Nutrisi Pribadi untuk seorang wanita bernama Sofia.
                Profil Klien:
                - Jenis Kelamin: Perempuan, Usia: 32 tahun, Tinggi Badan: 155 cm
                - Berat Badan Awal: 67 kg (1 September 2026) -> Berat Saat Ini: $latestWeight kg -> Target Berat Badan: 52 kg (Penurunan 15 kg dalam 4 bulan)
                - Periode Diet: 1 September 2026 s/d 30 Desember 2026 (4 Bulan penuh)
                - Target Kalori Harian: 1.400 kcal (Defisit sehat untuk wanita Asia 32 th tanpa kelaparan ekstrem)
                - Target Aktivitas: Olahraga rutin 30-45 mnt (sinkronisasi Huawei Health), Langkah: 8.000 langkah/hari, Air: 2.200 ml/hari
                - Data Hari Ini: Kalori masuk = ${todayFoods.sumOf { it.caloriesKcal }} kcal, Kalori olahraga terbakar = ${todayWorkouts.sumOf { it.caloriesBurnedKcal }} kcal, Air = $todayWater ml.

                Gaya Komunikasi:
                - Ramah, empatik, berbasis sains gizi praktis, memotivasi, dan profesional.
                - Berikan jawaban yang terstruktur, padat, dan langsung bisa dipraktekkan (resep, timing makan, tips mengatasi craving, porsi sayur/protein, arahan olahraga).
                - Selalu ingatkan bahwa konsistensi 4 bulan akan memberikan hasil nyata yang berkelanjutan tanpa efek yoyo.
                - Gunakan Bahasa Indonesia yang luwes, santun, dan menyemangati.
            """.trimIndent()

            val result = geminiService.sendChatMessage(userText, currentHistory, systemInstruction)
            result.onSuccess { reply ->
                repository.insertCoachMessage(
                    CoachMessage(
                        sender = "coach",
                        text = reply
                    )
                )
            }.onFailure { error ->
                repository.insertCoachMessage(
                    CoachMessage(
                        sender = "coach",
                        text = "Maaf, koneksi sedang ada kendala: ${error.message}. Namun saran saya: tetap jaga porsi makan seimbang, penuhi air 2.2L, dan bergerak aktif hari ini!"
                    )
                )
            }

            _isSendingChat.value = false
        }
    }

    fun toggleSaveInsight(message: CoachMessage, category: String = "Nutrisi", customNote: String = "") {
        viewModelScope.launch {
            val newStatus = !message.isSavedAsInsight
            repository.toggleSaveInsight(message.id, newStatus, category, customNote)
            _snackbarMessage.value = if (newStatus) "Pesan disimpan ke Catatan Insight Coach!" else "Insight dihapus dari daftar simpanan."
        }
    }

    fun deleteCoachMessage(message: CoachMessage) {
        viewModelScope.launch {
            repository.deleteCoachMessage(message)
        }
    }

    // --- AI Recipe Generator ---
    fun generateAiRecipe(prompt: String, category: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isGeneratingRecipe.value = true
            val result = geminiService.generateAiDietRecipe(prompt, category)
            result.onSuccess { json ->
                val newRecipe = CustomRecipe(
                    title = json.optString("title", "Resep Diet Sehat"),
                    category = json.optString("category", category),
                    description = json.optString("description", ""),
                    prepTimeMinutes = json.optInt("prepTimeMinutes", 10),
                    cookTimeMinutes = json.optInt("cookTimeMinutes", 15),
                    caloriesKcal = json.optInt("caloriesKcal", 300),
                    proteinGrams = json.optDouble("proteinGrams", 25.0),
                    carbsGrams = json.optDouble("carbsGrams", 20.0),
                    fatGrams = json.optDouble("fatGrams", 8.0),
                    fiberGrams = json.optDouble("fiberGrams", 5.0),
                    ingredients = json.optString("ingredients", ""),
                    instructions = json.optString("instructions", ""),
                    isAiGenerated = true
                )
                repository.insertRecipe(newRecipe)
                _snackbarMessage.value = "Resep '${newRecipe.title}' berhasil dibuat & disimpan!"
            }.onFailure { error ->
                _snackbarMessage.value = "Gagal membuat resep: ${error.message}"
            }
            _isGeneratingRecipe.value = false
        }
    }

    // --- PDF Export ---
    fun generatePdfReport(reportType: String) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            _generatedPdfFile.value = null

            val profile = _userProfile.value
            val todayStr = _selectedDate.value

            val (startD, endD, foods, workouts, water) = withContext(Dispatchers.IO) {
                when (reportType) {
                    "Harian" -> {
                        val f = repository.getFoodLogsByDate(todayStr).first()
                        val w = repository.getWorkoutLogsByDate(todayStr).first()
                        val wat = repository.getTotalWaterByDate(todayStr).first() ?: 0
                        Tuple5(todayStr, todayStr, f, w, wat)
                    }
                    "Mingguan" -> {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -7)
                        val startStr = dateFormat.format(cal.time)
                        val f = repository.getFoodLogsBetween(startStr, todayStr).first()
                        val w = repository.getWorkoutLogsBetween(startStr, todayStr).first()
                        val wat = repository.getWaterLogsBetween(startStr, todayStr).first().sumOf { it.amountMl }
                        Tuple5(startStr, todayStr, f, w, wat)
                    }
                    else -> { // Bulanan
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -30)
                        val startStr = dateFormat.format(cal.time)
                        val f = repository.getFoodLogsBetween(startStr, todayStr).first()
                        val w = repository.getWorkoutLogsBetween(startStr, todayStr).first()
                        val wat = repository.getWaterLogsBetween(startStr, todayStr).first().sumOf { it.amountMl }
                        Tuple5(startStr, todayStr, f, w, wat)
                    }
                }
            }

            val weights = allWeightLogs.value
            val insights = savedInsights.value

            val file = withContext(Dispatchers.IO) {
                pdfReportGenerator.generateAndSharePdf(
                    reportType = reportType,
                    startDate = startD,
                    endDate = endD,
                    userProfile = profile,
                    foodLogs = foods,
                    workoutLogs = workouts,
                    waterTotalMl = water,
                    weightLogs = weights,
                    coachInsights = insights
                )
            }

            _generatedPdfFile.value = file
            _isGeneratingPdf.value = false

            if (file != null) {
                _snackbarMessage.value = "Laporan PDF $reportType berhasil dibuat!"
                pdfReportGenerator.sharePdf(file)
            } else {
                _snackbarMessage.value = "Gagal membuat file PDF."
            }
        }
    }

    fun shareCurrentPdf() {
        val file = _generatedPdfFile.value
        if (file != null) {
            pdfReportGenerator.sharePdf(file)
        }
    }
}

data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
