package com.akameiot.coreui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OtpField(
    otpLength: Int = 6,
    onOtpComplete: (String) -> Unit
) {

    val spacing = LocalSpacing.current

    val focusRequesters = List(otpLength) { FocusRequester() }

    var otpValues by remember {
        mutableStateOf(List(otpLength) { "" })
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        otpValues.forEachIndexed { index, value ->

            BasicTextField(
                value = value,
                onValueChange = { newValue ->

                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {

                        val updated = otpValues.toMutableList()
                        updated[index] = newValue
                        otpValues = updated

                        // avanzar focus
                        if (newValue.isNotEmpty() && index < otpLength - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }

                        // OTP completo
                        if (otpValues.all { it.isNotEmpty() }) {
                            onOtpComplete(otpValues.joinToString(""))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { event ->

                        if (
                            event.key == Key.Backspace &&
                            event.type == KeyEventType.KeyDown &&
                            otpValues[index].isEmpty() &&
                            index > 0
                        ) {
                            focusRequesters[index - 1].requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }

    // autofocus en el primero
    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }
}
