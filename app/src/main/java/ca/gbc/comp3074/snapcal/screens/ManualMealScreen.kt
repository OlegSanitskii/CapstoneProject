package ca.gbc.comp3074.snapcal.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.ui.state.MealsViewModel

@Composable
fun ManualMealScreen(onSaved: () -> Unit) {
    val vm: MealsViewModel = viewModel()

    // form state
    var name by rememberSaveable { mutableStateOf("") }
    var portion by rememberSaveable { mutableStateOf("") }        // grams
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var mealType by rememberSaveable { mutableStateOf("Breakfast") }

    // when editing existing meal
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }

    val isValid = name.isNotBlank() && calories.toIntOrNull() != null
    val scrollState = rememberScrollState()

    fun clearForm() {
        name = ""; portion = ""; calories = ""
        protein = ""; carbs = ""; fat = ""
        mealType = "Breakfast"
        editingId = null
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(if (editingId == null) "Add Meal Manually" else "Edit Meal")

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),      // <-- прокрутка
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Form ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = portion,
                onValueChange = { portion = it },
                label = { Text("Portion size (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = calories,
                onValueChange = { if (it.length <= 6) calories = it },
                label = { Text("Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = mealType,
                onValueChange = { mealType = it },
                label = { Text("Meal type") },
                modifier = Modifier.fillMaxWidth()
            )

            // Actions row (Save/Update + Cancel edit)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val base = Meal(
                            name = name.trim(),
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toFloatOrNull() ?: 0f,
                            carbs = carbs.toFloatOrNull() ?: 0f,
                            fat = fat.toFloatOrNull() ?: 0f,
                            mealType = mealType.trim().ifEmpty { "Meal" },
                            createdAt = System.currentTimeMillis(),
                            portionGrams = portion.toFloatOrNull()
                        )
                        if (editingId != null) {
                            vm.editMeal(base.copy(id = editingId!!))
                        } else {
                            vm.addMeal(base)
                        }
                        clearForm()
                        onSaved()
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(if (editingId == null) {
                        if (isValid) "Save Meal" else "Fill required fields"
                    } else "Update Meal")
                }

                if (editingId != null) {
                    OutlinedButton(
                        onClick = { clearForm() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(14.dp)
                    ) { Text("Cancel") }
                }
            }

            // --- Recent list (last 10) ---
            Divider(Modifier.padding(top = 8.dp))
            Text("Recent meals", style = MaterialTheme.typography.titleSmall)

            val recent by vm.meals.collectAsState()
            if (recent.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No meals yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Используем LazyColumn внутри фиксированного размера, чтобы сохранить общую прокрутку экрана
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 360.dp)   // чтобы список был прокручиваемым, не растягивая экран
                ) {
                    items(recent.take(10), key = { it.id }) { meal ->
                        RecentMealItem(
                            meal = meal,
                            onEdit = {
                                // заполняем форму данными
                                editingId = meal.id
                                name = meal.name
                                portion = meal.portionGrams?.toString().orEmpty()
                                calories = meal.calories.toString()
                                protein = meal.protein.toString()
                                carbs = meal.carbs.toString()
                                fat = meal.fat.toString()
                                mealType = meal.mealType
                                // прокрутиться наверх к форме (опционально можно добавить)
                            },
                            onDelete = { vm.deleteMeal(meal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentMealItem(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            Modifier
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

@SuppressLint("DefaultLocale")
private fun Float.round1(): String = String.format("%.1f", this)
