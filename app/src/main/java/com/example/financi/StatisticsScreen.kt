package com.example.financi


import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(35.dp))

        Text("Статистика", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Доходы", style = MaterialTheme.typography.labelMedium)
                Text("+%.2f".format(state.totalIncome), color = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text("Расходы", style = MaterialTheme.typography.labelMedium)
                Text("-%.2f".format(state.totalExpense), color =MaterialTheme.colorScheme.error)
            }
            Column {
                Text("Баланс", style = MaterialTheme.typography.labelMedium)
                Text("%.2f".format(state.balance))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Period.values().forEach { period ->
                FilterChip(
                    selected = state.selectedPeriod == period,
                    onClick = { viewModel.setPeriod(period) },
                    label = {
                        Text(
                            when (period) {
                                Period.WEEK -> "Неделя"
                                Period.MONTH -> "Месяц"
                                Period.YEAR -> "Год"
                                Period.ALL -> "Всё"
                            }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.selectedType == TransactionType.EXPENSE,
                onClick = { viewModel.setType(TransactionType.EXPENSE) },
                label = { Text("Расходы") }
            )
            FilterChip(
                selected = state.selectedType == TransactionType.INCOME,
                onClick = { viewModel.setType(TransactionType.INCOME) },
                label = { Text("Доходы") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (state.selectedType == TransactionType.EXPENSE && state.expenseCategoryData.isNotEmpty()) {
            Text("Расходы по категориям", style = MaterialTheme.typography.titleSmall)
            PieChartView(state.expenseCategoryData)
        } else if (state.selectedType == TransactionType.INCOME && state.incomeCategoryData.isNotEmpty()) {
            Text("Доходы по категориям", style = MaterialTheme.typography.titleSmall)
            PieChartView(state.incomeCategoryData)
        } else {
            Text("Нет данных")
        }

        Spacer(modifier = Modifier.height(24.dp))

        val filteredLineData = when (state.selectedType) {
            TransactionType.INCOME -> LineChartData(
                incomeList = state.lineChartData.incomeList,
                expenseList = emptyList()
            )
            TransactionType.EXPENSE -> LineChartData(
                incomeList = emptyList(),
                expenseList = state.lineChartData.expenseList
            )
        }

        if (state.lineChartData.incomeList.isNotEmpty() || state.lineChartData.expenseList.isNotEmpty()) {
            Text("Динамика за период", style = MaterialTheme.typography.titleSmall)
            LineChartView(filteredLineData)
        }
    }
}

@Composable
fun PieChartView(data: List<CategorySumData>) {

    val textColor = MaterialTheme.colorScheme.onSurface
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(false)
                setDrawHoleEnabled(true)
                setTouchEnabled(false)
                highlightValues(null)
                setHoleColor(Color.TRANSPARENT)
                setTransparentCircleAlpha(0)
                legend.isEnabled = true
                legend.textColor = textColor.toArgb()
                setEntryLabelColor(textColor.toArgb())
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.total.toFloat(), it.categoryName) }
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = listOf(
                Color.rgb(255, 102, 0),
                Color.rgb(0, 153, 204),
                Color.rgb(102, 204, 0),
                Color.rgb(255, 204, 0),
                Color.rgb(204, 0, 204)
            )
            dataSet.valueFormatter = PercentFormatter()
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(300.dp)
    )
}

@Composable
fun LineChartView(data: LineChartData) {


    val textColor = MaterialTheme.colorScheme.onSurface
    val legendTextColor = textColor
    val incomeColor =Color.rgb(76, 175, 80)
    val expenseColor = Color.rgb(244, 67, 54)
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                setDoubleTapToZoomEnabled(false)
                legend.isEnabled = true
                legend.textColor = legendTextColor.toArgb()
                legend.textSize = 12f
                xAxis.textColor = legendTextColor.toArgb()
                xAxis.textSize = 12f
                axisLeft.textSize = 12f
                axisRight.textColor = legendTextColor.toArgb()
                axisRight.textSize = 12f
                legend.isEnabled = true
                legend.textColor = legendTextColor.toArgb()
                axisLeft.textColor = legendTextColor.toArgb()
                axisRight.textColor = legendTextColor.toArgb()
                xAxis.apply {
                   // setLabelCount(6, true)
                   // setAvoidFirstLastClipping(true)
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val sdf = java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault())
                            return sdf.format(java.util.Date(value.toLong()))
                        }
                    }
                }
            }
        },
        update = { chart ->
            val incomeEntries = data.incomeList.map { Entry(it.first.toFloat(), it.second.toFloat()) }
            val expenseEntries = data.expenseList.map { Entry(it.first.toFloat(), it.second.toFloat()) }

            val incomeDataSet = LineDataSet(incomeEntries, "Доходы").apply {
                color = incomeColor
                valueTextColor = textColor.toArgb()
                valueTextSize = 14f
                lineWidth = 2f
            }
            val expenseDataSet = LineDataSet(expenseEntries, "Расходы").apply {
                color = expenseColor
                valueTextColor = textColor.toArgb()
                valueTextSize = 14f
                lineWidth = 2f
            }

            chart.data = LineData(incomeDataSet, expenseDataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(300.dp)
    )
}

private fun Int.toComposeColor(): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(this)
}