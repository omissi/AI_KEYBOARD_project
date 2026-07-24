package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screens.AnalyticsScreen
import com.example.screens.PlaygroundScreen
import com.example.screens.SettingsScreen
import com.example.screens.ThemeStoreScreen
import com.example.screens.KeyboardSetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.KeyboardViewModel
import com.example.viewmodel.KeyboardViewModelFactory

enum class ScreenState { SPLASH, SETUP, MAIN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current
                val viewModel: KeyboardViewModel = viewModel(factory = KeyboardViewModelFactory(context))
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }

                when (currentScreen) {
                    ScreenState.SPLASH -> {
                        SplashScreen(onSplashFinished = {
                            val isEnabled = com.example.screens.isKeyboardEnabled(context)
                            val isSelected = com.example.screens.isKeyboardSelected(context)
                            currentScreen = if (isEnabled && isSelected) ScreenState.MAIN else ScreenState.SETUP
                        })
                    }
                    ScreenState.SETUP -> {
                        KeyboardSetupScreen(viewModel = viewModel, onBack = { currentScreen = ScreenState.MAIN })
                    }
                    ScreenState.MAIN -> {
                        MainScaffold(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onNavigateToSetup = { currentScreen = ScreenState.SETUP }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val duration = 1600f
        val interval = 25f
        val steps = (duration / interval).toInt()
        for (i in 1..steps) {
            kotlinx.coroutines.delay(interval.toLong())
            progress = i.toFloat() / steps
        }
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(400.dp).background(
                Brush.radialGradient(colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.12f), Color.Transparent))
            )
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.07f))
                    .border(BorderStroke(1.dp, Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.02f)))), shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape))
                Icon(imageVector = Icons.Default.Keyboard, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "AI KEYBOARD", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "الجيل الجديد من الكتابة الذكية والأدوات السريعة", fontSize = 11.sp, fontWeight = FontWeight.Light, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(56.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(200.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = Color(0xFF60A5FA),
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${(progress * 100).toInt()}%", color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "تطوير وإشراف", fontSize = 9.sp, color = Color.White.copy(alpha = 0.35f), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = "Alomessi Tech", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF60A5FA), letterSpacing = 0.5.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: KeyboardViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val selectedFontState by viewModel.selectedFontFamily.collectAsState()

    val fontFamily = when (selectedFontState) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Elegant" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }

    val tabTitles = listOf("لوحة تجربة الكتابة", "متجر ثيمات الكيبورد", "تقارير التحليل والإنتاجية", "الأدوات ومفكرة المهام")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "AI KEYBOARD", fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = fontFamily, color = MaterialTheme.colorScheme.primary)
                        Text(text = tabTitles[selectedTab], fontSize = 11.sp, fontFamily = fontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("dark_mode_toggle_btn")) {
                        Icon(imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle Night Mode", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                navigationIcon = {
                    Row(modifier = Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "محمي E2EE", fontSize = 9.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), tonalElevation = 8.dp) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Keyboard, contentDescription = "Playground") }, label = { Text("الكتابة", fontFamily = fontFamily, fontSize = 11.sp) }, modifier = Modifier.testTag("nav_playground"))
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Palette, contentDescription = "Themes Store") }, label = { Text("الثيمات", fontFamily = fontFamily, fontSize = 11.sp) }, modifier = Modifier.testTag("nav_themes"))
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") }, label = { Text("الإنتاجية", fontFamily = fontFamily, fontSize = 11.sp) }, modifier = Modifier.testTag("nav_analytics"))
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Settings, contentDescription = "Settings & Shortcuts") }, label = { Text("الأدوات", fontFamily = fontFamily, fontSize = 11.sp) }, modifier = Modifier.testTag("nav_settings"))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> PlaygroundScreen(viewModel = viewModel, onNavigateToSettings = { selectedTab = 3 }, onNavigateToSetup = onNavigateToSetup)
                1 -> ThemeStoreScreen(viewModel = viewModel)
                2 -> AnalyticsScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel, onNavigateToSetup = onNavigateToSetup)
            }
        }
    }
}
