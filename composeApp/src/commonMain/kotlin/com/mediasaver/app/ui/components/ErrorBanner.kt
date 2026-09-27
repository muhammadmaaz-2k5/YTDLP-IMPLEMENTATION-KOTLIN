package com.mediasaver.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.BentoDarkCardBg
import com.mediasaver.app.ui.theme.BentoPeachContainer

/**
 * Dismissible error banner styled with modern Bento pastel aesthetics.
 */
@Composable
fun ErrorBanner(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = BentoPeachContainer,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCircleBadge(
                icon = Icons.Default.ErrorOutline,
                backgroundColor = Color.White,
                tint = Color(0xFFDC2626),
                size = 40.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Something went wrong",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkCardBg
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
                if (canRetry) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BentoDarkCardBg
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = BentoDarkCardBg
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Retry",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

