package com.example.namma_vastra.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.namma_vastra.model.Saree
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.net.URLEncoder
import java.util.*

@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    val db = FirebaseFirestore.getInstance()
    val sarees = remember { mutableStateListOf<Saree>() }

    // Fetch Sarees from Firebase Firestore in Real-time
    LaunchedEffect(Unit) {
        db.collection("sarees").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            if (value != null) {
                sarees.clear()
                for (doc in value.documents) {
                    val saree = Saree(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        material = doc.getString("material") ?: "",
                        priceEstimate = doc.getDouble("price") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        weaverName = doc.getString("weaverName") ?: "",
                        weaverPhone = doc.getString("weaverPhone") ?: ""
                    )
                    sarees.add(saree)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (sarees.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("Gallery is empty", color = Color.Gray)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Loom Gallery", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        db.collection("sarees").get().addOnSuccessListener { snapshot ->
                            for (doc in snapshot.documents) { doc.reference.delete() }
                        }
                    }) {
                        Text("Clear All", color = Color.Red, fontSize = 12.sp)
                    }
                    FloatingActionButton(onClick = { showDialog = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
            }

            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(top = 16.dp)) {
                items(sarees) { saree -> SareeCard(saree) }
            }
        }

        if (isUploading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    if (showDialog) {
        AddSareeDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, material, price, phone, desc, uri ->
                isUploading = true
                uploadSaree(title, material, price, phone, desc, uri) { success ->
                    isUploading = false
                    showDialog = false
                    if (success) Toast.makeText(context, "Saree Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// Function to handle Firebase Storage and Firestore
fun uploadSaree(title: String, mat: String, price: String, phone: String, desc: String, uri: Uri?, onComplete: (Boolean) -> Unit) {
    if (uri == null) { onComplete(false); return }
    
    val user = FirebaseAuth.getInstance().currentUser
    val weaverName = user?.displayName ?: user?.email?.split("@")?.get(0) ?: "Artisan"
    
    val storageRef = FirebaseStorage.getInstance().reference.child("sarees/${UUID.randomUUID()}")
    storageRef.putFile(uri).continueWithTask { task ->
        if (!task.isSuccessful) task.exception?.let { throw it }
        storageRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val data = hashMapOf(
                "title" to title,
                "material" to mat,
                "price" to (price.toDoubleOrNull() ?: 0.0),
                "weaverPhone" to phone,
                "description" to desc,
                "imageUrl" to task.result.toString(),
                "weaverName" to weaverName,
                "userId" to (user?.uid ?: "")
            )
            FirebaseFirestore.getInstance().collection("sarees").add(data)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            onComplete(false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSareeDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String, Uri?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Saree to Gallery") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        label = { Text("WhatsApp Number") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("10 digits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (imageUri == null) "Select Image" else "Image Selected ✓")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotEmpty() && imageUri != null && phone.length == 10, 
                onClick = { 
                    val fullPhone = countryCode + phone
                    onAdd(title, material, price, fullPhone, description, imageUri) 
                }
            ) { Text("Upload") }
        }
    )
}

@Composable
fun SareeCard(saree: Saree) {
    val context = LocalContext.current
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column {
            AsyncImage(model = saree.imageUrl, contentDescription = null, modifier = Modifier.height(120.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(saree.title, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("by ${saree.weaverName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("₹${saree.priceEstimate}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    val url = "https://wa.me/${saree.weaverPhone}?text=Interested in ${saree.title}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }, modifier = Modifier.fillMaxWidth().height(32.dp), contentPadding = PaddingValues(0.dp)) {
                    Text("Inquire", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
