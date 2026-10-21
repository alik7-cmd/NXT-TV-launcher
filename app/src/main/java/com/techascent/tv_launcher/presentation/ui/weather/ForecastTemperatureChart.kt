package com.techascent.tv_launcher.presentation.ui.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.DailyForecastUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
internal fun ForecastTemperatureChart(days: List<DailyForecastUiState>) {
    val temperatures = days.flatMap { listOfNotNull(it.high, it.low) }
    if (temperatures.isEmpty()) return
    val minimum = temperatures.min() - 2f
    val maximum = temperatures.max() + 2f
    val highColor = LauncherColors.TextPrimary.copy(alpha = 0.6f)
    val lowColor = LauncherColors.Accent.copy(alpha = 0.5f)

    Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        ) {
            Text(stringResource(R.string.weather_chart_high), color = highColor, fontSize = 11.sp)
            Text(stringResource(R.string.weather_chart_low), color = lowColor, fontSize = 11.sp)
        }
        // The daily text below provides the chart's dates and exact values to accessibility.
        Canvas(Modifier.fillMaxWidth().height(44.dp)) {
            val gap = LauncherSpacing.Small.toPx()
            val columnWidth = (size.width - gap * (days.size - 1)) / days.size
            val inset = 3.dp.toPx()
            fun point(index: Int, temperature: Int) = Offset(
                x = columnWidth / 2 + index * (columnWidth + gap),
                y = inset + (maximum - temperature) / (maximum - minimum) * (size.height - inset * 2),
            )
            fun series(color: Color, value: (DailyForecastUiState) -> Int?) {
                var previous: Offset? = null
                days.forEachIndexed { index, day ->
                    val current = value(day)?.let { point(index, it) }
                    if (current != null) {
                        previous?.let {
                            drawLine(color, it, current, strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        }
                        drawCircle(color, radius = 2.5.dp.toPx(), center = current)
                    }
                    previous = current
                }
            }
            series(highColor) { it.high }
            series(lowColor) { it.low }
        }
    }
}
