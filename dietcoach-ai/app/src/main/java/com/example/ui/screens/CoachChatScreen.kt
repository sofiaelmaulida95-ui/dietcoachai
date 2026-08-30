package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.model.CoachMessage
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(
    messages: List<CoachMessage>,
    savedInsights: List<CoachMessage>,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    onToggleSaveInsight: (CoachMessage, String, String) -> Unit,
    onDeleteMessage: (CoachMessage) -> Unit
) {
    var chatOrInsightsTab by remember { mutableStateOf(0) } // 0: Chat, 1: Buku Insight
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Bagaimana cara cegah lapar malam hari?",
        "Ide camilan sehat di bawah 100 kalori?",
        "Cara atasi timbangan stagnan (plateau)?",
        "Tips jalan kaki 8.000 langkah tanpa lelah",
        "Porsi makan seimbang untuk defisit 1400 kcal"
    )

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
                selectedTabIndex = chatOrInsightsTab,
                containerColor = Color.White,
                contentColor = PrimaryGreen
            ) {
                Tab(
                    selected = chatOrInsightsTab == 0,
                    onClick = { chatOrInsightsTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Konsultasi Coach", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = chatOrInsightsTab == 1,
                    onClick = { chatOrInsightsTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buku Insight (${savedInsights.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        if (chatOrInsightsTab == 0) {
            // Chat Thread View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            onToggleInsight = { category, note ->
                                onToggleSaveInsight(msg, category, note)
                            },
                            onDelete = { onDeleteMessage(msg) }
                        )
                    }

                    if (isSending) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PrimaryGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Coach sedang mengetik saran profesional...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQuestions) { q ->
                        SuggestionChip(
                            onClick = {
                                onSendMessage(q)
                            },
                            label = { Text(q, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = PrimaryGreen.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                // Input Bar
                Surface(
                    color = Color.White,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Tanya Coach Dietmu...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isSending) {
                                    val text = inputText
                                    inputText = ""
                                    onSendMessage(text)
                                }
                            },
                            enabled = inputText.isNotBlank() && !isSending,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank()) PrimaryGreen else SurfaceVariantColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Kirim",
                                tint = if (inputText.isNotBlank()) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        } else {
            // Saved Insights Book View
            SavedInsightsBookView(
                insights = savedInsights,
                onRemoveInsight = { insight ->
                    onToggleSaveInsight(insight, insight.insightCategory, "")
                }
            )
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: CoachMessage,
    onToggleInsight: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == "user"
    var showCategoryDialog by remember { mutableStateOf(false) }

    val timeFormatted = try {
        SimpleDateFormat("HH:mm", Locale.US).format(Date(message.timestamp))
    } catch (_: Exception) {
        ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Coach",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) PrimaryGreen else Color.White)
                    .padding(12.dp)
            ) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Coach Sofia (Ahli Gizi)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PrimaryGreenDark
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    if (message.isSavedAsInsight) {
                                        onToggleInsight("Umum", "")
                                    } else {
                                        showCategoryDialog = true
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (message.isSavedAsInsight) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Simpan Insight",
                                    tint = if (message.isSavedAsInsight) WarningColor else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isSavedAsInsight) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WarningColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Insight Tersimpan",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else TextMuted
                    )
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CoralOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showCategoryDialog) {
        SaveInsightDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = { category, note ->
                onToggleInsight(category, note)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun SavedInsightsBookView(
    insights: List<CoachMessage>,
    onRemoveInsight: (CoachMessage) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = listOf("Semua", "Nutrisi", "Pola Makan", "Olahraga", "Mindset", "Motivasi")

    val filteredInsights = if (selectedCategory == "Semua") {
        insights
    } else {
        insights.filter { it.insightCategory == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, tint = PurpleAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Catatan Insight & Kunci Sukses Diet 4 Bulan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                    Text(
                        text = "Semua saran penting dari Coach tersimpan rapi di sini sebagai panduan menjaga komitmen 1 September - 30 Desember 2026.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredInsights.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada insight yang disimpan di kategori ini.", fontSize = 13.sp, color = TextSecondary)
                        Text("Tekan ikon bookmark pada chat coach untuk menyimpannya.", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(filteredInsights) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PurpleLight
                            ) {
                                Text(
                                    text = item.insightCategory,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { onRemoveInsight(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.BookmarkRemove, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.text,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )

                        if (item.insightNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan: ${item.insightNotes}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveInsightDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Nutrisi") }
    var noteInput by remember { mutableStateOf("") }
    val categories = listOf("Nutrisi", "Pola Makan", "Olahraga", "Mindset", "Motivasi")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simpan Sebagai Insight Coach", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Catatan Tambahan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedCategory, noteInput) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Simpan ke Buku Insight")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
