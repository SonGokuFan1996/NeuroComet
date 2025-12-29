package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Explicit imports for symbols in the same package
import com.kyilmaz.neuronetworkingtitle.AuthViewModel
import com.kyilmaz.neuronetworkingtitle.DevOptionsViewModel
import com.kyilmaz.neuronetworkingtitle.SafetyViewModel
import com.kyilmaz.neuronetworkingtitle.ThemeViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    safetyViewModel: SafetyViewModel,
    devOptionsViewModel: DevOptionsViewModel,
    canShowDevOptions: Boolean,
    onOpenDevOptions: () -> Unit,
    themeViewModel: ThemeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val authUser by authViewModel.user.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                if (authUser != null) {
                    AccountInfoCard(user = authUser!!, onLogout = onLogout)
                } else {
                    Text("User not authenticated.")
                }
            }

            item { Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dark Mode Toggle
                    SettingsToggle(
                        title = "Dark Mode",
                        description = "Use a dark theme for low-light environments.",
                        icon = Icons.Default.DarkMode,
                        isChecked = themeState.isDarkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                    // Quiet Mode Toggle (uses VolumeOff icon, fixing ambiguity by using Outlined explicitly)
                    SettingsToggle(
                        title = "Quiet Mode (Low Saturation)",
                        description = "Reduces color saturation and contrast for a calmer experience.",
                        icon = Icons.Outlined.VolumeOff,
                        isChecked = themeState.selectedState == NeuroState.OVERLOAD,
                        onCheckedChange = { 
                            themeViewModel.setSelectedState(if (it) NeuroState.OVERLOAD else NeuroState.DEFAULT)
                        }
                    )
                }
            }

            item { Text("Safety", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsItem(
                        title = "Parental Controls",
                        description = if (safetyState.isParentalPinSet) "PIN is set. Manage controls." else "PIN is not set. Set up controls.",
                        icon = Icons.Default.Lock,
                        onClick = { /* Navigate to parental control screen */ }
                    )
                    SettingsToggle(
                        title = "Kid Mode Filtering",
                        description = "Enforces content filtering for users under 13.",
                        icon = Icons.Default.ChildFriendly,
                        isChecked = safetyState.isKidsMode,
                        onCheckedChange = { safetyViewModel.setAudience(if (it) Audience.UNDER_13 else Audience.ADULT, context) }
                    )
                }
            }
            
            if (canShowDevOptions) {
                item { HorizontalDivider() }
                item { 
                    SettingsItem(
                        title = stringResource(R.string.settings_developer_options_group),
                        description = "Advanced options for testing and debugging.",
                        icon = Icons.Default.Build,
                        onClick = onOpenDevOptions
                    )
                }
            }
        }
    }
}

@Composable
fun AccountInfoCard(user: User, onLogout: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = "Avatar", modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(user.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
