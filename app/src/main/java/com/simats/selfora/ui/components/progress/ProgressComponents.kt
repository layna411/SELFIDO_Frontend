package com.simats.selfora.ui.components.progress

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.GlassCard

// ============================================================================
// SELFORA BRAND COLORS FOR PROGRESS VISUALIZATION
// ============================================================================
val SelforaProgressBlue = Color(0xFF2563EB)     // Dressing & Primary
val SelforaProgressPurple = Color(0xFF7C3AED)   // Eating & Secondary
val SelforaProgressGreen = Color(0xFF10B981)    // Grooming & Success
val SelforaProgressAmber = Color(0xFFF59E0B)    // Shoes & Socks & Warning
val SelforaProgressError = Color(0xFFEF4444)    // Error
val SelforaProgressBackground = Color(0xFFF5F7FC)
val SelforaProgressTextPrimary = Color(0xFF172033)
val SelforaProgressTextSecondary = Color(0xFF64748B)

// ============================================================================
// 1. REUSABLE LINEAR PROGRESS (Modern Pill-Shaped Animated Indicator)
// ============================================================================
@Composable
fun SelforaLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SelforaProgressBlue,
    trackColor: Color = color.copy(alpha = 0.14f),
    height: Dp = 8.dp,
    cornerRadius: Dp = height / 2,
    animated: Boolean = true
) {
    val safeProgress = if (progress.isNaN() || progress.isInfinite()) 0f else progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = if (animated) tween(durationMillis = 750, easing = FastOutSlowInEasing) else snap(),
        label = "selforaLinearProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.88f), color)
                        )
                    )
            )
        }
    }
}

@Composable
fun SelforaLinearProgress(
    percentage: Int,
    modifier: Modifier = Modifier,
    color: Color = SelforaProgressBlue,
    trackColor: Color = color.copy(alpha = 0.14f),
    height: Dp = 8.dp,
    cornerRadius: Dp = height / 2,
    animated: Boolean = true
) {
    val floatProgress = (percentage.coerceIn(0, 100)) / 100f
    SelforaLinearProgress(
        progress = floatProgress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        height = height,
        cornerRadius = cornerRadius,
        animated = animated
    )
}

// ============================================================================
// 2. CIRCULAR PROGRESS RING & SELFORA CIRCULAR PROGRESS
// ============================================================================
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = SelforaProgressBlue,
    trackColor: Color = ringColor.copy(alpha = 0.14f),
    strokeWidth: Dp = 8.dp,
    size: Dp = 72.dp,
    showPercentageText: Boolean = true,
    centerText: String? = null,
    subText: String? = null
) {
    val safeProgress = if (progress.isNaN() || progress.isInfinite()) 0f else progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
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

            // Animated Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(ringColor.copy(alpha = 0.85f), ringColor),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (showPercentageText) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textToDisplay = centerText ?: "${(animatedProgress * 100).toInt()}%"
                Text(
                    text = textToDisplay,
                    fontSize = when {
                        size < 50.dp -> 10.sp
                        size < 70.dp -> 12.sp
                        size < 110.dp -> 16.sp
                        else -> 28.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = SelforaProgressTextPrimary,
                    textAlign = TextAlign.Center
                )
                if (subText != null && size >= 65.dp) {
                    Text(
                        text = subText,
                        fontSize = if (size >= 110.dp) 11.sp else 8.5.sp,
                        color = SelforaProgressTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SelforaCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = SelforaProgressBlue,
    trackColor: Color = ringColor.copy(alpha = 0.14f),
    strokeWidth: Dp = 8.dp,
    size: Dp = 80.dp,
    title: String? = "Overall",
    subtitle: String? = null
) {
    CircularProgressRing(
        progress = progress,
        modifier = modifier,
        ringColor = ringColor,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
        size = size,
        showPercentageText = true,
        centerText = null,
        subText = title ?: subtitle
    )
}

// ============================================================================
// 3. ADL CATEGORY PROGRESS (Single Metric Row & 4-Category Grid)
// ============================================================================
@Composable
fun SelforaAdlMetricRow(
    category: String,
    percentage: Int,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    height: Dp = 6.dp
) {
    val (color, icon, _) = getADLCategoryVisuals(category)
    val safePercentage = percentage.coerceIn(0, 100)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (showIcon) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category,
                            tint = color,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = category,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaProgressTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$safePercentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        SelforaLinearProgress(
            percentage = safePercentage,
            color = color,
            height = height
        )
    }
}

/**
 * Responsive 2x2 Grid or vertical stack displaying all 4 core ADL categories:
 * 1. Dressing (#2563EB)
 * 2. Eating (#7C3AED)
 * 3. Grooming (#10B981)
 * 4. Shoes & Socks (#F59E0B)
 */
@Composable
fun SelforaAdlCategoryGrid(
    dressingPct: Int,
    eatingPct: Int,
    groomingPct: Int,
    shoesPct: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelforaAdlMetricRow(
                category = "Dressing",
                percentage = dressingPct,
                modifier = Modifier.weight(1f)
            )
            SelforaAdlMetricRow(
                category = "Eating",
                percentage = eatingPct,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelforaAdlMetricRow(
                category = "Grooming",
                percentage = groomingPct,
                modifier = Modifier.weight(1f)
            )
            SelforaAdlMetricRow(
                category = "Shoes & Socks",
                percentage = shoesPct,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// 4. ADL PROGRESS CARD (Full Card with Badge, Trend & Dual Indicators)
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
    val safePct = percentage.coerceIn(0, 100)

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
                        color = SelforaProgressTextPrimary
                    )
                    if (trendDelta != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SelforaProgressGreen.copy(alpha = 0.12f)
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
                    color = SelforaProgressTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                SelforaLinearProgress(
                    percentage = safePct,
                    color = accentColor,
                    height = 7.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Circular Ring
            CircularProgressRing(
                progress = safePct / 100f,
                ringColor = accentColor,
                size = 46.dp,
                strokeWidth = 5.dp
            )
        }
    }
}

// ============================================================================
// 5. SKILL PROGRESS CARD
// ============================================================================
@Composable
fun SkillProgressCard(
    skillName: String?,
    categoryName: String?,
    progressPercentage: Int,
    promptLevelText: String? = null,
    accentColor: Color = SelforaProgressBlue,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val safeSkill = skillName ?: "ADL Skill"
    val safeCategory = (categoryName ?: "General").uppercase()
    val safePct = progressPercentage.coerceIn(0, 100)

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
                    color = SelforaProgressTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!promptLevelText.isNullOrBlank()) {
                    Text(
                        text = promptLevelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaProgressTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$safePct%",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SelforaLinearProgress(
            percentage = safePct,
            color = accentColor,
            height = 7.dp
        )
    }
}

// ============================================================================
// 6. PROGRESS TREND CHART (Curved Line Chart with Canvas)
// ============================================================================
@Composable
fun ProgressTrendChart(
    dataPoints: List<Pair<String, Float>>,
    lineColor: Color = SelforaProgressBlue,
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
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "trendLineProgressAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
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
                        color = SelforaProgressTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SelforaProgressTextSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = lineColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
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
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No clinical trend data available yet",
                            fontSize = 12.sp,
                            color = SelforaProgressTextSecondary
                        )
                    }
                }
            } else {
                val textMeasurer = rememberTextMeasurer()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
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
                                        if (kotlin.math.abs(tapOffset.x - nodeX) < 28.dp.toPx()) {
                                            selectedPointIndex = if (selectedPointIndex == idx) null else idx
                                        }
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height

                        val paddingLeft = 38.dp.toPx()
                        val paddingRight = 28.dp.toPx()
                        val paddingTop = 32.dp.toPx()
                        val paddingBottom = 30.dp.toPx()

                        val chartWidth = width - paddingLeft - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom

                        val points = dataPoints.mapIndexed { index, pair ->
                            val x = paddingLeft + if (dataPoints.size > 1) {
                                (index.toFloat() / (dataPoints.size - 1)) * chartWidth
                            } else {
                                chartWidth / 2f
                            }
                            val safeVal = if (pair.second.isNaN() || pair.second.isInfinite()) 0f else pair.second.coerceIn(0f, 100f)
                            val yRatio = safeVal / 100f
                            val y = paddingTop + chartHeight * (1f - yRatio)
                            Offset(x, y)
                        }

                        // 1. Horizontal dashed grid lines
                        val gridRatios = listOf(0f, 0.25f, 0.50f, 0.75f, 1.0f)
                        gridRatios.forEach { ratio ->
                            val gridY = paddingTop + chartHeight * (1f - ratio)

                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(paddingLeft, gridY),
                                end = Offset(width - paddingRight, gridY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            val labelText = "${(ratio * 100).toInt()}%"
                            val textLayoutResult = textMeasurer.measure(
                                text = AnnotatedString(labelText),
                                style = TextStyle(
                                    fontSize = 9.sp,
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

                        // 2. Smooth Bézier Curve
                        val fullLinePath = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val current = points[i]
                                    val next = points[i + 1]
                                    val dx = next.x - current.x
                                    val dy = next.y - current.y

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

                        // 3. Partial animated line
                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(fullLinePath, false)
                        val totalLength = pathMeasure.length
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, totalLength * progressAnim, animatedPath, true)

                        // 4. Subtle vertical gradient fill under line
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
                                        lineColor.copy(alpha = 0.28f),
                                        lineColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        // 5. Main Stroke
                        drawPath(
                            path = animatedPath,
                            color = lineColor,
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 6. Data Nodes & Labels
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

                                val outerRadius = (if (isSelected || isLatestNode) 6.dp else 4.5.dp).toPx() * nodeScale
                                val innerRadius = (if (isSelected) 3.5.dp else 2.5.dp).toPx() * nodeScale

                                // Outer glow on latest or selected
                                if (isLatestNode || isSelected) {
                                    drawCircle(
                                        color = lineColor.copy(alpha = 0.25f),
                                        radius = 10.dp.toPx() * pulseScale,
                                        center = point
                                    )
                                }

                                drawCircle(
                                    color = if (isSelected) SelforaProgressAmber else lineColor,
                                    radius = outerRadius,
                                    center = point
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = innerRadius,
                                    center = point
                                )

                                // Floating badge if selected or latest
                                if (isSelected || isLatestNode) {
                                    val badgeText = "$pctValue%"
                                    val badgeLayout = textMeasurer.measure(
                                        text = AnnotatedString(badgeText),
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    val badgeW = badgeLayout.size.width + 12.dp.toPx()
                                    val badgeH = badgeLayout.size.height + 6.dp.toPx()
                                    val badgeX = (point.x - badgeW / 2f).coerceIn(paddingLeft, width - paddingRight - badgeW)
                                    val badgeY = point.y - badgeH - 8.dp.toPx()

                                    drawRoundRect(
                                        color = if (isSelected) SelforaProgressAmber else lineColor,
                                        topLeft = Offset(badgeX, badgeY),
                                        size = Size(badgeW, badgeH),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                    )
                                    drawText(
                                        textLayoutResult = badgeLayout,
                                        topLeft = Offset(badgeX + 6.dp.toPx(), badgeY + 3.dp.toPx())
                                    )
                                }

                                // Date label under X-axis
                                val dateLayout = textMeasurer.measure(
                                    text = AnnotatedString(dateLabel),
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
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
// 7. BAR CHART (Rounded Vertical Comparison Bars)
// ============================================================================
@Composable
fun SelforaBarChart(
    items: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    title: String = "Category Comparison",
    accentColor: Color = SelforaProgressBlue
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaProgressTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No comparison data available", fontSize = 12.sp, color = SelforaProgressTextSecondary)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items.forEach { (label, value) ->
                        val safeVal = value.coerceIn(0, 100)
                        val animatedFraction by animateFloatAsState(
                            targetValue = safeVal / 100f,
                            animationSpec = tween(750, easing = FastOutSlowInEasing),
                            label = "barHeightAnim"
                        )

                        val barColor = when {
                            label.contains("Dress", ignoreCase = true) -> SelforaProgressBlue
                            label.contains("Eat", ignoreCase = true) -> SelforaProgressPurple
                            label.contains("Groom", ignoreCase = true) -> SelforaProgressGreen
                            else -> SelforaProgressAmber
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$safeVal%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedFraction)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(barColor, barColor.copy(alpha = 0.85f))
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaProgressTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 8. DONUT CHART (Category / Goal Distribution)
// ============================================================================
@Composable
fun SelforaDonutChart(
    slices: List<Triple<String, Float, Color>>,
    modifier: Modifier = Modifier,
    title: String = "Skill Distribution",
    centerLabel: String = "Total"
) {
    val total = slices.sumOf { it.second.toDouble() }.toFloat()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaProgressTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (total <= 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No distribution data available", fontSize = 12.sp, color = SelforaProgressTextSecondary)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Donut Canvas
                    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = 16.dp.toPx()
                            val arcSize = Size(size.width - strokePx, size.height - strokePx)
                            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                            var startAngle = -90f
                            slices.forEach { (_, value, color) ->
                                val sweepAngle = (value / total) * 360f
                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${total.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SelforaProgressTextPrimary
                            )
                            Text(
                                text = centerLabel,
                                fontSize = 9.sp,
                                color = SelforaProgressTextSecondary
                            )
                        }
                    }

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        slices.forEach { (label, value, color) ->
                            val pct = ((value / total) * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$label: $pct%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SelforaProgressTextPrimary
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
// 9. ACTIVITY STEP PROGRESS (Child Mode & Workflow Steps)
// ============================================================================
@Composable
fun ActivityStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = SelforaProgressBlue
) {
    val safeTotal = totalSteps.coerceAtLeast(1)
    val safeStep = currentStep.coerceIn(0, safeTotal)
    val progressPct = ((safeStep.toFloat() / safeTotal) * 100).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $safeStep of $safeTotal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Text(
                text = "$progressPct% Completed",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SelforaProgressTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            for (step in 1..safeTotal) {
                val isCompleted = step <= safeStep
                val isCurrent = step == safeStep

                val barColor = when {
                    isCompleted -> accentColor
                    else -> Color(0xFFE2E8F0)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
    }
}

// ============================================================================
// 10. SESSION PROGRESS INDICATOR (Clinical Assistance Hierarchy L0 - L6)
// ============================================================================
@Composable
fun SessionProgressIndicator(
    promptLevelCode: String,
    promptLevelName: String,
    modifier: Modifier = Modifier
) {
    val levels = listOf("L0", "L1", "L2", "L3", "L4", "L5", "L6")
    val currentIndex = when (promptLevelCode.uppercase()) {
        "L0", "INDEPENDENT" -> 0
        "L1", "VISUAL" -> 1
        "L2", "GESTURE" -> 2
        "L3", "VERBAL" -> 3
        "L4", "MODEL", "MODEL_VIDEO" -> 4
        "L5", "PARTIAL_PHYSICAL" -> 5
        "L6", "FULL_PHYSICAL" -> 6
        else -> 1
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prompt Hierarchy Level",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SelforaProgressTextSecondary
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getPromptLevelColor(currentIndex).copy(alpha = 0.12f)
            ) {
                Text(
                    text = promptLevelName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = getPromptLevelColor(currentIndex),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Track
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            levels.forEachIndexed { index, _ ->
                val isFilled = index <= currentIndex
                val isCurrent = index == currentIndex
                val color = if (isFilled) getPromptLevelColor(index) else Color(0xFFE2E8F0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 10.dp else 7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                    color = if (isCurrent) getPromptLevelColor(index) else SelforaProgressTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ============================================================================
// 11. PROGRAMME COMPLETION CARD
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
    val accentColor = if (isPendingReview) SelforaProgressAmber else SelforaProgressBlue

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
                        color = SelforaProgressTextPrimary,
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
                    color = SelforaProgressTextSecondary
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
// 12. MILESTONE CARD
// ============================================================================
@Composable
fun MilestoneCard(
    title: String?,
    description: String?,
    achievedDate: String? = null,
    isAchieved: Boolean = true,
    icon: ImageVector = Icons.Default.EmojiEvents,
    accentColor: Color = SelforaProgressAmber,
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
                    color = SelforaProgressTextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = safeDesc,
                    fontSize = 11.5.sp,
                    color = SelforaProgressTextSecondary
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
// 13. CHILD REWARD PROGRESS (Child Mode Stars & Streak)
// ============================================================================
@Composable
fun ChildRewardProgress(
    starsEarned: Int,
    totalStars: Int = 5,
    currentStreak: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Awesome Effort! ⭐",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentStreak > 0) "$currentStreak Day Practice Streak 🔥" else "Start your practice streak today! 🔥",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Total Stars",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$starsEarned",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Stars",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 14. PROGRESS LEGEND
// ============================================================================
@Composable
fun ProgressLegend(
    type: String = "PROMPT_LEVELS",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (type == "PROMPT_LEVELS") "CLINICAL PROMPT HIERARCHY (L0 - L6)" else "ADL CATEGORIES",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaProgressTextSecondary
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
                                    color = SelforaProgressTextPrimary
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
                                    color = SelforaProgressTextPrimary
                                )
                            }
                        }
                    }
                }
            } else {
                val adlItems = listOf(
                    Pair("Dressing", SelforaProgressBlue),
                    Pair("Eating", SelforaProgressPurple),
                    Pair("Grooming", SelforaProgressGreen),
                    Pair("Shoes & Socks", SelforaProgressAmber)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    adlItems.forEach { (name, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaProgressTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 15. COMPACT DASHBOARD PROGRESS
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
                    color = SelforaProgressTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = safeSubtitle,
                    fontSize = 10.5.sp,
                    color = SelforaProgressTextSecondary,
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
            SelforaProgressBlue, // Dressing: #2563EB
            Icons.Default.Checkroom,
            listOf(SelforaProgressBlue, Color(0xFF1D4ED8))
        )
        safeCat.contains("Eat", ignoreCase = true) || safeCat.contains("Feed", ignoreCase = true) -> Triple(
            SelforaProgressPurple, // Eating: #7C3AED
            Icons.Default.Restaurant,
            listOf(SelforaProgressPurple, Color(0xFF6D28D9))
        )
        safeCat.contains("Groom", ignoreCase = true) || safeCat.contains("Hygiene", ignoreCase = true) || safeCat.contains("Brush", ignoreCase = true) -> Triple(
            SelforaProgressGreen, // Grooming: #10B981
            Icons.Default.CleanHands,
            listOf(SelforaProgressGreen, Color(0xFF059669))
        )
        else -> Triple(
            SelforaProgressAmber, // Shoes & Socks: #F59E0B
            Icons.AutoMirrored.Filled.DirectionsWalk,
            listOf(SelforaProgressAmber, Color(0xFFD97706))
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
