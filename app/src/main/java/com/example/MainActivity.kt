package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.models.Group
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeadersScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.PartinoTheme
import com.example.utils.LanguageManager
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.GroupViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.LeadersViewModel
import com.example.viewmodel.ReportViewModel
import com.example.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PartinoApp()
        }
    }
}

@Composable
fun PartinoApp() {
    val authViewModel: AuthViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val reportViewModel: ReportViewModel = viewModel()
    val groupViewModel: GroupViewModel = viewModel()
    val leadersViewModel: LeadersViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    val currentLangIsPersian = language == "fa"
    val username = currentUser?.username ?: ""
    val displayName = currentUser?.displayName ?: username

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    PartinoTheme(darkTheme = isDarkMode) {
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            // 1. Splash Screen
            composable("splash") {
                SplashScreen(
                    onNavigateNext = {
                        if (authViewModel.isLoggedIn.value) {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                )
            }

            // 2. Login Screen
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    snackbarHostState = snackbarHostState,
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // 3. Main Screen with Bottom Navigation Bar
            composable("main") {
                MainContainer(
                    homeViewModel = homeViewModel,
                    reportViewModel = reportViewModel,
                    groupViewModel = groupViewModel,
                    leadersViewModel = leadersViewModel,
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    username = username,
                    displayName = displayName,
                    isPersian = currentLangIsPersian,
                    snackbarHostState = snackbarHostState,
                    onNavigateToTimer = { navController.navigate("timer") },
                    onNavigateToReport = { duration -> navController.navigate("report?duration=$duration") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToGroupDetail = { navController.navigate("group_detail") },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }

            // 4. Timer Screen
            composable("timer") {
                TimerScreen(
                    isPersian = currentLangIsPersian,
                    onRecordToReport = { minutes ->
                        navController.navigate("report?duration=$minutes")
                    }
                )
            }

            // 5. Report Screen
            composable(
                route = "report?duration={duration}",
                arguments = listOf(navArgument("duration") {
                    type = NavType.IntType
                    defaultValue = 0
                })
            ) { backStackEntry ->
                val duration = backStackEntry.arguments?.getInt("duration") ?: 0
                ReportScreen(
                    reportViewModel = reportViewModel,
                    username = username,
                    initialDurationMinutes = duration,
                    isPersian = currentLangIsPersian,
                    snackbarHostState = snackbarHostState,
                    onReportSubmitted = {
                        navController.popBackStack()
                    }
                )
            }

            // 6. History Screen
            composable("history") {
                HistoryScreen(
                    reportViewModel = reportViewModel,
                    username = username,
                    isPersian = currentLangIsPersian
                )
            }

            // 7. Group Detail Screen
            composable("group_detail") {
                val selectedGroup by groupViewModel.selectedGroup.collectAsState()
                selectedGroup?.let { group ->
                    GroupDetailScreen(
                        groupViewModel = groupViewModel,
                        group = group,
                        username = username,
                        isPersian = currentLangIsPersian,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContainer(
    homeViewModel: HomeViewModel,
    reportViewModel: ReportViewModel,
    groupViewModel: GroupViewModel,
    leadersViewModel: LeadersViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    username: String,
    displayName: String,
    isPersian: Boolean,
    snackbarHostState: SnackbarHostState,
    onNavigateToTimer: () -> Unit,
    onNavigateToReport: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToGroupDetail: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(LanguageManager.getString("home", isPersian)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // Groups Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_groups),
                            contentDescription = "Groups",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(LanguageManager.getString("groups", isPersian)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // Leaders Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_leaders),
                            contentDescription = "Leaders",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(LanguageManager.getString("leaders", isPersian)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // Settings Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(LanguageManager.getString("settings", isPersian)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> HomeScreen(
                homeViewModel = homeViewModel,
                username = username,
                displayName = displayName,
                isPersian = isPersian,
                onNavigateToTimer = onNavigateToTimer,
                onNavigateToReport = { onNavigateToReport(0) },
                onNavigateToHistory = onNavigateToHistory
            )
            1 -> GroupsScreen(
                groupViewModel = groupViewModel,
                username = username,
                isPersian = isPersian,
                onSelectGroup = { onNavigateToGroupDetail() }
            )
            2 -> LeadersScreen(
                leadersViewModel = leadersViewModel,
                isPersian = isPersian
            )
            3 -> SettingsScreen(
                settingsViewModel = settingsViewModel,
                username = username,
                displayName = displayName,
                isPersian = isPersian,
                snackbarHostState = snackbarHostState,
                onLogout = onLogout
            )
        }
    }
}
