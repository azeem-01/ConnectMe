package com.ce46.connectme.ui.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class ConnectTab(val label: String) {
    WIFI("Wi-Fi"),
    STATUS("Status"),
    SETTINGS("Settings")
}

private data class TabVisual(
    val tab: ConnectTab,
    val selected: ImageVector,
    val unselected: ImageVector
)

@Composable
fun SlidingBottomBar(
    selected: ConnectTab,
    onSelect: (ConnectTab) -> Unit
) {
    val tabs = listOf(
        TabVisual(ConnectTab.WIFI, Icons.Rounded.Wifi, Icons.Outlined.Wifi),
        TabVisual(ConnectTab.STATUS, Icons.Rounded.SentimentSatisfiedAlt, Icons.Outlined.SentimentSatisfied),
        TabVisual(ConnectTab.SETTINGS, Icons.Rounded.Settings, Icons.Outlined.Settings)
    )
    val index = tabs.indexOfFirst { it.tab == selected }.coerceAtLeast(0)
    val fraction by animateFloatAsState(
        targetValue = index.toFloat(),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f),
        label = "tabSlider"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().fillMaxHeight()) {
            val itemWidth = maxWidth / tabs.size
            val px = with(LocalDensity.current) { (itemWidth * fraction).toPx() }
            Box(
                Modifier
                    .offset { IntOffset(px.roundToInt(), 0) }
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                tabs.forEach { visual ->
                    val isOn = visual.tab == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelect(visual.tab) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isOn) visual.selected else visual.unselected,
                                    contentDescription = visual.tab.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isOn) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = visual.tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOn) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
