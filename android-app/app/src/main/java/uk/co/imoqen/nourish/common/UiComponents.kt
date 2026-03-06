package uk.co.imoqen.nourish.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.imoqen.nourish.R

@Composable
fun NourishTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDone: () -> Unit, // for submitting to make the enter button work. this will close the keyboard.
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onLeadingIconPress: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None, //only for password fields
    trailingIcon: ImageVector? = null, // optional: for clearing input
    onTrailingIconPress: () -> Unit = {}, // optional: for clearing input
    alwaysDisplayTrailingIcon: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder)
        },
        leadingIcon =  if (leadingIcon != null ) {
            {
                IconButton(
                    onClick = onLeadingIconPress
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else null
        ,
        modifier = modifier
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

        trailingIcon = { if (trailingIcon != null && (value != "" || alwaysDisplayTrailingIcon)) // trailing icon is only displayed if it is not null and the text entered is not empty (so there is something to remove.)
            {
                IconButton(
                    onClick = onTrailingIconPress
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
        modifier = Modifier
            .padding(6.dp)
            .then(
                modifier
            ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NourishH3(
    text: String, modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(6.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NourishH4(
    text: String, modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .padding(6.dp),
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
    text: String,
    enabled: Boolean = true
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 4.dp),
        enabled = enabled
    ) {
        Text(text)
    }
}

// grey button instead of app theme (green)
@Composable
fun NourishSurfaceButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true
) {
    Button(
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(2.dp)
        )
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

// also suports without text, just put "" and will scale accordingly.
@Composable
fun NourishIconTextButton(
    onClick: () -> Unit,
    painter: Painter,
    description: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    fillMaxWidth: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.40f)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        modifier = if (fillMaxWidth) { modifier.fillMaxWidth().fillMaxHeight() } else modifier,
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                //tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(24.dp)
            )

            if (description != "") {
                Text(
                    text = description,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun NourishIconButton(
    onClick: () -> Unit,
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColour: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColour: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    IconButton(
        onClick = {
            onClick()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = backgroundColour
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = iconColour,
        )
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
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
fun NourishDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .then( modifier ),
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
        override fun createShader(size: Size): Shader {
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

// text message box
@Composable
fun NourishTextMessage(
    textSender: TextSender,
    modifier: Modifier = Modifier,
    textMessage: @Composable () -> Unit
) {
    val textMessageShape = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 32.dp,
        bottomStart = if (textSender == TextSender.LLM) { 0.dp } else { 32.dp },
        bottomEnd = if (textSender == TextSender.USER) { 0.dp } else { 32.dp },
    )
    Box(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(8.dp)
            .shadow(
                elevation = 8.dp,
                shape = textMessageShape
            )
            .background(
                color = if (textSender == TextSender.LLM) { MaterialTheme.colorScheme.surface } else { MaterialTheme.colorScheme.surfaceContainer },
                shape = textMessageShape
            )
    ) {
        textMessage()
    }
}

enum class TextSender {
    LLM,
    USER
}

// alert dialog using android's built in: https://developer.android.com/develop/ui/views/components/dialogs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NourishConfirmationDialog (
    show: Boolean,
    title: String,
    description: String,
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,

            title = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(title)
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    },

            text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(description)
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                   },

            confirmButton = {
                // contains both dismiss and confirm so i can override the right alignment in alert dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NourishButton(
                        text = "Cancel",
                        onClick = { onDismiss() }
                    )
                    NourishButton(
                        text = "Confirm",
                        onClick = { onConfirmation(); onDismiss() } // confirm then dismsis it
                    )
                }
            },

            modifier = Modifier.padding(32.dp)
        )
    }
}