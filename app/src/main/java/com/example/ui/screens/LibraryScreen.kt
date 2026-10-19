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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LibraryMaterial
import com.example.ui.theme.*
import com.example.ui.viewmodel.BilimViewModel

@Composable
fun LibraryScreen(
    viewModel: BilimViewModel
) {
    val materials by viewModel.libraryMaterials.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("Barchasi") }
    var selectedMaterialForView by remember { mutableStateOf<LibraryMaterial?>(null) }

    val filteredMaterials = remember(materials, searchQuery, selectedSubjectFilter) {
        materials.filter {
            val matchesQuery = searchQuery.isBlank() ||
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.author.contains(searchQuery, ignoreCase = true)
            val matchesSubj = selectedSubjectFilter == "Barchasi" || it.subjectName == selectedSubjectFilter
            matchesQuery && matchesSubj
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Raqamli Kutubxona va Materiallar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Darslik yoki muallif nomi...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredMaterials) { item ->
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
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldKnowledge.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.fileType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldKnowledge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Muallif: ${item.author} • ${item.gradeLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.pageCount} sahifa • 14.5 MB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Button(
                                onClick = {
                                    selectedMaterialForView = item
                                    viewModel.showNotice("'${item.title}' o'quv qo'llanmasi ochildi")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = GoldPrimary)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("O'qish", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedMaterialForView != null) {
        val mat = selectedMaterialForView!!
        AlertDialog(
            onDismissRequest = { selectedMaterialForView = null },
            title = { Text(mat.title) },
            text = {
                Column {
                    Text(
                        text = "Fan: ${mat.subjectName} (${mat.gradeLevel})\nMuallif: ${mat.author}\nHajmi: ${mat.pageCount} sahifa\n\nQo'llanma bo'limlari:\n1. Kirish va asosiy tushunchalar\n2. Nazariy qoidalar va formulalar\n3. Amaliy mashqlar va test savollari\n4. Mustaqil ta'lim topshiriqlari",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedMaterialForView = null }) {
                    Text("Tushunarli")
                }
            }
        )
    }
}
