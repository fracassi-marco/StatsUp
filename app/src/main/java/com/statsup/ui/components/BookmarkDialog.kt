package com.statsup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.statsup.R
import com.statsup.domain.Difficulty

@Composable
fun BookmarkDialog(
    trainingName: String,
    isBookmarked: Boolean,
    currentNote: String,
    currentCustomTitle: String = "",
    currentDifficulty: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit, // note, customTitle, difficulty
    onRemove: () -> Unit
) {
    var noteText by remember { mutableStateOf(currentNote) }
    var customTitle by remember { mutableStateOf(currentCustomTitle) }
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Titolo
                Text(
                    text = if (isBookmarked)
                        stringResource(id = R.string.edit_bookmark)
                    else
                        stringResource(id = R.string.add_to_bookmarks),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Nome training originale
                Text(
                    text = trainingName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Custom Title
                Text(
                    text = stringResource(id = R.string.custom_title_optional),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.custom_title_placeholder),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                // Difficulty Selection
                Text(
                    text = stringResource(id = R.string.difficulty_optional),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Easy
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedDifficulty = if (selectedDifficulty == Difficulty.EASY.value) "" else Difficulty.EASY.value
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SentimentSatisfied,
                            contentDescription = stringResource(id = R.string.difficulty_easy),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            tint = if (selectedDifficulty == Difficulty.EASY.value)
                                Color(0xFF4CAF50)
                            else
                                Color(0xFF4CAF50).copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.difficulty_easy),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (selectedDifficulty == Difficulty.EASY.value) 1f else 0.5f
                            )
                        )
                    }

                    // Medium
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedDifficulty = if (selectedDifficulty == Difficulty.MEDIUM.value) "" else Difficulty.MEDIUM.value
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = stringResource(id = R.string.difficulty_medium),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            tint = if (selectedDifficulty == Difficulty.MEDIUM.value)
                                Color(0xFFFF9800)
                            else
                                Color(0xFFFF9800).copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.difficulty_medium),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (selectedDifficulty == Difficulty.MEDIUM.value) 1f else 0.5f
                            )
                        )
                    }

                    // Hard
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedDifficulty = if (selectedDifficulty == Difficulty.HARD.value) "" else Difficulty.HARD.value
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = stringResource(id = R.string.difficulty_hard),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            tint = if (selectedDifficulty == Difficulty.HARD.value)
                                Color(0xFFF44336)
                            else
                                Color(0xFFF44336).copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.difficulty_hard),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (selectedDifficulty == Difficulty.HARD.value) 1f else 0.5f
                            )
                        )
                    }
                }

                // Campo note
                Text(
                    text = stringResource(id = R.string.note_optional),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.add_note_placeholder),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pulsanti
                if (isBookmarked) {
                    // Se già bookmarkato: mostra Rimuovi e Salva
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onRemove()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(id = R.string.remove_bookmark))
                        }

                        Button(
                            onClick = {
                                onConfirm(noteText, customTitle, selectedDifficulty)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                } else {
                    // Se non bookmarkato: mostra Annulla e Aggiungi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(id = R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onConfirm(noteText, customTitle, selectedDifficulty)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(id = R.string.add_bookmark))
                        }
                    }
                }
            }
        }
    }
}

