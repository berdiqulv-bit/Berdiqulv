package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BilimViewModel

enum class SuperAdminTab(val title: String) {
    STUDENTS("O'quvchilar"),
    TEACHERS("O'qituvchilar"),
    PARENTS("Ota-onalar"),
    ADMINS("Administratorlar"),
    BLOCKED("Bloklanganlar"),
    ORGANIZATIONS("Muassasalar"),
    SETTINGS("Tizim Sozlamalari")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(viewModel: BilimViewModel) {
    var selectedTab by remember { mutableStateOf(SuperAdminTab.STUDENTS) }
    var searchQuery by remember { mutableStateOf("") }

    val allUsers by viewModel.users.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val isMaintenance by viewModel.isMaintenanceMode.collectAsState()

    val selectedUserIds = remember { mutableStateListOf<String>() }
    var isSelectionMode by remember { mutableStateOf(false) }

    var userToVerify by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddOrgDialog by remember { mutableStateOf(false) }

    val tabUsers = remember(allUsers, selectedTab, searchQuery) {
        allUsers.filter { user ->
            val matchesTab = when (selectedTab) {
                SuperAdminTab.STUDENTS -> user.role == UserRole.STUDENT && user.status != UserStatus.BLOCKED
                SuperAdminTab.TEACHERS -> user.role == UserRole.TEACHER && user.status != UserStatus.BLOCKED
                SuperAdminTab.PARENTS -> user.role == UserRole.PARENT && user.status != UserStatus.BLOCKED
                SuperAdminTab.ADMINS -> (user.role == UserRole.ADMIN || user.role == UserRole.SUPERADMIN) && user.status != UserStatus.BLOCKED
                SuperAdminTab.BLOCKED -> user.status == UserStatus.BLOCKED
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    (user.assignedClass?.contains(searchQuery, ignoreCase = true) == true)
            matchesTab && matchesQuery && !user.isSoftDeleted
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (selectedTab != SuperAdminTab.SETTINGS && selectedTab != SuperAdminTab.ORGANIZATIONS) {
                FloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    containerColor = GoldPrimary,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("add_user_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Foydalanuvchi qo'shish")
                }
            } else if (selectedTab == SuperAdminTab.ORGANIZATIONS) {
                FloatingActionButton(
                    onClick = { showAddOrgDialog = true },
                    containerColor = GoldPrimary,
                    contentColor = NavyDark
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = "Muassasa qo'shish")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Tab Header Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = DarkSurface,
                contentColor = GoldPrimary,
                edgePadding = 0.dp,
                divider = {}
            ) {
                SuperAdminTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            selectedUserIds.clear()
                            isSelectionMode = false
                        },
                        text = {
                            Text(
                                tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab != SuperAdminTab.SETTINGS) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ism, login yoki sinf bo'yicha qidiruv...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Tozalash")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab != SuperAdminTab.ORGANIZATIONS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tabUsers.size} ta foydalanuvchi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelectionMode) {
                                TextButton(
                                    onClick = {
                                        if (selectedUserIds.size == tabUsers.size) {
                                            selectedUserIds.clear()
                                        } else {
                                            selectedUserIds.clear()
                                            selectedUserIds.addAll(tabUsers.map { it.id })
                                        }
                                    }
                                ) {
                                    Text(if (selectedUserIds.size == tabUsers.size) "Barchasini bekor qilish" else "Barchasini tanlash", fontSize = 12.sp)
                                }

                                if (selectedUserIds.isNotEmpty()) {
                                    Button(
                                        onClick = { showBatchDeleteDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("O'chirish (${selectedUserIds.size})", fontSize = 12.sp)
                                    }
                                }

                                IconButton(onClick = {
                                    isSelectionMode = false
                                    selectedUserIds.clear()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Bekor qilish", tint = CrimsonAlert)
                                }
                            } else {
                                TextButton(onClick = { isSelectionMode = true }) {
                                    Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ommaviy tanlash", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                SuperAdminTab.SETTINGS -> {
                    SystemSettingsContent(
                        viewModel = viewModel,
                        isMaintenance = isMaintenance
                    )
                }
                SuperAdminTab.ORGANIZATIONS -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(organizations) { org ->
                            OrganizationCard(org = org)
                        }
                    }
                }
                else -> {
                    if (tabUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Hech qanday hisob topilmadi",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(tabUsers, key = { it.id }) { user ->
                                UserManagementCard(
                                    user = user,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedUserIds.contains(user.id),
                                    onSelectToggle = {
                                        if (selectedUserIds.contains(user.id)) {
                                            selectedUserIds.remove(user.id)
                                        } else {
                                            selectedUserIds.add(user.id)
                                        }
                                    },
                                    onVerifyClick = { userToVerify = user },
                                    onToggleStatus = { viewModel.toggleUserStatus(user.id) },
                                    onDeleteClick = { userToDelete = user }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    userToVerify?.let { user ->
        var reasonInput by remember { mutableStateOf(user.verifiedReason ?: "") }
        val isCurrentlyVerified = user.isVerified

        AlertDialog(
            onDismissRequest = { userToVerify = null },
            title = {
                Text(
                    if (isCurrentlyVerified) "Tasdiq belgisini olib tashlash" else "Foydalanuvchini tasdiqlash",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "${user.fullName} (@${user.username}) uchun tasdiqlangan ko'k nishon maqomi:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Tasdiqlash sababi / qayd") },
                        placeholder = { Text("Masalan: Respublika olimpiada mutlaq g'olibi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setVerificationStatus(
                            userId = user.id,
                            isVerified = !isCurrentlyVerified,
                            reason = reasonInput.ifBlank { "Superadmin tomonidan berildi" }
                        )
                        userToVerify = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyVerified) CrimsonAlert else GoldPrimary
                    )
                ) {
                    Text(
                        if (isCurrentlyVerified) "Nishonni olib tashlash" else "Tasdiqlash (✓)",
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { userToVerify = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    userToDelete?.let { user ->
        var adminPasswordInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hisobni xavfsiz o'chirish", color = CrimsonAlert, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Haqiqatan ham quyidagi foydalanuvchini tizimdan butunlay o'chirmoqchimisiz?\n\n" +
                                "• Ism: ${user.fullName}\n" +
                                "• Login: @${user.username}\n" +
                                "• Rol: ${user.role.displayName}\n\n" +
                                "Amalni tasdiqlash uchun superadmin parolini ('superadmin') kiriting:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = {
                            adminPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("Superadmin paroli") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMessage ?: "", color = CrimsonAlert, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.deleteUserSecurely(user.id, adminPasswordInput)
                        if (success) {
                            userToDelete = null
                        } else {
                            errorMessage = "Parol noto'g'ri yoki ruxsat berilmadi"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("O'chirish", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        var adminPasswordInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CrimsonAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ommaviy o'chirish", color = CrimsonAlert, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Tanlangan ${selectedUserIds.size} ta hisob butunlay o'chiriladi. Bu amalni ortga qaytarib bo'lmaydi.\n\n" +
                                "Xavfsizlik uchun superadmin parolini ('superadmin') kiriting:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = {
                            adminPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("Superadmin paroli") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMessage ?: "", color = CrimsonAlert, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.deleteUsersBatchSecurely(selectedUserIds.toList(), adminPasswordInput)
                        if (success) {
                            selectedUserIds.clear()
                            isSelectionMode = false
                            showBatchDeleteDialog = false
                        } else {
                            errorMessage = "Parol tasdiqlanmadi"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Ommaviy o'chirish", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    if (showAddUserDialog) {
        var fullName by remember { mutableStateOf("") }
        var usernameInput by remember { mutableStateOf("") }
        var roleSelect by remember { mutableStateOf(UserRole.STUDENT) }
        var classOrSubject by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("+998 90 ") }
        var isVerifiedCheck by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Yangi foydalanuvchi qo'shish", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Ism-familiya") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Login") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rolni tanlang:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(UserRole.entries) { r ->
                            FilterChip(
                                selected = roleSelect == r,
                                onClick = { roleSelect = r },
                                label = { Text(r.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = classOrSubject,
                        onValueChange = { classOrSubject = it },
                        label = { Text(if (roleSelect == UserRole.STUDENT) "Sinf (masalan: 9-A)" else "Fan / Bo'lim") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Telefon raqami") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isVerifiedCheck = !isVerifiedCheck }
                    ) {
                        Checkbox(checked = isVerifiedCheck, onCheckedChange = { isVerifiedCheck = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tasdiqlangan nishon berilsin (✓)", fontSize = 13.sp, color = BlueInfo)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && usernameInput.isNotBlank()) {
                            val newUser = User(
                                id = "user_${System.currentTimeMillis()}",
                                fullName = fullName,
                                username = usernameInput,
                                role = roleSelect,
                                assignedClass = if (roleSelect == UserRole.STUDENT) classOrSubject else null,
                                assignedSubjects = if (roleSelect == UserRole.TEACHER) listOf(classOrSubject) else emptyList(),
                                phone = phoneInput,
                                isVerified = isVerifiedCheck,
                                verifiedReason = if (isVerifiedCheck) "Superadmin tomonidan berildi" else null,
                                verifiedAt = if (isVerifiedCheck) System.currentTimeMillis() else 0L
                            )
                            viewModel.createNewUser(newUser)
                            showAddUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Saqlash", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    if (showAddOrgDialog) {
        var orgName by remember { mutableStateOf("") }
        var orgAddress by remember { mutableStateOf("") }
        var orgAdmin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddOrgDialog = false },
            title = { Text("Yangi Ta'lim Muassasasi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        label = { Text("Muassasa nomi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = orgAddress,
                        onValueChange = { orgAddress = it },
                        label = { Text("Manzil") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = orgAdmin,
                        onValueChange = { orgAdmin = it },
                        label = { Text("Mas'ul Administrator") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (orgName.isNotBlank()) {
                            val newOrg = Organization(
                                id = "org_${System.currentTimeMillis()}",
                                name = orgName,
                                address = orgAddress,
                                adminUserName = orgAdmin
                            )
                            viewModel.createOrganization(newOrg)
                            showAddOrgDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Qo'shish", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrgDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun UserManagementCard(
    user: User,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onVerifyClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E2E46) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) GoldPrimary else if (user.isVerified) BlueInfo.copy(alpha = 0.5f) else Color(0xFF223450)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            UserAvatar(
                user = user,
                size = 46.dp,
                showGoldBorder = user.subscriptionPlanId != "plan_free"
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "@${user.username} • ${user.assignedClass ?: user.assignedSubjects.firstOrNull() ?: user.role.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.isVerified && !user.verifiedReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "✓ ${user.verifiedReason}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueInfo,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVerifyClick) {
                    Icon(
                        imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.CheckCircleOutline,
                        contentDescription = "Tasdiqlash",
                        tint = if (user.isVerified) BlueInfo else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                IconButton(onClick = onToggleStatus) {
                    Icon(
                        imageVector = if (user.status == UserStatus.ACTIVE) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = "Holat",
                        tint = if (user.status == UserStatus.ACTIVE) GoldPrimary else CrimsonAlert
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "O'chirish",
                        tint = CrimsonAlert
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizationCard(org: Organization) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF223450))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = org.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(text = "Faol", isSuccess = true)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = org.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("O'quvchilar: ${org.activeStudentsCount} / ${org.maxStudents}", style = MaterialTheme.typography.labelSmall, color = GoldLight)
                Text("O'qituvchilar: ${org.activeTeachersCount} / ${org.maxTeachers}", style = MaterialTheme.typography.labelSmall, color = BlueInfo)
            }
        }
    }
}

@Composable
fun SystemSettingsContent(
    viewModel: BilimViewModel,
    isMaintenance: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Texnik Tanaffus Rejimi (Maintenance)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Rejim yoqilganda foydalanuvchilar ekranida ogohlantirish qizil tasma ko'rinadi va tizim yangilanishi haqida xabar beriladi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.toggleMaintenanceMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMaintenance) CrimsonAlert else GoldPrimary
                    )
                ) {
                    Text(
                        if (isMaintenance) "Texnik tanaffusni to'xtatish" else "Texnik rejimni yoqish",
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ma'lumotlar Zaxirasi va Qayta Tiklash", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Barcha foydalanuvchilar, baholar, davomat va bot sozlamalari zaxira nusxasini olish.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.createBackup() },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueInfo)
                    ) {
                        Text("Zaxira Nusxa Olish", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { viewModel.restoreBackup() }
                    ) {
                        Text("Zaxiradan Tiklash")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sinov Ma'lumotlarini Tozalash", fontWeight = FontWeight.Bold, color = CrimsonAlert)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sinov ma'lumotlarini tozalaydi. Superadmin hisobi va tizim sozlamalari saqlanadi. Oldindan avtomatik zaxira nusxasi yaratiladi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.cleanDemoData(backupFirst = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Sinov Ma'lumotlarini Tozalash", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
