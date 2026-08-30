package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.*
import com.example.ui.theme.DietCoachTheme
import com.example.ui.viewmodel.DietViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietCoachTheme {
                val viewModel: DietViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val selectedDate by viewModel.selectedDate.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()
                val activeTab by viewModel.activeTab.collectAsState()

                val dailyFoodLogs by viewModel.dailyFoodLogs.collectAsState()
                val dailyWorkoutLogs by viewModel.dailyWorkoutLogs.collectAsState()
                val dailyWaterTotal by viewModel.dailyWaterTotal.collectAsState()
                val allWeightLogs by viewModel.allWeightLogs.collectAsState()
                val latestWeightLog by viewModel.latestWeightLog.collectAsState()
                val coachMessages by viewModel.coachMessages.collectAsState()
                val savedInsights by viewModel.savedInsights.collectAsState()
                val allRecipes by viewModel.allRecipes.collectAsState()
                val recipeCategoryFilter by viewModel.recipeCategoryFilter.collectAsState()

                val isAnalyzingFood by viewModel.isAnalyzingFood.collectAsState()
                val foodAnalysisResult by viewModel.foodAnalysisResult.collectAsState()
                val isAnalyzingHuawei by viewModel.isAnalyzingHuawei.collectAsState()
                val huaweiAnalysisResult by viewModel.huaweiAnalysisResult.collectAsState()
                val isSendingChat by viewModel.isSendingChat.collectAsState()
                val isGeneratingRecipe by viewModel.isGeneratingRecipe.collectAsState()
                val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()
                val generatedPdfFile by viewModel.generatedPdfFile.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()

                val currentWeight = latestWeightLog?.weightKg ?: userProfile.currentWeightKg

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearSnackbarMessage()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(
                            selectedDate = selectedDate,
                            userProfile = userProfile,
                            onDateSelected = { date -> viewModel.setSelectedDate(date) },
                            onPdfExportClick = { viewModel.setActiveTab(5) }
                        )
                    },
                    bottomBar = {
                        BottomNavBar(
                            selectedTab = activeTab,
                            onTabSelected = { tab -> viewModel.setActiveTab(tab) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> DashboardScreen(
                                userProfile = userProfile,
                                selectedDate = selectedDate,
                                foodLogs = dailyFoodLogs,
                                workoutLogs = dailyWorkoutLogs,
                                waterTotalMl = dailyWaterTotal,
                                currentWeight = currentWeight,
                                onNavigateToTab = { tab -> viewModel.setActiveTab(tab) },
                                onAddWater = { ml -> viewModel.logWater(ml) },
                                onResetWater = { viewModel.resetWaterForSelectedDate() },
                                onLogWeight = { w, fat, note -> viewModel.logWeight(w, fat, note) },
                                onDeleteFoodLog = { food -> viewModel.deleteFoodLog(food) }
                            )
                            1 -> FoodScannerScreen(
                                foodLogs = dailyFoodLogs,
                                isAnalyzing = isAnalyzingFood,
                                analysisResult = foodAnalysisResult,
                                onAnalyzeImage = { bmp, note -> viewModel.analyzeFoodImage(bmp, note) },
                                onSaveAnalyzedFood = { type, uri -> viewModel.saveAnalyzedFoodLog(type, uri) },
                                onSaveManualFood = { name, type, port, cal, p, c, f, fib, v ->
                                    viewModel.saveManualFoodLog(name, type, port, cal, p, c, f, fib, v)
                                },
                                onDeleteFood = { food -> viewModel.deleteFoodLog(food) }
                            )
                            2 -> HuaweiHealthScreen(
                                workoutLogs = dailyWorkoutLogs,
                                isAnalyzing = isAnalyzingHuawei,
                                analysisResult = huaweiAnalysisResult,
                                onAnalyzeScreenshot = { bmp -> viewModel.analyzeHuaweiScreenshot(bmp) },
                                onSaveAnalyzedWorkout = { uri -> viewModel.saveAnalyzedHuaweiWorkout(uri) },
                                onSaveManualWorkout = { type, dur, cal, steps, dist, hr, notes ->
                                    viewModel.saveManualWorkoutLog(type, dur, cal, steps, dist, hr, notes)
                                },
                                onDeleteWorkout = { log -> viewModel.deleteWorkoutLog(log) }
                            )
                            3 -> CoachChatScreen(
                                messages = coachMessages,
                                savedInsights = savedInsights,
                                isSending = isSendingChat,
                                onSendMessage = { text -> viewModel.sendChatMessage(text) },
                                onToggleSaveInsight = { msg, cat, note -> viewModel.toggleSaveInsight(msg, cat, note) },
                                onDeleteMessage = { msg -> viewModel.deleteCoachMessage(msg) }
                            )
                            4 -> RecipesAndWorkoutScreen(
                                recipes = allRecipes,
                                selectedCategory = recipeCategoryFilter,
                                isGeneratingAiRecipe = isGeneratingRecipe,
                                onCategorySelected = { cat -> viewModel.setRecipeCategoryFilter(cat) },
                                onGenerateAiRecipe = { prompt, cat -> viewModel.generateAiRecipe(prompt, cat) },
                                onDeleteRecipe = { recipe ->
                                    scope.launch {
                                        com.example.data.db.AppDatabase.getInstance(applicationContext).recipeDao().deleteRecipe(recipe)
                                    }
                                }
                            )
                            5 -> ProgressChartsScreen(
                                userProfile = userProfile,
                                weightLogs = allWeightLogs,
                                foodLogs = dailyFoodLogs,
                                workoutLogs = dailyWorkoutLogs,
                                isGeneratingPdf = isGeneratingPdf,
                                generatedPdf = generatedPdfFile,
                                onGeneratePdf = { type -> viewModel.generatePdfReport(type) },
                                onSharePdf = { viewModel.shareCurrentPdf() }
                            )
                        }
                    }
                }
            }
        }
    }
}
