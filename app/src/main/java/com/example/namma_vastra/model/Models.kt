package com.example.namma_vastra.model

import androidx.compose.ui.graphics.Color

data class TrendPattern(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String = "Ilkal"
)

data class TrendColor(
    val title: String,
    val color: Color
)

data class Saree(
    val id: String,
    val title: String,
    val description: String,
    val material: String,
    val priceEstimate: Double,
    val imageUrl: String,
    val weaverName: String,
    val weaverPhone: String
)

data class CalculationHistory(
    val id: String = "",
    val materialCost: Double,
    val laborCost: Double,
    val timeTaken: String,
    val profitMargin: Double,
    val suggestedPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)
