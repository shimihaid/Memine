package com.example.lasttime.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lasttime.R
import com.example.lasttime.ui.theme.GrayText
import com.example.lasttime.ui.theme.PrimaryNavy

enum class MemineTab {
    ATIVIDADES,
    CALENDARIO
}

@Composable
fun MemineBottomBar(
    currentTab: MemineTab,
    onTabSelected: (MemineTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(76.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aba Atividades (Esquerda)
            val isAtividades = currentTab == MemineTab.ATIVIDADES
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isAtividades) PrimaryNavy else Color.White)
                    .clickable { onTabSelected(MemineTab.ATIVIDADES) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notebook),
                        contentDescription = "Atividades",
                        tint = if (isAtividades) Color.White else GrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Atividades",
                        color = if (isAtividades) Color.White else GrayText,
                        fontSize = 12.sp,
                        fontWeight = if (isAtividades) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Centro: Botão Nova atividade (+)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(PrimaryNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Nova atividade",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Nova atividade",
                        color = GrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Aba Calendário (Direita)
            val isCalendario = currentTab == MemineTab.CALENDARIO
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isCalendario) PrimaryNavy else Color.White)
                    .clickable { onTabSelected(MemineTab.CALENDARIO) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar_custom),
                        contentDescription = "Calendário",
                        tint = if (isCalendario) Color.White else GrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Calendário",
                        color = if (isCalendario) Color.White else GrayText,
                        fontSize = 12.sp,
                        fontWeight = if (isCalendario) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}
