package com.example.permissionhandlingandroid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.permissionhandlingandroid.ui.theme.PermissionHandlingAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionHandlingAndroidTheme {
                val permissionViewModel = viewModels<PermissionsViewModel>()
                val permissionQueue = permissionViewModel.value.permissionQueue
                val requestCameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { result ->
                        permissionViewModel.value.onPermissionResult(
                            Manifest.permission.CAMERA,
                            isGranted = result
                        )
                    }
                )

                val requestMultiplePermissions = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->
                        perms.forEach {
                            permissionViewModel.value.onPermissionResult(
                                permission = it.key,
                                isGranted = it.value
                            )
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        requestCameraLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text(text = "request CameraPermission Only")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    Button(onClick = {
                        requestMultiplePermissions.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CALL_PHONE
                            )
                        )
                    }) {
                        Text(text = "request CameraPermission Only")
                    }

                    permissionQueue
                        .reversed()
                        .forEach { permission ->
                            PermissionDialog(
                                permission = when (permission) {
                                    Manifest.permission.CAMERA -> CameraPermissionProvider()
                                    Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionProvider()
                                    Manifest.permission.CALL_PHONE -> CallPhonePermissionProvider()
                                    else -> return@forEach
                                },
                                isPermanentlyDenied = !shouldShowRequestPermissionRationale(
                                    permission
                                ),
                                onDismiss = { permissionViewModel.value.dismissDialog() },
                                onOkClick = {
                                    permissionViewModel.value.dismissDialog()
                                    requestMultiplePermissions.launch(
                                        arrayOf(permission)
                                    )
                                },
                                onGotoAppSettings = { gotoPermissionSetting() },
                            )
                        }
                }
            }
        }
    }
}

fun Activity.gotoPermissionSetting() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

@Composable
fun PermissionDialog(
    permission: PermissionProvider,
    isPermanentlyDenied: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGotoAppSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column {
                Divider()
                Text(
                    text = if (isPermanentlyDenied) {
                        "GrandPermission"
                    } else {
                        "Ok"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPermanentlyDenied) {
                                onGotoAppSettings()
                            } else {
                                onOkClick()
                            }
                        }
                        .padding(16.dp)
                )
            }
        },
        title = {
            Text(text = "Permission Required")
        },
        text = {
            Text(
                text = permission.permissionName(
                    isPermanentlyDenied = isPermanentlyDenied
                )
            )
        },
    )
}

interface PermissionProvider {
    fun permissionName(isPermanentlyDenied: Boolean): String
}

class CameraPermissionProvider : PermissionProvider {
    override fun permissionName(isPermanentlyDenied: Boolean): String {
        return if (isPermanentlyDenied)
            "Please Give the Camera permission Because this permission Need To Perform Camera Related Activities..."
        else
            "This App need Microphone permission to perform some Action"
    }
}

class RecordAudioPermissionProvider : PermissionProvider {
    override fun permissionName(isPermanentlyDenied: Boolean): String {
        return if (isPermanentlyDenied)
            "Please Give the Record_audio permission Because this permission Need To Perform Camera Related Activities..."
        else
            "This App need Camera permission to perform some Action"
    }
}

class CallPhonePermissionProvider : PermissionProvider {
    override fun permissionName(isPermanentlyDenied: Boolean): String {
        return if (isPermanentlyDenied)
            "Please Give the CallPermission permission Because this permission Need To Perform Camera Related Activities..."
        else
            "This App need Camera permission to perform some Action"
    }
}
