package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.simats.selfora.ui.components.glass.GlassNavigationBar
import com.simats.selfora.ui.components.glass.TherapistTab
import com.simats.selfora.ui.theme.SelforaBackground
import kotlinx.coroutines.launch

@Composable
fun TherapistMainSwipeableScreen(
    initialTab: TherapistTab = TherapistTab.DASHBOARD,
    onNavigateToAddChildWorkflow: () -> Unit,
    onNavigateToChildProfile: (Long) -> Unit,
    onNavigateToAssessment: (Long) -> Unit,
    onNavigateToHomePrograms: (Long) -> Unit,
    onNavigateToProgress: (Long) -> Unit,
    onNavigateToMessages: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val tabs = TherapistTab.values()
    val initialPageIndex = tabs.indexOf(initialTab).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

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
                TherapistTab.DASHBOARD -> TherapistDashboardScreen(
                    onNavigateToAddChildWorkflow = onNavigateToAddChildWorkflow,
                    onNavigateToChildren = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    },
                    onNavigateToAssessment = {
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    },
                    onNavigateToHomePrograms = { onNavigateToHomePrograms(1L) },
                    onNavigateToProgress = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onNavigateToMessages = {
                        coroutineScope.launch { pagerState.animateScrollToPage(4) }
                    },
                    onNavigateToNotifications = onNavigateToNotifications,
                    onLogout = onLogout
                )

                TherapistTab.CHILDREN -> ChildListScreen(
                    onChildSelected = onNavigateToChildProfile,
                    onAddChildClick = onNavigateToAddChildWorkflow,
                    onBack = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )

                TherapistTab.ASSESSMENTS -> AssessmentScreen(
                    childId = 1L,
                    onAssessmentSaved = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onBack = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )

                TherapistTab.PROGRESS -> TherapistProgressScreen(
                    childId = 1L,
                    onBack = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )

                TherapistTab.MESSAGES -> MessageScreen(
                    childId = 1L,
                    onBack = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }

        GlassNavigationBar(
            currentTab = tabs[pagerState.currentPage],
            onTabSelected = { selectedTab ->
                val pageIndex = tabs.indexOf(selectedTab)
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pageIndex)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
