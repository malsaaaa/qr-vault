package com.qrvault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrvault.R

private const val MIN_PIN_LENGTH = 4
private const val MAX_PIN_LENGTH = 6

@Composable
fun PinPad(
    modifier: Modifier = Modifier,
    key: Any? = null,
    onPinSubmitted: (String) -> Unit,
) {
    var pin by remember(key) { mutableStateOf("") }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(MAX_PIN_LENGTH) { index ->
                Surface(
                    modifier = Modifier.padding(horizontal = 7.dp),
                    shape = CircleShape,
                    color = if (index < pin.length) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                ) {
                    Spacer(Modifier.size(14.dp))
                }
            }
        }

        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9").chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                row.forEach { digit ->
                    PinKey(Modifier.padding(6.dp), digit) {
                        if (pin.length < MAX_PIN_LENGTH) pin += digit
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                modifier = Modifier.padding(6.dp).size(60.dp),
                onClick = { pin = pin.dropLast(1) },
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.pin_delete),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            PinKey(Modifier.padding(6.dp), "0") {
                if (pin.length < MAX_PIN_LENGTH) pin += "0"
            }
            IconButton(
                modifier = Modifier.padding(6.dp).size(60.dp),
                enabled = pin.length >= MIN_PIN_LENGTH,
                onClick = { onPinSubmitted(pin) },
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.pin_confirm),
                    tint = if (pin.length >= MIN_PIN_LENGTH) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.size(60.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text = text, fontSize = 22.sp)
    }
}