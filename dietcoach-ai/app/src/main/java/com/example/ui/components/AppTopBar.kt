package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    selectedDate: String,
    userProfile: UserProfile,
    onDateSelected: (String) -> Unit,
    onPdfExportClick: () -> Unit
) {
    val context = LocalContext.current

    // Parse date for display
    val displayDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
        val date = parser.parse(selectedDate) ?: Date()
        formatter.format(date)
    } catch (e: Exception) {
        selectedDate
    }

    // Calculate days elapsed from 2026-09-01
    val dayNumber = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val start = parser.parse("2026-09-01")?.time ?: 0L
        val current = parser.parse(selectedDate)?.time ?: 0L
        val diffDays = ((current - start) / (1000 * 60 * 60 * 24)).toInt() + 1
        diffDays.coerceIn(1, 121)
    } catch (e: Exception) {
        1
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            try {
                                val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
                                if (d != null) cal.time = d
                            } catch (_: Exception) {}

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    onDateSelected(newDate)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = displayDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Tanggal",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryGreenContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Hari ke-$dayNumber / 121 • 1 Sep - 30 Des 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onPdfExportClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimaryGreenLight)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = PrimaryGreenDark
                )
            }
        }
    )
}
