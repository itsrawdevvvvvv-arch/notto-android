package com.notto.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notto.app.ui.theme.LocalNottoColors
import com.notto.app.ui.theme.NottoMotion

fun Modifier.pressable(onClick: () -> Unit): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = NottoMotion.standardTween(110),
        label = "pressScale"
    )
    val haptic = LocalHapticFeedback.current
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                },
                onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
        }
}

@Composable
fun AnimatedCheckRow(
    text: String,
    done: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNottoColors.current
    val checkScale by animateFloatAsState(
        targetValue = if (done) 1.06f else 1f,
        animationSpec = NottoMotion.bouncySpring(),
        label = "checkScale"
    )
    val checkProgress by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = NottoMotion.standardTween(240),
        label = "checkProgress"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (done) 0.45f else 1f,
        animationSpec = NottoMotion.standardTween(200),
        label = "textAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                }
        ) {
            val r = size.minDimension / 2f
            if (done) {
                drawCircle(color = colors.accent, radius = r)
            } else {
                drawCircle(
                    color = colors.muted.copy(alpha = 0.5f),
                    radius = r - 1.dp.toPx(),
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }
            if (checkProgress > 0.01f) {
                val path = Path().apply {
                    moveTo(size.width * 0.26f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.70f)
                    lineTo(size.width * 0.76f, size.height * 0.32f)
                }
                val out = Path()
                val pm = PathMeasure()
                pm.setPath(path, false)
                pm.getSegment(0f, pm.length * checkProgress, out, true)
                drawPath(
                    path = out,
                    color = Color.White,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Text(
            text = text,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = textAlpha }
        )
    }
}

@Composable
fun FloatingPillNav(
    selectedTab: Int,
    createOpen: Boolean,
    onSelectTab: (Int) -> Unit,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNottoColors.current
    val icons = remember {
        listOf(
            Icons.Rounded.Home to "Notes",
            Icons.Rounded.Search to "Search",
            Icons.Rounded.Schedule to "Focus",
            Icons.Rounded.Settings to "Settings"
        )
    }
    val plusRotation by animateFloatAsState(
        targetValue = if (createOpen) 45f else 0f,
        animationSpec = NottoMotion.bouncySpring(),
        label = "plusRot"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .shadow(14.dp, RoundedCornerShape(30.dp), spotColor = Color(0x28000000))
                .clip(RoundedCornerShape(30.dp))
                .background(colors.surface)
                .padding(5.dp)
        ) {
            val tabW = maxWidth / 4
            val sliderOffset by animateDpAsState(
                targetValue = tabW * selectedTab,
                animationSpec = NottoMotion.bouncySpring(),
                label = "navSlide"
            )

            Box(
                modifier = Modifier
                    .offset(x = sliderOffset)
                    .width(tabW)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(25.dp))
                    .background(colors.accent.copy(alpha = 0.14f))
            )

            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                icons.forEachIndexed { idx, (icon, desc) ->
                    val isSelected = idx == selectedTab
                    val tint by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else colors.muted,
                        animationSpec = NottoMotion.standardTween(180),
                        label = "navTint"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1f,
                        animationSpec = NottoMotion.bouncySpring(),
                        label = "navIconScale"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pressable { onSelectTab(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            tint = tint,
                            modifier = Modifier
                                .size(23.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(14.dp, CircleShape, spotColor = Color(0x355E5CE6))
                .clip(CircleShape)
                .background(colors.accent)
                .pressable(onPlusClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "New note",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = plusRotation }
            )
        }
    }
}
