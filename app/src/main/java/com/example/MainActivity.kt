package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TransitNavy
import com.example.ui.theme.TransitTeal
import com.example.viewmodel.GoWeaveViewModel

enum class MainNavTab(val label: String, val icon: ImageVector) {
    PLAN("Plan", Icons.Default.DirectionsTransit),
    TRACKER("Live Map", Icons.Default.LocationOn),
    ROUTES("Routes", Icons.Default.Route),
    NETWORK("Custom Area", Icons.Default.EditLocationAlt),
    COMMUTE("Commute", Icons.Default.Person)
}

enum class ScreenState {
    MAIN_TABS,
    JOURNEY_DETAIL,
    UNIFIED_CHECKOUT,
    BOOKING_CONFIRMATION,
    AI_ASSISTANT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GoWeaveApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoWeaveApp(viewModel: GoWeaveViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(MainNavTab.PLAN) }
    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_TABS) }

    // Toast handler
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Hardware Back Button Handling
    BackHandler(enabled = currentScreen != ScreenState.MAIN_TABS) {
        when (currentScreen) {
            ScreenState.JOURNEY_DETAIL -> currentScreen = ScreenState.MAIN_TABS
            ScreenState.UNIFIED_CHECKOUT -> currentScreen = ScreenState.JOURNEY_DETAIL
            ScreenState.AI_ASSISTANT -> currentScreen = ScreenState.MAIN_TABS
            ScreenState.BOOKING_CONFIRMATION -> {
                currentScreen = ScreenState.MAIN_TABS
                selectedTab = MainNavTab.COMMUTE
            }
            ScreenState.MAIN_TABS -> { /* Default exit */ }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentScreen == ScreenState.MAIN_TABS) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    MainNavTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) TransitNavy else Color(0xFF94A3B8)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TransitNavy else Color(0xFF64748B)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TransitTeal.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenState.MAIN_TABS -> {
                    when (selectedTab) {
                        MainNavTab.PLAN -> {
                            HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateToResults = {
                                    selectedTab = MainNavTab.ROUTES
                                },
                                onNavigateToAi = {
                                    currentScreen = ScreenState.AI_ASSISTANT
                                }
                            )
                        }
                        MainNavTab.TRACKER -> {
                            LiveVehicleTrackerScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBookVehicle = { veh ->
                                    selectedTab = MainNavTab.ROUTES
                                }
                            )
                        }
                        MainNavTab.ROUTES -> {
                            RouteResultsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = { selectedTab = MainNavTab.PLAN },
                                onViewJourneyDetail = { currentScreen = ScreenState.JOURNEY_DETAIL },
                                onProceedToCheckout = { currentScreen = ScreenState.UNIFIED_CHECKOUT }
                            )
                        }
                        MainNavTab.NETWORK -> {
                            CustomNetworkManagerScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        MainNavTab.COMMUTE -> {
                            UserDashboardScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onPlanSavedTrip = {
                                    selectedTab = MainNavTab.ROUTES
                                },
                                onViewTicketDetails = {
                                    currentScreen = ScreenState.BOOKING_CONFIRMATION
                                }
                            )
                        }
                    }
                }

                ScreenState.AI_ASSISTANT -> {
                    AiCommuteAssistantScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = ScreenState.MAIN_TABS }
                    )
                }

                ScreenState.JOURNEY_DETAIL -> {
                    JourneyDetailScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = ScreenState.MAIN_TABS },
                        onProceedToCheckout = { currentScreen = ScreenState.UNIFIED_CHECKOUT }
                    )
                }

                ScreenState.UNIFIED_CHECKOUT -> {
                    UnifiedCheckoutScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = ScreenState.JOURNEY_DETAIL },
                        onBookingSuccess = { currentScreen = ScreenState.BOOKING_CONFIRMATION }
                    )
                }

                ScreenState.BOOKING_CONFIRMATION -> {
                    BookingConfirmationScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onStartNavigation = {
                            currentScreen = ScreenState.JOURNEY_DETAIL
                        },
                        onGoHome = {
                            currentScreen = ScreenState.MAIN_TABS
                            selectedTab = MainNavTab.PLAN
                        }
                    )
                }
            }
        }
    }
}
