package com.akameiot.app.ui.indexfactory.components

import androidx.compose.foundation.BorderStroke
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
import com.akameiot.coreui.theme.LocalAppColors


val columnHorizontalPadding = 8.dp

@Composable
fun LimitEditorCard(
    item: NodeLimitItem,
    userMin: String,
    userMax: String,
    onUserMinChange: (String) -> Unit,
    onUserMaxChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(width = 1.dp, color = appColors.cardBorder),
        colors    = CardDefaults.cardColors(containerColor = appColors.cardBackground),
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = item.metricDisplayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text = "  Establecer Límites Útiles  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))


            val isDirty = userMin.isNotEmpty() || userMax.isNotEmpty()

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LimitTextField(
                    value = userMin,
                    onValueChange = onUserMinChange,
                    label = "Mínimo",
                    modifier = Modifier.weight(1f),
                )
                LimitTextField(
                    value = userMax,
                    onValueChange = onUserMaxChange,
                    label = "Máximo",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSave,
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
            text = stat.min,   // ← ya es String, sin format
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (stat.min != "—") 1f else 0.4f
            ),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = true),
        )
        Text(
            text = stat.max,   // ← ya es String, sin format
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (stat.max != "—") 1f else 0.4f
            ),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = true),
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