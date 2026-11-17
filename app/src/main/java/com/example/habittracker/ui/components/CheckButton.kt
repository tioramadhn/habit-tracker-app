package com.example.habittracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Round check-off control that pops when toggled. */
@Composable
fun CheckButton(
    checked: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check-scale",
    )
    Surface(
        onClick = onClick,
        modifier = modifier.size(size).scale(scale),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (checked) accent else Color.Transparent,
        contentColor = if (checked) Color.White else MaterialTheme.colorScheme.onSurface,
        border = if (checked) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = contentDescription,
                tint = if (checked) Color.White else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(size * 0.55f)
                    .alpha(if (checked) 1f else 0.35f),
            )
        }
    }
}
