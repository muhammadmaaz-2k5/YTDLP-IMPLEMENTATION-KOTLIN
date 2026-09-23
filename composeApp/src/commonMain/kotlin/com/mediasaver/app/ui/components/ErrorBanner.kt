package com.mediasaver.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dismissible error banner with optional Retry action.
 *
 * @param message   User-facing error text.
 * @param canRetry  Shows a "Retry" button when true.
 * @param onRetry   Called when the user taps Retry.
 * @param onDismiss Called when the user taps the ✕ button.
 */
@Composable
fun ErrorBanner(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier            = Modifier.padding(16.dp),
            verticalAlignment   = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint               = MaterialTheme.colorScheme.onErrorContainer,
                modifier           = Modifier.size(24.dp).padding(top = 2.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Something went wrong",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
                if (canRetry) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint               = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
