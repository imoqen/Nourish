package uk.co.imoqen.nourish.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Declares colours to use throughout the app; used within the tag colours and recipe background
 * colours. These colours are also sent to the LLM, for determination of the recipe's colour if an
 * image is not present.
 * Support is provided for both light and dark mode; the colour types are selected appropriately.
 *
 * @author Imogen Pemberton, 2026
 */

enum class NourishColour() {
    RED,
    PINK,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE,
    GREY,
    BROWN
}

/**
 * The remaining functions provide mapping to colour, required because colorScheme can only be
 * accessed from within a composable.
 */

// Text colour in Dark Mode
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
    NourishColour.BROWN -> Color(0xFFD7C1BB)
}

// Background colour in Light Mode
@Composable
fun NourishColour.dimmedBackgroundColour(): Color = when (this) {
    NourishColour.RED -> Color(0xFFF8BCBC)
    NourishColour.PINK -> Color(0xFFF0BEF6)
    NourishColour.ORANGE -> Color(0xFFF3CBAE)
    NourishColour.YELLOW -> Color(0xFFF2F3B7)
    NourishColour.GREEN -> Color(0xFFB8EFAF)
    NourishColour.BLUE -> Color(0xFFB3EFED)
    NourishColour.PURPLE -> Color(0xFFC0B4EF)
    NourishColour.GREY -> Color(0xFFCCCCCC)
    NourishColour.BROWN -> Color(0xFFE5CCC8)
}

// Background colour in Dark Mode and text colour in Light Mode
@Composable
fun NourishColour.onBackgroundColour(): Color = when (this) {
    NourishColour.RED -> Color(0xFF3F0707)
    NourishColour.PINK -> Color(0xFF310634)
    NourishColour.ORANGE -> Color(0xFF341705)
    NourishColour.YELLOW -> Color(0xFF2B2D05)
    NourishColour.GREEN -> Color(0xFF0B3406)
    NourishColour.BLUE -> Color(0xFF042A28)
    NourishColour.PURPLE -> Color(0xFF100638)
    NourishColour.GREY -> Color(0xFF282828)
    NourishColour.BROWN -> Color(0xFF310E05)
}

// Colours of Recipe Cards, the same colour is used in both Light and Dark Mode.
@Composable
fun NourishColour.recipeColour(): Color = when (this) {
    NourishColour.RED -> Color(0xFFFFB9B9)
    NourishColour.PINK -> Color(0xFFFFC6F2)
    NourishColour.ORANGE -> Color(0xFFFFD09C)
    NourishColour.YELLOW -> Color(0xFFFFF69D)
    NourishColour.GREEN -> Color(0xFFC2FFB9)
    NourishColour.BLUE -> Color(0xFFBBE7FF)
    NourishColour.PURPLE -> Color(0xFFC8BBFF)
    NourishColour.GREY -> Color(0xFFC0C0C0)
    NourishColour.BROWN -> Color(0xFFC99380)
}