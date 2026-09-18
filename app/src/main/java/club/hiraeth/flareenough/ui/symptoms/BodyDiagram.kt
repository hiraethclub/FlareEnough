package club.hiraeth.flareenough.ui.symptoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState

/** A region and where its dot sits on the figure, as fractions of the figure box. */
private data class RegionPos(val region: BodyRegion, val fx: Float, val fy: Float)

// Front view. A dot for each region the person can mark from the front.
private val frontPositions = listOf(
    RegionPos(BodyRegion.JAW, 0.50f, 0.10f),
    RegionPos(BodyRegion.NECK, 0.50f, 0.15f),
    RegionPos(BodyRegion.SHOULDER_RIGHT, 0.32f, 0.21f),
    RegionPos(BodyRegion.SHOULDER_LEFT, 0.68f, 0.21f),
    RegionPos(BodyRegion.ELBOW_RIGHT, 0.24f, 0.37f),
    RegionPos(BodyRegion.ELBOW_LEFT, 0.76f, 0.37f),
    RegionPos(BodyRegion.WRIST_RIGHT, 0.18f, 0.51f),
    RegionPos(BodyRegion.WRIST_LEFT, 0.82f, 0.51f),
    RegionPos(BodyRegion.HAND_RIGHT, 0.14f, 0.58f),
    RegionPos(BodyRegion.HAND_LEFT, 0.86f, 0.58f),
    RegionPos(BodyRegion.FINGERS_RIGHT, 0.12f, 0.64f),
    RegionPos(BodyRegion.FINGERS_LEFT, 0.88f, 0.64f),
    RegionPos(BodyRegion.HIP_RIGHT, 0.40f, 0.53f),
    RegionPos(BodyRegion.HIP_LEFT, 0.60f, 0.53f),
    RegionPos(BodyRegion.KNEE_RIGHT, 0.42f, 0.72f),
    RegionPos(BodyRegion.KNEE_LEFT, 0.58f, 0.72f),
    RegionPos(BodyRegion.ANKLE_RIGHT, 0.43f, 0.87f),
    RegionPos(BodyRegion.ANKLE_LEFT, 0.57f, 0.87f),
    RegionPos(BodyRegion.FOOT_RIGHT, 0.43f, 0.93f),
    RegionPos(BodyRegion.FOOT_LEFT, 0.57f, 0.93f),
)

// Back view. Mirrored left and right, plus the back itself.
private val backPositions = listOf(
    RegionPos(BodyRegion.NECK, 0.50f, 0.15f),
    RegionPos(BodyRegion.BACK, 0.50f, 0.36f),
    RegionPos(BodyRegion.SHOULDER_LEFT, 0.32f, 0.21f),
    RegionPos(BodyRegion.SHOULDER_RIGHT, 0.68f, 0.21f),
    RegionPos(BodyRegion.ELBOW_LEFT, 0.24f, 0.37f),
    RegionPos(BodyRegion.ELBOW_RIGHT, 0.76f, 0.37f),
    RegionPos(BodyRegion.HIP_LEFT, 0.40f, 0.53f),
    RegionPos(BodyRegion.HIP_RIGHT, 0.60f, 0.53f),
    RegionPos(BodyRegion.KNEE_LEFT, 0.42f, 0.72f),
    RegionPos(BodyRegion.KNEE_RIGHT, 0.58f, 0.72f),
)

private val soreLight = Color(0xFFE7B77E)
private val swollenLight = Color(0xFFD98C6A)
private val soreDark = Color(0xFF7A5A34)
private val swollenDark = Color(0xFF7A4634)

/**
 * A simple, original body outline with a tappable dot at each region. Tapping a dot
 * cycles sore, swollen, clear, the same as the list. Every dot carries a spoken
 * description, and a plain list is available too, so this works with TalkBack.
 */
@Composable
fun BodyDiagram(
    marks: Map<BodyRegion, BodyState>,
    onCycle: (BodyRegion) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DiagramLegend()
        Figure(stringResource(R.string.body_figure_front), frontPositions, marks, onCycle)
        Figure(stringResource(R.string.body_figure_back), backPositions, marks, onCycle)
    }
}

@Composable
private fun Figure(
    title: String,
    positions: List<RegionPos>,
    marks: Map<BodyRegion, BodyState>,
    onCycle: (BodyRegion) -> Unit,
) {
    val outline = MaterialTheme.colorScheme.outline
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BoxWithConstraints(
            modifier = Modifier
                .size(width = 190.dp, height = 360.dp),
        ) {
            val w = maxWidth
            val h = maxHeight
            Canvas(modifier = Modifier.size(w, h)) {
                drawFigure(outline)
            }
            positions.forEach { pos ->
                RegionDot(
                    region = pos.region,
                    state = marks[pos.region],
                    boxW = w,
                    boxH = h,
                    fx = pos.fx,
                    fy = pos.fy,
                    onClick = { onCycle(pos.region) },
                )
            }
        }
    }
}

@Composable
private fun RegionDot(
    region: BodyRegion,
    state: BodyState?,
    boxW: androidx.compose.ui.unit.Dp,
    boxH: androidx.compose.ui.unit.Dp,
    fx: Float,
    fy: Float,
    onClick: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val dot = 40.dp
    val fill = when (state) {
        null -> MaterialTheme.colorScheme.surfaceVariant
        BodyState.SORE -> if (dark) soreDark else soreLight
        BodyState.SWOLLEN -> if (dark) swollenDark else swollenLight
    }
    val name = stringResource(region.labelRes())
    val stateWord = when (state) {
        BodyState.SORE -> stringResource(R.string.body_state_sore)
        BodyState.SWOLLEN -> stringResource(R.string.body_state_swollen)
        null -> stringResource(R.string.body_state_none)
    }
    val glyph = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .offset(x = boxW * fx - dot / 2, y = boxH * fy - dot / 2)
            .size(dot)
            .clip(CircleShape)
            .background(fill)
            .border(if (state == BodyState.SWOLLEN) 3.dp else 1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$name, $stateWord" },
        contentAlignment = Alignment.Center,
    ) {
        // A shape as well as colour, so state never depends on colour alone:
        // a small filled dot for sore, a small ring for swollen.
        when (state) {
            BodyState.SORE -> Box(Modifier.size(10.dp).clip(CircleShape).background(glyph))
            BodyState.SWOLLEN -> Box(Modifier.size(16.dp).clip(CircleShape).border(2.dp, glyph, CircleShape))
            null -> {}
        }
    }
}

@Composable
private fun DiagramLegend() {
    val dark = isSystemInDarkTheme()
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(if (dark) soreDark else soreLight, stringResource(R.string.body_state_sore), filled = true)
        LegendItem(if (dark) swollenDark else swollenLight, stringResource(R.string.body_state_swollen), filled = false)
    }
}

@Composable
private fun LegendItem(color: Color, label: String, filled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val glyph = MaterialTheme.colorScheme.onSurface
            if (filled) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(glyph))
            } else {
                Box(Modifier.size(10.dp).clip(CircleShape).border(1.5.dp, glyph, CircleShape))
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Draw a plain, generic body outline as a soft guide behind the dots. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 4f)

    // Head.
    drawCircle(color = color, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.07f), style = stroke)
    // Torso.
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.35f, h * 0.16f),
        size = Size(w * 0.30f, h * 0.37f),
        cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
        style = stroke,
    )
    // Arms.
    drawLine(color, Offset(w * 0.37f, h * 0.20f), Offset(w * 0.14f, h * 0.60f), strokeWidth = 4f)
    drawLine(color, Offset(w * 0.63f, h * 0.20f), Offset(w * 0.86f, h * 0.60f), strokeWidth = 4f)
    // Legs.
    drawLine(color, Offset(w * 0.44f, h * 0.53f), Offset(w * 0.43f, h * 0.97f), strokeWidth = 4f)
    drawLine(color, Offset(w * 0.56f, h * 0.53f), Offset(w * 0.57f, h * 0.97f), strokeWidth = 4f)
}
