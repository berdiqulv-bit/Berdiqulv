package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

val TelegramBlue = Color(0xFF0088CC)
val VerifiedBadgeColor = Color(0xFF0088CC)

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(VerifiedBadgeColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Tasdiqlangan hisob",
            tint = Color.White,
            modifier = Modifier.size(size * 0.72f)
        )
    }
}

@Composable
fun UserAvatar(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showGoldBorder: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isPremium = showGoldBorder || (user?.subscriptionPlanId != null && user.subscriptionPlanId != "plan_free")
    val borderModifier = if (isPremium) {
        Modifier.border(
            BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(GoldLight, GoldPrimary, GoldDark))
            ),
            CircleShape
        )
    } else {
        Modifier.border(1.dp, Color(0xFF334B68), CircleShape)
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(borderModifier)
            .padding(if (isPremium) 2.dp else 0.dp)
            .clip(CircleShape)
            .then(clickableModifier)
            .background(NavyLight),
        contentAlignment = Alignment.Center
    ) {
        if (!user?.avatarUri.isNullOrBlank()) {
            AsyncImage(
                model = user?.avatarUri,
                contentDescription = user?.fullName ?: "Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            val initial = user?.fullName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(
                text = initial,
                color = if (isPremium) GoldVibrant else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BilimTopBar(
    title: String,
    currentUser: User?,
    isMaintenanceMode: Boolean = false,
    canGoBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onRoleSwitchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    unreadNotificationCount: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isMaintenanceMode) {
            Surface(
                color = CrimsonAlert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diqqat: Rejali texnik ta'mirlash holati yoqilgan",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentUser?.isVerified == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(size = 16.dp)
                        }
                    }
                    if (currentUser != null) {
                        Text(
                            text = "${currentUser.fullName} (${currentUser.role.displayName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                if (canGoBack) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        UserAvatar(
                            user = currentUser,
                            size = 36.dp
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = CrimsonAlert,
                                    contentColor = Color.White
                                ) {
                                    Text("$unreadNotificationCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Xabarnomalar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun BilimBottomNavigationBar(
    currentScreen: AppScreen,
    currentUser: User?,
    onNavigate: (AppScreen) -> Unit
) {
    if (currentUser == null || currentScreen == AppScreen.LOGIN) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Asosiy") },
            label = { Text("Asosiy", fontSize = 11.sp) }
        )

        val roleScreen = when (currentUser.role) {
            UserRole.SUPERADMIN -> AppScreen.SUPERADMIN
            UserRole.ADMIN -> AppScreen.SUPERADMIN
            UserRole.TEACHER -> AppScreen.TEACHER_PORTAL
            UserRole.STUDENT -> AppScreen.STUDENT_PORTAL
            UserRole.PARENT -> AppScreen.PARENT_PORTAL
        }
        val roleLabel = when (currentUser.role) {
            UserRole.SUPERADMIN -> "Boshqaruv"
            UserRole.ADMIN -> "Admin"
            UserRole.TEACHER -> "Jurnal"
            UserRole.STUDENT -> "Kundalik"
            UserRole.PARENT -> "Farzandim"
        }
        val roleIcon = when (currentUser.role) {
            UserRole.SUPERADMIN -> Icons.Default.AdminPanelSettings
            UserRole.ADMIN -> Icons.Default.SupervisorAccount
            UserRole.TEACHER -> Icons.Default.School
            UserRole.STUDENT -> Icons.Default.Person
            UserRole.PARENT -> Icons.Default.FamilyRestroom
        }

        NavigationBarItem(
            selected = currentScreen == roleScreen,
            onClick = { onNavigate(roleScreen) },
            icon = { Icon(roleIcon, contentDescription = roleLabel) },
            label = { Text(roleLabel, fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.GAMES_HUB || currentScreen == AppScreen.QUIZ_RUNNER,
            onClick = { onNavigate(AppScreen.GAMES_HUB) },
            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "O'yinlar") },
            label = { Text("O'yinlar", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.LIBRARY,
            onClick = { onNavigate(AppScreen.LIBRARY) },
            icon = { Icon(Icons.Default.LocalLibrary, contentDescription = "Kutubxona") },
            label = { Text("Kutubxona", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AI_ASSISTANT,
            onClick = { onNavigate(AppScreen.AI_ASSISTANT) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
            label = { Text("AI Yordamchi", fontSize = 11.sp) }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    iconColor: Color = GoldPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Card(
        modifier = modifier.then(clickableModifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, Color(0xFF2A3F60))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldLight
                )
            }
        }
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    Surface(
        color = Color(role.badgeColorHex).copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(role.badgeColorHex).copy(alpha = 0.4f))
    ) {
        Text(
            text = role.displayName,
            color = Color(role.badgeColorHex),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusBadge(text: String, isSuccess: Boolean = true) {
    val color = if (isSuccess) EmeraldKnowledge else CrimsonAlert
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ProfilePhotoPickerDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSaveAvatar: (String) -> Unit
) {
    var selectedUri by remember { mutableStateOf(user?.avatarUri ?: "") }
    var customUrlInput by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableStateOf(-1) }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&crop=faces",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&h=200&fit=crop&crop=faces",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=faces",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&h=200&fit=crop&crop=faces",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=faces"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Profil rasmini tanlash",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldPrimary, CircleShape)
                        .background(NavyLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri.isNotBlank()) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Tanlangan rasm",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Tayyor rasmlardan tanlang (Galereya):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetAvatars.indices.toList()) { idx ->
                        val url = presetAvatars[idx]
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(
                                    if (selectedPresetIndex == idx) 2.dp else 1.dp,
                                    if (selectedPresetIndex == idx) GoldVibrant else Color(0xFF334B68),
                                    CircleShape
                                )
                                .clickable {
                                    selectedPresetIndex = idx
                                    selectedUri = url
                                }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Avatar $idx",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = {
                        customUrlInput = it
                        selectedUri = it
                        selectedPresetIndex = -1
                    },
                    label = { Text("Yoki rasm havolasini kiriting") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            selectedUri = ""
                            customUrlInput = ""
                            selectedPresetIndex = -1
                        }
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rasmni olib tashlash", color = CrimsonAlert)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAvatar(selectedUri)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Saqlash", color = NavyDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
