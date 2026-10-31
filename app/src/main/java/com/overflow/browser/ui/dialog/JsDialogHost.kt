package com.overflow.browser.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.overflow.browser.browser.JsDialogState
import com.overflow.browser.browser.JsDialogType

@Composable
fun JsDialogHost(
    dialog: JsDialogState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember(dialog.message, dialog.defaultValue) {
        mutableStateOf(dialog.defaultValue)
    }

    val title = when (dialog.type) {
        JsDialogType.ALERT -> "Alert"
        JsDialogType.CONFIRM -> "Confirm"
        JsDialogType.PROMPT -> "Prompt"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(dialog.message)
                if (dialog.type == JsDialogType.PROMPT) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        label = { Text("Input") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(if (dialog.type == JsDialogType.PROMPT) input else "")
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = if (dialog.type == JsDialogType.ALERT) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
