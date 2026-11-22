package com.sakuva.app.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.domain.model.ExpenseTrendPoint
import kotlin.math.roundToInt

@Composable
fun ExpenseLineChart(
    points: List<ExpenseTrendPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val strings = LocalStrings.current
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val tooltipStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.inverseOnSurface,
    )
    val tooltipBackground = MaterialTheme.colorScheme.inverseSurface
    val textMeasurer = rememberTextMeasurer()
    val horizontalPaddingPx = with(LocalDensity.current) { 14.dp.toPx() }

    var selectedIndex by remember(points) { mutableIntStateOf(-1) }

    val maxAmount = points.maxOf { it.amount }.coerceAtLeast(1L)
    val total = points.sumOf { it.amount }
    val summaryDescription = strings.trendContentDescription
        .replace("{total}", MoneyFormatter.format(total, currencyCode))

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .semantics { contentDescription = summaryDescription }
            .pointerInput(points) {
                detectTapGestures { offset ->
                    selectedIndex = nearestIndex(offset.x, size.width, points.size, horizontalPaddingPx)
                }
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedIndex = nearestIndex(offset.x, size.width, points.size, horizontalPaddingPx)
                    },
                    onDrag = { change, _ ->
                        selectedIndex = nearestIndex(
                            change.position.x,
                            size.width,
                            points.size,
                            horizontalPaddingPx,
                        )
                    },
                    onDragEnd = { selectedIndex = -1 },
                    onDragCancel = { selectedIndex = -1 },
                )
            },
    ) {
        val topPadding = 26.dp.toPx()
        val bottomPadding = 26.dp.toPx()
        val chartWidth = (size.width - horizontalPaddingPx * 2).coerceAtLeast(1f)
        val chartHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(1f)

        fun xFor(index: Int): Float = if (points.size == 1) {
            size.width / 2f
        } else {
            horizontalPaddingPx + chartWidth * index / (points.size - 1)
        }

        fun yFor(amount: Long): Float =
            topPadding + chartHeight * (1f - amount.toFloat() / maxAmount.toFloat())

        repeat(4) { line ->
            val y = topPadding + chartHeight * line / 3f
            drawLine(
                color = gridColor,
                start = Offset(horizontalPaddingPx, y),
                end = Offset(size.width - horizontalPaddingPx, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val offsets = points.mapIndexed { index, point -> Offset(xFor(index), yFor(point.amount)) }
        val linePath = smoothPath(offsets)
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(offsets.last().x, topPadding + chartHeight)
            lineTo(offsets.first().x, topPadding + chartHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.30f), lineColor.copy(alpha = 0.02f)),
                startY = topPadding,
                endY = topPadding + chartHeight,
            ),
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx()),
        )

        offsets.forEach { offset ->
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = offset)
        }

        val labelStep = ((points.size + 3) / 4).coerceAtLeast(1)
        points.forEachIndexed { index, point ->
            if (index % labelStep != 0 && index != points.lastIndex) return@forEachIndexed
            val layout = textMeasurer.measure(AnnotatedString(point.label), style = labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = (offsets[index].x - layout.size.width / 2f)
                        .coerceIn(0f, size.width - layout.size.width),
                    y = size.height - layout.size.height,
                ),
            )
        }

        val index = selectedIndex
        if (index in points.indices) {
            val point = points[index]
            val center = offsets[index]
            drawLine(
                color = lineColor.copy(alpha = 0.45f),
                start = Offset(center.x, topPadding),
                end = Offset(center.x, topPadding + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )
            drawCircle(color = lineColor, radius = 5.dp.toPx(), center = center)

            val tooltipText = AnnotatedString(
                "${point.label}  ${MoneyFormatter.format(point.amount, currencyCode)}",
            )
            val layout = textMeasurer.measure(tooltipText, style = tooltipStyle)
            val padding = 8.dp.toPx()
            val tooltipWidth = layout.size.width + padding * 2
            val tooltipHeight = layout.size.height + padding
            val left = (center.x - tooltipWidth / 2f)
                .coerceIn(0f, (size.width - tooltipWidth).coerceAtLeast(0f))
            val top = (center.y - tooltipHeight - 12.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(
                color = tooltipBackground,
                topLeft = Offset(left, top),
                size = Size(tooltipWidth, tooltipHeight),
                cornerRadius = CornerRadius(10.dp.toPx()),
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(left + padding, top + padding / 2f),
            )
        }
    }
}

private fun nearestIndex(x: Float, width: Int, count: Int, padding: Float): Int {
    if (count <= 1) return 0
    val usable = (width - padding * 2).coerceAtLeast(1f)
    val step = usable / (count - 1)
    return ((x - padding) / step).roundToInt().coerceIn(0, count - 1)
}

private fun smoothPath(offsets: List<Offset>): Path {
    val path = Path()
    if (offsets.isEmpty()) return path
    path.moveTo(offsets.first().x, offsets.first().y)
    for (index in 1 until offsets.size) {
        val previous = offsets[index - 1]
        val current = offsets[index]
        val midX = (previous.x + current.x) / 2f
        path.cubicTo(
            x1 = midX,
            y1 = previous.y,
            x2 = midX,
            y2 = current.y,
            x3 = current.x,
            y3 = current.y,
        )
    }
    return path
}
