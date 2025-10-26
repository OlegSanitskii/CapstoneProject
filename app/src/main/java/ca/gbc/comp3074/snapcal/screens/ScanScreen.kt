package ca.gbc.comp3074.snapcal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.components.Nutriple

@Composable
fun ScanScreen(onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Scan Product / Receipt")
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.fillMaxWidth().height(220.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.75f)
                        .clip(RoundedCornerShape(8.dp)).background(Color(0xFF1F2937)),
                    contentAlignment = Alignment.Center
                ) { Text("Align label inside the frame\nGood lighting improves accuracy", color = Color(0xFFCBD5E1)) }
            }
            Button(onClick = { /* simulate capture */ }) {
                Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Capture")
            }
            CardBlock(title = "Detected Item") {
                Text("Greek Yogurt 2% (170 g)")
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Nutriple("Calories", "130 kcal"); Nutriple("Protein", "12 g")
                    Nutriple("Carbs", "5 g"); Nutriple("Fat", "4 g")
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { }) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(6.dp)); Text("Edit") }
                    Button(onClick = onSaved) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("Save") }
                }
            }
        }
    }
}
