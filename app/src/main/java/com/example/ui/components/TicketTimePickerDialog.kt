package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TimingType
import com.example.ui.theme.TransitNavy
import com.example.ui.theme.TransitTeal
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketTimePickerDialog(
    currentTime: String = "08:30 AM",
    currentDate: String = "Today",
    currentTimingType: TimingType = TimingType.DEPART_AT,
    onTimingSelected: ((date: String, timingType: TimingType, time: String) -> Unit)? = null,
    onTimeSelected: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(currentDate) }
    var selectedTimingType by remember { mutableStateOf(currentTimingType) }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(30) }
    var isAm by remember { mutableStateOf(true) }

    val dateOptions = listOf("Today", "Tomorrow", "In 2 Days", "Weekend")

    val quickSlots = listOf(
        "08:00 AM" to "Morning Early",
        "08:30 AM" to "Morning Peak",
        "09:15 AM" to "Office Arrival",
        "12:30 PM" to "Midday Transit",
        "05:30 PM" to "Evening Early",
        "06:15 PM" to "Evening Rush",
        "08:00 PM" to "Night Return",
        "10:00 PM" to "Late Night"
    )

    fun applySelection(timeString: String, timingType: TimingType = selectedTimingType) {
        if (onTimingSelected != null) {
            onTimingSelected(selectedDate, timingType, timeString)
        } else {
            onTimeSelected?.invoke(timeString)
        }
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TransitTeal,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Travel Date & Time",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Travel Date Selection
                Text(
                    text = "TRAVEL DATE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateOptions.forEach { dateLabel ->
                        val isSelected = selectedDate == dateLabel
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TransitNavy else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable { selectedDate = dateLabel }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (isSelected) TransitTeal else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TransitNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Timing Preference Tabs (Leave Now vs Depart At vs Arrive By)
                Text(
                    text = "TIMING PREFERENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimingType.values().forEach { tType ->
                        val isSelected = selectedTimingType == tType
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TransitTeal.copy(alpha = 0.2f) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) TransitTeal else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedTimingType = tType
                                    if (tType == TimingType.LEAVE_NOW) {
                                        val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                        applySelection(now, TimingType.LEAVE_NOW)
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (tType == TimingType.LEAVE_NOW) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = if (isSelected) TransitTeal else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = tType.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TransitNavy else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Quick Commute Slots
                Text(
                    text = when (selectedTimingType) {
                        TimingType.ARRIVE_BY -> "QUICK DESIRED ARRIVAL SLOTS"
                        else -> "QUICK COMMUTE DEPARTURE SLOTS"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickSlots.chunked(2).forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { (timeStr, tag) ->
                                val isSelected = currentTime == timeStr
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) TransitNavy else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            applySelection(timeStr)
                                        }
                                        .testTag("time_slot_$timeStr")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = timeStr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TransitNavy
                                        )
                                        Text(
                                            text = tag,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(14.dp))

                // 4. Custom Time Picker (Hour, Minute, AM/PM)
                Text(
                    text = when (selectedTimingType) {
                        TimingType.ARRIVE_BY -> "CUSTOM ARRIVAL TIME"
                        else -> "CUSTOM DEPARTURE TIME"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HOUR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedHour = if (selectedHour > 1) selectedHour - 1 else 12 }) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format("%02d", selectedHour),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            IconButton(onClick = { selectedHour = if (selectedHour < 12) selectedHour + 1 else 1 }) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(":", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TransitNavy)

                    // Minutes Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedMinute = (selectedMinute - 15 + 60) % 60 }) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format("%02d", selectedMinute),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            IconButton(onClick = { selectedMinute = (selectedMinute + 15) % 60 }) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // AM / PM Switcher
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PERIOD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAm) TransitNavy else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { isAm = true }
                            ) {
                                Text(
                                    text = "AM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAm) Color.White else TransitNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isAm) TransitNavy else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { isAm = false }
                            ) {
                                Text(
                                    text = "PM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isAm) Color.White else TransitNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Apply Button
                Button(
                    onClick = {
                        val formatted = String.format("%02d:%02d %s", selectedHour, selectedMinute, if (isAm) "AM" else "PM")
                        applySelection(formatted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_custom_time_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                ) {
                    Text(
                        text = "Confirm $selectedDate • ${selectedTimingType.label}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
