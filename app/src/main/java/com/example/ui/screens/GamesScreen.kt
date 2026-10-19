package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameCategory
import com.example.data.repository.GameRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@Composable
fun GamesScreen(
    viewModel: BilimViewModel
) {
    val selectedCategory by viewModel.selectedGameCategory.collectAsState()
    val currentQuestionIndex by viewModel.currentGameQuestionIndex.collectAsState()
    val gameScore by viewModel.gameScore.collectAsState()
    val gameStreak by viewModel.gameStreak.collectAsState()
    val feedback by viewModel.lastGameFeedback.collectAsState()

    var gameMode by remember { mutableStateOf(0) } // 0: Viktorina, 1: Tarixiy Vaqt Chizig'i, 2: Juftliklar

    val categoryQuestions = remember(selectedCategory) {
        GameRepository.gameQuestions.filter { it.category == selectedCategory }
    }
    val currentQuestion = categoryQuestions.getOrNull(currentQuestionIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Score & Streak Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("O'yinlar Markazi", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("Bilimlar Bellashuvi", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ochko", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$gameScore", color = GoldLight, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Seriya (Streak)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("🔥 $gameStreak", color = GoldVibrant, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // 2. Mode Selector (Viktorina, Vaqt chizig'i, Juftliklar)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = gameMode == 0,
                    onClick = { gameMode = 0 },
                    label = { Text("Fan Viktorinasi", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gameMode == 1,
                    onClick = { gameMode = 1 },
                    label = { Text("Vaqt Chizig'i", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gameMode == 2,
                    onClick = { gameMode = 2 },
                    label = { Text("Juftliklar", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Subject Selector Chips (Tarix, Geografiya, Matematika, etc.)
        item {
            Text(
                text = "Fanni tanlang:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    Card(
                        modifier = Modifier
                            .clickable { viewModel.setGameCategory(cat) }
                            .testTag("game_chip_${cat.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                    ) {
                        Text(
                            text = cat.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GoldLight else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // 4. Main Game Play Area
        when (gameMode) {
            0 -> {
                // Interactive Question Mode
                item {
                    if (currentQuestion != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${selectedCategory.title} • Savol ${currentQuestionIndex + 1} / ${categoryQuestions.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "+${currentQuestion.points} ball",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldKnowledge
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentQuestion.prompt,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                currentQuestion.options.forEachIndexed { optIdx, optText ->
                                    Button(
                                        onClick = { viewModel.answerGameQuestion(optIdx) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .testTag("game_opt_${optIdx}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = "${('A'.code + optIdx).toChar()}) $optText",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (feedback != null) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = feedback!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Ajoyib natija! Ushbu fanning barcha savollarini yakunladingiz.", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = { viewModel.resetGame() }) {
                                    Text("Qaytadan o'ynash")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Interactive Historical Timeline
                item {
                    Text(
                        text = "Tarixiy vaqt chizig'i (Xronologiya)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(GameRepository.historyTimeline) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavyPrimary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.yearDisplay,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.event,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Pairs Matching Challenge
                item {
                    PairsMatchingArea(viewModel)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun PairsMatchingArea(viewModel: BilimViewModel) {
    val pairs = GameRepository.capitalsMatching
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var matchedPairs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var attempts by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Geografiya: Davlat va Poytaxtlarni moslashtiring",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Chap tarafdan davlatni, o'ng tarafdan uning poytaxtini tanlang:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            pairs.take(4).forEach { pair ->
                val isMatched = pair.left in matchedPairs
                val isLeftSelected = selectedLeft == pair.left

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left (Country)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isMatched) {
                                selectedLeft = pair.left
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isMatched -> EmeraldKnowledge.copy(alpha = 0.2f)
                            isLeftSelected -> GoldPrimary.copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = pair.left,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isMatched) EmeraldKnowledge else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Right (Capital)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isMatched && selectedLeft != null) {
                                attempts++
                                if (selectedLeft == pair.left) {
                                    matchedPairs = matchedPairs + pair.left
                                    selectedLeft = null
                                    viewModel.showNotice("To'g'ri moslashtirildi! +50 ball")
                                } else {
                                    viewModel.showNotice("Noto'g'ri poytaxt, qayta urinib ko'ring!")
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isMatched -> EmeraldKnowledge.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = pair.right,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isMatched) EmeraldKnowledge else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (matchedPairs.size >= 4) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = EmeraldKnowledge.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Barakalla! Barcha poytaxtlar to'g'ri topildi! ($attempts ta urinish)",
                        color = EmeraldKnowledge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
