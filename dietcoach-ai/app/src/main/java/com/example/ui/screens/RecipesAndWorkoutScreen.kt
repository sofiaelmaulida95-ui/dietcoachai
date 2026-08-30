package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomRecipe
import com.example.data.model.ExerciseStep
import com.example.data.model.WorkoutGuideData
import com.example.data.model.WorkoutGuideItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesAndWorkoutScreen(
    recipes: List<CustomRecipe>,
    selectedCategory: String,
    isGeneratingAiRecipe: Boolean,
    onCategorySelected: (String) -> Unit,
    onGenerateAiRecipe: (String, String) -> Unit,
    onDeleteRecipe: (CustomRecipe) -> Unit
) {
    var mainTab by remember { mutableStateOf(0) } // 0: Resep Diet, 1: Panduan Olahraga Rutin
    var selectedRecipeDetail by remember { mutableStateOf<CustomRecipe?>(null) }
    var showAiRecipeDialog by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Sarapan", "Makan Siang", "Makan Malam", "Camilan Rendah Kalori")

    val filteredRecipes = if (selectedCategory == "Semua") {
        recipes
    } else {
        recipes.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // Tab Header
        Surface(
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = mainTab,
                containerColor = Color.White,
                contentColor = PrimaryGreen
            ) {
                Tab(
                    selected = mainTab == 0,
                    onClick = { mainTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resep Diet Harian", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = mainTab == 1,
                    onClick = { mainTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Panduan Olahraga", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        if (mainTab == 0) {
            // RECIPES LIST VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card with AI Recipe button
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Koleksi Resep Diet Sehat",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Lengkap takaran gizi, kalori & cara memasak praktis.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Button(
                                    onClick = { showAiRecipeDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Resep", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Filter Categories
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelected(cat) },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (isGeneratingAiRecipe) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Gemini sedang meracik resep diet bergizi sesuai bahanmu...",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                items(filteredRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { selectedRecipeDetail = recipe }
                    )
                }
            }
        } else {
            // WORKOUT ROUTINES LIST VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CoralOrangeLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = CoralOrange)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Jadwal Olahraga Rutin 4 Bulan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Disesuaikan untuk wanita 32 th (Aman sendi lutut, fokus toning & pembakaran)",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                items(WorkoutGuideData.weeklySchedule) { guide ->
                    WorkoutGuideCard(guide = guide)
                }
            }
        }
    }

    if (selectedRecipeDetail != null) {
        RecipeDetailDialog(
            recipe = selectedRecipeDetail!!,
            onDismiss = { selectedRecipeDetail = null },
            onDelete = {
                onDeleteRecipe(selectedRecipeDetail!!)
                selectedRecipeDetail = null
            }
        )
    }

    if (showAiRecipeDialog) {
        AiRecipeGeneratorDialog(
            onDismiss = { showAiRecipeDialog = false },
            onGenerate = { prompt, cat ->
                onGenerateAiRecipe(prompt, cat)
                showAiRecipeDialog = false
            }
        )
    }
}

@Composable
fun RecipeCard(
    recipe: CustomRecipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryGreenContainer
                ) {
                    Text(
                        text = recipe.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} menit",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )

            Text(
                text = recipe.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${recipe.caloriesKcal} kcal", fontWeight = FontWeight.Bold, color = CoralOrange, fontSize = 12.sp)
                    Text(text = "P: ${recipe.proteinGrams.toInt()}g", color = PrimaryGreen, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Text(text = "K: ${recipe.carbsGrams.toInt()}g", color = WaterBlue, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Text(text = "L: ${recipe.fatGrams.toInt()}g", color = WarningColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }

                Text(
                    text = "Lihat Cara Masak ->",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun RecipeDetailDialog(
    recipe: CustomRecipe,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryGreenContainer
                ) {
                    Text(
                        text = recipe.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = recipe.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Macro Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NutrientBadge("Kalori", "${recipe.caloriesKcal} k", CoralOrange)
                        NutrientBadge("Protein", "${recipe.proteinGrams}g", PrimaryGreen)
                        NutrientBadge("Karbo", "${recipe.carbsGrams}g", WaterBlue)
                        NutrientBadge("Lemak", "${recipe.fatGrams}g", WarningColor)
                        NutrientBadge("Serat", "${recipe.fiberGrams}g", PurpleAccent)
                    }
                }

                item {
                    Text(text = "Bahan-Bahan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = recipe.ingredients,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }

                item {
                    Text(text = "Cara Memasak:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = recipe.instructions,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Tutup")
            }
        },
        dismissButton = {
            if (recipe.isAiGenerated) {
                TextButton(onClick = onDelete) {
                    Text("Hapus Resep", color = CoralOrange)
                }
            }
        }
    )
}

@Composable
fun WorkoutGuideCard(guide: WorkoutGuideItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralOrangeLight
                    ) {
                        Text(
                            text = guide.dayOfWeek,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CoralOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceVariantColor
                    ) {
                        Text(
                            text = guide.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = guide.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Text(
                text = guide.description,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "🔥 Target: ~${guide.targetCaloriesKcal} kcal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralOrange)
                Text(text = "⏱️ ${guide.durationMinutes} mnt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = "⚡ ${guide.intensity}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceVariantColor)
                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Rangkaian Gerakan:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                guide.exercises.forEachIndexed { idx, ex ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${idx + 1}. ${ex.name}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PrimaryGreenDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "(${ex.repsOrDuration})", fontSize = 11.sp, color = CoralOrange, fontWeight = FontWeight.Medium)
                        }
                        Text(text = ex.instruction, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(start = 14.dp, top = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Tips Coach: ${guide.tips}",
                        fontSize = 11.sp,
                        color = PrimaryGreenDark,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiRecipeGeneratorDialog(
    onDismiss: () -> Unit,
    onGenerate: (String, String) -> Unit
) {
    var ingredientPrompt by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Makan Siang") }
    val categories = listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan Rendah Kalori")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buat Resep Diet AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tulis bahan makanan yang kamu punya di kulkas:", fontSize = 12.sp, color = TextSecondary)

                OutlinedTextField(
                    value = ingredientPrompt,
                    onValueChange = { ingredientPrompt = it },
                    placeholder = { Text("Contoh: Dada ayam, brokoli, telur, sawi putih, kecap asin", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(2).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(2).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ingredientPrompt.isNotBlank()) {
                        onGenerate(ingredientPrompt, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Buat Resep")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
