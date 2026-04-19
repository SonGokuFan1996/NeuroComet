package com.kyilmaz.neurocomet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kyilmaz.neurocomet.ui.design.M3EAvatar
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem
import com.kyilmaz.neurocomet.ui.design.M3ENavItem
import com.kyilmaz.neurocomet.ui.design.M3ENavigationBar
import com.kyilmaz.neurocomet.ui.design.M3ESurface
import com.kyilmaz.neurocomet.ui.design.M3ESurfaceVariant
import kotlinx.coroutines.launch

/**
 * Adaptive Navigation System for NeuroComet
 *
 * Neurodivergent-Friendly Design Principles:
 * - Clear visual hierarchy and consistent layout
 * - Predictable navigation patterns
 * - Large touch targets for reduced motor precision requirements
 * - High contrast icons and labels
 * - Reduced cognitive load with grouped sections
 * - Optional haptic feedback
 * - Smooth, non-jarring animations (can be disabled)
 *
 * Supports:
 * - Bottom Navigation (phones)
 * - Navigation Rail (tablets portrait, foldables)
 * - Navigation Drawer (tablets landscape, desktops)
 */

// ═══════════════════════════════════════════════════════════════
// NAVIGATION TYPE DETECTION
// ═══════════════════════════════════════════════════════════════

enum class NavigationType {
    BOTTOM_NAVIGATION,  // < 600dp width (phones)
    NAVIGATION_RAIL,    // 600dp-840dp width (tablets portrait, foldables)
    PERMANENT_DRAWER    // > 840dp width (tablets landscape, desktops, large screens)
}

enum class ContentType {
    SINGLE_PANE,    // Show only main content
    DUAL_PANE       // Show list-detail side by side
}

/**
 * Calculate the appropriate navigation type based on window size
 */
@Composable
fun calculateNavigationType(): NavigationType {
    return when (rememberCanonicalLayout().navigationChrome) {
        CanonicalNavigationChrome.PERMANENT_DRAWER -> NavigationType.PERMANENT_DRAWER
        CanonicalNavigationChrome.NAVIGATION_RAIL -> NavigationType.NAVIGATION_RAIL
        CanonicalNavigationChrome.BOTTOM_BAR -> NavigationType.BOTTOM_NAVIGATION
    }
}

/**
 * Calculate content type based on window size
 */
@Composable
fun calculateContentType(): ContentType {
    return if (rememberCanonicalLayout().supportsMultiPane) ContentType.DUAL_PANE else ContentType.SINGLE_PANE
}

// ═══════════════════════════════════════════════════════════════
// NAVIGATION DATA MODELS
// ═══════════════════════════════════════════════════════════════

/**
 * Navigation item with neurodivergent-friendly metadata
 */
data class AdaptiveNavItem(
    val route: String,
    val labelRes: Int,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val badgeCount: Int = 0,
    val accessibilityDescription: String? = null,
    val section: NavigationSection = NavigationSection.MAIN
)

enum class NavigationSection {
    MAIN,       // Primary navigation items
    SECONDARY,  // Secondary features
    SETTINGS    // Settings and preferences
}

// ═══════════════════════════════════════════════════════════════
// ADAPTIVE NAVIGATION SCAFFOLD
// ═══════════════════════════════════════════════════════════════

/**
 * Main adaptive navigation scaffold that automatically chooses
 * the appropriate navigation pattern based on screen size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNavigationScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    navItems: List<AdaptiveNavItem>,
    userAvatar: String? = null,
    userName: String? = null,
    isPremium: Boolean = false,
    reducedMotion: Boolean = false,
    highContrast: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val navigationType = calculateNavigationType()

    when (navigationType) {
        NavigationType.BOTTOM_NAVIGATION -> {
            // Standard bottom navigation for phones
            Scaffold(
                bottomBar = {
                    NeurodivergentBottomNavBar(
                        currentRoute = currentRoute,
                        navItems = navItems.filter { it.section == NavigationSection.MAIN },
                        onNavigate = onNavigate,
                        highContrast = highContrast
                    )
                },
                content = content
            )
        }

        NavigationType.NAVIGATION_RAIL -> {
            // Navigation rail for tablets in portrait
            Row(modifier = Modifier.fillMaxSize()) {
                NeurodivergentNavigationRail(
                    currentRoute = currentRoute,
                    navItems = navItems.filter { it.section == NavigationSection.MAIN },
                    onNavigate = onNavigate,
                    userAvatar = userAvatar,
                    onProfileClick = onProfileClick,
                    highContrast = highContrast
                )

                Scaffold(
                    content = { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            content(PaddingValues(0.dp))
                        }
                    }
                )
            }
        }

        NavigationType.PERMANENT_DRAWER -> {
            // Permanent navigation drawer for large screens
            PermanentNavigationDrawer(
                drawerContent = {
                    NeurodivergentPermanentDrawerContent(
                        currentRoute = currentRoute,
                        navItems = navItems,
                        onNavigate = onNavigate,
                        userAvatar = userAvatar,
                        userName = userName,
                        isPremium = isPremium,
                        onProfileClick = onProfileClick,
                        onSettingsClick = onSettingsClick,
                        highContrast = highContrast
                    )
                },
                content = {
                    Scaffold(content = content)
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT-FRIENDLY BOTTOM NAVIGATION
// ═══════════════════════════════════════════════════════════════

@Composable
fun NeurodivergentBottomNavBar(
    currentRoute: String,
    navItems: List<AdaptiveNavItem>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    val primaryItems = navItems.take(5)
    val selectedIndex = primaryItems.indexOfFirst { currentRoute == it.route }.let { index ->
        if (index >= 0) index else 0
    }

    if (!highContrast) {
        M3ENavigationBar(
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                primaryItems.getOrNull(index)?.route?.let(onNavigate)
            },
            items = primaryItems.map { item ->
                M3ENavItem(
                    icon = item.iconOutlined,
                    selectedIcon = item.iconFilled,
                    label = stringResource(item.labelRes),
                    badgeCount = item.badgeCount,
                    route = item.route,
                )
            },
            modifier = modifier
                .navigationBarsPadding()
                .semantics { contentDescription = "Main navigation" },
        )
        return
    }

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .semantics { contentDescription = "Main navigation" },
        containerColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        navItems.take(5).forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Box {
                        Icon(
                            imageVector = if (selected) item.iconFilled else item.iconOutlined,
                            contentDescription = null,
                            modifier = Modifier.size(if (highContrast) 28.dp else 24.dp)
                        )
                        // Badge for notifications
                        if (item.badgeCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-4).dp)
                            ) {
                                Text(
                                    text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        fontSize = if (highContrast) 12.sp else 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                    selectedTextColor = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                    indicatorColor = if (highContrast) Color.White.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT-FRIENDLY NAVIGATION RAIL
// ═══════════════════════════════════════════════════════════════

@Composable
fun NeurodivergentNavigationRail(
    currentRoute: String,
    navItems: List<AdaptiveNavItem>,
    onNavigate: (String) -> Unit,
    userAvatar: String? = null,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    val railShape = RoundedCornerShape(
        topEnd = M3EDesignSystem.Shapes.extraLarge,
        bottomEnd = M3EDesignSystem.Shapes.extraLarge,
    )

    M3ESurface(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = M3EDesignSystem.Spacing.xs, horizontal = M3EDesignSystem.Spacing.xxs)
            .semantics { contentDescription = "Navigation rail" },
        shape = railShape,
        variant = M3ESurfaceVariant.Navigation,
        shadowElevation = if (highContrast) 0.dp else M3EDesignSystem.Elevation.navigation,
        containerColor = if (highContrast) Color.Black else null,
    ) {
        NavigationRail(
            modifier = Modifier.padding(vertical = M3EDesignSystem.Spacing.xs),
            containerColor = Color.Transparent,
            header = {
                Box(
                    modifier = Modifier
                        .padding(vertical = M3EDesignSystem.Spacing.md)
                        .size(M3EDesignSystem.AvatarSize.lg)
                        .clip(M3EDesignSystem.Shapes.Avatar)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center,
                ) {
                    if (userAvatar != null) {
                        M3EAvatar(
                            imageUrl = userAvatar,
                            size = 56.dp,
                            showGradientRing = !highContrast,
                            contentDescription = "Profile",
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            tint = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.route

                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Box {
                            Icon(
                                imageVector = if (selected) item.iconFilled else item.iconOutlined,
                                contentDescription = null,
                                modifier = Modifier.size(if (highContrast) 28.dp else 24.dp),
                            )
                            if (item.badgeCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-4).dp),
                                ) {
                                    Text(
                                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = if (highContrast) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = if (highContrast) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = if (highContrast) Color.White.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT-FRIENDLY PERMANENT DRAWER
// ═══════════════════════════════════════════════════════════════

@Composable
fun NeurodivergentPermanentDrawerContent(
    currentRoute: String,
    navItems: List<AdaptiveNavItem>,
    onNavigate: (String) -> Unit,
    userAvatar: String? = null,
    userName: String? = null,
    isPremium: Boolean = false,
    isGuestUser: Boolean = false,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    collapsed: Boolean = false,
    onToggleCollapsed: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    val mainItems = navItems.filter { it.section == NavigationSection.MAIN }
    val secondaryItems = navItems.filter { it.section == NavigationSection.SECONDARY }

    PermanentDrawerSheet(
        modifier = modifier
            .width(if (collapsed) 96.dp else 280.dp)
            .clip(
                RoundedCornerShape(
                    topEnd = M3EDesignSystem.Shapes.extraLarge,
                    bottomEnd = M3EDesignSystem.Shapes.extraLarge,
                )
            ),
        drawerContainerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(M3EDesignSystem.Spacing.md)
        ) {
            DrawerBrandHeader(
                collapsed = collapsed,
                highContrast = highContrast,
                onToggleCollapsed = { onToggleCollapsed(!collapsed) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = M3EDesignSystem.Spacing.xs))

            // User profile section
            if (collapsed) {
                DrawerProfileCompactButton(
                    userAvatar = userAvatar,
                    userName = userName,
                    isGuestUser = isGuestUser,
                    onClick = onProfileClick,
                    highContrast = highContrast
                )

                Spacer(Modifier.height(16.dp))

                mainItems.forEach { item ->
                    DrawerCompactNavItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        highContrast = highContrast
                    )
                }

                if (secondaryItems.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    secondaryItems.forEach { item ->
                        DrawerCompactNavItem(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            highContrast = highContrast
                        )
                    }
                }
            } else {
                DrawerProfileCard(
                    userAvatar = userAvatar,
                    userName = userName,
                    isPremium = isPremium,
                    isGuestUser = isGuestUser,
                    onClick = onProfileClick,
                    onToggleCollapsed = { onToggleCollapsed(true) },
                    highContrast = highContrast
                )

                Spacer(Modifier.height(16.dp))

                // Main navigation
                Text(
                    "Navigation",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                mainItems.forEach { item ->
                    DrawerNavItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        highContrast = highContrast
                    )
                }

                // Secondary items
                if (secondaryItems.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "More",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    secondaryItems.forEach { item ->
                        DrawerNavItem(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            highContrast = highContrast
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Settings at bottom
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val settingsItem = AdaptiveNavItem(
                route = "settings",
                labelRes = R.string.nav_settings,
                iconFilled = Icons.Filled.Settings,
                iconOutlined = Icons.Outlined.Settings,
                section = NavigationSection.SETTINGS
            )

            if (collapsed) {
                DrawerCompactNavItem(
                    item = settingsItem,
                    selected = currentRoute == "settings",
                    onClick = onSettingsClick,
                    highContrast = highContrast
                )
            } else {
                DrawerNavItem(
                    item = settingsItem,
                    selected = currentRoute == "settings",
                    onClick = onSettingsClick,
                    highContrast = highContrast
                )
            }
        }
    }
}

@Composable
private fun DrawerBrandHeader(
    collapsed: Boolean,
    highContrast: Boolean,
    onToggleCollapsed: () -> Unit
) {
    if (collapsed) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onToggleCollapsed) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.a11y_open_menu),
                    tint = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "NeuroComet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Your neurodivergent space",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleCollapsed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = stringResource(R.string.a11y_close_menu),
                    tint = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DrawerProfileCard(
    userAvatar: String?,
    userName: String?,
    isPremium: Boolean,
    isGuestUser: Boolean,
    onClick: () -> Unit,
    onToggleCollapsed: () -> Unit,
    highContrast: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = M3EDesignSystem.Shapes.MediumShape,
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.DarkGray
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(M3EDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with shape clipping
            ClippedImage(
                imageUrl = userAvatar,
                shape = CircleShape,
                size = M3EDesignSystem.AvatarSize.lg,
                contentDescription = "Profile picture"
            )

            Spacer(Modifier.width(M3EDesignSystem.Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName ?: "Guest User",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )
                if (isPremium) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700)
                        )
                    }
                } else if (isGuestUser) {
                    Text(
                        text = stringResource(R.string.settings_sign_in_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (highContrast) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onToggleCollapsed) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = stringResource(R.string.a11y_close_menu),
                    tint = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DrawerProfileCompactButton(
    userAvatar: String?,
    userName: String?,
    isGuestUser: Boolean,
    onClick: () -> Unit,
    highContrast: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ClippedImage(
                imageUrl = userAvatar,
                shape = CircleShape,
                size = 40.dp,
                contentDescription = if (isGuestUser) stringResource(R.string.auth_sign_in) else (userName ?: "Profile")
            )
            Text(
                text = if (isGuestUser) stringResource(R.string.auth_sign_in) else (userName ?: "Profile"),
                style = MaterialTheme.typography.labelSmall,
                color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerCompactNavItem(
    item: AdaptiveNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    highContrast: Boolean
) {
    val label = stringResource(item.labelRes)
    val backgroundColor = if (selected) {
        if (highContrast) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        if (highContrast) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .semantics { contentDescription = label }
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (selected) item.iconFilled else item.iconOutlined,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                if (item.badgeCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                    ) {
                        Text(
                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: AdaptiveNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    highContrast: Boolean
) {
    val backgroundColor = if (selected) {
        if (highContrast) Color.White.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.primaryContainer
    } else Color.Transparent

    val contentColor = if (selected) {
        if (highContrast) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(M3EDesignSystem.Shapes.SmallShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(M3EDesignSystem.Spacing.sm)
            .semantics {
                item.accessibilityDescription?.let { contentDescription = it }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Icon(
                imageVector = if (selected) item.iconFilled else item.iconOutlined,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(M3EDesignSystem.IconSize.md)
            )
            if (item.badgeCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-4).dp)
                ) {
                    Text(
                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CLIPPED IMAGE COMPONENT
// ═══════════════════════════════════════════════════════════════

/**
 * Display an image clipped to a custom shape
 *
 * Supports:
 * - CircleShape
 * - RoundedCornerShape
 * - Custom shapes
 */
@Composable
fun ClippedImage(
    imageUrl: String?,
    shape: Shape,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholderIcon: ImageVector = Icons.Filled.AccountCircle,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.background(borderColor)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(if (borderWidth > 0.dp) size - borderWidth * 2 else size)
                    .clip(shape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholderIcon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}

/**
 * Hexagon shape for special profile displays
 */
val HexagonShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(centerX, centerY)

            // Create hexagon path
            for (i in 0..5) {
                val angle = Math.toRadians((60.0 * i) - 30)
                val x = (centerX + radius * kotlin.math.cos(angle)).toFloat()
                val y = (centerY + radius * kotlin.math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

/**
 * Diamond shape for achievements/badges
 */
val DiamondShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(size.width / 2, size.height)
            lineTo(0f, size.height / 2)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

// ═══════════════════════════════════════════════════════════════
// MODAL NAVIGATION DRAWER (for phones when needed)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeurodivergentModalDrawer(
    drawerState: DrawerState,
    currentRoute: String,
    navItems: List<AdaptiveNavItem>,
    onNavigate: (String) -> Unit,
    userAvatar: String? = null,
    userName: String? = null,
    isPremium: Boolean = false,
    onProfileClick: () -> Unit = {},
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (highContrast) Color.Black
                                       else MaterialTheme.colorScheme.surface
            ) {
                NeurodivergentPermanentDrawerContent(
                    currentRoute = currentRoute,
                    navItems = navItems,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        onNavigate(route)
                    },
                    userAvatar = userAvatar,
                    userName = userName,
                    isPremium = isPremium,
                    onProfileClick = {
                        scope.launch { drawerState.close() }
                        onProfileClick()
                    },
                    highContrast = highContrast
                )
            }
        },
        content = content
    )
}

