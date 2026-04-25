package com.akameiot.app.ui.indexfactory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akameiot.app.ui.indexfactory.NodeLimitItem
import com.akameiot.app.ui.indexfactory.RangeStats


val columnEndPadding = 8.dp
val columnHorizontalPadding = 8.dp

@Composable
fun LimitEditorCard(
    item: NodeLimitItem,
    onUserMinChange: (String) -> Unit,
    onUserMaxChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp + columnHorizontalPadding,
                end = 16.dp + columnHorizontalPadding,
                top = 16.dp,
                bottom = 16.dp
            )
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nodeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                }
                // Metric badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,  // era primaryContainer
                ) {
                    Text(
                        text = item.metricDisplayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,                // era Bold
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // era onPrimaryContainer
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))


            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Período",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f, fill = true),
                )
                Text(
                    "Mínimo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                Text(
                    "Máximo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f, fill = true),
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(
                thickness = 1.dp,
            )
            Spacer(Modifier.height(4.dp))

            item.stats.forEach { stat -> StatsRow(stat = stat) }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  Límites Útiles  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))


            val isDirty = item.userMin.isNotEmpty() || item.userMax.isNotEmpty()

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LimitTextField(
                    value = item.userMin,
                    onValueChange = onUserMinChange,
                    label = "Mínimo",
                    modifier = Modifier.weight(1f),
                )
                LimitTextField(
                    value = item.userMax,
                    onValueChange = onUserMaxChange,
                    label = "Máximo",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { /* onSave — implementar después */ },
                enabled = isDirty,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Guardar límites")
            }
        }
    }
}


@Composable
private fun StatsRow(stat: RangeStats) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = true),
        )
        Text(
            text = stat.min?.let { "%.2f".format(it) } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (stat.min != null) 1f else 0.4f
            ),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f, fill = true),
        )
        Text(
            text = stat.max?.let { "%.2f".format(it) } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (stat.max != null) 1f else 0.4f
            ),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f, fill = true),
        )
    }
}

@Composable
private fun LimitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val normalized = newValue.replace(',', '.')

            val filtered = buildString {
                var hasDot = false

                for (char in normalized) {
                    if (char.isDigit()) {
                        append(char)
                    } else if (char == '.' && !hasDot) {
                        append(char)
                        hasDot = true
                    }
                }
            }

            val finalValue = if (filtered == ".") "" else filtered
            onValueChange(finalValue)
        },
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.End
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}