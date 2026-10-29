package com.example.lasttime.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.lasttime.R
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.components.CenteredLoading
import com.example.lasttime.presentation.components.CenteredMessage
import com.example.lasttime.presentation.components.daysLabel
import com.example.lasttime.ui.theme.AppBackground
import com.example.lasttime.ui.theme.CategoryHeaderBlue
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.PurpleSubtitle
import com.example.lasttime.ui.theme.TabBorderDark
import com.example.lasttime.ui.theme.TabSelectedBg
import com.example.lasttime.ui.theme.TabUnselectedBg

@Composable
fun HomeScreen(
    onActivityClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Todos") }

    val tabs = listOf("Todos", "Trabalho", "Pessoal", "Plantas")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))

            // Barra Superior de Categorias: [Todos][Trabalho][Pessoal][Plantas]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, TabBorderDark)),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = selectedTab == tabName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(if (isSelected) TabSelectedBg else TabUnselectedBg)
                            .then(
                                if (index > 0) Modifier.border(
                                    BorderStroke(
                                        width = 0.8.dp,
                                        color = TabBorderDark
                                    )
                                ) else Modifier
                            )
                            .clickable { selectedTab = tabName },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            color = DarkText,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Lista de Atividades agrupadas por Categoria
            when (val state = uiState) {
                HomeUiState.Loading -> CenteredLoading()
                is HomeUiState.Error -> CenteredMessage(state.message)
                is HomeUiState.Success -> {
                    val activities = state.activities

                    val categoriesToShow = if (selectedTab == "Todos") {
                        listOf("Trabalho", "Pessoal", "Plantas")
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
                        categoriesToShow.forEach { categoryName ->
                            val categoryActivities = activities
                                .filter { it.category.equals(categoryName, ignoreCase = true) }
                                .sortedBy { it.daysSinceLastPerformed }

                            item(key = "group_$categoryName") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Cabeçalho azul
                                    CategorySectionHeader(title = categoryName)

                                    // Bloco contínuo de cards brancos
                                    if (categoryActivities.isEmpty()) {
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
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                categoryActivities.forEachIndexed { idx, activity ->
                                                    if (idx > 0) {
                                                        HorizontalDivider(
                                                            color = Color(0xFFDCE5ED),
                                                            thickness = 1.dp
                                                        )
                                                    }
                                                    ActivityItemRow(
                                                        activity = activity,
                                                        onClick = { onActivityClick(activity.id) }
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

@Composable
private fun ActivityItemRow(
    activity: Activity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = activity.name,
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = daysLabel(activity.daysSinceLastPerformed),
            color = PurpleSubtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

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
            Icon(
                painter = painterResource(id = R.drawable.ic_circle_down),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
