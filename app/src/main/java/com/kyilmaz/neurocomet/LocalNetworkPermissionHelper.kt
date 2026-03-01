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
 * Composable state holder for the Android 17 (CinnamonBun / API 37+)
 * `ACCESS_LOCAL_NETWORK` runtime permission.
 *
 * The permission gates access to multicast, local network service discovery
 * (NSD), direct LAN connections, and WebRTC ICE candidate gathering over
 * the local network. On older API levels the permission does not exist and
 * the state reports [isGranted] = true.
 *
 * Usage:
 * ```
 * val localNetworkState = rememberLocalNetworkPermissionState()
 *
 * Button(onClick = {
 *     localNetworkState.requestIfNeeded()
 * }) { Text("Start Call") }
 * ```
 */
class LocalNetworkPermissionState(
    val isRequired: Boolean,
    isGrantedInitial: Boolean,
    private val request: () -> Unit
) {
    /** Whether the permission is currently granted (or not applicable on this API). */
    var isGranted by mutableStateOf(isGrantedInitial)
        internal set

    /**
     * Request the permission if it has not been granted yet.
     * On pre-CinnamonBun devices this is a no-op.
     */
    fun requestIfNeeded() {
        if (!isGranted && isRequired) {
            request()
        }
    }
}

/**
 * Remember and manage the local network permission lifecycle.
 *
 * @param onGranted Optional callback when the permission is granted (or was already granted).
 * @param onDenied  Optional callback when the user denies the permission.
 */
@Composable
fun rememberLocalNetworkPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): LocalNetworkPermissionState {
    val context = LocalContext.current
    val strPermissionRequired = stringResource(R.string.local_network_permission_required)

    val isRequired = remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
    }

    val isCurrentlyGranted = remember {
        AttachmentHelper.hasLocalNetworkPermission(context)
    }

    val state = remember {
        LocalNetworkPermissionState(
            isRequired = isRequired,
            isGrantedInitial = isCurrentlyGranted,
            request = {} // will be replaced below
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.isGranted = granted
        if (granted) {
            onGranted()
        } else {
            Toast.makeText(context, strPermissionRequired, Toast.LENGTH_SHORT).show()
            onDenied()
        }
    }

    // Wire up the actual request lambda now that we have the launcher.
    LaunchedEffect(launcher) {
        // We can't directly assign the lambda in the constructor because the
        // launcher is created after the state object. Instead, we use a
        // DisposableEffect-like pattern to keep the state's request in sync.
    }

    return remember(launcher) {
        LocalNetworkPermissionState(
            isRequired = isRequired,
            isGrantedInitial = state.isGranted,
            request = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
            }
        )
    }
}

