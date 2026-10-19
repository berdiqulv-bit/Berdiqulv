package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ProfilePhotoPickerDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    viewModel: BilimViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Baholash, 1: Davomat, 2: Testlar

    val students = remember(users) {
        users.filter { it.role == UserRole.STUDENT }
    }

    var showAddGradeDialog by remember { mutableStateOf(false) }
    var showRecordAttendanceDialog by remember { mutableStateOf(false) }
    var showCreateQuizDialog by remember { mutableStateOf(false) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Teacher Profile Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentUser != null) {
                            UserAvatar(
                                user = currentUser!!,
                                size = 50.dp,
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
                                    text = currentUser?.fullName ?: "Ustoz",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUser?.isVerified == true) {
                                    VerifiedBadge(size = 16.dp)
                                }
                            }
                            Text(
                                text = "Biriktirilgan sinflar: ${currentUser?.assignedClass ?: "9-A"} • ${currentUser?.assignedSubjects?.joinToString(", ") ?: "Tarix, Matematika"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(onClick = { showPhotoPicker = true }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Rasm", tint = NavyAccent)
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
                text = { Text("Baholash daftari", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("teacher_tab_grades")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Davomat", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("teacher_tab_attendance")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Test tuzish", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("teacher_tab_quizzes")
            )
        }

        when (selectedTab) {
            0 -> {
                // Baholash
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "O'quvchilar baholari",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddGradeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_add_grade")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Baho qo'yish", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(grades) { g ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                color = if (g.score >= 86) EmeraldKnowledge.copy(alpha = 0.15f) else GoldVibrant.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${g.score}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (g.score >= 86) EmeraldKnowledge else GoldDark,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = g.studentName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${g.subjectName} • ${g.assignmentTitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (g.comment.isNotBlank()) {
                                            Text(
                                                text = "\"${g.comment}\"",
                                                style = MaterialTheme.typography.labelSmall,
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
                    }
                }
            }

            1 -> {
                // Davomat
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bugungi dars davomati",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showRecordAttendanceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_record_attendance")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Davomat kiritish", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(attendance) { att ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = att.studentName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${att.subjectName} (${att.classGroup}) • ${att.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (att.note.isNotBlank()) {
                                            Text(
                                                text = "Izoh: ${att.note}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(att.status.colorHex).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = att.status.displayName,
                                            color = Color(att.status.colorHex),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Testlar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mavjud testlar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showCreateQuizDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_create_quiz")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yangi test tuzish", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(quizzes) { q ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = q.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = EmeraldKnowledge.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = q.difficulty.displayName,
                                                fontSize = 10.sp,
                                                color = EmeraldKnowledge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${q.subjectName} • ${q.questions.size} ta savol • Vaqt: ${q.timeLimitMinutes} daqiqa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.startQuiz(q) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Testni sinab ko'rish", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Grade Dialog
    if (showAddGradeDialog) {
        var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
        var subjectName by remember { mutableStateOf("Tarix") }
        var assignment by remember { mutableStateOf("Oraliq nazorat savollari") }
        var scoreText by remember { mutableStateOf("90") }
        var comment by remember { mutableStateOf("Darsda faol ishtirok etdi") }

        AlertDialog(
            onDismissRequest = { showAddGradeDialog = false },
            title = { Text("O'quvchiga baho qo'yish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("O'quvchini tanlang:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    students.forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStudent?.id == st.id,
                                onClick = { selectedStudent = st }
                            )
                            Text("${st.fullName} (${st.assignedClass ?: "9-A"})", fontSize = 13.sp)
                        }
                    }

                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Fan nomi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignment,
                        onValueChange = { assignment = it },
                        label = { Text("Topshiriq mavzusi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text("Ball (1 - 100)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("O'qituvchi izohi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val st = selectedStudent
                        val score = scoreText.toIntOrNull() ?: 85
                        if (st != null) {
                            viewModel.recordGrade(
                                studentId = st.id,
                                subjectName = subjectName,
                                classGroup = st.assignedClass ?: "9-A",
                                assignment = assignment,
                                score = score,
                                comment = comment
                            )
                            showAddGradeDialog = false
                        }
                    }
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGradeDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // 2. Record Attendance Dialog
    if (showRecordAttendanceDialog) {
        var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
        var status by remember { mutableStateOf(AttendanceStatus.PRESENT) }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordAttendanceDialog = false },
            title = { Text("Davomat qayd etish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("O'quvchi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    students.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStudent?.id == st.id,
                                onClick = { selectedStudent = st }
                            )
                            Text(st.fullName, fontSize = 13.sp)
                        }
                    }

                    Text("Holat:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AttendanceStatus.values().forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st.displayName, fontSize = 9.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Izoh (ixtiyoriy)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val st = selectedStudent
                        if (st != null) {
                            viewModel.recordAttendance(
                                studentId = st.id,
                                subjectName = "Tarix",
                                classGroup = st.assignedClass ?: "9-A",
                                status = status,
                                note = note
                            )
                            showRecordAttendanceDialog = false
                        }
                    }
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordAttendanceDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // 3. Create Quiz Dialog
    if (showCreateQuizDialog) {
        var quizTitle by remember { mutableStateOf("") }
        var subjectName by remember { mutableStateOf("Geografiya") }
        var questionPrompt by remember { mutableStateOf("") }
        var opt1 by remember { mutableStateOf("") }
        var opt2 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateQuizDialog = false },
            title = { Text("Yangi test savollari tuzish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quizTitle,
                        onValueChange = { quizTitle = it },
                        label = { Text("Test sarlavhasi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Fan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = questionPrompt,
                        onValueChange = { questionPrompt = it },
                        label = { Text("Savol matni") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        label = { Text("To'g'ri javob varianti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        label = { Text("Noto'g'ri javob varianti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quizTitle.isNotBlank() && questionPrompt.isNotBlank() && opt1.isNotBlank()) {
                            val newQuiz = Quiz(
                                title = quizTitle,
                                subjectId = "subj_geo",
                                subjectName = subjectName,
                                classGroup = "9-A",
                                difficulty = QuizDifficulty.MEDIUM,
                                questions = listOf(
                                    QuizQuestion(
                                        questionText = questionPrompt,
                                        options = listOf(opt1, opt2, "Boshqa javob", "Hech biri"),
                                        correctOptionIndex = 0,
                                        points = 20
                                    )
                                )
                            )
                            viewModel.publishQuiz(newQuiz)
                            showCreateQuizDialog = false
                        }
                    }
                ) {
                    Text("E'lon qilish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateQuizDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
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
