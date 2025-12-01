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
    var nutrition by remember { mutableStateOf(NutritionParser.NutritionData()) }
    var productName by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(ocrText) {
        if (ocrText.isNotBlank()) {
            nutrition = NutritionParser.parse(ocrText)

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
            productName = ""
        }
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

    val canSave = ocrText.isNotBlank() && nutrition.calories != null

    Scaffold(
        topBar = { AppTopBar("Scan Receipt", nav) }
    ) { padding ->
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

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Macros per serving",
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

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                            }
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
                                    mealType = "Meal", // потом можно дать выбор типа
                                    createdAt = System.currentTimeMillis(),
                                    portionGrams = null,
                                    photoPath = selectedImage?.toString(),
                                    notes = ocrText.ifBlank { null }
                                )

                                mealsVm.addMeal(meal)

                                selectedImage = null
                                ocrText = ""
                                productName = ""
                                nutrition = NutritionParser.NutritionData()

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
