package com.example.lasttime.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lasttime.ui.theme.AppBackground
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.GrayText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {}
) {
    // Começamos em Outubro de 2026 para corresponder exatamente ao print do usuário
    var currentYearMonth by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(top = 16.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card do Calendário
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header: - Mês Ano +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "–",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = DarkText,
                            modifier = Modifier
                                .clickable {
                                    currentYearMonth = currentYearMonth.minusMonths(1)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        Text(
                            text = "$monthName ${currentYearMonth.year}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = DarkText,
                            fontFamily = FontFamily.Serif
                        )

                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = DarkText,
                            modifier = Modifier
                                .clickable {
                                    currentYearMonth = currentYearMonth.plusMonths(1)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Dias da Semana: Sun Mon Tue Wed Thu Fri Sat
                    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = DarkText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Grid dos Dias do Mês
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val daysInMonth = currentYearMonth.lengthOfMonth()
                    // Java Time DayOfWeek: Monday is 1, Sunday is 7. No grid (Sun = 0, Mon = 1, ..., Sat = 6)
                    val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value % 7)

                    val totalCells = ((firstDayOfWeekIndex + daysInMonth + 6) / 7) * 7

                    for (weekIndex in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (dayCol in 0..6) {
                                val cellIndex = weekIndex * 7 + dayCol
                                val dayNumber = cellIndex - firstDayOfWeekIndex + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val date = currentYearMonth.atDay(dayNumber)
                                    val isSelected = selectedDate == date

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedDate = date
                                                onDayClick(date)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = DarkText,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Espaço central: "Clique em + para adicionar uma nova atividade"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Clique em + para adicionar\numa nova atividade",
                    color = GrayText,
                    fontSize = 19.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
