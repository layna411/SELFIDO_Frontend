package com.simats.selfora.ui.components.form

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.GlassButton
import com.simats.selfora.ui.components.glass.GlassOutlinedButton
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * SELFORA GLOBAL FORM UX ENHANCEMENT SYSTEM
 * ============================================================================
 * Provides smooth scrolling, automatic bring-into-view for focused fields,
 * edge-to-edge IME keyboard padding, and focus navigation across the entire app.
 */

/**
 * 1. SELFORA SCROLLABLE FORM CONTAINER
 * A full-screen or nested scrollable container that automatically resizes with the
 * soft keyboard, applies IME insets, and enables smooth field-to-field scrolling.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelforaScrollableForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    includeTopPadding: Boolean = true,
    includeBottomPadding: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (includeTopPadding) Modifier.statusBarsPadding() else Modifier)
            .then(if (includeBottomPadding) Modifier.navigationBarsPadding() else Modifier)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * 2. SELFORA FORM CONTAINER (For forms inside Scaffolds with existing padding values)
 */
@Composable
fun SelforaFormContainer(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/**
 * 3. SELFORA FORM FIELD WRAPPER
 * Wraps any form element to automatically bring it into view when focused,
 * with label and animated validation error handling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelforaFormField(
    modifier: Modifier = Modifier,
    label: String? = null,
    isRequired: Boolean = false,
    helperText: String? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    content: @Composable () -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            delay(150)
            bringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(isError) {
        if (isError) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        if (!label.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaTextPrimary
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaError
                    )
                }
            }
        }

        content()

        AnimatedVisibility(
            visible = isError && !errorMessage.isNullOrBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                color = SelforaError,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            )
        }

        if (!isError && !helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                color = SelforaTextSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(start = 6.dp, top = 3.dp)
            )
        }
    }
}

/**
 * 4. SELFORA FORM ACTIONS
 * Bottom action area that safely accommodates the keyboard and guarantees that
 * primary/secondary buttons remain accessible, dismissing the keyboard on submit.
 */
@Composable
fun SelforaFormActions(
    submitText: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String? = null,
    onCancel: (() -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    primaryColor: Color = SelforaPrimary
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cancelText != null && onCancel != null) {
            GlassOutlinedButton(
                text = cancelText,
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onCancel()
                },
                accentColor = SelforaTextSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        GlassButton(
            text = submitText,
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onSubmit()
            },
            enabled = enabled && !isLoading,
            gradient = listOf(primaryColor, primaryColor.copy(alpha = 0.85f)),
            modifier = Modifier.weight(if (cancelText != null) 1.5f else 1f)
        )
    }
}

/**
 * 5. FOCUS NAVIGATION EXTENSIONS
 */
class SelforaFormFocusManager(
    private val focusRequesters: List<FocusRequester>
) {
    fun requestFirstInvalid(invalidIndex: Int) {
        if (invalidIndex in focusRequesters.indices) {
            focusRequesters[invalidIndex].requestFocus()
        }
    }
}

