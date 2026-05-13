package com.example.namma_vastra.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.namma_vastra.model.TrendColor
import com.example.namma_vastra.model.TrendPattern
import com.example.namma_vastra.ui.theme.PastelPeach
import com.example.namma_vastra.ui.theme.Lavender
import com.example.namma_vastra.ui.theme.MintGreen
import com.example.namma_vastra.ui.theme.DustyPink
import com.example.namma_vastra.ui.theme.PowderBlue
import com.example.namma_vastra.ui.theme.PeachPink
import com.example.namma_vastra.ui.theme.Lilac
import com.example.namma_vastra.ui.theme.Cream
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendsScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentMonth = remember { SimpleDateFormat("MMMM", Locale.ENGLISH).format(Date()) }
    
    var ilkalPatterns by remember { mutableStateOf<List<TrendPattern>>(emptyList()) }
    var molakalmuruPatterns by remember { mutableStateOf<List<TrendPattern>>(emptyList()) }
    var trendingColors by remember { mutableStateOf<List<TrendColor>>(emptyList()) }
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(currentMonth) {
        db.collection("trends").whereEqualTo("month", currentMonth)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    val all = value.documents.map { doc ->
                        TrendPattern(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            category = doc.getString("category") ?: "Ilkal"
                        )
                    }
                    ilkalPatterns = all.filter { it.category == "Ilkal" }
                    molakalmuruPatterns = all.filter { it.category == "Molakalmuru" }
                }
            }

        db.collection("trend_colors").addSnapshotListener { value, _ ->
            if (value != null) {
                trendingColors = value.documents.mapNotNull { doc ->
                    try {
                        val hex = doc.getString("hex") ?: "#FF0000"
                        TrendColor(doc.getString("title") ?: "", Color(android.graphics.Color.parseColor(hex)))
                    } catch (e: Exception) { null }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { StylishHeader(currentMonth) }

        if (ilkalPatterns.isEmpty() && molakalmuruPatterns.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Button(onClick = { 
                        isSyncing = true
                        syncCustomUserTrends(db, currentMonth) { isSyncing = false }
                    }, shape = RoundedCornerShape(12.dp)) {
                        if (isSyncing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("SYNC MY CUSTOM SAREES")
                    }
                }
            }
        } else {
            item { TrendCategorySection("Ilkal Masterpieces", ilkalPatterns, trendingColors.take(4), MaterialTheme.colorScheme.primary) }
            item { TrendCategorySection("Molakalmuru Traditions", molakalmuruPatterns, trendingColors.takeLast(4), Color(0xFF673AB7)) }
            
            item {
                TextButton(
                    onClick = { syncCustomUserTrends(db, currentMonth) {} },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh Data", color = Color.Gray)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

private fun syncCustomUserTrends(db: FirebaseFirestore, month: String, onComplete: () -> Unit) {
    val batch = db.batch()
    val patterns = listOf(
        // Ilkal Section
        mapOf("title" to "Tope Teni Pallu", "description" to "Classic silk loop-join technique for a rich pallu.", "category" to "Ilkal", "month" to month, "imageUrl" to "top_silk_pallu"),
        mapOf("title" to "Chokki Paras Border", "description" to "The iconic broad border with distinct geometry.", "category" to "Ilkal", "month" to month, "imageUrl" to "chikki_paras_border"),
        mapOf("title" to "Chequered Patterns", "description" to "Traditional Gadi or Kaddi checks in the body.", "category" to "Ilkal", "month" to month, "imageUrl" to "chequered_patterns"),
        mapOf("title" to "Kasuti Embroidery", "description" to "Exquisite hand-stitched folk embroidery details.", "category" to "Ilkal", "month" to month, "imageUrl" to "kasuti_embroidery"),

        // Molkalmuru Section
        mapOf("title" to "Temple Borders", "description" to "Distinctive sharp-edged temple motifs in silk.", "category" to "Molakalmuru", "month" to month, "imageUrl" to "korvai_borders"),
        mapOf("title" to "Silk Butti Work", "description" to "Elegant floral and geometric buttas on the body.", "category" to "Molakalmuru", "month" to month, "imageUrl" to "rudrakshi_motifs"),
        mapOf("title" to "Peacock Motifs", "description" to "Traditional hand-woven peacock designs.", "category" to "Molakalmuru", "month" to month, "imageUrl" to "peacock"),
        mapOf("title" to "Premium Silk Pattern", "description" to "Cotton silk blend with contemporary motifs.", "category" to "Molakalmuru", "month" to month, "imageUrl" to "starplus_pattern_cottonsilk")
    )
    val colors = listOf(
        mapOf("title" to "Pastel Peach", "hex" to "#FFDAB9"),
        mapOf("title" to "Lavender", "hex" to "#E6E6FA"),
        mapOf("title" to "Mint Green", "hex" to "#98FF98"),
        mapOf("title" to "Dusty Pink", "hex" to "#DCAE96"),
        mapOf("title" to "Powder Blue", "hex" to "#B0E0E6"),
        mapOf("title" to "Peach Pink", "hex" to "#FDB9C8"),
        mapOf("title" to "Lilac", "hex" to "#C8A2C8"),
        mapOf("title" to "Cream", "hex" to "#FFFDD0")
    )

    db.collection("trends").get().addOnSuccessListener { tSnap ->
        db.collection("trend_colors").get().addOnSuccessListener { cSnap ->
            tSnap.forEach { batch.delete(it.reference) }
            cSnap.forEach { batch.delete(it.reference) }
            patterns.forEach { batch.set(db.collection("trends").document(), it) }
            colors.forEach { batch.set(db.collection("trend_colors").document(), it) }
            batch.commit().addOnSuccessListener { onComplete() }
        }
    }
}

@Composable
fun StylishHeader(month: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text("Trend Board", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
            Text("$month's high-demand patterns for weavers.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun TrendCategorySection(title: String, patterns: List<TrendPattern>, colors: List<TrendColor>, accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Clean, Professional Header without symbols or bars
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Always visible horizontal scroll (Traditional View)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            patterns.forEach { StylishPatternCard(it) }
        }

        // Professional Palette Section
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, Color.LightGray.copy(0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Trending Palette:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { ColorChip(it.color) }
                }
            }
        }
    }
}

@Composable
fun StylishPatternCard(pattern: TrendPattern) {
    val context = LocalContext.current
    val imageRes = context.resources.getIdentifier(pattern.imageUrl, "drawable", context.packageName)
    
    Card(
        modifier = Modifier.width(260.dp).height(320.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (imageRes != 0) {
                AsyncImage(
                    model = imageRes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Text("Add your image to drawable!", fontSize = 10.sp)
                }
            }

            // Gradient Overlay for Text
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 300f)))

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(pattern.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(pattern.description, color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

@Composable
fun ColorChip(color: Color) {
    Box(Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White, CircleShape))
}
