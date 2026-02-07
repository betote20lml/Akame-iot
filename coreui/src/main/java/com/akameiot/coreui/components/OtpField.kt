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
    value: String,
    onValueChange: (String) -> Unit,
) {

    val spacing = LocalSpacing.current
    val focusRequesters = List(otpLength) { FocusRequester() }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(otpLength) { index ->

            val char = value.getOrNull(index)?.toString() ?: ""

            BasicTextField(
                value = char,
                onValueChange = { newValue ->

                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {

                        val updated = value.toMutableList()

                        if (newValue.isEmpty()) {
                            if (index < updated.size) {
                                updated.removeAt(index)
                            }
                        } else {

                            if (index < updated.size) {
                                updated[index] = newValue.first()
                            } else {
                                updated.add(newValue.first())
                            }

                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }

                        onValueChange(updated.joinToString(""))
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
                            char.isEmpty() &&
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

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }
}

