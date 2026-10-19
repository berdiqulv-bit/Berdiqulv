package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BilimViewModel

@Composable
fun DashboardScreen(viewModel: BilimViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val topChampions by viewModel.topStudentsRanking.collectAsState()
    val rankingCriteria by viewModel.rankingCriteria.collectAsState()
    val users by viewModel.users.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var showPhotoPicker by remember { mutableStateOf(false) }
    var showCriteriaDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Rasmni almashtirish",
                                tint = NavyDark,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.fullName ?: "Foydalanuvchi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentUser?.isVerified == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedBadge(size = 18.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoleBadge(role = currentUser?.role ?: UserRole.STUDENT)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentUser?.organizationName ?: "BILIM MARKAZI PRO",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // TOP-5 CHEMPIONLAR Bo'limi
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_5_champions_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(GoldVibrant, GoldPrimary, NavyAccent)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏆",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TOP-5 CHEMPIONLAR",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Baholar, testlar va davomat bo'yicha eng ilg'or o'quvchilar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }

                        if (currentUser?.role == UserRole.SUPERADMIN || currentUser?.role == UserRole.ADMIN) {
                            IconButton(onClick = { showCriteriaDialog = true }) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Mezonlarni sozlash",
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (topChampions.isEmpty()) {
                        Text(
                            text = "Hozircha reyting ma'lumotlari hisoblanmoqda...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        topChampions.forEachIndexed { index, item ->
                            ChampionRowItem(
                                item = item,
                                isFirstPlace = index == 0
                            )
                            if (index < topChampions.size - 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }

        // Statistika Kartalari
        item {
            Text(
                text = "Tizim Ko'rsatkichlari",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "O'quvchilar",
                    value = "${users.count { it.role == UserRole.STUDENT }}",
                    subtitle = "Faol ta'lim oluvchilar",
                    icon = Icons.Default.Groups,
                    iconColor = BlueInfo,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.SUPERADMIN) }
                )
                StatCard(
                    title = "Ustozlar",
                    value = "${users.count { it.role == UserRole.TEACHER }}",
                    subtitle = "Pedagogik jamoa",
                    icon = Icons.Default.School,
                    iconColor = EmeraldKnowledge,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.SUPERADMIN) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Muassasalar",
                    value = "${organizations.size}",
                    subtitle = "Prezident va Ixtisos",
                    icon = Icons.Default.Apartment,
                    iconColor = GoldPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.SUPERADMIN) }
                )
                StatCard(
                    title = "Fanlar",
                    value = "${subjects.size}",
                    subtitle = "O'quv rejasi",
                    icon = Icons.Default.MenuBook,
                    iconColor = PurpleSubject,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.LIBRARY) }
                )
            }
        }

        // Tezkor Harakatlar
        item {
            Text(
                text = "Bo'limlar va Xizmatlar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    QuickActionTile(
                        title = "O'yinlar va Intellektual Boshqotirmalar",
                        desc = "Tarix, matematika, lug'at va xotira o'yinlari",
                        icon = Icons.Default.SportsEsports,
                        color = GoldPrimary,
                        onClick = { viewModel.navigateTo(AppScreen.GAMES_HUB) }
                    )
                    HorizontalDivider(color = Color(0xFF223450))
                    QuickActionTile(
                        title = "Elektron Kutubxona va Darsliklar",
                        desc = "PDF qo'llanmalar, olimpiada materiallari",
                        icon = Icons.Default.LocalLibrary,
                        color = BlueInfo,
                        onClick = { viewModel.navigateTo(AppScreen.LIBRARY) }
                    )
                    HorizontalDivider(color = Color(0xFF223450))
                    QuickActionTile(
                        title = "Sun'iy Intellekt Ta'lim Yordamchisi",
                        desc = "24/7 savollarga tezkor tushuntirish va yechimlar",
                        icon = Icons.Default.AutoAwesome,
                        color = EmeraldKnowledge,
                        onClick = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) }
                    )
                    if (currentUser?.role == UserRole.SUPERADMIN) {
                        HorizontalDivider(color = Color(0xFF223450))
                        QuickActionTile(
                            title = "Telegram Botlar Boshqaruvi",
                            desc = "Ota-onalar, o'quvchilar va admin botlari",
                            icon = Icons.Default.Send,
                            color = TelegramBlue,
                            onClick = { viewModel.navigateTo(AppScreen.TELEGRAM_BOTS) }
                        )
                        HorizontalDivider(color = Color(0xFF223450))
                        QuickActionTile(
                            title = "Audit va Xavfsizlik Jurnali",
                            desc = "Barcha o'zgarishlar va hisoblar monitoringi",
                            icon = Icons.Default.Security,
                            color = CrimsonAlert,
                            onClick = { viewModel.navigateTo(AppScreen.AUDIT_LOGS) }
                        )
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

    if (showCriteriaDialog) {
        var gradesWeight by remember { mutableIntStateOf(rankingCriteria.gradesWeightPercent) }
        var quizzesWeight by remember { mutableIntStateOf(rankingCriteria.quizzesWeightPercent) }
        var attendanceWeight by remember { mutableIntStateOf(rankingCriteria.attendanceWeightPercent) }

        AlertDialog(
            onDismissRequest = { showCriteriaDialog = false },
            title = {
                Text(
                    "Reyting Mezonlarini Sozlash",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        "Top-5 Chempionlar hisoblanishida har bir mezonning foizdagi ulushini belgilang (Jami 100%):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Fan baholari ulushi: $gradesWeight%", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = gradesWeight.toFloat(),
                        onValueChange = { gradesWeight = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19
                    )

                    Text("Test natijalari ulushi: $quizzesWeight%", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = quizzesWeight.toFloat(),
                        onValueChange = { quizzesWeight = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19
                    )

                    Text("Davomat ko'rsatkichi ulushi: $attendanceWeight%", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = attendanceWeight.toFloat(),
                        onValueChange = { attendanceWeight = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19
                    )

                    val sum = gradesWeight + quizzesWeight + attendanceWeight
                    Text(
                        text = "Jami foiz: $sum% ${if (sum != 100) "(Tavsiya: 100% bo'lishi lozim)" else "✓"}",
                        color = if (sum == 100) EmeraldKnowledge else CrimsonAlert,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateRankingCriteria(
                            RankingCriteria(gradesWeight, quizzesWeight, attendanceWeight)
                        )
                        showCriteriaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Saqlash", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCriteriaDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun ChampionRowItem(
    item: StudentRankItem,
    isFirstPlace: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "champion_border")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    val backgroundModifier = if (isFirstPlace) {
        Modifier
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF261E0A),
                        DarkSurfaceVariant
                    )
                )
            )
            .border(
                BorderStroke(2.dp, GoldVibrant.copy(alpha = animatedAlpha)),
                RoundedCornerShape(16.dp)
            )
    } else {
        Modifier
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(BorderStroke(1.dp, Color(0xFF263952)), RoundedCornerShape(16.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(backgroundModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            when (item.rank) {
                1 -> Text("🏆", fontSize = 24.sp)
                2 -> Text("🥈", fontSize = 20.sp)
                3 -> Text("🥉", fontSize = 20.sp)
                4 -> Text("🏅", fontSize = 18.sp)
                5 -> Text("🎖️", fontSize = 18.sp)
                else -> Text("#${item.rank}", fontWeight = FontWeight.Bold, color = GoldLight)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        UserAvatar(
            user = item.student,
            size = if (isFirstPlace) 46.dp else 40.dp,
            showGoldBorder = isFirstPlace
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.student.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isFirstPlace) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isFirstPlace) GoldLight else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.student.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.student.assignedClass ?: "9-A"} sinf",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Baho: ${String.format("%.1f", item.gradesAverage)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldKnowledge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Davomat: ${item.attendanceRate}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueInfo
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.totalScore}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isFirstPlace) GoldVibrant else GoldPrimary
            )
            Text(
                text = "ball",
                style = MaterialTheme.typography.labelSmall,
                color = GoldDark
            )
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
