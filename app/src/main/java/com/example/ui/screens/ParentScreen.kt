package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.ProfilePhotoPickerDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    viewModel: BilimViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showPhotoPicker by remember { mutableStateOf(false) }

    val children = remember(users, currentUser) {
        val linked = currentUser?.linkedStudentIds ?: emptyList()
        val found = users.filter { it.id in linked }
        if (found.isNotEmpty()) found else users.filter { it.role == UserRole.STUDENT }
    }

    var selectedChild by remember(children) {
        mutableStateOf(children.firstOrNull())
    }

    val childGrades = remember(grades, selectedChild) {
        if (selectedChild != null) {
            grades.filter { it.studentId == selectedChild!!.id }
        } else grades
    }

    val childAttendance = remember(attendance, selectedChild) {
        if (selectedChild != null) {
            attendance.filter { it.studentId == selectedChild!!.id }
        } else attendance
    }

    val myNotifications = remember(notifications, currentUser) {
        notifications.filter { it.recipientUserId == currentUser?.id || currentUser?.role == UserRole.SUPERADMIN }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Farzand ko'rsatkichlari, 1: Bildirishnomalar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Parent Profile & Child selector header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Parent profile bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentUser != null) {
                            UserAvatar(
                                user = currentUser!!,
                                size = 44.dp,
                                showGoldBorder = currentUser?.isVerified == true,
                                onClick = { showPhotoPicker = true }
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = currentUser?.fullName ?: "Ota-ona",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (currentUser?.isVerified == true) {
                                    VerifiedBadge(size = 15.dp)
                                }
                            }
                            Text(
                                text = "Ota-onalar portali • ${children.size} nafar farzand bog'langan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(onClick = { showPhotoPicker = true }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Rasm", tint = GoldDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Farzandni tanlang:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    children.forEach { child ->
                        val isSelected = selectedChild?.id == child.id
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedChild = child },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                UserAvatar(
                                    user = child,
                                    size = 34.dp,
                                    showGoldBorder = isSelected
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = child.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) GoldLight else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (child.isVerified) {
                                            VerifiedBadge(size = 12.dp)
                                        }
                                    }
                                    Text(
                                        text = "Sinf: ${child.assignedClass ?: "9-A"}",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Ko'rsatkichlar", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("parent_tab_metrics")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Bildirishnomalar (${myNotifications.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("parent_tab_notifications")
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Summary card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${selectedChild?.fullName ?: "Farzand"} — O'zlashtirish tahlili",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("O'rtacha ball", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            text = if (childGrades.isNotEmpty()) "${childGrades.map { it.score }.average().toInt()}" else "95",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = EmeraldKnowledge
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Dars qatnashishi", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            text = "100%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = NavyAccent
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Topshiriqlar", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            text = "${childGrades.size} ta",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = GoldDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Grades list
                    item {
                        Text(
                            text = "Baholar daftarchasi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(childGrades) { g ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EmeraldKnowledge.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${g.score}",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldKnowledge,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${g.subjectName} — ${g.assignmentTitle}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (g.comment.isNotBlank()) {
                                        Text(
                                            text = "Ustoz fikri: \"${g.comment}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Text(
                                    text = g.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Attendance records
                    item {
                        Text(
                            text = "Dars davomati",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(childAttendance) { att ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${att.subjectName} (${att.date})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = att.status.displayName,
                                    color = Color(att.status.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Notifications List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(myNotifications) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.markNotificationAsRead(notif.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!notif.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GoldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = GoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhotoPicker && currentUser != null) {
        ProfilePhotoPickerDialog(
            user = currentUser!!,
            onDismiss = { showPhotoPicker = false },
            onSaveAvatar = { newUri ->
                viewModel.updateUserAvatar(currentUser!!.id, newUri)
            }
        )
    }
}
