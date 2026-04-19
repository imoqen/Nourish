package uk.co.imoqen.nourish.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.imoqen.nourish.R

/**
 * The custom Nourish UI Composable Library.
 * Doc comments are not provided for each Composable as they are simple Composables.
 * Where further explanation is required pertaining to parameters, these are provided inline.
 *
 * @author Imogen Pemberton, 2026
 */

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
    keyboardType: KeyboardType = KeyboardType.Text
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
                        contentDescription = "Leading Icon", // e.g. for the filter
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

        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType, // allows different keyboard e.g. for entering only numbers
            imeAction = ImeAction.Done // binds the enter button to the done action
        ),
        // performs done function (closes keyboard)
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),

        // trailing icon is only displayed if it is not null and the text entered is not empty
        trailingIcon = { if (trailingIcon != null && (value != "" || alwaysDisplayTrailingIcon))
            {
                IconButton(
                    onClick = onTrailingIconPress
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Trailing Icon",
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

// Grey button instead of app theme (green)
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

// Icon text button which also supports without text, just put "" and will scale accordingly.
@Composable
fun NourishIconTextButton(
    onClick: () -> Unit,
    painter: Painter,
    description: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    fillMaxWidth: Boolean = false,
    enabled: Boolean = true,
    // so that can change button colour, this is used in the add a recipe bit:
    buttonColour: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Button(
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                buttonColour.copy(alpha=0.40f)
            } else {
                buttonColour
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
    contentDescription: String,
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
            contentDescription = contentDescription,
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
            contentDescription = "Google Logo",
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
                    .padding(top = 84.dp),
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
                Surface(
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
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 32.dp), // room for logo,
                            tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
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
                color = if (textSender == TextSender.LLM) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                       },
                shape = textMessageShape
            )
    ) {
        textMessage()
    }
}

// To differentiate between LLM and User-sent texts in the above NourishTextMessage.
enum class TextSender {
    LLM,
    USER
}

// Alert dialog using android's built in Dialog.
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
                // Contains both dismiss and confirm, to override the right alignment in alert dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NourishButton(
                        text = "No",
                        onClick = { onDismiss() }
                    )
                    NourishButton(
                        text = "Yes",
                        onClick = { onConfirmation(); onDismiss() } // confirm then dismisses it
                    )
                }
            },

            modifier = Modifier.padding(32.dp)
        )
    }
}

// Progress bar (loading bar):
// Adapted from https://developer.android.com/develop/ui/compose/components/progress
@Composable
fun IndeterminateCircularIndicator(
    awaiting: Boolean
) {
    // if awaiting anything, need to show the loading screen
    if (!awaiting) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}