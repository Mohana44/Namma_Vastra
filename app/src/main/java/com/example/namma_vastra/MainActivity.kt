package com.example.namma_vastra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namma_vastra.navigation.Screen
import com.example.namma_vastra.navigation.bottomNavItems
import com.example.namma_vastra.ui.screens.*
import com.example.namma_vastra.ui.screens.HomeScreen
import com.example.namma_vastra.ui.theme.NammaVastraTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NammaVastraTheme {
                MainNavigationHost()
            }
        }
    }
}

@Composable
fun MainNavigationHost() {
    val auth = FirebaseAuth.getInstance()
    // Sign out every time the app starts to make login mandatory
    LaunchedEffect(Unit) {
        auth.signOut()
    }
    var authState by remember { mutableStateOf("signin") }

    Crossfade(targetState = authState, label = "auth_fade") { state ->
        when (state) {
            "signin" -> SignInScreen(
                onSignInSuccess = { authState = "app" },
                onNavigateToSignUp = { authState = "signup" }
            )
            "signup" -> SignUpScreen(
                onNavigateToSignIn = { authState = "signin" }
            )
            "app" -> MainApp(onLogout = { authState = "signin" })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Namma Vastra",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                bottomNavItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.label) },
                        selected = currentScreen == screen,
                        onClick = {
                            currentScreen = screen
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Namma Vastra",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 24.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            // Removed BottomBar as requested
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = Color.Transparent
            ) {
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        Screen.Home -> HomeScreen()
                        Screen.Trends -> TrendsScreen()
                        Screen.Gallery -> GalleryScreen()
                        Screen.Pricing -> CalculatorScreen()
                        Screen.Story -> StoryScreen()
                        Screen.Profile -> ProfileScreen()
                    }
                }
            }
        }
    }
}
