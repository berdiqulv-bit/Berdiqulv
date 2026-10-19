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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TelegramBot
import com.example.data.model.TelegramBotType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramBotsScreen(
    viewModel: BilimViewModel
) {
    val bots by viewModel.bots.collectAsState()
    var showAddBotDialog by remember { mutableStateOf(false) }
    var selectedBotForTest by remember { mutableStateOf<TelegramBot?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                            text = "Telegram Botlar Markazi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(Icons.Default.Send, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ota-onalar, o'quvchilar va ma'muriyat uchun xavfsiz bildirishnomalar va xizmat botlari boshqaruvi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ulangan Telegram botlar (${bots.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddBotDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_bot")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bot qo'shish", fontSize = 12.sp)
                }
            }
        }

        // Bots List
        items(bots) { bot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bot.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bot.username,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        StatusBadge(
                            text = if (bot.isConnected) "Faol / Ulangan" else "Uzilgan",
                            isSuccess = bot.isConnected
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bot vazifasi: ${bot.type.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Ruxsat etilgan xabarlar: ${bot.enabledNotifications.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Yuborilgan xabarlar: ${bot.messagesSentCount} ta",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { selectedBotForTest = bot },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_bot_${bot.id}")
                            ) {
                                Text("Sinash", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.toggleBotConnection(bot.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (bot.isConnected) CrimsonAlert else EmeraldKnowledge
                                )
                            ) {
                                Text(if (bot.isConnected) "Uzish" else "Ulash", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Add Bot Dialog
    if (showAddBotDialog) {
        var botName by remember { mutableStateOf("") }
        var botUsername by remember { mutableStateOf("@") }
        var botToken by remember { mutableStateOf("") }
        var botType by remember { mutableStateOf(TelegramBotType.PARENT_NOTIFICATION) }

        AlertDialog(
            onDismissRequest = { showAddBotDialog = false },
            title = { Text("Yangi Telegram bot ulash") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = botName,
                        onValueChange = { botName = it },
                        label = { Text("Bot nomi (Masalan: Ota-onalar axboroti)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = botUsername,
                        onValueChange = { botUsername = it },
                        label = { Text("Bot username (Masalan: @BilimOtaOnaBot)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = botToken,
                        onValueChange = { botToken = it },
                        label = { Text("Bot Token (Xavfsiz saqlanadi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Bot vazifasi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TelegramBotType.values().forEach { t ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = botType == t,
                                onClick = { botType = t }
                            )
                            Text(t.displayName, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (botName.isNotBlank() && botUsername.isNotBlank()) {
                            val newBot = TelegramBot(
                                name = botName,
                                username = botUsername,
                                tokenMasked = if (botToken.length > 6) botToken.take(4) + "••••••••••••••••" else "7812••••••••",
                                type = botType,
                                isConnected = true
                            )
                            viewModel.registerTelegramBot(newBot)
                            showAddBotDialog = false
                        }
                    }
                ) {
                    Text("Ulash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBotDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // Test Notification Dispatcher Dialog
    if (selectedBotForTest != null) {
        val testBot = selectedBotForTest!!
        var recipient by remember { mutableStateOf("Otabek Qodirov (+998 90 999 88 77)") }
        var testMsg by remember { mutableStateOf("Assalomu alaykum! Farzandingiz Javohir bugun Tarix fanidan 95 ball oldi.") }

        AlertDialog(
            onDismissRequest = { selectedBotForTest = null },
            title = { Text("${testBot.name} — Test xabari") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Telegram bot orqali haqiqiy xabar yuborish mexanizmini tekshiring:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("Qabul qiluvchi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = testMsg,
                        onValueChange = { testMsg = it },
                        label = { Text("Xabar matni") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.testSendBotNotification(testBot.id, recipient, testMsg)
                        selectedBotForTest = null
                    }
                ) {
                    Text("Xabarni yuborish")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBotForTest = null }) {
                    Text("Yopish")
                }
            }
        )
    }
}
