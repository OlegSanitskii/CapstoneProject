package ca.gbc.comp3074.snapcal.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.MealsViewModel
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressScreen() {
    val vm: MealsViewModel = viewModel()
    val (from, to) = vm.todayBounds()

    val kcal by vm.observeTotalCalories(from, to).collectAsState(initial = 0)
    val protein by vm.observeTotalProtein(from, to).collectAsState(initial = 0f)
    val carbs by vm.observeTotalCarbs(from, to).collectAsState(initial = 0f)
    val fat by vm.observeTotalFat(from, to).collectAsState(initial = 0f)

    val todayMeals by vm.observeMealsByDay(from).collectAsState(initial = emptyList())


    val intake7 by vm.observeDailyIntake(7).collectAsState(initial = emptyList())
    val weekTotal = remember(intake7) { intake7.sumOf { it.kcal } }

    var editing by remember { mutableStateOf<Meal?>(null) }

    if (editing != null) {
        EditMealDialog(
            meal = editing!!,
            onDismiss = { editing = null },
            onConfirm = { updated ->
                vm.editMeal(updated)
                editing = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Progress") }

        item {
            CardBlock(title = "Today's Summary") {
                SummaryRow(kcal, protein, carbs, fat)
            }
        }


        item {
            CardBlock(title = "Calories In (weekly) — $weekTotal kcal") {
                WeeklyInOutChart(
                    inData = intake7.map { it.kcal },
                    outData = null
                )
            }
        }

        item {
            CardBlock(title = "Today's Meals") {
                if (todayMeals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No meals yet today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayMeals.forEach { meal ->
                            MealRow(
                                meal = meal,
                                onEdit = { editing = meal },
                                onDelete = { vm.deleteMeal(meal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Meals list + summary ---------- */

@Composable
private fun MealRow(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(meal.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${meal.calories} kcal • P ${meal.protein.round1()} • C ${meal.carbs.round1()} • F ${meal.fat.round1()}",
                    style = MaterialTheme.typography.bodySmall
                )
                meal.portionGrams?.let {
                    Text("Portion: ${it.round1()} g", style = MaterialTheme.typography.bodySmall)
                }
                Text(meal.mealType, style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}

@Composable
private fun SummaryRow(kcal: Int, protein: Float, carbs: Float, fat: Float) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Calories", "$kcal kcal", Modifier.weight(1f))
        StatCard("Protein", "${protein.round1()} g", Modifier.weight(1f))
        StatCard("Carbs", "${carbs.round1()} g", Modifier.weight(1f))
        StatCard("Fat", "${fat.round1()} g", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/* ---------- Edit dialog ---------- */

@Composable
private fun EditMealDialog(
    meal: Meal,
    onDismiss: () -> Unit,
    onConfirm: (Meal) -> Unit
) {
    var name by remember(meal) { mutableStateOf(meal.name) }
    var portion by remember(meal) { mutableStateOf(meal.portionGrams?.toString().orEmpty()) }
    var calories by remember(meal) { mutableStateOf(meal.calories.toString()) }
    var protein by remember(meal) { mutableStateOf(meal.protein.toString()) }
    var carbs by remember(meal) { mutableStateOf(meal.carbs.toString()) }
    var fat by remember(meal) { mutableStateOf(meal.fat.toString()) }
    var mealType by remember(meal) { mutableStateOf(meal.mealType) }

    val isValid = name.isNotBlank() && calories.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = portion, onValueChange = { portion = it },
                    label = { Text("Portion (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calories, onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = protein, onValueChange = { protein = it },
                        label = { Text("P (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs, onValueChange = { carbs = it },
                        label = { Text("C (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat, onValueChange = { fat = it },
                        label = { Text("F (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = mealType, onValueChange = { mealType = it }, label = { Text("Meal type") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    val updated = meal.copy(
                        name = name.trim(),
                        calories = calories.toIntOrNull() ?: meal.calories,
                        protein = protein.toFloatOrNull() ?: meal.protein,
                        carbs = carbs.toFloatOrNull() ?: meal.carbs,
                        fat = fat.toFloatOrNull() ?: meal.fat,
                        mealType = mealType.trim().ifEmpty { meal.mealType },
                        portionGrams = portion.toFloatOrNull()
                    )
                    onConfirm(updated)
                }
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ---------- "In/Out ---------- */

@Composable
private fun WeeklyInOutChart(
    inData: List<Int>,
    outData: List<Int>? = null
) {
    val days = listOf("M","T","W","T","F","S","S")
    val inVals = (0 until 7).map { idx -> inData.getOrNull(idx) ?: 0 }
    val outVals = (0 until 7).map { idx -> outData?.getOrNull(idx) ?: 0 }

    val maxVal = (inVals + outVals).maxOrNull()?.coerceAtLeast(1) ?: 1
    val barWidth = 12.dp
    val gap = 16.dp
    val inColor = MaterialTheme.colorScheme.primary
    val outColor = MaterialTheme.colorScheme.error

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(inColor); Text("In", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
            LegendDot(outColor.copy(alpha = if (outData == null) 0.35f else 1f))
            Text("Out", style = MaterialTheme.typography.labelSmall, color = if (outData == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.indices.forEach { i ->
                val inH = (inVals[i].toFloat() / maxVal.toFloat()).coerceIn(0f,1f)
                val outH = (outVals[i].toFloat() / maxVal.toFloat()).coerceIn(0f,1f)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val dayTotal = inVals[i] + outVals[i]
                    if (dayTotal > 0) {
                        Text("${inVals[i]}", style = MaterialTheme.typography.labelSmall)
                    } else {
                        Spacer(Modifier.height(14.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        Box(
                            Modifier
                                .width(barWidth)
                                .fillMaxHeight(fraction = inH)
                                .clip(MaterialTheme.shapes.small)
                                .background(inColor)
                        )
                        Box(
                            Modifier
                                .width(barWidth)
                                .fillMaxHeight(fraction = outH)
                                .clip(MaterialTheme.shapes.small)
                                .background(outColor.copy(alpha = if (outData == null) 0.35f else 1f))
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(days[i], style = MaterialTheme.typography.labelSmall)
                }
                if (i != days.lastIndex) Spacer(Modifier.width(gap))
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .size(10.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
    )
}

@SuppressLint("DefaultLocale")
private fun Float.round1(): String = String.format("%.1f", this)

private fun dayLabel(dayStartMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = dayStartMillis }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "M"
        Calendar.TUESDAY -> "T"
        Calendar.WEDNESDAY -> "W"
        Calendar.THURSDAY -> "T"
        Calendar.FRIDAY -> "F"
        Calendar.SATURDAY -> "S"
        else -> "S"
    }
}
