package ca.gbc.comp3074.snapcal.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.nutrition.NutritionParser
import ca.gbc.comp3074.snapcal.ocr.OcrRecognizer
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.components.Nutriple
import ca.gbc.comp3074.snapcal.ui.state.MealsViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    nav: NavHostController? = null,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mealsVm: MealsViewModel = viewModel()

    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var ocrText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // What user sees and what gets saved after recalculation
    var nutrition by remember { mutableStateOf(NutritionParser.NutritionData()) }

    // Base parsed nutrition for the original label serving
    var baseNutrition by remember { mutableStateOf(NutritionParser.NutritionData()) }

    var productName by rememberSaveable { mutableStateOf("") }

    // Serving size detected from label (base reference)
    var detectedServingSizeGrams by remember { mutableStateOf<Float?>(null) }

    // Editable serving/portion field shown on screen
    var portionSizeGramsText by rememberSaveable { mutableStateOf("") }

    var showAdjustDialog by remember { mutableStateOf(false) }
    var editCalories by rememberSaveable { mutableStateOf("") }
    var editProtein by rememberSaveable { mutableStateOf("") }
    var editCarbs by rememberSaveable { mutableStateOf("") }
    var editFat by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(ocrText) {
        if (ocrText.isNotBlank()) {
            val parsedNutrition = NutritionParser.parse(ocrText)
            val parsedServingSize = extractServingSizeGrams(ocrText)

            baseNutrition = parsedNutrition
            detectedServingSizeGrams = parsedServingSize
            portionSizeGramsText = parsedServingSize?.toPrettyString().orEmpty()

            editCalories = parsedNutrition.calories?.toString().orEmpty()
            editProtein = parsedNutrition.protein?.toString().orEmpty()
            editCarbs = parsedNutrition.carbs?.toString().orEmpty()
            editFat = parsedNutrition.fat?.toString().orEmpty()

            nutrition = recalculateNutrition(
                baseNutrition = parsedNutrition,
                detectedServingSizeGrams = parsedServingSize,
                currentPortionSizeGrams = parsedServingSize
            )

            if (productName.isBlank()) {
                productName = ocrText
                    .lineSequence()
                    .firstOrNull()
                    ?.take(40)
                    ?.trim()
                    .orEmpty()
            }
        } else {
            nutrition = NutritionParser.NutritionData()
            baseNutrition = NutritionParser.NutritionData()
            detectedServingSizeGrams = null
            portionSizeGramsText = ""
            productName = ""
            editCalories = ""
            editProtein = ""
            editCarbs = ""
            editFat = ""
        }
    }

    LaunchedEffect(portionSizeGramsText, detectedServingSizeGrams, baseNutrition) {
        val currentPortionSize = portionSizeGramsText.toFloatOrNull()?.takeIf { it > 0f }

        nutrition = recalculateNutrition(
            baseNutrition = baseNutrition,
            detectedServingSizeGrams = detectedServingSizeGrams,
            currentPortionSizeGrams = currentPortionSize
        )
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImage = uri
            isLoading = true
            error = null
            ocrText = ""

            scope.launch {
                try {
                    ocrText = OcrRecognizer.recognizeTextFromUri(context, uri)
                } catch (e: Exception) {
                    error = e.localizedMessage ?: "Failed to run OCR"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val currentPortionGrams = portionSizeGramsText.toFloatOrNull()?.takeIf { it > 0f }
    val canSave = ocrText.isNotBlank() && nutrition.calories != null

    Scaffold(
        topBar = { AppTopBar("Scan Receipt", nav) }
    ) { padding ->

        if (showAdjustDialog) {
            AlertDialog(
                onDismissRequest = { showAdjustDialog = false },
                title = { Text("Adjust nutrition") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editCalories,
                            onValueChange = { editCalories = it.filter(Char::isDigit) },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editProtein,
                            onValueChange = { editProtein = it.filter(Char::isDigit) },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editCarbs,
                            onValueChange = { editCarbs = it.filter(Char::isDigit) },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editFat,
                            onValueChange = { editFat = it.filter(Char::isDigit) },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            baseNutrition = NutritionParser.NutritionData(
                                calories = editCalories.toIntOrNull(),
                                protein = editProtein.toIntOrNull(),
                                carbs = editCarbs.toIntOrNull(),
                                fat = editFat.toIntOrNull()
                            )

                            nutrition = recalculateNutrition(
                                baseNutrition = baseNutrition,
                                detectedServingSizeGrams = detectedServingSizeGrams,
                                currentPortionSizeGrams = portionSizeGramsText.toFloatOrNull()?.takeIf { it > 0f }
                            )

                            showAdjustDialog = false
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAdjustDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            CardBlock(title = "Receipt Snapshot") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF111827)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImage == null) {
                            Text(
                                "Tap \"Scan receipt\" to pick a photo from gallery",
                                color = Color(0xFF9CA3AF)
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImage),
                                contentDescription = "Selected receipt",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isLoading) "Running OCR..." else "Scan receipt")
                    }

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            CardBlock(title = "Recognized Text") {
                if (ocrText.isBlank() && !isLoading) {
                    Text(
                        "After scanning a receipt, recognized text will appear here. " +
                                "You can edit it before saving.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    OutlinedTextField(
                        value = ocrText,
                        onValueChange = { ocrText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        placeholder = { Text("Recognized text...") }
                    )
                }
            }

            CardBlock(title = "Parsed Details") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Product name") },
                        placeholder = { Text("e.g. Ground beef 85/15") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = portionSizeGramsText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            val firstDotIndex = filtered.indexOf('.')

                            portionSizeGramsText = if (firstDotIndex == -1) {
                                filtered
                            } else {
                                val beforeDot = filtered.substring(0, firstDotIndex + 1)
                                val afterDot = filtered.substring(firstDotIndex + 1).replace(".", "")
                                beforeDot + afterDot
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Portion size (grams)") },
                        placeholder = { Text("e.g. 30") },
                        singleLine = true,
                        supportingText = {
                            when {
                                detectedServingSizeGrams != null -> {
                                    Text(
                                        "Detected: ${detectedServingSizeGrams!!.toPrettyString()} g. " +
                                                "Adjust to match what you ate — macros update automatically."
                                    )
                                }
                                else -> {
                                    Text(
                                        "Adjust to match what you ate. Macros update automatically."
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Macros for selected portion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Nutriple(
                            "Calories",
                            nutrition.calories?.let { "$it kcal" } ?: "--"
                        )
                        Nutriple(
                            "Protein",
                            nutrition.protein?.let { "$it g" } ?: "--"
                        )
                        Nutriple(
                            "Carbs",
                            nutrition.carbs?.let { "$it g" } ?: "--"
                        )
                        Nutriple(
                            "Fat",
                            nutrition.fat?.let { "$it g" } ?: "--"
                        )
                    }

                    if (detectedServingSizeGrams != null) {
                        Text(
                            text = "Recalculated from ${detectedServingSizeGrams!!.toPrettyString()} g serving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showAdjustDialog = true },
                            enabled = ocrText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Adjust")
                        }

                        Button(
                            onClick = {
                                val meal = Meal(
                                    name = productName.ifBlank {
                                        ocrText.lineSequence().firstOrNull()
                                            ?.take(40)?.trim().orEmpty()
                                            .ifBlank { "Scanned item" }
                                    },
                                    calories = nutrition.calories ?: 0,
                                    protein = (nutrition.protein ?: 0).toFloat(),
                                    carbs = (nutrition.carbs ?: 0).toFloat(),
                                    fat = (nutrition.fat ?: 0).toFloat(),
                                    mealType = "Meal",
                                    createdAt = System.currentTimeMillis(),
                                    portionGrams = currentPortionGrams,
                                    photoPath = selectedImage?.toString(),
                                    notes = ocrText.ifBlank { null }
                                )

                                mealsVm.addMeal(meal)

                                selectedImage = null
                                ocrText = ""
                                productName = ""
                                nutrition = NutritionParser.NutritionData()
                                baseNutrition = NutritionParser.NutritionData()
                                detectedServingSizeGrams = null
                                portionSizeGramsText = ""
                                editCalories = ""
                                editProtein = ""
                                editCarbs = ""
                                editFat = ""
                                showAdjustDialog = false

                                onSaved()
                            },
                            enabled = canSave,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Save to log")
                        }
                    }
                }
            }
        }
    }
}

private fun recalculateNutrition(
    baseNutrition: NutritionParser.NutritionData,
    detectedServingSizeGrams: Float?,
    currentPortionSizeGrams: Float?
): NutritionParser.NutritionData {
    if (
        detectedServingSizeGrams == null ||
        currentPortionSizeGrams == null ||
        detectedServingSizeGrams <= 0f ||
        currentPortionSizeGrams <= 0f
    ) {
        return baseNutrition
    }

    val ratio = currentPortionSizeGrams / detectedServingSizeGrams

    return NutritionParser.NutritionData(
        calories = baseNutrition.calories?.let { (it * ratio).roundToInt() },
        protein = baseNutrition.protein?.let { (it * ratio).roundToInt() },
        carbs = baseNutrition.carbs?.let { (it * ratio).roundToInt() },
        fat = baseNutrition.fat?.let { (it * ratio).roundToInt() }
    )
}

private fun extractServingSizeGrams(text: String): Float? {
    val servingLineRegex = Regex(
        pattern = """(?im)^.*serving\s*size.*?(\d+(?:\.\d+)?)\s*g\b.*$""",
        option = RegexOption.IGNORE_CASE
    )

    val genericLineRegex = Regex(
        pattern = """(?im)^.*per\s+(\d+(?:\.\d+)?)\s*g\b.*$""",
        option = RegexOption.IGNORE_CASE
    )

    val directRegex = Regex(
        pattern = """(?i)\b(\d+(?:\.\d+)?)\s*g\b"""
    )

    val servingMatch = servingLineRegex.find(text)
        ?.groupValues?.getOrNull(1)
        ?.toFloatOrNull()
    if (servingMatch != null) return servingMatch

    val genericMatch = genericLineRegex.find(text)
        ?.groupValues?.getOrNull(1)
        ?.toFloatOrNull()
    if (genericMatch != null) return genericMatch

    return directRegex.find(text)
        ?.groupValues?.getOrNull(1)
        ?.toFloatOrNull()
}

private fun Float.toPrettyString(): String {
    return if (this % 1f == 0f) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}