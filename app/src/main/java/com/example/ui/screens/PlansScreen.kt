package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.SubscriptionPlan
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel
import java.text.DecimalFormat
import java.util.UUID

@Composable
fun PlansScreen(
    viewModel: BilimViewModel
) {
    val plans by viewModel.plans.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()
    val aiQueriesUsed by viewModel.aiQueriesUsed.collectAsState()

    var selectedPlanForEdit by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showAssignUserDialog by remember { mutableStateOf(false) }
    var showPaymentGatewayDialog by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }

    val currentPlanId = currentUser?.subscriptionPlanId ?: "plan_free"
    val activeUserPlan = plans.firstOrNull { it.id == currentPlanId } ?: plans.first { it.id == "plan_free" }

    val studentCount = users.count { it.role == UserRole.STUDENT }
    val teacherCount = users.count { it.role == UserRole.TEACHER }
    val quizCount = quizzes.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tariflar va Limitlar Nazorati",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To'liq moslashuvchan narxlar, o'quvchilar va o'qituvchilar soni, testlar va AI so'rovlari limitlari",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // 2. Active Plan & Quota Usage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sizning amaldagi tarifingiz:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = activeUserPlan.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (activeUserPlan.priceUzs > 0) "${formatter.format(activeUserPlan.priceUzs)} so'm/oy" else "Bepul",
                                fontWeight = FontWeight.Bold,
                                color = GoldDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Joriy limitlar va resurslardan foydalanish:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    QuotaProgressItem(
                        label = "O'quvchilar soni",
                        used = studentCount,
                        limit = activeUserPlan.maxStudents,
                        unit = "nafar"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuotaProgressItem(
                        label = "O'qituvchilar",
                        used = teacherCount,
                        limit = activeUserPlan.maxTeachers,
                        unit = "nafar"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuotaProgressItem(
                        label = "Oylik testlar",
                        used = quizCount,
                        limit = activeUserPlan.monthlyQuizzesLimit,
                        unit = "ta"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuotaProgressItem(
                        label = "AI Ta'lim so'rovlari",
                        used = aiQueriesUsed,
                        limit = activeUserPlan.monthlyAiQueriesLimit,
                        unit = "so'rov"
                    )
                }
            }
        }

        // 3. Action Buttons for Superadmin (Create Plan, Assign Plan, Payment Gateway)
        if (currentUser?.role == UserRole.SUPERADMIN) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showCreatePlanDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_create_new_plan"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yangi tarif", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showAssignUserDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_assign_plan_user"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tarif biriktirish", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showPaymentGatewayDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("To'lov tizimlari sozlamalari (Click, Payme, Uzum)", fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Plans List
        item {
            Text(
                text = "Mavjud tarif rejalari (${plans.size} ta)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(plans) { plan ->
            val isEnterprise = plan.id == "plan_enterprise"
            val isCurrent = plan.id == currentPlanId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCurrent) Modifier.border(2.dp, EmeraldKnowledge, RoundedCornerShape(18.dp))
                        else if (isEnterprise) Modifier.border(2.dp, GoldPrimary, RoundedCornerShape(18.dp))
                        else Modifier
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldKnowledge.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Faol rejangiz",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = EmeraldKnowledge
                                )
                            }
                        } else if (isEnterprise) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Tashkilotlar uchun",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GoldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (plan.priceUzs > 0) "${formatter.format(plan.priceUzs)} so'm" else "Bepul",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Amal qilish muddati: ${plan.durationDays} kun",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Limits grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PlanFeatureItem("Maksimal o'quvchilar: ${plan.maxStudents} nafar")
                        PlanFeatureItem("O'qituvchilar: ${plan.maxTeachers} nafar")
                        PlanFeatureItem("Sinflar soni: ${plan.maxClasses} ta")
                        PlanFeatureItem("Oylik testlar limiti: ${plan.monthlyQuizzesLimit} ta")
                        PlanFeatureItem("AI so'rovlar limiti: ${plan.monthlyAiQueriesLimit} ta")
                        PlanFeatureItem("Fayllar xotirasi: ${plan.storageMb / 1024} GB")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentUser?.role == UserRole.SUPERADMIN) {
                            OutlinedButton(
                                onClick = { selectedPlanForEdit = plan },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_edit_plan_${plan.id}"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tahrirlash", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    viewModel.assignPlanToUser(currentUser!!.id, plan.id, plan.durationDays)
                                    viewModel.showNotice("'${plan.name}' tarifi hisobingizga muvaffaqiyatli faollashtirildi!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary)
                        ) {
                            Text(if (isCurrent) "Uzaytirish" else "Faollashtirish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // --- DIALOGS ---

    // 1. Create New Plan Dialog
    if (showCreatePlanDialog) {
        var planName by remember { mutableStateOf("") }
        var planDesc by remember { mutableStateOf("") }
        var priceText by remember { mutableStateOf("300000") }
        var durationText by remember { mutableStateOf("30") }
        var maxStudentsText by remember { mutableStateOf("250") }
        var maxTeachersText by remember { mutableStateOf("20") }
        var maxClassesText by remember { mutableStateOf("10") }
        var quizzesLimitText by remember { mutableStateOf("50") }
        var aiLimitText by remember { mutableStateOf("200") }

        AlertDialog(
            onDismissRequest = { showCreatePlanDialog = false },
            title = { Text("Yangi tarif rejasi yaratish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = planName,
                        onValueChange = { planName = it },
                        label = { Text("Tarif nomi (Masalan: 'Litsey Pro')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = planDesc,
                        onValueChange = { planDesc = it },
                        label = { Text("Tavsifi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Narxi (so'm)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Muddati (kun)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = maxStudentsText,
                            onValueChange = { maxStudentsText = it },
                            label = { Text("Max o'quvchi") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxTeachersText,
                            onValueChange = { maxTeachersText = it },
                            label = { Text("Max o'qituvchi") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quizzesLimitText,
                            onValueChange = { quizzesLimitText = it },
                            label = { Text("Testlar limiti") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = aiLimitText,
                            onValueChange = { aiLimitText = it },
                            label = { Text("AI limiti") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (planName.isNotBlank()) {
                            val newPlan = SubscriptionPlan(
                                id = "plan_" + UUID.randomUUID().toString().take(8),
                                name = planName,
                                description = planDesc,
                                priceUzs = priceText.toLongOrNull() ?: 300_000L,
                                durationDays = durationText.toIntOrNull() ?: 30,
                                maxStudents = maxStudentsText.toIntOrNull() ?: 250,
                                maxTeachers = maxTeachersText.toIntOrNull() ?: 20,
                                maxClasses = maxClassesText.toIntOrNull() ?: 10,
                                monthlyQuizzesLimit = quizzesLimitText.toIntOrNull() ?: 50,
                                monthlyAiQueriesLimit = aiLimitText.toIntOrNull() ?: 200,
                                storageMb = 2048
                            )
                            viewModel.createPlan(newPlan)
                            showCreatePlanDialog = false
                        }
                    }
                ) {
                    Text("Yaratish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlanDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // 2. Assign Plan To User Dialog
    if (showAssignUserDialog) {
        var selectedUser by remember { mutableStateOf(users.firstOrNull()) }
        var selectedPlan by remember { mutableStateOf(plans.firstOrNull()) }
        var daysText by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAssignUserDialog = false },
            title = { Text("Foydalanuvchiga tarif biriktirish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Foydalanuvchini tanlang:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    users.take(5).forEach { u ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedUser?.id == u.id,
                                onClick = { selectedUser = u }
                            )
                            Text("${u.fullName} (${u.role.displayName})", fontSize = 12.sp)
                        }
                    }

                    Text("Tarifni tanlang:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    plans.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedPlan?.id == p.id,
                                onClick = { selectedPlan = p }
                            )
                            Text("${p.name} (${if (p.priceUzs > 0) "${formatter.format(p.priceUzs)} so'm" else "Bepul"})", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Muddat (kun)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = selectedUser
                        val p = selectedPlan
                        if (u != null && p != null) {
                            viewModel.assignPlanToUser(u.id, p.id, daysText.toIntOrNull() ?: 30)
                            showAssignUserDialog = false
                        }
                    }
                ) {
                    Text("Biriktirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignUserDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // 3. Edit Plan Dialog
    if (selectedPlanForEdit != null) {
        val plan = selectedPlanForEdit!!
        var priceText by remember { mutableStateOf(plan.priceUzs.toString()) }
        var maxStudentsText by remember { mutableStateOf(plan.maxStudents.toString()) }
        var maxTeachersText by remember { mutableStateOf(plan.maxTeachers.toString()) }
        var quizzesLimitText by remember { mutableStateOf(plan.monthlyQuizzesLimit.toString()) }
        var aiLimitText by remember { mutableStateOf(plan.monthlyAiQueriesLimit.toString()) }

        AlertDialog(
            onDismissRequest = { selectedPlanForEdit = null },
            title = { Text("${plan.name} tarifini tahrirlash") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Narxi (so'm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxStudentsText,
                        onValueChange = { maxStudentsText = it },
                        label = { Text("Maksimal o'quvchilar soni") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxTeachersText,
                        onValueChange = { maxTeachersText = it },
                        label = { Text("Maksimal o'qituvchilar soni") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quizzesLimitText,
                        onValueChange = { quizzesLimitText = it },
                        label = { Text("Oylik testlar limiti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = aiLimitText,
                        onValueChange = { aiLimitText = it },
                        label = { Text("Oylik AI so'rovlar limiti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = plan.copy(
                            priceUzs = priceText.toLongOrNull() ?: plan.priceUzs,
                            maxStudents = maxStudentsText.toIntOrNull() ?: plan.maxStudents,
                            maxTeachers = maxTeachersText.toIntOrNull() ?: plan.maxTeachers,
                            monthlyQuizzesLimit = quizzesLimitText.toIntOrNull() ?: plan.monthlyQuizzesLimit,
                            monthlyAiQueriesLimit = aiLimitText.toIntOrNull() ?: plan.monthlyAiQueriesLimit
                        )
                        viewModel.updatePlan(updated)
                        selectedPlanForEdit = null
                    }
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPlanForEdit = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // 4. Payment Gateway Dialog
    if (showPaymentGatewayDialog) {
        var isTestMode by remember { mutableStateOf(true) }
        var merchantId by remember { mutableStateOf("CLICK_MERCHANT_10482") }

        AlertDialog(
            onDismissRequest = { showPaymentGatewayDialog = false },
            title = { Text("To'lov Shlyuzlari Integratsiyasi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O'zbekiston milliy to'lov provayderlari (Click, Payme, Uzum Pay) API konfiguratsiyasi:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = merchantId,
                        onValueChange = { merchantId = it },
                        label = { Text("Merchant / Service ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Test rejimi (Sandbox):", fontSize = 12.sp)
                        Switch(
                            checked = isTestMode,
                            onCheckedChange = { isTestMode = it }
                        )
                    }
                    Text(
                        text = "Status: Integratsiya shlyuzi tayyor. Haqiqiy to'lov havolalari xavfsiz backend orqali shakllantiriladi.",
                        color = EmeraldKnowledge,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.showNotice("To'lov shlyuzi parametrlari muvaffaqiyatli yangilandi")
                    showPaymentGatewayDialog = false
                }) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentGatewayDialog = false }) {
                    Text("Yopish")
                }
            }
        )
    }
}

@Composable
private fun QuotaProgressItem(
    label: String,
    used: Int,
    limit: Int,
    unit: String
) {
    val fraction = if (limit > 0) (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    val isNearLimit = fraction >= 0.8f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "$used / $limit $unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNearLimit) CrimsonAlert else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isNearLimit) CrimsonAlert else EmeraldKnowledge,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PlanFeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldKnowledge,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
