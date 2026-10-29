package com.example.lasttime.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lasttime.R
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.ModalBorder
import com.example.lasttime.ui.theme.ModalButtonCancel
import com.example.lasttime.ui.theme.ModalButtonCancelText
import com.example.lasttime.ui.theme.PrimaryNavy
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, date: LocalDate, category: String, notes: String) -> Unit
) {
    var category by remember { mutableStateOf("Categoria") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var hasPickedDate by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf("Trabalho", "Pessoal", "Plantas")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dropdown Categoria
                Box {
                    Row(
                        modifier = Modifier
                            .background(
                                color = ModalButtonCancel,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = ModalBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { categoryMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = DarkText
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Selecionar Categoria",
                            tint = DarkText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Campo Nome da Atividade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            border = BorderStroke(1.dp, ModalBorder),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (name.isEmpty()) {
                        Text(
                            text = "Dê um nome à atividade",
                            color = Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Campo Data da Atividade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            border = BorderStroke(1.dp, ModalBorder),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (hasPickedDate) date.toBr() else "Data da atividade",
                            color = if (hasPickedDate) DarkText else Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_custom),
                            contentDescription = "Escolher data",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Campo Anotações
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .border(
                            border = BorderStroke(1.dp, ModalBorder),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (notes.isEmpty()) {
                        Text(
                            text = "Anotações",
                            color = Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botões Cancelar e Salvar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModalButtonCancel
                        ),
                        shape = RoundedCornerShape(4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = ModalButtonCancelText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val cat = if (category == "Categoria") "Trabalho" else category
                                onSave(name, date, cat, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryNavy
                        ),
                        shape = RoundedCornerShape(4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "Salvar",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = it.toLocalDateFromUtcMillis()
                        hasPickedDate = true
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
