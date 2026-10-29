package com.example.lasttime.presentation.home

// Importações do Compose Foundation (Layouts, Bordas, Gestos de Rolagem)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

// Importações do Material Design 3 (Ícones, Cards, Divisores, Botões)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

// Importações do ciclo de vida e estado reativo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Recursos da aplicação e modelos de domínio
import com.example.lasttime.R
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.components.CenteredLoading
import com.example.lasttime.presentation.components.CenteredMessage
import com.example.lasttime.presentation.components.daysLabel
import com.example.lasttime.ui.theme.AppBackground
import com.example.lasttime.ui.theme.CategoryHeaderBlue
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.PrimaryNavy
import com.example.lasttime.ui.theme.PurpleSubtitle
import com.example.lasttime.ui.theme.TabBorderDark
import com.example.lasttime.ui.theme.TabSelectedBg
import com.example.lasttime.ui.theme.TabUnselectedBg

/**
 * Tela Principal (Home) do aplicativo LastTime.
 *
 * Exibe as atividades organizadas em abas de categorias e agrupadas visualmente em blocos.
 * Permite navegação ao histórico de cada atividade ao tocar no item e marcação imediata
 * da atividade como "realizada hoje" por meio de um botão dedicado em cada card.
 *
 * @param onActivityClick Callback invocado ao clicar em uma atividade, recebendo seu ID.
 * @param modifier Modificador de layout repassado pelo container pai.
 * @param viewModel ViewModel da tela responsável pelo fluxo de dados e eventos do Room.
 */
@Composable
fun HomeScreen(
    onActivityClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Coleta o estado da UI do ViewModel observando alterações em tempo real no Room
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado da aba de categoria atualmente selecionada (Padrão: "Todos")
    var selectedTab by remember { mutableStateOf("Todos") }

    // Obtém as categorias cadastradas a partir do estado da UI ou define lista de contingência
    val categories = when (val state = uiState) {
        is HomeUiState.Success -> state.categories.ifEmpty { listOf("Trabalho", "Pessoal", "Plantas") }
        else -> listOf("Trabalho", "Pessoal", "Plantas")
    }

    // Concatena a aba global "Todos" com as demais categorias dinâmicas
    val tabs = remember(categories) { listOf("Todos") + categories }

    // Container raiz da tela com fundo padrão e espaçamento lateral
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp) // Limita a largura máxima para boa responsividade em tablets/telas grandes
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))

            // =========================================================
            // BARRA SUPERIOR DE CATEGORIAS (ABAS)
            // =========================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Adiciona rolagem horizontal automática se houver mais de 4 categorias
                    .then(
                        if (tabs.size > 4) Modifier.horizontalScroll(rememberScrollState())
                        else Modifier
                    )
                    .border(BorderStroke(1.5.dp, TabBorderDark)),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = selectedTab == tabName
                    Box(
                        modifier = Modifier
                            .then(
                                // Se houver até 4 abas, divide a largura igualmente; caso contrário, usa espaçamento fixo
                                if (tabs.size <= 4) Modifier.weight(1f)
                                else Modifier.padding(horizontal = 12.dp)
                            )
                            .height(36.dp)
                            .background(if (isSelected) TabSelectedBg else TabUnselectedBg)
                            .then(
                                // Adiciona borda divisória entre as abas
                                if (index > 0) Modifier.border(
                                    BorderStroke(
                                        width = 0.8.dp,
                                        color = TabBorderDark
                                    )
                                ) else Modifier
                            )
                            .clickable { selectedTab = tabName }, // Seleciona a aba ao clicar
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            color = DarkText,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // =========================================================
            // CONTEÚDO PRINCIPAL (LISTA DE ATIVIDADES AGRUPADAS)
            // =========================================================
            when (val state = uiState) {
                // Estado de Carregamento inicial
                HomeUiState.Loading -> CenteredLoading()

                // Estado de Erro na leitura do banco de dados
                is HomeUiState.Error -> CenteredMessage(state.message)

                // Estado Sucesso: renderiza as categorias e suas atividades
                is HomeUiState.Success -> {
                    val activities = state.activities

                    // Define quais categorias serão exibidas no feed
                    val categoriesToShow = if (selectedTab == "Todos") {
                        categories
                    } else {
                        listOf(selectedTab)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Itera sobre as categorias filtradas
                        categoriesToShow.forEach { categoryName ->
                            // Filtra e ordena as atividades da categoria por tempo desde a última realização
                            val categoryActivities = activities
                                .filter { it.category.equals(categoryName, ignoreCase = true) }
                                .sortedBy { it.daysSinceLastPerformed }

                            // Renderiza a seção de cada categoria no LazyColumn
                            item(key = "group_$categoryName") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Cabeçalho estilizado em azul da categoria
                                    CategorySectionHeader(title = categoryName)

                                    // Renderização do bloco de atividades
                                    if (categoryActivities.isEmpty()) {
                                        // Card informativo caso não haja atividades na categoria
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(0.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Text(
                                                text = "Nenhuma atividade nesta categoria.",
                                                color = Color.Gray,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }
                                    } else {
                                        // Bloco contínuo branco com cantos arredondados na base
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                categoryActivities.forEachIndexed { idx, activity ->
                                                    // Adiciona linha divisória sutil entre os itens
                                                    if (idx > 0) {
                                                        HorizontalDivider(
                                                            color = Color(0xFFDCE5ED),
                                                            thickness = 1.dp
                                                        )
                                                    }
                                                    // Renderiza a linha com o nome, dias e o botão "Marcar Hoje"
                                                    ActivityItemRow(
                                                        activity = activity,
                                                        onClick = { onActivityClick(activity.id) },
                                                        onMarkToday = { viewModel.onEvent(HomeEvent.MarkToday(activity.id)) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que renderiza uma linha individual de atividade dentro da lista da Home.
 *
 * @param activity Objeto de domínio [Activity] contendo id, nome e dias desde a realização.
 * @param onClick Callback executado ao clicar no corpo do item (abre o histórico).
 * @param onMarkToday Callback executado ao clicar no botão de confirmação "Marcar Hoje".
 */
@Composable
private fun ActivityItemRow(
    activity: Activity,
    onClick: () -> Unit,
    onMarkToday: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Clique no card abre o histórico da atividade
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Coluna da esquerda: Nome da atividade e tempo transcorrido
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Nome da atividade
            Text(
                text = activity.name,
                color = DarkText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif
            )
            // Descrição do tempo (ex: "Há 6 dias", "Realizada hoje")
            Text(
                text = daysLabel(activity.daysSinceLastPerformed),
                color = PurpleSubtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Botão da direita: Adiciona histórico para a data de hoje
        IconButton(
            onClick = onMarkToday,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Marcar para hoje",
                tint = PrimaryNavy,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Cabeçalho azul característico de cada seção de categoria.
 *
 * @param title Nome da categoria a ser exibido.
 * @param modifier Modificador opcional de layout.
 */
@Composable
fun CategorySectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = CategoryHeaderBlue,
                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de seta circular indicando grupo expandido
            Icon(
                painter = painterResource(id = R.drawable.ic_circle_down),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            // Título em destaque com fonte em negrito
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
