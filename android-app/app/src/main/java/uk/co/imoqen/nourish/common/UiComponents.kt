package uk.co.imoqen.nourish.common

import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.authentication.AuthenticationUiStatus

@Composable
fun NourishTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    onDone: () -> Unit, // for submitting to make the enter button work. this will close the keyboard.
    visualTransformation: VisualTransformation = VisualTransformation.None, //only for password fields
    onValueReset: () -> Unit = {}, // optional: for clearing input
    trailingIcon: ImageVector? = null // optional: for clearing input
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,

            focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,

            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

            // see-through placeholder:
            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),

            // no border:
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        visualTransformation = visualTransformation,

        // binds the enter button to the done action
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        // performs done function (closes keyboard)
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),

        trailingIcon = { if (trailingIcon != null && value != "") // trailing icon is only displayed if it is not null and the text entered is not empty (so there is something to remove.)
            {
                IconButton(
                    onClick = onValueReset
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    )
}

@Composable
fun NourishText(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(4.dp)
            .padding(horizontal = 8.dp)
    )
}

@Composable
fun NourishH1(
    text: String
) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .padding(horizontal = 6.dp)
    )
}

@Composable
fun NourishH2(
    text: String, modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(bottom = 6.dp)
            .padding(horizontal = 6.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NourishErrorText(
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
fun NourishButton(
    onClick: () -> Unit,
    text: String
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 4.dp)
    ) {
        Text(text)
    }
}

@Composable
fun NourishDangerButton(
    onClick: () -> Unit,
    text: String
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 4.dp),

        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(text)
    }
}


@Composable
fun GoogleButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.padding(vertical=8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.google_logo),
            contentDescription = "Nourish Logo",
            modifier = Modifier
                .size(32.dp)
                .padding(end = 4.dp)
        )
        Text("Sign in with Google")
    }
}

@Composable
fun NourishTextDivider(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NourishDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
    }
}

// gradient button:
// https://developer.android.com/develop/ui/compose/graphics/draw/brush
@Composable
fun GradientButton(text:String) {
    val colorStops = arrayOf(
        0.0f to Color.Yellow,
        0.2f to Color.Red,
        1f to Color.Blue
    )
    val nourishGreen = MaterialTheme.colorScheme.primary
    //val gradient = Brush.radialGradient(colorStops = colorStops)
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: androidx.compose.ui.geometry.Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(Color(0xFF47734F), nourishGreen),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }
    val gradient = largeRadialGradient


    //val gradient = Brush.horizontalGradient(listOf(Color(0xFF2C7745), MaterialTheme.colorScheme.primary))
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = {},
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        )
        {
            Text(text = text, color = Color.White)
        }
    }
}

@Composable
fun NourishBottomCard(
    titleSection: @Composable (()-> Unit)? = null,
    iconSection: @Composable (()-> Unit)? = null,
    cardContents: @Composable (()-> Unit)
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (titleSection != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 84.dp),//.background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.TopCenter,

                ) {
                titleSection.invoke()
            }
        }


        // ensure bottom box sticks to bottom:
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { clip = false }  // to prevent parent clipping
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 28.dp,
                            shape = RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 32.dp) // room for logo
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 40.dp),
                        // extra padding at bottom for the built in mobile navbar
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        cardContents.invoke()
                    }
                }

                if (iconSection != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        iconSection.invoke()
                    }
                }

            }
        }
    }
}