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
    PURPLE,
    GREY
}

// mapping to colour, required because colorScheme can only be accessed from within a composable.
// Colour:
@Composable
fun NourishColour.backgroundColour(): Color = when (this) {
    NourishColour.RED -> Color(0xFFFFC9C9)
    NourishColour.PINK -> Color(0xFFFAC8FF)
    NourishColour.ORANGE -> Color(0xFFFFD5B8)
    NourishColour.YELLOW -> Color(0xFFFDFFB1)
    NourishColour.GREEN -> Color(0xFFBEFFB3)
    NourishColour.BLUE -> Color(0xFFB8FFFD)
    NourishColour.PURPLE -> Color(0xFFC5B8FF)
    NourishColour.GREY -> Color(0xFFCCCCCC)
}

// Background colour:
@Composable
fun NourishColour.onBackgroundColour(): Color = when (this) {
    NourishColour.RED -> Color(0xFF3F0707)
    NourishColour.PINK -> Color(0xFF310634)
    NourishColour.ORANGE -> Color(0xFF341705)
    NourishColour.YELLOW -> Color(0xFF2B2D05)
    NourishColour.GREEN -> Color(0xFF0B3406)
    NourishColour.BLUE -> Color(0xFF042A28)
    NourishColour.PURPLE -> Color(0xFF100638)
    NourishColour.GREY -> Color(0xFF1C1C1C)
}