package com.mediasaver.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Always-visible URL input bar at the top of the SPA.
 *
 * @param url         Current text field value (hoisted state).
 * @param onUrlChange Called on every keystroke.
 * @param onSubmit    Called when user taps Submit or presses Enter.
 * @param isLoading   Shows a spinner on the submit button (extraction in progress).
 * @param enabled     False disables input entirely without the spinner — used while a download
 *                    is active, since there's no queue yet and a second submit would orphan it.
 */
@Composable
fun UrlInputBar(
    url: String,
    onUrlChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    isLoading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current

    Surface(
        modifier  = modifier.fillMaxWidth(),
        color     = MaterialTheme.colorScheme.surfaceVariant,
        shape     = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Text field
            OutlinedTextField(
                value            = url,
                onValueChange    = onUrlChange,
                enabled          = enabled,
                modifier         = Modifier.weight(1f),
                leadingIcon      = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder      = {
                    Text(
                        "Paste your link here or auto-detect",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                },
                singleLine       = true,
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction    = ImeAction.Go
                ),
                keyboardActions  = KeyboardActions(onGo = {
                    if (url.isNotBlank() && !isLoading && enabled) onSubmit(url)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = MaterialTheme.shapes.extraLarge
            )

            // Paste (when empty) / submit (when filled) button
            FilledIconButton(
                onClick  = {
                    if (url.isBlank()) {
                        val text = clipboard.getText()?.text ?: ""
                        if (text.isNotBlank()) onUrlChange(text)
                    } else {
                        onSubmit(url)
                    }
                },
                enabled  = !isLoading && enabled,
                modifier = Modifier.size(48.dp)
            ) {
                if (isLoading) {
                    LoaderWidget(
                        variant  = LoaderVariant.ThinArc,
                        size     = 20.dp,
                        modifier = Modifier
                    )
                } else {
                    Icon(
                        imageVector        = if (url.isBlank()) Icons.Default.ContentPaste else Icons.Default.Send,
                        contentDescription = if (url.isBlank()) "Paste from clipboard" else "Fetch media",
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
