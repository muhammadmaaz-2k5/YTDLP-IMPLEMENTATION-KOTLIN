package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediasaver.app.ui.theme.HeroGradient
import com.mediasaver.app.ui.theme.PremiumLavender

/** Promo card advertising watermark removal / audio-only downloads / extra features. Tap → Premium screen. */
@Composable
fun PremiumBannerCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = PremiumLavender)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(CircleShape).background(HeroGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Premium Features",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = "Remove watermarks, download audio, and unlock exclusive features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
