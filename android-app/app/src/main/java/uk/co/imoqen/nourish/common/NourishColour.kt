package uk.co.imoqen.nourish.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class NourishColour() {
    RED,
    PINK,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    BROWN,
    PURPLE,
    GREY
}

// mapping to colour, required because colorScheme can only be accessed from within a composable.
// Colour:
@Composable
fun NourishColour.backgroundColour(): Color = when (this) {
    NourishColour.RED -> MaterialTheme.colorScheme.onPrimaryContainer
    NourishColour.PINK -> MaterialTheme.colorScheme.primary
    NourishColour.ORANGE -> MaterialTheme.colorScheme.tertiary
    NourishColour.YELLOW   -> MaterialTheme.colorScheme.error
    NourishColour.GREEN   -> MaterialTheme.colorScheme.error
    NourishColour.BLUE   -> MaterialTheme.colorScheme.error
    NourishColour.BROWN   -> MaterialTheme.colorScheme.error
    NourishColour.PURPLE   -> MaterialTheme.colorScheme.error
    NourishColour.GREY   -> MaterialTheme.colorScheme.error
}

// Background colour:
@Composable
fun NourishColour.onBackgroundColour(): Color = when (this) {
    NourishColour.RED -> MaterialTheme.colorScheme.onPrimaryContainer
    NourishColour.PINK -> MaterialTheme.colorScheme.primary
    NourishColour.ORANGE -> MaterialTheme.colorScheme.tertiary
    NourishColour.YELLOW   -> MaterialTheme.colorScheme.error
    NourishColour.GREEN   -> MaterialTheme.colorScheme.error
    NourishColour.BLUE   -> MaterialTheme.colorScheme.error
    NourishColour.BROWN   -> MaterialTheme.colorScheme.error
    NourishColour.PURPLE   -> MaterialTheme.colorScheme.error
    NourishColour.GREY   -> MaterialTheme.colorScheme.error
}