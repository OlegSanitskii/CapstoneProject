package ca.gbc.comp3074.snapcal.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel

@Composable
fun DashboardScreen(
    onScan: () -> Unit,
    onManual: () -> Unit,
    onProgress: () -> Unit,
    onSignOut: () -> Unit,
) {
    val authVM: AuthViewModel = viewModel()
    val email by authVM.userEmail.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Dashboard") }

        item {
            CardBlock(title = "Calories (in vs out)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = 14.dp.toPx()
                        drawArc(
                            color = Color(0xFFE5E5E5),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = Color(0xFF4A90E2),
                            startAngle = -90f,
                            sweepAngle = 234f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("In: 1,650 kcal   Out: 700 kcal")
                        Text("Balance: –950 kcal", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(12.dp)
                                    .background(Color(0xFF4A90E2), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(6.dp)); Text("Consumed", color = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Box(
                                Modifier.size(12.dp)
                                    .background(Color(0xFFE5E5E5), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(6.dp)); Text("Remaining", color = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CardBlock(modifier = Modifier.weight(1f), title = "Steps") {
                    Text("6,842", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Goal: 8,000", color = Color.Gray)
                }
                CardBlock(modifier = Modifier.weight(1f), title = "Workouts") {
                    Text("Cycling 45 min", fontWeight = FontWeight.SemiBold)
                    Text("Burned: 520 kcal", color = Color.Gray)
                }
            }
        }

        item {
            CardBlock(title = "Quick Actions") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onScan, modifier = Modifier.weight(1f)) { Text("Scan Label") }
                    OutlinedButton(onManual, modifier = Modifier.weight(1f)) { Text("Log Meal") }
                    OutlinedButton(onProgress, modifier = Modifier.weight(1f)) { Text("Progress") }
                }
            }
        }

        // --- Account / Sign out ---
        item {
            CardBlock(title = "Account") {
                if (email.isNotBlank()) {
                    Text("Signed in as: $email", color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                }
                Button(
                    onClick = {
                        authVM.signOut {
                            onSignOut()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}
