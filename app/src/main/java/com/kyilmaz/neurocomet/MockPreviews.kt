package com.kyilmaz.neurocomet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyilmaz.neurocomet.calling.NeurodivergentPersona
import com.kyilmaz.neurocomet.ui.theme.NeuroCometWorkingTitleTheme
import com.kyilmaz.neurocomet.calling.PracticeCallScreen
import com.kyilmaz.neurocomet.calling.PracticeCallSelectionScreen

@Preview(showBackground = true)
@Composable
fun PreviewAuthScreen() {
    NeuroCometWorkingTitleTheme {
        AuthScreen(
            onSignIn = { _, _ -> },
            onSignUp = { _, _, _ -> },
            onVerify2FA = { _ -> },
            is2FARequired = false,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBackupSettingsScreen() {
    NeuroCometWorkingTitleTheme {
        BackupSettingsScreen(
            onBack = {},
            backupViewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExploreScreen() {
    NeuroCometWorkingTitleTheme {
        ExploreScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFontSettingsScreen() {
    NeuroCometWorkingTitleTheme {
        FontSettingsScreen(
            themeViewModel = viewModel(),
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMockCategoryDetailScreen() {
    NeuroCometWorkingTitleTheme {
        MockCategoryDetailScreen(
            categoryName = "Neurobiology",
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewParentalControlsScreen() {
    NeuroCometWorkingTitleTheme {
        ParentalControlsScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    NeuroCometWorkingTitleTheme {
        ProfileScreen(
            userId = "me",
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewThemeSettingsScreen() {
    NeuroCometWorkingTitleTheme {
        ThemeSettingsScreen(
            themeViewModel = viewModel(),
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    NeuroCometWorkingTitleTheme {
        NeuroSplashScreen(
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSubscriptionScreen() {
    NeuroCometWorkingTitleTheme {
        SubscriptionScreen(
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPracticeCallSelectionScreen() {
    NeuroCometWorkingTitleTheme {
        PracticeCallSelectionScreen(
            onBack = {},
            onPersonaSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPracticeCallScreen() {
    NeuroCometWorkingTitleTheme {
        PracticeCallScreen(
            persona = NeurodivergentPersona.ADHD_FRIEND,
            onEndCall = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStaySignedInScreen() {
    NeuroCometWorkingTitleTheme {
        StaySignedInScreen(
            onYes = {},
            onNo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    NeuroCometWorkingTitleTheme {
        SettingsScreen(
            authViewModel = viewModel(),
            onLogout = {},
            safetyViewModel = viewModel(),
            devOptionsViewModel = viewModel(),
            canShowDevOptions = true,
            onOpenDevOptions = {},
            onOpenParentalControls = {},
            themeViewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDevOptionsScreen() {
    NeuroCometWorkingTitleTheme {
        DevOptionsScreen(
            onBack = {},
            devOptionsViewModel = viewModel(),
            safetyViewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFeedScreen() {
    NeuroCometWorkingTitleTheme {
        FeedScreen(
            feedUiState = FeedUiState(
                posts = emptyList(),
                stories = emptyList()
            ),
            onLikePost = {},
            onReplyPost = {},
            onSharePost = { _, _ -> },
            onAddPost = { _, _, _, _, _, _ -> },
            onDeletePost = {},
            onProfileClick = {},
            onViewStory = {},
            onAddStory = { _, _, _, _, _, _, _ -> },
            isPremium = false,
            onUpgradeClick = {},
            isMockInterfaceEnabled = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNotificationsScreen() {
    NeuroCometWorkingTitleTheme {
        NotificationsScreen(
            notifications = emptyList()
        )
    }
}
