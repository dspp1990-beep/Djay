package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight inlined Material Icon definitions.
 * Eliminates material-icons-extended to shrink DEX size from ~55MB to ~4MB,
 * preventing install timeouts and device package-manager stalls.
 */
private fun iconBuilder(name: String, block: ImageVector.Builder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()
}

val Icons.Filled.Refresh: ImageVector by lazy {
    iconBuilder("Refresh") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.65f, 6.35f)
            curveTo(16.2f, 4.9f, 14.21f, 4.0f, 12.0f, 4.0f)
            curveToRelative(-4.42f, 0.0f, -7.99f, 3.58f, -7.99f, 8.0f)
            reflectiveCurveToRelative(3.57f, 8.0f, 7.99f, 8.0f)
            curveToRelative(3.73f, 0.0f, 6.84f, -2.55f, 7.73f, -6.0f)
            horizontalLineToRelative(-2.08f)
            curveToRelative(-0.82f, 2.33f, -3.04f, 4.0f, -5.65f, 4.0f)
            curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
            reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
            curveToRelative(1.66f, 0.0f, 3.14f, 0.69f, 4.22f, 1.78f)
            lineTo(13.0f, 11.0f)
            horizontalLineToRelative(7.0f)
            verticalLineTo(4.0f)
            lineToRelative(-2.35f, 2.35f)
            close()
        }
    }
}

val Icons.Filled.ArrowForward: ImageVector by lazy {
    iconBuilder("ArrowForward") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 4.0f)
            lineToRelative(-1.41f, 1.41f)
            lineTo(16.17f, 11.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(12.17f)
            lineToRelative(-5.58f, 5.59f)
            lineTo(12.0f, 20.0f)
            lineToRelative(8.0f, -8.0f)
            close()
        }
    }
}

val Icons.Filled.Info: ImageVector by lazy {
    iconBuilder("Info") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(13.0f, 17.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-6.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(6.0f)
            close()
            moveTo(13.0f, 9.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(7.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.PlayArrow: ImageVector by lazy {
    iconBuilder("PlayArrow") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(8.0f, 5.0f)
            lineTo(19.0f, 12.0f)
            lineTo(8.0f, 19.0f)
            close()
        }
    }
}

val Icons.Filled.CheckCircle: ImageVector by lazy {
    iconBuilder("CheckCircle") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(10.0f, 17.0f)
            lineToRelative(-5.0f, -5.0f)
            lineToRelative(1.41f, -1.41f)
            lineTo(10.0f, 14.17f)
            lineToRelative(7.59f, -7.59f)
            lineTo(19.0f, 8.0f)
            lineToRelative(-9.0f, 9.0f)
            close()
        }
    }
}

val Icons.Filled.WaterDrop: ImageVector by lazy {
    iconBuilder("WaterDrop") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 2.69f)
            lineTo(6.42f, 8.27f)
            curveToRelative(-3.12f, 3.12f, -3.12f, 8.19f, 0.0f, 11.31f)
            curveToRelative(3.12f, 3.12f, 8.19f, 3.12f, 11.31f, 0.0f)
            curveToRelative(3.12f, -3.12f, 3.12f, -8.19f, 0.0f, -11.31f)
            lineTo(12.0f, 2.69f)
            close()
            moveTo(12.0f, 20.0f)
            curveToRelative(-2.21f, 0.0f, -4.0f, -1.79f, -4.0f, -4.0f)
            curveToRelative(0.0f, -1.5f, 1.0f, -3.0f, 2.5f, -4.5f)
            lineTo(12.0f, 9.8f)
            lineToRelative(1.5f, 1.7f)
            curveToRelative(1.5f, 1.5f, 2.5f, 3.0f, 2.5f, 4.5f)
            curveToRelative(0.0f, 2.21f, -1.79f, 4.0f, -4.0f, 4.0f)
            close()
        }
    }
}

val Icons.Filled.Water: ImageVector by lazy {
    iconBuilder("Water") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 3.0f)
            lineTo(7.0f, 9.0f)
            curveToRelative(-2.5f, 3.0f, -2.5f, 7.0f, 0.0f, 9.5f)
            curveToRelative(2.5f, 2.5f, 7.5f, 2.5f, 10.0f, 0.0f)
            curveToRelative(2.5f, -2.5f, 2.5f, -6.5f, 0.0f, -9.5f)
            lineTo(12.0f, 3.0f)
            close()
        }
    }
}

val Icons.Filled.Speed: ImageVector by lazy {
    iconBuilder("Speed") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.38f, 8.57f)
            lineToRelative(-1.23f, 1.85f)
            curveToRelative(0.53f, 0.98f, 0.85f, 2.1f, 0.85f, 3.3f)
            curveToRelative(0.0f, 4.41f, -3.59f, 8.0f, -8.0f, 8.0f)
            reflectiveCurveToRelative(-8.0f, -3.59f, -8.0f, -8.0f)
            curveToRelative(0.0f, -4.41f, 3.59f, -8.0f, 8.0f, -8.0f)
            curveToRelative(1.88f, 0.0f, 3.6f, 0.65f, 4.97f, 1.74f)
            lineToRelative(1.45f, -1.45f)
            curveTo(16.71f, 4.67f, 14.48f, 4.0f, 12.0f, 4.0f)
            curveTo(6.48f, 4.0f, 2.0f, 8.48f, 2.0f, 14.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            curveToRelative(0.0f, -1.97f, -0.58f, -3.8f, -1.62f, -5.43f)
            close()
            moveTo(10.59f, 15.41f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
            lineToRelative(4.5f, -4.5f)
            lineToRelative(-1.41f, -1.41f)
            lineToRelative(-3.79f, 3.79f)
            lineToRelative(-0.71f, -0.71f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
            lineToRelative(1.41f, 1.42f)
            close()
        }
    }
}

val Icons.Filled.ElectricBolt: ImageVector by lazy {
    iconBuilder("ElectricBolt") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.0f, 2.0f)
            lineTo(6.0f, 13.0f)
            horizontalLineToRelative(5.0f)
            lineTo(8.5f, 22.0f)
            lineTo(18.0f, 11.0f)
            horizontalLineToRelative(-5.0f)
            lineTo(15.0f, 2.0f)
            close()
        }
    }
}

val Icons.Filled.Bolt: ImageVector by lazy {
    iconBuilder("Bolt") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.0f, 21.0f)
            horizontalLineToRelative(-1.0f)
            lineToRelative(1.0f, -7.0f)
            horizontalLineTo(7.5f)
            curveToRelative(-0.58f, 0.0f, -0.57f, -0.32f, -0.38f, -0.66f)
            curveToRelative(0.19f, -0.34f, 0.05f, -0.08f, 0.07f, -0.12f)
            curveTo(8.48f, 10.94f, 10.42f, 7.54f, 13.0f, 3.0f)
            horizontalLineToRelative(1.0f)
            lineToRelative(-1.0f, 7.0f)
            horizontalLineToRelative(3.5f)
            curveToRelative(0.49f, 0.0f, 0.56f, 0.33f, 0.47f, 0.51f)
            lineToRelative(-0.07f, 0.15f)
            curveTo(12.97f, 17.55f, 11.0f, 21.0f, 11.0f, 21.0f)
            close()
        }
    }
}

val Icons.Filled.PowerSettingsNew: ImageVector by lazy {
    iconBuilder("PowerSettingsNew") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(13.0f, 3.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(10.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(3.0f)
            close()
            moveTo(17.83f, 5.17f)
            lineToRelative(-1.42f, 1.42f)
            curveTo(18.25f, 8.12f, 19.0f, 10.47f, 19.0f, 13.0f)
            curveToRelative(0.0f, 3.87f, -3.13f, 7.0f, -7.0f, 7.0f)
            reflectiveCurveToRelative(-7.0f, -3.13f, -7.0f, -7.0f)
            curveToRelative(0.0f, -2.53f, 0.75f, -4.88f, 2.59f, -6.41f)
            lineTo(6.17f, 5.17f)
            curveTo(3.99f, 7.15f, 3.0f, 9.98f, 3.0f, 13.0f)
            curveToRelative(0.0f, 4.97f, 4.03f, 9.0f, 9.0f, 9.0f)
            reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
            curveToRelative(0.0f, -3.02f, -0.99f, -5.85f, -3.17f, -7.83f)
            close()
        }
    }
}

val Icons.Filled.AccountTree: ImageVector by lazy {
    iconBuilder("AccountTree") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(22.0f, 11.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-7.0f)
            verticalLineToRelative(3.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(2.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(7.0f)
            verticalLineTo(8.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(10.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(7.0f)
            verticalLineToRelative(-8.0f)
            horizontalLineToRelative(-7.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(8.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(7.0f)
            close()
        }
    }
}

val Icons.Filled.GridView: ImageVector by lazy {
    iconBuilder("GridView") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3.0f, 3.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(8.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(13.0f, 3.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(8.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-8.0f)
            close()
            moveTo(3.0f, 13.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(-8.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(13.0f, 13.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(-8.0f)
            horizontalLineToRelative(-8.0f)
            close()
        }
    }
}

val Icons.Filled.Schedule: ImageVector by lazy {
    iconBuilder("Schedule") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.99f, 2.0f)
            curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
            curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
            reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
            close()
            moveTo(12.0f, 20.0f)
            curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
            reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
            reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
            reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
            close()
            moveTo(12.5f, 7.0f)
            horizontalLineTo(11.0f)
            verticalLineToRelative(6.0f)
            lineToRelative(5.25f, 3.15f)
            lineToRelative(0.75f, -1.23f)
            lineToRelative(-4.5f, -2.67f)
            verticalLineTo(7.0f)
            close()
        }
    }
}

val Icons.Filled.Grass: ImageVector by lazy {
    iconBuilder("Grass") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.0f, 20.0f)
            horizontalLineToRelative(16.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(6.0f, 16.0f)
            curveToRelative(0.0f, -4.0f, 3.0f, -8.0f, 6.0f, -10.0f)
            curveToRelative(3.0f, 2.0f, 6.0f, 6.0f, 6.0f, 10.0f)
            horizontalLineTo(6.0f)
            close()
        }
    }
}

val Icons.Filled.Air: ImageVector by lazy {
    iconBuilder("Air") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(14.5f, 17.0f)
            curveToRelative(0.0f, 1.65f, -1.35f, 3.0f, -3.0f, 3.0f)
            reflectiveCurveToRelative(-3.0f, -1.35f, -3.0f, -3.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
            reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
            reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineTo(2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(9.5f)
            curveToRelative(1.65f, 0.0f, 3.0f, 1.35f, 3.0f, 3.0f)
            close()
            moveTo(19.0f, 11.0f)
            curveToRelative(0.0f, 1.65f, -1.35f, 3.0f, -3.0f, 3.0f)
            reflectiveCurveToRelative(-3.0f, -1.35f, -3.0f, -3.0f)
            reflectiveCurveToRelative(1.35f, -3.0f, 3.0f, -3.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
            reflectiveCurveToRelative(-3.0f, 1.35f, -3.0f, 3.0f)
            horizontalLineTo(2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.65f, 0.0f, 3.0f, -1.35f, 3.0f, -3.0f)
            close()
        }
    }
}

val Icons.Filled.Opacity: ImageVector by lazy {
    iconBuilder("Opacity") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.66f, 8.0f)
            lineTo(12.0f, 2.35f)
            lineTo(6.34f, 8.0f)
            curveTo(4.78f, 9.56f, 4.0f, 11.64f, 4.0f, 13.72f)
            curveToRelative(0.0f, 4.42f, 3.58f, 8.0f, 8.0f, 8.0f)
            reflectiveCurveToRelative(8.0f, -3.58f, 8.0f, -8.0f)
            curveToRelative(0.0f, -2.08f, -0.78f, -4.16f, -2.34f, -5.72f)
            close()
            moveTo(12.0f, 19.72f)
            curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
            curveToRelative(0.0f, -1.56f, 0.62f, -3.04f, 1.76f, -4.17f)
            lineTo(12.0f, 5.27f)
            lineToRelative(4.24f, 4.28f)
            curveTo(17.38f, 10.68f, 18.0f, 12.16f, 18.0f, 13.72f)
            curveToRelative(0.0f, 3.31f, -2.69f, 6.0f, -6.0f, 6.0f)
            close()
        }
    }
}

val Icons.Filled.Science: ImageVector by lazy {
    iconBuilder("Science") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.79f, 19.34f)
            lineToRelative(-5.79f, -7.63f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(1.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(8.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(6.71f)
            lineTo(3.21f, 19.34f)
            curveToRelative(-0.67f, 0.88f, -0.04f, 2.16f, 1.07f, 2.16f)
            horizontalLineToRelative(15.44f)
            curveToRelative(1.11f, 0.0f, 1.74f, -1.28f, 1.07f, -2.16f)
            close()
            moveTo(6.0f, 19.0f)
            lineToRelative(4.0f, -5.27f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(8.73f)
            lineTo(18.0f, 19.0f)
            horizontalLineTo(6.0f)
            close()
        }
    }
}

val Icons.Filled.FastForward: ImageVector by lazy {
    iconBuilder("FastForward") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.0f, 18.0f)
            lineToRelative(8.5f, -6.0f)
            lineTo(4.0f, 6.0f)
            verticalLineToRelative(12.0f)
            close()
            moveTo(13.0f, 6.0f)
            verticalLineToRelative(12.0f)
            lineToRelative(8.5f, -6.0f)
            lineTo(13.0f, 6.0f)
            close()
        }
    }
}

val Icons.Filled.History: ImageVector by lazy {
    iconBuilder("History") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(13.0f, 3.0f)
            curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
            horizontalLineTo(1.0f)
            lineToRelative(3.89f, 3.89f)
            lineToRelative(0.07f, 0.14f)
            lineTo(9.0f, 12.0f)
            horizontalLineTo(6.0f)
            curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
            reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
            reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
            curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
            lineToRelative(-1.42f, 1.42f)
            curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
            curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
            reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
            close()
            moveTo(12.0f, 8.0f)
            verticalLineToRelative(5.0f)
            lineToRelative(4.28f, 2.54f)
            lineToRelative(0.72f, -1.21f)
            lineToRelative(-3.5f, -2.08f)
            verticalLineTo(8.0f)
            horizontalLineTo(12.0f)
            close()
        }
    }
}

val Icons.Filled.Pause: ImageVector by lazy {
    iconBuilder("Pause") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6.0f, 19.0f)
            horizontalLineToRelative(4.0f)
            verticalLineTo(5.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(14.0f)
            close()
            moveTo(14.0f, 5.0f)
            verticalLineToRelative(14.0f)
            horizontalLineToRelative(4.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(-4.0f)
            close()
        }
    }
}

val Icons.Filled.SettingsSuggest: ImageVector by lazy {
    iconBuilder("SettingsSuggest") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.43f, 12.98f)
            curveToRelative(0.04f, -0.32f, 0.07f, -0.64f, 0.07f, -0.98f)
            reflectiveCurveToRelative(-0.03f, -0.66f, -0.07f, -0.98f)
            lineToRelative(2.11f, -1.65f)
            curveToRelative(0.19f, -0.15f, 0.24f, -0.42f, 0.12f, -0.64f)
            lineToRelative(-2.0f, -3.46f)
            curveToRelative(-0.12f, -0.22f, -0.39f, -0.3f, -0.61f, -0.22f)
            lineToRelative(-2.49f, 1.0f)
            curveToRelative(-0.52f, -0.4f, -1.08f, -0.73f, -1.69f, -0.98f)
            lineToRelative(-0.38f, -2.65f)
            curveTo(14.46f, 2.18f, 14.25f, 2.0f, 14.0f, 2.0f)
            horizontalLineToRelative(-4.0f)
            curveToRelative(-0.25f, 0.0f, -0.46f, 0.18f, -0.49f, 0.42f)
            lineToRelative(-0.38f, 2.65f)
            curveToRelative(-0.61f, 0.25f, -1.17f, 0.59f, -1.69f, 0.98f)
            lineToRelative(-2.49f, -1.0f)
            curveToRelative(-0.23f, -0.09f, -0.49f, 0.0f, -0.61f, 0.22f)
            lineToRelative(-2.0f, 3.46f)
            curveToRelative(-0.13f, 0.22f, -0.07f, 0.49f, 0.12f, 0.64f)
            lineToRelative(2.11f, 1.65f)
            curveToRelative(-0.04f, 0.32f, -0.07f, 0.65f, -0.07f, 0.98f)
            reflectiveCurveToRelative(0.03f, 0.66f, 0.07f, 0.98f)
            lineToRelative(-2.11f, 1.65f)
            curveToRelative(-0.19f, 0.15f, -0.24f, 0.42f, -0.12f, 0.64f)
            lineToRelative(2.0f, 3.46f)
            curveToRelative(0.12f, 0.22f, 0.39f, 0.3f, 0.61f, 0.22f)
            lineToRelative(2.49f, -1.0f)
            curveToRelative(0.52f, 0.4f, 1.08f, 0.73f, 1.69f, 0.98f)
            lineToRelative(0.38f, 2.65f)
            curveToRelative(0.03f, 0.24f, 0.24f, 0.42f, 0.49f, 0.42f)
            horizontalLineToRelative(4.0f)
            curveToRelative(0.25f, 0.0f, 0.46f, -0.18f, 0.49f, -0.42f)
            lineToRelative(0.38f, -2.65f)
            curveToRelative(0.61f, -0.25f, 1.17f, -0.59f, 1.69f, -0.98f)
            lineToRelative(2.49f, 1.0f)
            curveToRelative(0.23f, 0.09f, 0.49f, 0.0f, 0.61f, -0.22f)
            lineToRelative(2.0f, -3.46f)
            curveToRelative(0.12f, -0.22f, 0.07f, -0.49f, -0.12f, -0.64f)
            lineToRelative(-2.11f, -1.65f)
            close()
            moveTo(12.0f, 15.5f)
            curveToRelative(-1.93f, 0.0f, -3.5f, -1.57f, -3.5f, -3.5f)
            reflectiveCurveToRelative(1.57f, -3.5f, 3.5f, -3.5f)
            reflectiveCurveToRelative(3.5f, 1.57f, 3.5f, 3.5f)
            reflectiveCurveToRelative(-1.57f, 3.5f, -3.5f, 3.5f)
            close()
        }
    }
}

val Icons.Filled.DirectionsWalk: ImageVector by lazy {
    iconBuilder("DirectionsWalk") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(13.5f, 5.5f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
            reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
            reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
            close()
            moveTo(9.8f, 8.9f)
            lineTo(7.0f, 23.0f)
            horizontalLineToRelative(2.1f)
            lineToRelative(1.8f, -8.0f)
            lineToRelative(2.1f, 2.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-7.5f)
            lineToRelative(-2.1f, -2.0f)
            lineToRelative(0.6f, -3.0f)
            curveTo(14.8f, 12.0f, 16.8f, 13.0f, 19.0f, 13.0f)
            verticalLineToRelative(-2.0f)
            curveToRelative(-1.9f, 0.0f, -3.5f, -1.0f, -4.3f, -2.4f)
            lineToRelative(-1.0f, -1.6f)
            curveToRelative(-0.4f, -0.6f, -1.0f, -1.0f, -1.7f, -1.0f)
            curveToRelative(-0.3f, 0.0f, -0.5f, 0.1f, -0.8f, 0.1f)
            lineTo(6.0f, 8.3f)
            verticalLineTo(13.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(9.6f)
            lineToRelative(1.8f, -0.7f)
            close()
        }
    }
}

val Icons.Filled.FilterAlt: ImageVector by lazy {
    iconBuilder("FilterAlt") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.25f, 5.61f)
            curveTo(6.27f, 8.2f, 10.0f, 13.0f, 10.0f, 13.0f)
            verticalLineToRelative(6.0f)
            curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            verticalLineToRelative(-6.0f)
            curveToRelative(0.0f, 0.0f, 3.72f, -4.8f, 5.74f, -7.39f)
            curveTo(20.25f, 4.95f, 19.78f, 4.0f, 18.95f, 4.0f)
            horizontalLineTo(5.04f)
            curveToRelative(-0.83f, 0.0f, -1.3f, 0.95f, -0.79f, 1.61f)
            close()
        }
    }
}

val Icons.Filled.FilterList: ImageVector by lazy {
    iconBuilder("FilterList") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(10.0f, 18.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(3.0f, 6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(18.0f)
            verticalLineTo(6.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(6.0f, 13.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.Layers: ImageVector by lazy {
    iconBuilder("Layers") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.99f, 18.54f)
            lineToRelative(-7.37f, -5.73f)
            lineTo(3.0f, 14.07f)
            lineToRelative(9.0f, 7.0f)
            lineToRelative(9.0f, -7.0f)
            lineToRelative(-1.63f, -1.27f)
            lineToRelative(-7.38f, 5.74f)
            close()
            moveTo(12.0f, 16.0f)
            lineToRelative(7.36f, -5.73f)
            lineTo(21.0f, 9.0f)
            lineToRelative(-9.0f, -7.0f)
            lineToRelative(-9.0f, 7.0f)
            lineToRelative(1.63f, 1.27f)
            lineTo(12.0f, 16.0f)
            close()
        }
    }
}

val Icons.Filled.LocationSearching: ImageVector by lazy {
    iconBuilder("LocationSearching") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.94f, 11.0f)
            curveToRelative(-0.46f, -4.17f, -3.77f, -7.48f, -7.94f, -7.94f)
            verticalLineTo(1.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.06f)
            curveTo(6.83f, 3.52f, 3.52f, 6.83f, 3.06f, 11.0f)
            horizontalLineTo(1.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.06f)
            curveToRelative(0.46f, 4.17f, 3.77f, 7.48f, 7.94f, 7.94f)
            verticalLineTo(23.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-2.06f)
            curveToRelative(4.17f, -0.46f, 7.48f, -3.77f, 7.94f, -7.94f)
            horizontalLineTo(23.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.06f)
            close()
            moveTo(12.0f, 19.0f)
            curveToRelative(-3.87f, 0.0f, -7.0f, -3.13f, -7.0f, -7.0f)
            reflectiveCurveToRelative(3.13f, -7.0f, 7.0f, -7.0f)
            reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
            reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
            close()
        }
    }
}

val Icons.Filled.MyLocation: ImageVector by lazy {
    iconBuilder("MyLocation") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 8.0f)
            curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
            reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
            reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
            reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
            close()
            moveTo(20.94f, 11.0f)
            curveToRelative(-0.46f, -4.17f, -3.77f, -7.48f, -7.94f, -7.94f)
            verticalLineTo(1.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.06f)
            curveTo(6.83f, 3.52f, 3.52f, 6.83f, 3.06f, 11.0f)
            horizontalLineTo(1.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.06f)
            curveToRelative(0.46f, 4.17f, 3.77f, 7.48f, 7.94f, 7.94f)
            verticalLineTo(23.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-2.06f)
            curveToRelative(4.17f, -0.46f, 7.48f, -3.77f, 7.94f, -7.94f)
            horizontalLineTo(23.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.06f)
            close()
            moveTo(12.0f, 19.0f)
            curveToRelative(-3.87f, 0.0f, -7.0f, -3.13f, -7.0f, -7.0f)
            reflectiveCurveToRelative(3.13f, -7.0f, 7.0f, -7.0f)
            reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
            reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
            close()
        }
    }
}

val Icons.Filled.ZoomOutMap: ImageVector by lazy {
    iconBuilder("ZoomOutMap") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.0f, 3.0f)
            lineToRelative(2.3f, 2.3f)
            lineToRelative(-2.89f, 2.87f)
            lineToRelative(1.42f, 1.42f)
            lineTo(18.7f, 6.7f)
            lineTo(21.0f, 9.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-6.0f)
            close()
            moveTo(3.0f, 9.0f)
            lineToRelative(2.3f, -2.3f)
            lineToRelative(2.87f, 2.89f)
            lineToRelative(1.42f, -1.42f)
            lineTo(6.7f, 5.3f)
            lineTo(9.0f, 3.0f)
            horizontalLineTo(3.0f)
            verticalLineToRelative(6.0f)
            close()
            moveTo(9.0f, 21.0f)
            lineToRelative(-2.3f, -2.3f)
            lineToRelative(2.89f, -2.87f)
            lineToRelative(-1.42f, -1.42f)
            lineTo(5.3f, 17.3f)
            lineTo(3.0f, 15.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(6.0f)
            close()
            moveTo(21.0f, 15.0f)
            lineToRelative(-2.3f, 2.3f)
            lineToRelative(-2.87f, -2.89f)
            lineToRelative(-1.42f, 1.42f)
            lineToRelative(2.89f, 2.87f)
            lineTo(15.0f, 21.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(-6.0f)
            close()
        }
    }
}

val Icons.Filled.Apartment: ImageVector by lazy {
    iconBuilder("Apartment") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.0f, 11.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(7.0f)
            verticalLineToRelative(4.0f)
            horizontalLineTo(3.0f)
            verticalLineToRelative(14.0f)
            horizontalLineToRelative(18.0f)
            verticalLineTo(11.0f)
            horizontalLineToRelative(-4.0f)
            close()
            moveTo(7.0f, 19.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(7.0f, 15.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(7.0f, 11.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(9.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(11.0f, 19.0f)
            horizontalLineTo(9.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(11.0f, 15.0f)
            horizontalLineTo(9.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(11.0f, 11.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(9.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(11.0f, 7.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(15.0f, 19.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(15.0f, 15.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(15.0f, 11.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(9.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(15.0f, 7.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(19.0f, 19.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(19.0f, 15.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.Thermostat: ImageVector by lazy {
    iconBuilder("Thermostat") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.0f, 13.0f)
            verticalLineTo(5.0f)
            curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
            reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
            verticalLineToRelative(8.0f)
            curveToRelative(-1.21f, 0.91f, -2.0f, 2.37f, -2.0f, 4.0f)
            curveToRelative(0.0f, 2.76f, 2.24f, 5.0f, 5.0f, 5.0f)
            reflectiveCurveToRelative(5.0f, -2.24f, 5.0f, -5.0f)
            curveToRelative(0.0f, -1.63f, -0.79f, -3.09f, -2.0f, -4.0f)
            close()
            moveTo(11.0f, 5.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(11.0f)
            verticalLineTo(5.0f)
            close()
        }
    }
}

val Icons.Filled.ZoomIn: ImageVector by lazy {
    iconBuilder("ZoomIn") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14.0f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
            curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
            reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
            reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
            curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
            lineToRelative(0.27f, 0.28f)
            verticalLineToRelative(0.79f)
            lineToRelative(5.0f, 4.99f)
            lineTo(20.49f, 19.0f)
            lineToRelative(-4.99f, -5.0f)
            close()
            moveTo(9.5f, 14.0f)
            curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
            reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
            reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
            reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
            close()
            moveTo(12.0f, 10.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(9.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(9.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(7.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(1.0f)
            close()
        }
    }
}

val Icons.Filled.ZoomOut: ImageVector by lazy {
    iconBuilder("ZoomOut") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14.0f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
            curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
            reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
            reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
            curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
            lineToRelative(0.27f, 0.28f)
            verticalLineToRelative(0.79f)
            lineToRelative(5.0f, 4.99f)
            lineTo(20.49f, 19.0f)
            lineToRelative(-4.99f, -5.0f)
            close()
            moveTo(9.5f, 14.0f)
            curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
            reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
            reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
            reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
            close()
            moveTo(7.0f, 9.0f)
            horizontalLineToRelative(5.0f)
            verticalLineToRelative(1.0f)
            horizontalLineTo(7.0f)
            close()
        }
    }
}

val Icons.Filled.Edit: ImageVector by lazy {
    iconBuilder("Edit") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3.0f, 17.25f)
            verticalLineTo(21.0f)
            horizontalLineToRelative(3.75f)
            lineTo(17.81f, 9.94f)
            lineToRelative(-3.75f, -3.75f)
            lineTo(3.0f, 17.25f)
            close()
            moveTo(20.71f, 7.04f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
            lineToRelative(-2.34f, -2.34f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
            lineToRelative(-1.83f, 1.83f)
            lineToRelative(3.75f, 3.75f)
            lineToRelative(1.83f, -1.83f)
            close()
        }
    }
}

val Icons.Filled.Add: ImageVector by lazy {
    iconBuilder("Add") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.0f, 13.0f)
            horizontalLineToRelative(-6.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-6.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(6.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.Delete: ImageVector by lazy {
    iconBuilder("Delete") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6.0f, 19.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(8.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(7.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(12.0f)
            close()
            moveTo(19.0f, 4.0f)
            horizontalLineToRelative(-3.5f)
            lineToRelative(-1.0f, -1.0f)
            horizontalLineToRelative(-5.0f)
            lineToRelative(-1.0f, 1.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(14.0f)
            verticalLineTo(4.0f)
            close()
        }
    }
}

val Icons.Filled.PinDrop: ImageVector by lazy {
    iconBuilder("PinDrop") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.0f, 8.0f)
            curveToRelative(0.0f, -3.31f, -2.69f, -6.0f, -6.0f, -6.0f)
            reflectiveCurveTo(6.0f, 4.69f, 6.0f, 8.0f)
            curveToRelative(0.0f, 4.5f, 6.0f, 11.0f, 6.0f, 11.0f)
            reflectiveCurveToRelative(6.0f, -6.5f, 6.0f, -11.0f)
            close()
            moveTo(10.0f, 8.0f)
            curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
            reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
            reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
            reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
            close()
            moveTo(5.0f, 20.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(5.0f)
            close()
        }
    }
}

val Icons.Filled.Save: ImageVector by lazy {
    iconBuilder("Save") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.0f, 3.0f)
            horizontalLineTo(5.0f)
            curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(7.0f)
            lineToRelative(-4.0f, -4.0f)
            close()
            moveTo(12.0f, 19.0f)
            curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
            reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
            reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
            reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
            close()
            moveTo(15.0f, 9.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(10.0f)
            verticalLineToRelative(4.0f)
            close()
        }
    }
}

val Icons.Filled.Search: ImageVector by lazy {
    iconBuilder("Search") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14.0f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
            curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
            reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
            reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
            curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
            lineToRelative(0.27f, 0.28f)
            verticalLineToRelative(0.79f)
            lineToRelative(5.0f, 4.99f)
            lineTo(20.49f, 19.0f)
            lineToRelative(-4.99f, -5.0f)
            close()
            moveTo(9.5f, 14.0f)
            curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
            reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
            reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
            reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
            close()
        }
    }
}

val Icons.Filled.Tune: ImageVector by lazy {
    iconBuilder("Tune") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3.0f, 17.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(3.0f, 5.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(10.0f)
            verticalLineTo(5.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(13.0f, 21.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-8.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(2.0f)
            close()
            moveTo(7.0f, 9.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(3.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(7.0f)
            close()
            moveTo(21.0f, 13.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(11.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(10.0f)
            close()
            moveTo(17.0f, 9.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(7.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(6.0f)
            close()
        }
    }
}

val Icons.Filled.CalendarMonth: ImageVector by lazy {
    iconBuilder("CalendarMonth") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.0f, 4.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineTo(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(8.0f)
            verticalLineTo(2.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(5.0f)
            curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
            lineTo(3.0f, 20.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(6.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(19.0f, 20.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(10.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(10.0f)
            close()
            moveTo(19.0f, 8.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(6.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.ExpandLess: ImageVector by lazy {
    iconBuilder("ExpandLess") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 8.0f)
            lineToRelative(-6.0f, 6.0f)
            lineToRelative(1.41f, 1.41f)
            lineTo(12.0f, 10.83f)
            lineToRelative(4.59f, 4.58f)
            lineTo(18.0f, 14.0f)
            close()
        }
    }
}

val Icons.Filled.ExpandMore: ImageVector by lazy {
    iconBuilder("ExpandMore") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16.59f, 8.59f)
            lineTo(12.0f, 13.17f)
            lineTo(7.41f, 8.59f)
            lineTo(6.0f, 10.0f)
            lineToRelative(6.0f, 6.0f)
            lineToRelative(6.0f, -6.0f)
            close()
        }
    }
}

val Icons.Filled.FiberManualRecord: ImageVector by lazy {
    iconBuilder("FiberManualRecord") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 12.0f)
            moveToRelative(-8.0f, 0.0f)
            curveToRelative(0.0f, -4.42f, 3.58f, -8.0f, 8.0f, -8.0f)
            curveToRelative(4.42f, 0.0f, 8.0f, 3.58f, 8.0f, 8.0f)
            curveToRelative(0.0f, 4.42f, -3.58f, 8.0f, -8.0f, 8.0f)
            curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
            close()
        }
    }
}

val Icons.Filled.Map: ImageVector by lazy {
    iconBuilder("Map") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.5f, 3.0f)
            lineToRelative(-0.16f, 0.03f)
            lineTo(15.0f, 5.1f)
            lineTo(9.0f, 3.0f)
            lineTo(3.36f, 4.9f)
            curveToRelative(-0.21f, 0.07f, -0.36f, 0.25f, -0.36f, 0.48f)
            verticalLineTo(20.5f)
            curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
            lineToRelative(0.16f, -0.03f)
            lineTo(9.0f, 18.9f)
            lineToRelative(6.0f, 2.1f)
            lineToRelative(5.64f, -1.9f)
            curveToRelative(0.21f, -0.07f, 0.36f, -0.25f, 0.36f, -0.48f)
            verticalLineTo(3.5f)
            curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
            close()
            moveTo(15.0f, 19.0f)
            lineToRelative(-6.0f, -2.11f)
            verticalLineTo(5.0f)
            lineToRelative(6.0f, 2.11f)
            verticalLineTo(19.0f)
            close()
        }
    }
}

val Icons.Filled.FitScreen: ImageVector by lazy {
    iconBuilder("FitScreen") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.0f, 4.0f)
            horizontalLineToRelative(3.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(6.0f)
            horizontalLineToRelative(-3.0f)
            verticalLineTo(4.0f)
            close()
            moveTo(4.0f, 9.0f)
            verticalLineTo(6.0f)
            horizontalLineToRelative(3.0f)
            verticalLineTo(4.0f)
            horizontalLineTo(4.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(2.0f)
            close()
            moveTo(20.0f, 15.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(-3.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(3.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-3.0f)
            horizontalLineToRelative(-2.0f)
            close()
            moveTo(7.0f, 18.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(-3.0f)
            horizontalLineTo(2.0f)
            verticalLineToRelative(3.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(3.0f)
            verticalLineToRelative(-2.0f)
            close()
            moveTo(18.0f, 8.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(8.0f)
            horizontalLineToRelative(12.0f)
            verticalLineTo(8.0f)
            close()
        }
    }
}

val Icons.Filled.ContentCopy: ImageVector by lazy {
    iconBuilder("ContentCopy") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16.0f, 1.0f)
            horizontalLineTo(4.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(12.0f)
            verticalLineTo(1.0f)
            close()
            moveTo(19.0f, 5.0f)
            horizontalLineTo(8.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(11.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(7.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(19.0f, 21.0f)
            horizontalLineTo(8.0f)
            verticalLineTo(7.0f)
            horizontalLineToRelative(11.0f)
            verticalLineToRelative(14.0f)
            close()
        }
    }
}

val Icons.Filled.Explore: ImageVector by lazy {
    iconBuilder("Explore") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 10.9f)
            curveToRelative(-0.61f, 0.0f, -1.1f, 0.49f, -1.1f, 1.1f)
            reflectiveCurveToRelative(0.49f, 1.1f, 1.1f, 1.1f)
            reflectiveCurveToRelative(1.1f, -0.49f, 1.1f, -1.1f)
            reflectiveCurveToRelative(-0.49f, -1.1f, -1.1f, -1.1f)
            close()
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(14.19f, 14.19f)
            lineTo(6.0f, 18.0f)
            lineToRelative(3.81f, -8.19f)
            lineTo(18.0f, 6.0f)
            lineToRelative(-3.81f, 8.19f)
            close()
        }
    }
}

val Icons.Filled.FileUpload: ImageVector by lazy {
    iconBuilder("FileUpload") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(9.0f, 16.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(-6.0f)
            horizontalLineToRelative(4.0f)
            lineToRelative(-7.0f, -7.0f)
            lineToRelative(-7.0f, 7.0f)
            horizontalLineToRelative(4.0f)
            close()
            moveTo(5.0f, 18.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(5.0f)
            close()
        }
    }
}

val Icons.Filled.Folder: ImageVector by lazy {
    iconBuilder("Folder") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(10.0f, 4.0f)
            horizontalLineTo(4.0f)
            curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
            lineTo(2.0f, 18.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(8.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            horizontalLineToRelative(-8.0f)
            lineToRelative(-2.0f, -2.0f)
            close()
        }
    }
}

val Icons.Filled.LinearScale: ImageVector by lazy {
    iconBuilder("LinearScale") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.5f, 9.5f)
            curveToRelative(-1.03f, 0.0f, -1.9f, 0.62f, -2.29f, 1.5f)
            horizontalLineToRelative(-2.92f)
            curveToRelative(-0.39f, -0.88f, -1.26f, -1.5f, -2.29f, -1.5f)
            reflectiveCurveToRelative(-1.9f, 0.62f, -2.29f, 1.5f)
            horizontalLineTo(6.79f)
            curveToRelative(-0.39f, -0.88f, -1.26f, -1.5f, -2.29f, -1.5f)
            curveTo(3.12f, 9.5f, 2.0f, 10.62f, 2.0f, 12.0f)
            reflectiveCurveToRelative(1.12f, 2.5f, 2.5f, 2.5f)
            curveToRelative(1.03f, 0.0f, 1.9f, -0.62f, 2.29f, -1.5f)
            horizontalLineToRelative(2.92f)
            curveToRelative(0.39f, 0.88f, 1.26f, 1.5f, 2.29f, 1.5f)
            reflectiveCurveToRelative(1.9f, -0.62f, 2.29f, -1.5f)
            horizontalLineToRelative(2.92f)
            curveToRelative(0.39f, 0.88f, 1.26f, 1.5f, 2.29f, 1.5f)
            curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
            reflectiveCurveToRelative(-1.12f, -2.5f, -2.5f, -2.5f)
            close()
        }
    }
}

val Icons.Filled.NearMe: ImageVector by lazy {
    iconBuilder("NearMe") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(21.0f, 3.0f)
            lineTo(3.0f, 10.53f)
            verticalLineToRelative(0.95f)
            lineToRelative(6.84f, 2.68f)
            lineTo(12.53f, 21.0f)
            horizontalLineToRelative(0.95f)
            lineTo(21.0f, 3.0f)
            close()
        }
    }
}

val Icons.Filled.Public: ImageVector by lazy {
    iconBuilder("Public") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(11.0f, 19.93f)
            curveToRelative(-3.95f, -0.49f, -7.0f, -3.85f, -7.0f, -7.93f)
            curveToRelative(0.0f, -0.62f, 0.08f, -1.21f, 0.21f, -1.79f)
            lineTo(9.0f, 15.0f)
            verticalLineToRelative(1.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            verticalLineToRelative(1.93f)
            close()
            moveTo(17.9f, 17.39f)
            curveToRelative(-0.26f, -0.81f, -1.0f, -1.39f, -1.9f, -1.39f)
            horizontalLineToRelative(-1.0f)
            verticalLineToRelative(-3.0f)
            curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineTo(8.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            verticalLineTo(7.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-0.41f)
            curveToRelative(2.93f, 1.19f, 5.0f, 4.06f, 5.0f, 7.41f)
            curveToRelative(0.0f, 2.08f, -0.8f, 3.97f, -2.1f, 5.39f)
            close()
        }
    }
}

val Icons.Filled.Remove: ImageVector by lazy {
    iconBuilder("Remove") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.0f, 13.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}

val Icons.Filled.SquareFoot: ImageVector by lazy {
    iconBuilder("SquareFoot") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17.66f, 17.66f)
            lineToRelative(-1.06f, 1.06f)
            lineToRelative(-0.71f, -0.71f)
            lineToRelative(1.06f, -1.06f)
            lineToRelative(-1.94f, -1.94f)
            lineToRelative(-1.06f, 1.06f)
            lineToRelative(-0.71f, -0.71f)
            lineToRelative(1.06f, -1.06f)
            lineToRelative(-1.94f, -1.94f)
            lineToRelative(-1.06f, 1.06f)
            lineToRelative(-0.71f, -0.71f)
            lineToRelative(1.06f, -1.06f)
            lineTo(9.7f, 9.7f)
            lineToRelative(-1.06f, 1.06f)
            lineToRelative(-0.71f, -0.71f)
            lineToRelative(1.06f, -1.06f)
            lineTo(7.05f, 7.05f)
            lineTo(5.99f, 8.11f)
            lineTo(5.28f, 7.4f)
            lineToRelative(1.06f, -1.06f)
            lineTo(4.0f, 4.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            lineToRelative(-2.34f, -2.34f)
            close()
            moveTo(7.0f, 17.0f)
            verticalLineToRelative(-5.88f)
            lineTo(12.88f, 17.0f)
            horizontalLineTo(7.0f)
            close()
        }
    }
}

val Icons.Filled.Timeline: ImageVector by lazy {
    iconBuilder("Timeline") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(23.0f, 8.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            curveToRelative(-0.1f, 0.0f, -0.19f, -0.02f, -0.28f, -0.05f)
            lineToRelative(-3.4f, 3.4f)
            curveToRelative(0.03f, 0.09f, 0.05f, 0.18f, 0.05f, 0.28f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            curveToRelative(0.0f, -0.1f, 0.02f, -0.19f, 0.05f, -0.28f)
            lineToRelative(-2.4f, -2.4f)
            curveToRelative(-0.09f, 0.03f, -0.18f, 0.05f, -0.28f, 0.05f)
            reflectiveCurveToRelative(-0.19f, -0.02f, -0.28f, -0.05f)
            lineToRelative(-4.4f, 4.4f)
            curveToRelative(0.03f, 0.09f, 0.05f, 0.18f, 0.05f, 0.28f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            curveToRelative(0.0f, -0.1f, 0.02f, -0.19f, 0.05f, -0.28f)
            lineTo(3.35f, 9.72f)
            curveTo(3.26f, 9.75f, 3.17f, 9.77f, 3.07f, 9.77f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            curveToRelative(0.1f, 0.0f, 0.19f, 0.02f, 0.28f, 0.05f)
            lineToRelative(4.4f, -4.4f)
            curveTo(7.72f, 3.39f, 7.7f, 3.3f, 7.7f, 3.2f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            curveToRelative(0.0f, 0.1f, -0.02f, 0.19f, -0.05f, 0.28f)
            lineToRelative(2.4f, 2.4f)
            curveToRelative(0.09f, -0.03f, 0.18f, -0.05f, 0.28f, -0.05f)
            reflectiveCurveToRelative(0.19f, 0.02f, 0.28f, 0.05f)
            lineToRelative(3.4f, -3.4f)
            curveTo(16.98f, 2.65f, 17.0f, 2.56f, 17.0f, 2.46f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            curveToRelative(0.0f, 0.1f, -0.02f, 0.19f, -0.05f, 0.28f)
            lineToRelative(4.77f, 4.77f)
            curveTo(22.81f, 7.54f, 23.0f, 7.75f, 23.0f, 8.0f)
            close()
        }
    }
}

val Icons.Filled.Undo: ImageVector by lazy {
    iconBuilder("Undo") {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.5f, 8.0f)
            curveToRelative(-2.65f, 0.0f, -5.05f, 0.99f, -6.9f, 2.6f)
            lineTo(2.0f, 7.0f)
            verticalLineToRelative(9.0f)
            horizontalLineToRelative(9.0f)
            lineToRelative(-3.62f, -3.62f)
            curveToRelative(1.39f, -1.16f, 3.16f, -1.88f, 5.12f, -1.88f)
            curveToRelative(3.54f, 0.0f, 6.55f, 2.31f, 7.6f, 5.5f)
            lineToRelative(2.37f, -0.78f)
            curveTo(21.08f, 11.03f, 17.15f, 8.0f, 12.5f, 8.0f)
            close()
        }
    }
}


