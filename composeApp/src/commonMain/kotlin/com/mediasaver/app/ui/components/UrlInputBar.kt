package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.*

/**
 * Reusable Bento URL input pill matching the clean elevated aesthetic of the reference UI mockup.
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
    val isDark = isAppInDarkTheme()
    val bgColor = if (isDark) BentoCardDark else BentoCardWhite
    val borderColor = if (isDark) BentoBorderDark else BentoBorderLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 100.dp,
        elevation = 3.dp,
        backgroundColor = bgColor,
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Link icon in subtle circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF232532) else Color(0xFFF1F3F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = BentoPurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Main Text Field
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Paste link here...",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = {
                    if (url.isNotBlank() && !isLoading && enabled) onSubmit(url)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                shape = RoundedCornerShape(100.dp)
            )

            // Clear button if URL is non-empty
            if (url.isNotBlank() && !isLoading && enabled) {
                IconButton(
                    onClick = { onUrlChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear input",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                }
                Spacer(Modifier.width(4.dp))
            }

            // Trailing action button: Paste (when empty) / Submit (when URL entered)
            FilledIconButton(
                onClick = {
                    if (url.isBlank()) {
                        val raw = clipboard.getText()?.text ?: ""
                        if (raw.isNotBlank()) {
                            val cleaned = com.mediasaver.app.domain.util.SmartUrlEngine.extractAndCleanUrl(raw) ?: raw.trim()
                            onUrlChange(cleaned)
                            if (cleaned.isNotBlank()) onSubmit(cleaned)
                        }
                    } else {
                        val cleaned = com.mediasaver.app.domain.util.SmartUrlEngine.extractAndCleanUrl(url) ?: url.trim()
                        onSubmit(cleaned)
                    }
                },
                enabled = !isLoading && enabled,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = BentoPurplePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = BentoPurplePrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    LoaderWidget(
                        variant = LoaderVariant.ThinArc,
                        size = 20.dp,
                        modifier = Modifier
                    )
                } else {
                    Icon(
                        imageVector = if (url.isBlank()) Icons.Default.ContentPaste else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (url.isBlank()) "Paste from clipboard" else "Fetch media",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

