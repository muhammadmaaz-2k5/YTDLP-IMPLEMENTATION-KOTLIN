package com.mediasaver.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Default colors are theme-driven (see call site below), not fixed constants — the bar uses
// MaterialTheme.colorScheme.inverseSurface (the same semantic token Snackbar/Tooltip use for "a
// floating contrast chip that should still respond to the app's Light/Dark/System setting"), and
// the selected chip uses the app's actual brand `primary` color, which already differs between
// the light and dark color schemes (see AppTheme.kt) — so switching Appearance in Settings
// visibly changes this bar instead of it staying permanently dark regardless of the setting.

/**
 * Data class describing a single item in [FloatingPillNavBar].
 *
 * @param label    Short display label shown when this item is selected.
 * @param icon     Material icon shown in all states.
 * @param contentDescription  Accessibility description for the icon.
 */
data class NavItem(
    val label:              String,
    val icon:               ImageVector,
    val contentDescription: String = label
)

/**
 * A floating, dark-pill bottom navigation bar where the selected tab expands
 * into a **white pill chip** showing icon + label, while unselected tabs show
 * only a white icon — matching the reference image.
 *
 * ### Usage
 * ```kotlin
 * val navItems = listOf(
 *     NavItem("Home",      Icons.Outlined.Home),
 *     NavItem("Downloads", Icons.Outlined.Download),
 *     NavItem("Settings",  Icons.Outlined.Settings),
 *     NavItem("Premium",   Icons.Outlined.WorkspacePremium),
 * )
 *
 * FloatingPillNavBar(
 *     items       = navItems,
 *     selectedIdx = currentTab,
 *     onSelect    = { currentTab = it }
 * )
 * ```
 *
 * @param items        List of [NavItem]s to display (2–6 recommended).
 * @param selectedIdx  Index of the currently-selected item.
 * @param onSelect     Called with the index of the tapped item.
 * @param modifier     Applied to the outer wrapper [Box].
 * @param barColor     Background of the pill bar (default near-black).
 * @param chipColor    Background of the selected chip (default white).
 * @param selectedContentColor  Icon + label colour on the selected chip.
 * @param unselectedIconColor   Icon colour for unselected items.
 */
@Composable
fun FloatingPillNavBar(
    items:                List<NavItem>,
    selectedIdx:          Int,
    onSelect:             (Int) -> Unit,
    modifier:             Modifier = Modifier,
    barColor:             Color    = Color(0xFF16161D),
    chipColor:            Color    = com.mediasaver.app.ui.theme.BentoPurplePrimary,
    selectedContentColor: Color    = Color.White,
    unselectedIconColor:  Color    = Color.White.copy(alpha = 0.55f)
) {
    // Outer container — adds bottom padding for Android gesture-nav insets
    Box(
        modifier         = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {

        // ── Dark pill bar ─────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .wrapContentWidth()
                .shadow(
                    elevation    = 24.dp,
                    shape        = RoundedCornerShape(100.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor    = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(100.dp))
                .background(barColor)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                NavBarItem(
                    item                 = item,
                    isSelected           = index == selectedIdx,
                    chipColor            = chipColor,
                    selectedContentColor = selectedContentColor,
                    unselectedIconColor  = unselectedIconColor,
                    onClick              = { onSelect(index) }
                )
            }
        }
    }
}

// ── The app's 4 persistent tabs ─────────────────────────────────────────────────

private val STANDARD_TABS = listOf(
    NavItem("Home",      Icons.Outlined.Home,             "Home"),
    NavItem("Downloads", Icons.Outlined.Download,         "Downloads"),
    NavItem("Premium",   Icons.Outlined.WorkspacePremium, "Premium"),
    NavItem("Settings",  Icons.Outlined.Settings,         "Settings")
)

/**
 * The standard 4-tab [FloatingPillNavBar] (Home / Downloads / Premium / Settings) used as the
 * `bottomBar` on every top-level screen — a single source of truth for the tab list and index
 * order, so each screen only has to supply "what happens when each tab is tapped" instead of
 * redeclaring [NavItem]s. Pass `{}` for whichever callback corresponds to [selectedIdx] (tapping
 * the tab you're already on is a no-op).
 */
@Composable
fun AppBottomNavBar(
    selectedIdx: Int,
    onHome: () -> Unit,
    onDownloads: () -> Unit,
    onPremium: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingPillNavBar(
        items       = STANDARD_TABS,
        selectedIdx = selectedIdx,
        modifier    = modifier,
        onSelect    = { idx ->
            when (idx) {
                0 -> onHome()
                1 -> onDownloads()
                2 -> onPremium()
                3 -> onSettings()
            }
        }
    )
}

/**
 * The bottom bar used by every top-level tab screen (Home/Downloads/Premium/Settings): the
 * floating pill nav bar with the app's one persistent banner-ad placement docked directly
 * beneath it (see [StickyBannerBar]). Replaces separately calling [AppBottomNavBar] and a
 * scroll-away inline `BannerAdSlot` — the pill still floats over scrolling content exactly as
 * before, the banner strip is the new, always-visible element beneath it.
 */
@Composable
fun AppBottomBarWithAd(
    selectedIdx: Int,
    onHome: () -> Unit,
    onDownloads: () -> Unit,
    onPremium: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AppBottomNavBar(
            selectedIdx = selectedIdx,
            onHome      = onHome,
            onDownloads = onDownloads,
            onPremium   = onPremium,
            onSettings  = onSettings
        )
        StickyBannerBar()
    }
}

// ── Single nav item ──────────────────────────────────────────────────────────

@Composable
private fun NavBarItem(
    item:                NavItem,
    isSelected:          Boolean,
    chipColor:           Color,
    selectedContentColor:Color,
    unselectedIconColor: Color,
    onClick:             () -> Unit
) {
    // Animate the chip width: expanded when selected, icon-sized when not
    val chipWidth by animateDpAsState(
        targetValue   = if (isSelected) 110.dp else 48.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "chipWidth"
    )

    // Icon + label colour
    val iconColor by animateColorAsState(
        targetValue   = if (isSelected) selectedContentColor else unselectedIconColor,
        animationSpec = tween(200),
        label         = "iconColor"
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) chipColor else Color.Transparent,
        animationSpec = tween(220),
        label         = "chipBg"
    )

    // Label alpha: fade in when selected
    val labelAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0f,
        animationSpec = tween(180),
        label         = "labelAlpha"
    )

    Box(
        modifier         = Modifier
            .width(chipWidth)
            .height(44.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.contentDescription,
                tint               = iconColor,
                modifier           = Modifier.size(22.dp)
            )
            // Label only visible for selected item
            if (isSelected && labelAlpha > 0.01f) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = item.label,
                    color      = iconColor.copy(alpha = labelAlpha),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1
                )
            }
        }
    }
}
