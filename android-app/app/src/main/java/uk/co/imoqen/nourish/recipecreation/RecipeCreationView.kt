package uk.co.imoqen.nourish.recipecreation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryViewModel

@Composable
fun RecipeCreationScreen(
    viewModel: RecipeCreationViewModel,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                // bottom corners are rounded, top are not:
                shape = RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
            .padding(vertical = 16.dp)
    ) {
        NourishH2(
            "Add a Recipe",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
        )

        // back button:
        IconButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = null
            )
        }
    }
}