package com.simats.selfora.ui.components.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.PathMeasure
import com.simats.selfora.ui.components.glass.GlassCard

// ============================================================================
// 1. CIRCULAR PROGRESS RING
// ============================================================================
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = Color(0xFF2563EB),
    trackColor: Color = ringColor.copy(alpha = 0.15f),
    strokeWidth: Dp = 8.dp,
    size: Dp = 72.dp,
    showPercentageText: Boolean = true,
    centerText: String? = null,
    subText: String? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "circularProgressAnimation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textToDisplay = centerText ?: "${(animatedProgress * 100).toInt()}%"
            Text(
                text = textToDisplay,
                fontSize = if (size < 60.dp) 11.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF172033)
            )
            if (subText != null && size >= 70.dp) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// 2. ADL PROGRESS CARD (With Standardized ADL Visual Identity)
// ============================================================================
@Composable
fun ADLProgressCard(
    category: String?,
    percentage: Int,
    sessionCount: Int? = null,
    trendDelta: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val safeCategory = category ?: "ADL Skill"
    val (accentColor, icon, bgGradient) = getADLCategoryVisuals(safeCategory)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(bgGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = safeCategory,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = safeCategory,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033)
                    )
                    if (trendDelta != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = trendDelta,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (sessionCount != null) "$sessionCount sessions tracked" else "ADL Skill Training",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Linear Animated Bar
                val animatedProgress by animateFloatAsState(
                    targetValue = (percentage / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "adlCardLinearProgress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Circular Ring
            CircularProgressRing(
                progress = percentage / 100f,
                ringColor = accentColor,
                size = 46.dp,
                strokeWidth = 5.dp
            )
        }
    }
}

// ============================================================================
// 3. SKILL PROGRESS CARD
// ============================================================================
@Composable
fun SkillProgressCard(
    skillName: String?,
    categoryName: String?,
    progressPercentage: Int,
    promptLevelText: String? = null,
    accentColor: Color = Color(0xFF2563EB),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val safeSkill = skillName ?: "ADL Skill"
    val safeCategory = (categoryName ?: "General").uppercase()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = safeCategory,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = safeSkill,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!promptLevelText.isNullOrBlank()) {
                    Text(
                        text = promptLevelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$progressPercentage%",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val animatedProgress by animateFloatAsState(
            targetValue = (progressPercentage / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            label = "skillProgressAnim"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.15f)
        )
    }
}

// ============================================================================
// 4. PROGRESS TREND CHART (Animated Clear View Canvas Line Chart)
// ============================================================================
@Composable
fun ProgressTrendChart(
    dataPoints: List<Pair<String, Float>>,
    lineColor: Color = Color(0xFF2563EB),
    modifier: Modifier = Modifier,
    title: String = "Longitudinal Independence Trend",
    subtitle: String = "Clinical L0-L6 progression over time"
) {
    var isAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(dataPoints) {
        isAnimated = false
        isAnimated = true
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "trendLineProgressAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScaleAnim"
    )

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = lineColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = lineColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clinical trend data available yet",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                val textMeasurer = rememberTextMeasurer()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(dataPoints) {
                                detectTapGestures { tapOffset ->
                                    val paddingLeft = 38.dp.toPx()
                                    val paddingRight = 28.dp.toPx()
                                    val chartWidth = size.width.toFloat() - paddingLeft - paddingRight
                                    val stepX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else 0f

                                    dataPoints.indices.forEach { idx ->
                                        val nodeX = paddingLeft + idx * stepX
                                        if (kotlin.math.abs(tapOffset.x - nodeX) < 30.dp.toPx()) {
                                            selectedPointIndex = if (selectedPointIndex == idx) null else idx
                                        }
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height

                        // Margins around chart area inside Canvas:
                        val paddingLeft = 38.dp.toPx()
                        val paddingRight = 28.dp.toPx()
                        val paddingTop = 36.dp.toPx()   // Space for floating percentage badges
                        val paddingBottom = 32.dp.toPx() // Space for date labels below points

                        val chartWidth = width - paddingLeft - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom

                        // Compute point coordinates
                        val points = dataPoints.mapIndexed { index, pair ->
                            val x = paddingLeft + if (dataPoints.size > 1) {
                                (index.toFloat() / (dataPoints.size - 1)) * chartWidth
                            } else {
                                chartWidth / 2f
                            }
                            val yRatio = (pair.second.coerceIn(0f, 100f) / 100f)
                            val y = paddingTop + chartHeight * (1f - yRatio)
                            Offset(x, y)
                        }

                        // 1. Draw Dashed Grid Lines & Y-Axis Labels (0%, 25%, 50%, 75%, 100%)
                        val gridRatios = listOf(0f, 0.25f, 0.50f, 0.75f, 1.0f)
                        gridRatios.forEach { ratio ->
                            val gridY = paddingTop + chartHeight * (1f - ratio)

                            // Horizontal grid line
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(paddingLeft, gridY),
                                end = Offset(width - paddingRight, gridY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )

                            // Y-axis percentage label
                            val labelText = "${(ratio * 100).toInt()}%"
                            val textLayoutResult = textMeasurer.measure(
                                text = AnnotatedString(labelText),
                                style = TextStyle(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            )
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(
                                    x = paddingLeft - textLayoutResult.size.width - 6.dp.toPx(),
                                    y = gridY - textLayoutResult.size.height / 2f
                                )
                            )
                        }

                        // 2. Build Full Smooth Bezier Line Path
                        val fullLinePath = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val current = points[i]
                                    val next = points[i + 1]
                                    val dx = next.x - current.x
                                    val dy = next.y - current.y

                                    // Smooth control points
                                    val controlPoint1 = Offset(current.x + dx * 0.45f, current.y + dy * 0.1f)
                                    val controlPoint2 = Offset(current.x + dx * 0.55f, next.y - dy * 0.1f)

                                    cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        next.x, next.y
                                    )
                                }
                            }
                        }

                        // 3. Measure Path & Build Animated Partial Path
                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(fullLinePath, false)
                        val totalLength = pathMeasure.length
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, totalLength * progressAnim, animatedPath, true)

                        // 4. Gradient Fill Under Animated Line
                        if (points.isNotEmpty() && progressAnim > 0f) {
                            val currentEndX = paddingLeft + (chartWidth * progressAnim)
                            val fillPath = Path().apply {
                                addPath(animatedPath)
                                lineTo(currentEndX.coerceAtMost(points.last().x), paddingTop + chartHeight)
                                lineTo(points.first().x, paddingTop + chartHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.38f),
                                        lineColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        // 5. Glowing Sub-line for High Contrast
                        drawPath(
                            path = animatedPath,
                            color = lineColor.copy(alpha = 0.25f),
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )

                        // 6. Main Smooth Trend Line
                        drawPath(
                            path = animatedPath,
                            color = lineColor,
                            style = Stroke(
                                width = 4.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )

                        // 7. Draw Node Dots, Floating Callout Badges, and Centered Date Labels
                        points.forEachIndexed { i, point ->
                            val pointProgressStart = if (dataPoints.size > 1) {
                                (i.toFloat() / (dataPoints.size - 1))
                            } else 0f

                            if (progressAnim >= pointProgressStart - 0.05f) {
                                val nodeScale = ((progressAnim - pointProgressStart) / 0.15f).coerceIn(0f, 1f)
                                val pctValue = dataPoints[i].second.toInt()
                                val dateLabel = dataPoints[i].first
                                val isLatestNode = (i == dataPoints.size - 1)
                                val isSelected = (selectedPointIndex == i)

                                // Dynamic aura radius
                                val auraRadius = if (isLatestNode) 11.dp.toPx() * pulseScale else 9.dp.toPx() * nodeScale
                                val outerRingRadius = (if (isSelected) 8.dp else 6.dp).toPx() * nodeScale
                                val innerCoreRadius = (if (isSelected) 4.5.dp else 3.5.dp).toPx() * nodeScale

                                // Outer Glowing Aura for Node
                                drawCircle(
                                    color = lineColor.copy(alpha = if (isLatestNode || isSelected) 0.35f else 0.18f),
                                    radius = auraRadius,
                                    center = point
                                )
                                // Outer Ring
                                drawCircle(
                                    color = if (isSelected) Color(0xFFF59E0B) else lineColor,
                                    radius = outerRingRadius,
                                    center = point
                                )
                                // White Inner Core
                                drawCircle(
                                    color = Color.White,
                                    radius = innerCoreRadius,
                                    center = point
                                )

                                // Draw Percentage Floating Pill Badge above Node
                                val badgeText = "$pctValue%"
                                val badgeLayout = textMeasurer.measure(
                                    text = AnnotatedString(badgeText),
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                val badgePaddingX = 7.dp.toPx()
                                val badgePaddingY = 3.5.dp.toPx()
                                val badgeWidth = badgeLayout.size.width + badgePaddingX * 2
                                val badgeHeight = badgeLayout.size.height + badgePaddingY * 2
                                val badgeLeft = point.x - badgeWidth / 2f
                                val badgeTop = point.y - 22.dp.toPx() - badgeHeight / 2f

                                val badgeBgColor = if (isSelected) Color(0xFFF59E0B) else lineColor

                                // Badge background rounded rectangle
                                drawRoundRect(
                                    color = badgeBgColor,
                                    topLeft = Offset(badgeLeft, badgeTop),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                                // Badge text
                                drawText(
                                    textLayoutResult = badgeLayout,
                                    topLeft = Offset(badgeLeft + badgePaddingX, badgeTop + badgePaddingY)
                                )

                                // Draw Date Label Centered below Point
                                val safeDateLabel = dateLabel ?: ""
                                val dateLayout = textMeasurer.measure(
                                    text = AnnotatedString(safeDateLabel),
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected || isLatestNode) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected || isLatestNode) Color(0xFF172033) else Color(0xFF64748B)
                                    )
                                )
                                drawText(
                                    textLayoutResult = dateLayout,
                                    topLeft = Offset(
                                        x = point.x - dateLayout.size.width / 2f,
                                        y = paddingTop + chartHeight + 8.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. SESSION PROGRESS INDICATOR (Clinical L0 - L6 Hierarchy)
// ============================================================================
@Composable
fun SessionProgressIndicator(
    promptLevelCode: String?,
    promptLevelName: String?,
    modifier: Modifier = Modifier
) {
    val safeCode = promptLevelCode ?: "L0"
    val safeName = promptLevelName ?: "Independent"
    val levels = listOf("L0", "L1", "L2", "L3", "L4", "L5", "L6")
    val currentIndex = when (safeCode.uppercase()) {
        "INDEPENDENT", "L0" -> 0
        "VISUAL", "L1" -> 1
        "GESTURE", "L2" -> 2
        "VERBAL", "L3" -> 3
        "MODEL_VIDEO", "MODEL", "L4" -> 4
        "PARTIAL_PHYSICAL", "L5" -> 5
        "FULL_PHYSICAL", "L6" -> 6
        else -> 0
    }

    val activeColor = getPromptLevelColor(currentIndex)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = safeName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = activeColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "L$currentIndex / 6",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7 Hierarchy Segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            levels.forEachIndexed { index, _ ->
                val isReached = index <= currentIndex
                val color = if (isReached) getPromptLevelColor(index) else Color(0xFFE2E8F0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // L0 to L6 Centered Labels Under Bar Segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            levels.forEachIndexed { index, lvl ->
                val isCurrent = index == currentIndex
                Text(
                    text = lvl,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isCurrent) getPromptLevelColor(index) else Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ============================================================================
// 6. ACTIVITY STEP PROGRESS (Child Mode / Step Workflow)
// ============================================================================
@Composable
fun ActivityStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )

            Text(
                text = "${((currentStep.toFloat() / totalSteps.coerceAtLeast(1)) * 100).toInt()}% Done",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step <= currentStep
                val isCurrent = step == currentStep

                val color = when {
                    isCompleted -> Color(0xFF2563EB)
                    else -> Color(0xFFCBD5E1)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 10.dp else 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
            }
        }
    }
}

// ============================================================================
// 7. PROGRAMME COMPLETION CARD
// ============================================================================
@Composable
fun ProgrammeCompletionCard(
    title: String?,
    assignedDate: String?,
    completionPercentage: Int,
    caregiverStatusText: String?,
    isPendingReview: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val safeTitle = title ?: "Home Programme"
    val safeDate = assignedDate ?: "Recent"
    val safeStatus = caregiverStatusText ?: "Active"
    val accentColor = if (isPendingReview) Color(0xFFF59E0B) else Color(0xFF2563EB)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressRing(
                progress = completionPercentage / 100f,
                ringColor = accentColor,
                size = 54.dp,
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = safeTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isPendingReview) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "Pending Review",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Assigned: $safeDate",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = safeStatus,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }
    }
}

// ============================================================================
// 8. MILESTONE CARD
// ============================================================================
@Composable
fun MilestoneCard(
    title: String?,
    description: String?,
    achievedDate: String? = null,
    isAchieved: Boolean = true,
    icon: ImageVector = Icons.Default.EmojiEvents,
    accentColor: Color = Color(0xFFF59E0B),
    modifier: Modifier = Modifier
) {
    val safeTitle = title ?: "Clinical Milestone"
    val safeDesc = description ?: "ADL Progress Milestone"

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isAchieved) accentColor.copy(alpha = 0.15f) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isAchieved) accentColor else Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = safeTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = safeDesc,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )

                if (!achievedDate.isNullOrBlank() && isAchieved) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Achieved on $achievedDate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// 9. CHILD REWARD PROGRESS (Child Mode Stars & Badges)
// ============================================================================
@Composable
fun ChildRewardProgress(
    starsEarned: Int,
    totalStars: Int = 5,
    currentStreak: Int = 3,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Awesome Effort! 🌟",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$currentStreak Day Practice Streak 🔥",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..totalStars) {
                    val isEarned = i <= starsEarned
                    Icon(
                        imageVector = if (isEarned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (isEarned) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 10. PROGRESS LEGEND
// ============================================================================
@Composable
fun ProgressLegend(
    type: String = "PROMPT_LEVELS",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (type == "PROMPT_LEVELS") "CLINICAL PROMPT HIERARCHY (L0 - L6)" else "ADL CATEGORIES",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (type == "PROMPT_LEVELS") {
                val row1 = listOf(
                    Pair("L0 Independent", Color(0xFF10B981)),
                    Pair("L1 Visual", Color(0xFF2563EB)),
                    Pair("L2 Gesture", Color(0xFF06B6D4)),
                    Pair("L3 Verbal", Color(0xFF7C3AED))
                )
                val row2 = listOf(
                    Pair("L4 Model", Color(0xFFF59E0B)),
                    Pair("L5 Partial Phys", Color(0xFFEC4899)),
                    Pair("L6 Full Phys", Color(0xFFEF4444))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row1.forEach { (label, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF172033)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row2.forEach { (label, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF172033)
                                )
                            }
                        }
                    }
                }
            } else {
                val adlItems = listOf(
                    Pair("Dressing", Color(0xFF2563EB)),
                    Pair("Eating", Color(0xFF10B981)),
                    Pair("Grooming", Color(0xFF7C3AED)),
                    Pair("Shoes & Socks", Color(0xFFF59E0B))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    adlItems.forEach { (name, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF172033)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 11. COMPACT DASHBOARD PROGRESS WIDGET
// ============================================================================
@Composable
fun CompactDashboardProgress(
    title: String?,
    subtitle: String?,
    percentage: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val safeTitle = title ?: "Progress"
    val safeSubtitle = subtitle ?: "Overall Progress"

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = safeTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = safeSubtitle,
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            CircularProgressRing(
                progress = percentage / 100f,
                ringColor = accentColor,
                size = 42.dp,
                strokeWidth = 4.5.dp
            )
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS FOR VISUAL IDENTITY
// ============================================================================
fun getADLCategoryVisuals(category: String?): Triple<Color, ImageVector, List<Color>> {
    val safeCat = category ?: ""
    return when {
        safeCat.contains("Dress", ignoreCase = true) -> Triple(
            Color(0xFF2563EB),
            Icons.Default.Checkroom,
            listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
        )
        safeCat.contains("Eat", ignoreCase = true) -> Triple(
            Color(0xFF10B981),
            Icons.Default.Restaurant,
            listOf(Color(0xFF10B981), Color(0xFF059669))
        )
        safeCat.contains("Groom", ignoreCase = true) -> Triple(
            Color(0xFF7C3AED),
            Icons.Default.CleanHands,
            listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
        )
        else -> Triple(
            Color(0xFFF59E0B),
            Icons.Default.DirectionsWalk,
            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        )
    }
}

fun getPromptLevelColor(index: Int): Color {
    return when (index) {
        0 -> Color(0xFF10B981) // Independent - Green
        1 -> Color(0xFF2563EB) // Visual - Blue
        2 -> Color(0xFF06B6D4) // Gesture - Cyan
        3 -> Color(0xFF7C3AED) // Verbal - Purple
        4 -> Color(0xFFF59E0B) // Model - Amber
        5 -> Color(0xFFEC4899) // Partial Physical - Pink
        else -> Color(0xFFEF4444) // Full Physical - Red
    }
}
