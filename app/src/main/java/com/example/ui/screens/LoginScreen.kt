package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@Composable
fun LoginScreen(viewModel: BilimViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showActivationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkBackground,
                        Color(0xFF0F1A2C),
                        DarkSurface
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(scrollState),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GoldDark, GoldPrimary, GoldLight)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NavyAccent, NavyDark)
                            )
                        )
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "BILIM MARKAZI PRO",
                        tint = GoldVibrant,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BILIM MARKAZI PRO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VerifiedBadge(size = 18.dp)
                }

                Text(
                    text = "Milliy Ta'lim va Boshqaruv Ekotizimi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Login yoki Elektron pochta") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parol") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Parolni yashirish" else "Parolni ko'rsatish"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                viewModel.loginWithCredentials(username, password)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showActivationDialog = true }) {
                        Text("Faollashtirish kodi", fontSize = 12.sp, color = BlueInfo)
                    }

                    TextButton(onClick = { showPasswordResetDialog = true }) {
                        Text("Parolni unutdingizmi?", fontSize = 12.sp, color = GoldLight)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            viewModel.showNotice("Iltimos, login va parolni kiriting")
                        } else {
                            viewModel.loginWithCredentials(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = NavyDark
                    )
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tizimga Kirish",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Yordam va Aloqa xizmati",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showPasswordResetDialog) {
        var resetLogin by remember { mutableStateOf("") }
        var resetSent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("Parolni tiklash", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (!resetSent) {
                        Text(
                            "Tizimdagi login yoki biriktirilgan telefon raqamingizni kiriting. Xavfsiz bir martalik SMS tasdiq kodi yuboriladi.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetLogin,
                            onValueChange = { resetLogin = it },
                            label = { Text("Login yoki Telefon") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(
                            "Tasdiqlash kodi telefoningizga yuborildi. Administrator bilan bog'lanib yangi parolni faollashtirishingiz mumkin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldKnowledge
                        )
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick = {
                            if (resetLogin.isNotBlank()) {
                                resetSent = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Kodni yuborish", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(onClick = { showPasswordResetDialog = false }) {
                        Text("Tushundim")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Yopish")
                }
            }
        )
    }

    if (showActivationDialog) {
        var code by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var isActivatedSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showActivationDialog = false },
            title = { Text("Hisobni faollashtirish", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (!isActivatedSuccess) {
                        Text(
                            "Muassasangiz yoki administrator tomonidan berilgan 8 xonali faollashtirish kodini kiriting:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Faollashtirish kodi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("Yangi parol") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(
                            "Hisob muvaffaqiyatli faollashtirildi! Endi ushbu parol orqali tizimga kirishingiz mumkin.",
                            color = EmeraldKnowledge
                        )
                    }
                }
            },
            confirmButton = {
                if (!isActivatedSuccess) {
                    Button(
                        onClick = {
                            if (code.isNotBlank() && newPass.isNotBlank()) {
                                isActivatedSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Faollashtirish", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(onClick = { showActivationDialog = false }) {
                        Text("Kirish")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivationDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Yordam va Aloqa", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("BILIM MARKAZI PRO axborot xizmati:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Ish vaqti: 24/7 rejimida", style = MaterialTheme.typography.bodySmall)
                    Text("• Call-markaz: +998 71 200-00-00", style = MaterialTheme.typography.bodySmall)
                    Text("• Telegram qo'llab-quvvatlash: @bilimmarkazi_support", style = MaterialTheme.typography.bodySmall)
                    Text("• Pochta: support@bilimmarkazi.uz", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Tushunarli")
                }
            }
        )
    }
}
