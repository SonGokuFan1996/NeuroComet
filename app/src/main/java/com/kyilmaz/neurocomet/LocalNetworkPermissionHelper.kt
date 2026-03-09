package com.kyilmaz.neurocomet

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/**
 * Composable state holder for the Android 17 (API 37+) `ACCESS_LOCAL_NETWORK`
 * runtime permission.
 *
 * On API 36 and below [isRequired] is false and [isGranted] is always true.
 */
class LocalNetworkPermissionState(
    val isRequired: Boolean,
    isGrantedInitial: Boolean,
    private val request: () -> Unit
) {
    var isGranted by mutableStateOf(isGrantedInitial)
        internal set

    /** Request the permission if needed. No-op on API < 37. */
    fun requestIfNeeded() {
        if (!isGranted && isRequired) {
            request()
        }
    }
}

@Composable
fun rememberLocalNetworkPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): LocalNetworkPermissionState {
    val context = LocalContext.current
    val strPermissionRequired = stringResource(R.string.local_network_permission_required)

    val isRequired = remember { Build.VERSION.SDK_INT >= 37 }
    val isCurrentlyGranted = remember { AttachmentHelper.hasLocalNetworkPermission(context) }

    val state = remember {
        LocalNetworkPermissionState(
            isRequired = isRequired,
            isGrantedInitial = isCurrentlyGranted,
            request = {}
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.isGranted = granted
        if (granted) onGranted() else {
            Toast.makeText(context, strPermissionRequired, Toast.LENGTH_SHORT).show()
            onDenied()
        }
    }

    return remember(launcher) {
        LocalNetworkPermissionState(
            isRequired = isRequired,
            isGrantedInitial = state.isGranted,
            request = {
                if (Build.VERSION.SDK_INT >= 37) {
                    @Suppress("NewApi")
                    launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
            }
        )
    }
}

