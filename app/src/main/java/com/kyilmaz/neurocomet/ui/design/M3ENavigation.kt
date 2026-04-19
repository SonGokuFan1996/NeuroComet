package com.kyilmaz.neurocomet.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * M3E Navigation Components for NeuroComet
 * 
 * Custom navigation bar matching the Flutter version's design with:
 * - Expressive pill-shaped indicators
 * - Smooth animations
 * - Badge support
 * - Accessibility compliance
 */

/**
 * M3E Navigation Bar - Custom bottom navigation with M3E design language.
 * 
 * @param selectedIndex Currently selected tab index
 * @param onItemSelected Callback when a tab is selected
 * @param items List of navigation items to display
 */
@Composable
fun M3ENavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<M3ENavItem>,
    modifier: Modifier = Modifier
) {
    M3ESurface(
        modifier = modifier
            .fillMaxWidth()
            .height(M3EDesignSystem.ComponentHeight.navigationBar),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        variant = M3ESurfaceVariant.Navigation,
        shadowElevation = M3EDesignSystem.Elevation.navigation,
        contentPadding = PaddingValues(horizontal = M3EDesignSystem.Spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = M3EDesignSystem.Spacing.xxs),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                M3ENavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = item.icon,
                    selectedIcon = item.selectedIcon,
                    label = item.label,
                    badgeCount = item.badgeCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Navigation item data class.
 */
data class M3ENavItem(
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String,
    val badgeCount: Int = 0,
    val route: String = ""
)

/**
 * Individual navigation bar item with M3E styling.
 */
@Composable
private fun M3ENavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val physics = LocalM3EPhysics.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressScale by rememberM3EPressScale(
        interactionSource = interactionSource,
        role = M3EPhysicsRole.STANDARD
    )

    val animatedIndicatorWidth by animateDpAsState(
        targetValue = if (selected) 64.dp else 0.dp,
        animationSpec = physics.dpSpec(M3EPhysicsRole.SNAP),
        label = "indicatorWidth"
    )
    
    val animatedIndicatorColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        } else {
            Color.Transparent
        },
        animationSpec = physics.colorSpec(M3EPhysicsRole.SNAP),
        label = "indicatorColor"
    )
    val animatedIndicatorBorder by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = physics.colorSpec(M3EPhysicsRole.SNAP),
        label = "indicatorBorder"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = physics.colorSpec(M3EPhysicsRole.STANDARD),
        label = "contentColor"
    )
    
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = label
            }
            .padding(vertical = M3EDesignSystem.Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with indicator background
        Box(
            modifier = Modifier
                .width(animatedIndicatorWidth.coerceAtLeast(48.dp))
                .height(32.dp)
                .clip(M3EDesignSystem.Shapes.PillShape)
                .background(
                    if (selected) {
                        Brush.verticalGradient(
                            colors = listOf(
                                animatedIndicatorColor,
                                animatedIndicatorColor.copy(alpha = animatedIndicatorColor.alpha * 0.85f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .border(0.5.dp, animatedIndicatorBorder, M3EDesignSystem.Shapes.PillShape),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else icon,
                    contentDescription = null,
                    modifier = Modifier.size(M3EDesignSystem.IconSize.navBar),
                    tint = contentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.W600 else FontWeight.W500
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * M3E Top App Bar with consistent styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ETopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
        )
        TopAppBar(
            title = title,
            modifier = Modifier.fillMaxWidth(),
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * M3E Large Top App Bar for screens with prominent headers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ELargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
        )
        LargeTopAppBar(
            title = title,
            modifier = Modifier.fillMaxWidth(),
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * M3E Search Bar with consistent styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ESearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = active,
                onExpandedChange = onActiveChange,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        modifier = modifier,
        shape = M3EDesignSystem.Shapes.LargeShape,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        tonalElevation = 0.dp,
        content = content
    )
}

/**
 * M3E Tab Row with indicator animation.
 */
@Composable
fun M3ETabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        },
        tabs = tabs
    )
}

/**
 * M3E Scrollable Tab Row for many tabs.
 */
@Composable
fun M3EScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = M3EDesignSystem.Spacing.md,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        },
        tabs = tabs
    )
}

/**
 * M3E Tab with proper styling.
 */
@Composable
fun M3ETab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.W600 else FontWeight.W500
            )
        },
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(M3EDesignSystem.IconSize.sm)
                )
            }
        } else null,
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


