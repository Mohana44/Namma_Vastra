package com.example.namma_vastra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namma_vastra.model.CalculationHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun CalculatorScreen() {
    var materialCost by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var timeTaken by remember { mutableStateOf("") }
    var profitMargin by remember { mutableStateOf("20") }
    var suggestedPrice by remember { mutableStateOf<Double?>(null) }
    
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val history = remember { mutableStateListOf<CalculationHistory>() }

    // Fetch History
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("calculations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) return@addSnapshotListener
                    if (value != null) {
                        history.clear()
                        for (doc in value.documents) {
                            val calc = CalculationHistory(
                                id = doc.id,
                                materialCost = doc.getDouble("materialCost") ?: 0.0,
                                laborCost = doc.getDouble("laborCost") ?: 0.0,
                                timeTaken = doc.getString("timeTaken") ?: "",
                                profitMargin = doc.getDouble("profitMargin") ?: 0.0,
                                suggestedPrice = doc.getDouble("suggestedPrice") ?: 0.0,
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                            history.add(calc)
                        }
                    }
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Fair Price Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Text("Calculate the ideal retail price for your work to ensure fair compensation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CalculatorInput("Raw Material Cost (₹)", materialCost, "e.g., 2500") { materialCost = it }
                    CalculatorInput("Total Labor Cost (₹)", laborCost, "e.g., 1500") { laborCost = it }
                    CalculatorInput("Time Taken (Days)", timeTaken, "e.g., 5") { timeTaken = it }
                    CalculatorInput("Desired Profit Margin (%)", profitMargin, "20") { profitMargin = it }

                    Button(
                        onClick = {
                            val material = materialCost.toDoubleOrNull() ?: 0.0
                            val labor = laborCost.toDoubleOrNull() ?: 0.0
                            val margin = profitMargin.toDoubleOrNull() ?: 0.0
                            val totalCost = material + labor
                            val price = totalCost + (totalCost * (margin / 100))
                            suggestedPrice = price
                            
                            // Save to Firebase
                            if (userId.isNotEmpty()) {
                                val data = hashMapOf(
                                    "materialCost" to material,
                                    "laborCost" to labor,
                                    "timeTaken" to timeTaken,
                                    "profitMargin" to margin,
                                    "suggestedPrice" to price,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("users").document(userId).collection("calculations").add(data)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Calculate Fair Price", fontWeight = FontWeight.Bold)
                    }

                    suggestedPrice?.let { price ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Suggested Retail Price", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                            Text("₹${String.format("%.2f", price)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                    }
                }
            }
        }

        if (history.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                    Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            items(history) { calc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Material + Labor", style = MaterialTheme.typography.labelSmall)
                            Text("₹${calc.materialCost + calc.laborCost}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Final Price", style = MaterialTheme.typography.labelSmall)
                            Text("₹${String.format("%.2f", calc.suggestedPrice)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CalculatorInput(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}
