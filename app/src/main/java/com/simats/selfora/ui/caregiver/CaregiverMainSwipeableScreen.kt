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

@Composable
fun CaregiverMainSwipeableScreen(
    initialRoute: String = "caregiver_dashboard",
    onStartPractice: (Long, String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onViewDetailedHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val tabs = listOf(
        CaregiverTab.Home,
        CaregiverTab.Programme,
        CaregiverTab.Progress,
        CaregiverTab.Messages,
        CaregiverTab.Profile
    )
    val initialPageIndex = tabs.indexOfFirst { it.route == initialRoute }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val currentTabRoute = tabs[pagerState.currentPage].route

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
                    onStartPractice = onStartPractice,
                    onNavigateToTab = { targetRoute ->
                        val idx = tabs.indexOfFirst { it.route == targetRoute }
                        if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                    },
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToProfile = {
                        coroutineScope.launch { pagerState.animateScrollToPage(4) }
                    },
                    onLogout = onLogout
                )

                CaregiverTab.Programme -> HomeProgrammeScreen(
                    onStartPractice = onStartPractice,
                    onNavigateToTab = { targetRoute ->
                        val idx = tabs.indexOfFirst { it.route == targetRoute }
                        if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                    }
                )

                CaregiverTab.Progress -> CaregiverProgressScreen(
                    onNavigateToHistory = onViewDetailedHistory,
                    onNavigateToTab = { targetRoute ->
                        val idx = tabs.indexOfFirst { it.route == targetRoute }
                        if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                    }
                )

                CaregiverTab.Messages -> CaregiverMessagesScreen(
                    onNavigateToTab = { targetRoute ->
                        val idx = tabs.indexOfFirst { it.route == targetRoute }
                        if (idx >= 0) coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                    }
                )

                CaregiverTab.Profile -> CaregiverProfileScreen(
                    onLogout = onLogout,
                    onBack = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
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
