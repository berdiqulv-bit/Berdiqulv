package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@Composable
fun StudentScreen(viewModel: BilimViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allGrades by viewModel.grades.collectAsState()
    val allAttendance by viewModel.attendance.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()
    val topChampions by viewModel.topStudentsRanking.collectAsState()

    var showPhotoPicker by remember { mutableStateOf(false) }

    val studentGrades = remember(allGrades, currentUser) {
        allGrades.filter { it.studentId == currentUser?.id }
    }
    val studentAttendance = remember(allAttendance, currentUser) {
        allAttendance.filter { it.studentId == currentUser?.id }
    }
    val studentRank = remember(topChampions, currentUser) {
        topChampions.firstOrNull { it.student.id == currentUser?.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GoldDark, GoldPrimary, GoldLight)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.clickable { showPhotoPicker = true }) {
                        UserAvatar(
                            user = currentUser,
                            size = 64.dp,
                            showGoldBorder = true
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NavyDark, modifier = Modifier.size(13.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.fullName ?: "O'quvchi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentUser?.isVerified == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedBadge(size = 16.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentUser?.assignedClass ?: "9-A"} sinf • ${currentUser?.organizationName ?: "Prezident Maktabi"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (studentRank != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🏆 Reytingdagi o'rni: #${studentRank.rank} (${studentRank.totalScore} ball)",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldVibrant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Summary Statistics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val avgScore = if (studentGrades.isEmpty()) 0.0 else studentGrades.map { it.score }.average()
                val attRate = if (studentAttendance.isEmpty()) 100 else {
                    val present = studentAttendance.count { it.status == AttendanceStatus.PRESENT }
                    ((present.toDouble() / studentAttendance.size) * 100).toInt()
                }

                StatCard(
                    title = "O'rtacha Baho",
                    value = String.format("%.1f", avgScore),
                    subtitle = "${studentGrades.size} ta baho",
                    icon = Icons.Default.Grade,
                    iconColor = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Davomat",
                    value = "$attRate%",
                    subtitle = "${studentAttendance.size} ta dars qaydi",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EmeraldKnowledge,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Available Quizzes
        item {
            Text(
                text = "Mavjud Testlar va Vazifalar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            if (quizzes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Hozircha yangi testlar yuklanmagan",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    quizzes.forEach { quiz ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF263952))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quiz.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${quiz.subjectName} • ${quiz.questions.size} ta savol • ${quiz.difficulty.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                                Button(
                                    onClick = { viewModel.startQuiz(quiz) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Boshlash", color = NavyDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Baholar Ro'yxati
        item {
            Text(
                text = "Mening Baholarim",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            if (studentGrades.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Hali baholar qo'yilmagan",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    studentGrades.forEach { g ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF223450))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = g.assignmentTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${g.subjectName} • ${g.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                    if (g.comment.isNotBlank()) {
                                        Text(
                                            text = "Izoh: ${g.comment}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldLight
                                        )
                                    }
                                }

                                Text(
                                    text = "${g.score}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (g.score >= 90) GoldVibrant else if (g.score >= 70) EmeraldKnowledge else CrimsonAlert
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhotoPicker) {
        ProfilePhotoPickerDialog(
            user = currentUser,
            onDismiss = { showPhotoPicker = false },
            onSaveAvatar = { uri ->
                currentUser?.let { user ->
                    viewModel.updateUserAvatar(user.id, uri)
                }
            }
        )
    }
}
