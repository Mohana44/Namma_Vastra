package com.example.namma_vastra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
            .verticalScroll(rememberScrollState())
    ) {
        // Professional Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "THE ART OF WEAVING",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A Legacy in Every Thread",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ProfessionalStorySection(
                title = "The Ilkal Heritage",
                content = "Tracing its roots back to the 8th century, the Ilkal saree is a masterpiece of North Karnataka. It is world-renowned for its 'Tope Teni' technique, where the body and the pallu are woven separately and then joined with a series of loops—a process that requires immense skill and patience. The vibrant 'Kasuti' embroidery often found on these sarees reflects the geometric precision and cultural richness of the Deccan plateau."
            )

            ProfessionalStorySection(
                title = "Molakalmuru Elegance",
                content = "Molakalmuru, often referred to as the silk hub of Karnataka, produces sarees that rival the finest in the world. Characterized by intricate 'Korvai' borders and temple motifs, these sarees are woven with high-quality mulberry silk. The weavers of Molakalmuru have preserved ancient designs like the 'Rudrakshi' and 'Peacock' motifs, ensuring that each piece is not just a garment, but a historical artifact."
            )

            ProfessionalStorySection(
                title = "The Namma Vastra Vision",
                content = "Namma Vastra was founded on a singular commitment: to honor the artisan. By eliminating traditional barriers, we connect the master weavers of Karnataka directly with global boutiques and designers. Our platform ensures that these ancient weaving traditions don't just survive in museums, but thrive in the modern wardrobe through fair trade and digital empowerment."
            )
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ProfessionalStorySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        HorizontalDivider(
            modifier = Modifier.width(60.dp),
            thickness = 3.dp,
            color = Color(0xFF3F51B5).copy(alpha = 0.4f)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = Color(0xFF455A64),
            textAlign = TextAlign.Justify
        )
    }
}
