package com.example.lasttime.presentation.components

// Importações do Jetpack Compose para estilização e estrutura de layout
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

// Importações do Material Design 3 (Ícones, Botões, Seletor de Data e Menus)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState

// Importações de estado e ambiente de execução do Compose
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

// Recursos locais e temas da aplicação
import com.example.lasttime.R
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.ModalBorder
import com.example.lasttime.ui.theme.ModalButtonCancel
import com.example.lasttime.ui.theme.ModalButtonCancelText
import com.example.lasttime.ui.theme.PrimaryNavy
import java.time.LocalDate

/**
 * Diálogo modal para criação de uma nova atividade no aplicativo.
 *
 * Exibe campos para:
 * 1. Seleção de Categoria (com opção de criar nova categoria).
 * 2. Nome da atividade.
 * 3. Data de realização da atividade (com seletor de data Material3).
 * 4. Anotações opcionais.
 *
 * @param onDismiss Callback invocado quando o diálogo é cancelado ou fechado.
 * @param onSave Callback invocado ao confirmar o salvamento da nova atividade.
 * @param availableCategories Lista de categorias registradas no aplicativo.
 * @param onAddCategory Callback opcional para adição de uma nova categoria.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, date: LocalDate, category: String, notes: String) -> Unit,
    availableCategories: List<String> = listOf("Trabalho", "Pessoal", "Plantas"),
    onAddCategory: ((String) -> Unit)? = null
) {
    // Estado da categoria atualmente selecionada no dropdown
    var category by remember { mutableStateOf("Categoria") }

    // Estado do menu suspenso (expandido ou recolhido)
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Estado para exibição do diálogo secundário de criação de nova categoria
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Estado do nome digitado para a atividade
    var name by remember { mutableStateOf("") }

    // Estado da data escolhida (inicializada com o dia de hoje)
    var date by remember { mutableStateOf(LocalDate.now()) }

    // Flag para indicar se o usuário já escolheu uma data explicitamente
    var hasPickedDate by remember { mutableStateOf(false) }

    // Estado para o texto do campo de anotações
    var notes by remember { mutableStateOf("") }

    // Estado para exibição do seletor de data (DatePickerDialog)
    var showDatePicker by remember { mutableStateOf(false) }

    // Diálogo principal flutuante
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Container branco arredondado do modal
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f) // Ocupa 92% da largura da tela
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // =========================================================
                // 1. SEÇÃO DE CATEGORIA (Dropdown com opção de criar nova)
                // =========================================================
                Box {
                    // Botão seletor de categoria estilizado
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
                            .clickable { categoryMenuExpanded = true } // Abre o menu ao clicar
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nome da categoria selecionada
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = DarkText
                        )
                        Spacer(Modifier.width(4.dp))
                        // Ícone da seta indicando menu suspenso
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Selecionar Categoria",
                            tint = DarkText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Menu suspenso com as opções de categorias
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        // Lista todas as categorias cadastradas
                        availableCategories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                        // Divisor entre as categorias existentes e o botão de criar
                        HorizontalDivider()
                        // Opção especial para abrir o modal de nova categoria
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Nova categoria",
                                    color = PrimaryNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = {
                                categoryMenuExpanded = false
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // =========================================================
                // 2. CAMPO NOME DA ATIVIDADE
                // =========================================================
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
                    // Placeholder exibido enquanto o campo está vazio
                    if (name.isEmpty()) {
                        Text(
                            text = "Dê um nome à atividade",
                            color = Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                    }
                    // Campo de entrada de texto puro
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // =========================================================
                // 3. CAMPO SELEÇÃO DE DATA DA ATIVIDADE
                // =========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            border = BorderStroke(1.dp, ModalBorder),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { showDatePicker = true } // Abre o DatePicker ao clicar
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Exibe a data formatada (caso escolhida) ou o texto padrão
                        Text(
                            text = if (hasPickedDate) date.toBr() else "Data da atividade",
                            color = if (hasPickedDate) DarkText else Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                        // Ícone do calendário
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_custom),
                            contentDescription = "Escolher data",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // =========================================================
                // 4. CAMPO ANOTAÇÕES (Multilinha)
                // =========================================================
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
                    // Placeholder exibido enquanto as anotações estiverem vazias
                    if (notes.isEmpty()) {
                        Text(
                            text = "Anotações",
                            color = Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                    }
                    // Campo de entrada multilinha
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                // =========================================================
                // 5. BOTÕES DE AÇÃO (Cancelar e Salvar)
                // =========================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Cancelar
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

                    // Botão Salvar
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                // Se nenhuma categoria foi alterada, usa "Trabalho" como padrão
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

    // =========================================================
    // DIÁLOGO SECUNDÁRIO PARA CRIAÇÃO DE NOVA CATEGORIA
    // =========================================================
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { newCategory ->
                onAddCategory?.invoke(newCategory) // Notifica o listener de criação
                category = newCategory // Seleciona automaticamente a categoria recém-criada
                showAddCategoryDialog = false
            }
        )
    }

    // =========================================================
    // SELETOR DE DATA MATERIAL3 (DatePickerDialog)
    // =========================================================
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = it.toLocalDateFromUtcMillis() // Converte milissegundos UTC para LocalDate
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

/**
 * Diálogo modal para inserção do nome de uma nova categoria personalizada.
 *
 * @param onDismiss Callback de cancelamento.
 * @param onSave Callback acionado ao submeter o nome da categoria.
 */
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // Nome digitado para a nova categoria
    var categoryName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Ocupa 85% da largura da tela
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Título do modal
                Text(
                    text = "Nova Categoria",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(Modifier.height(14.dp))

                // Campo de texto para digitação da categoria
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
                    if (categoryName.isEmpty()) {
                        Text(
                            text = "Nome da categoria",
                            color = Color(0xFF6E8192),
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botões Cancelar e Adicionar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ModalButtonCancel),
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
                            if (categoryName.isNotBlank()) {
                                onSave(categoryName.trim()) // Envia o nome sem espaços nas extremidades
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        shape = RoundedCornerShape(4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "Adicionar",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
