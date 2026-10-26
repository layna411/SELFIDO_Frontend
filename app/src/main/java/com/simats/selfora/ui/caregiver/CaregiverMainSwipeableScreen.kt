package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.simats.selfora.ui.theme.SelforaBackground
import kotlinx.coroutines.launch

data class ActivePracticeSessionInfo(val programId: Long, val activityCode: String)

@Composable
fun CaregiverMainSwipeableScreen(
    initialRoute: String = "caregiver_dashboard",
    onStartPractice: (Long, String) -> Unit = { _, _ -> },
    onNavigateToMessages: () -> Unit = {},
    onNavigateToNotifications: () -> Unit,
    onViewDetailedHistory: () -> Unit,
    onOpenChildMode: () -> Unit = {},
    onLogout: () -> Unit
) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) } // "messages", "practice", or null
    var currentPracticeInfo by remember { mutableStateOf<ActivePracticeSessionInfo?>(null) }

    val tabs = listOf(
        CaregiverTab.Home,
        CaregiverTab.History,
        CaregiverTab.Messages,
        CaregiverTab.Resources,
        CaregiverTab.Profile
    )
    val initialPageIndex = tabs.indexOfFirst { it.route == initialRoute }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val currentTabRoute = tabs[pagerState.currentPage].route

    if (activeSubScreen == "messages") {
        CaregiverMessagesScreen(
            onNavigateToTab = { activeSubScreen = null },
            onBack = { activeSubScreen = null }
        )
    } else if (activeSubScreen == "practice" && currentPracticeInfo != null) {
        HomePracticeScreen(
            programId = currentPracticeInfo!!.programId,
            activityCode = currentPracticeInfo!!.activityCode,
            onPracticeCompleted = { activeSubScreen = null },
            onBack = { activeSubScreen = null }
        )
    } else if (activeSubScreen == "history") {
        CaregiverHistoryScreen(
            onBack = { activeSubScreen = null }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SelforaBackground)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    CaregiverTab.Home -> CaregiverDashboardScreen(
                        onStartPractice = { progId, actCode ->
                            currentPracticeInfo = ActivePracticeSessionInfo(progId, actCode)
                            activeSubScreen = "practice"
                        },
                        onNavigateToTab = { targetRoute ->
                            if (targetRoute == "caregiver_messages") {
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            } else if (targetRoute == "caregiver_history") {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            } else if (targetRoute == "caregiver_resources") {
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
                            } else {
                                val idx = tabs.indexOfFirst { it.route == targetRoute }
                                if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                            }
                        },
                        onNavigateToMessages = {
                            coroutineScope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToProfile = {
                            coroutineScope.launch { pagerState.animateScrollToPage(4) }
                        },
                        onOpenChildMode = onOpenChildMode,
                        onLogout = onLogout
                    )

                    CaregiverTab.History -> CaregiverHistoryScreen(
                        onBack = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )

                    CaregiverTab.Messages -> CaregiverMessagesScreen(
                        onNavigateToTab = { targetRoute ->
                            val idx = tabs.indexOfFirst { it.route == targetRoute }
                            if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                        },
                        onBack = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )

                    CaregiverTab.Resources -> CaregiverResourcesScreen(
                        onBack = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )

                    CaregiverTab.Profile -> CaregiverProfileScreen(
                        onLogout = onLogout,
                        onBack = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )

                    else -> {}
                }
            }

            CaregiverBottomNavigation(
                currentRoute = currentTabRoute,
                onTabSelected = { selectedRoute ->
                    val pageIndex = tabs.indexOfFirst { it.route == selectedRoute }
                    if (pageIndex >= 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}
