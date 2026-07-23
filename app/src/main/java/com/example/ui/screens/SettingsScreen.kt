package com.example.ui.screens

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomOutlinedButton
import com.example.ui.components.CustomTextField
import com.example.ui.components.CustomTopBar
import com.example.utils.ImageUtils
import com.example.utils.LanguageManager
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    username: String,
    displayName: String,
    isPersian: Boolean,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profile by settingsViewModel.profile.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val allowInvites by settingsViewModel.allowGroupInvites.collectAsState()
    val uiMessage by settingsViewModel.uiMessage.collectAsState()

    var editableUsername by remember { mutableStateOf(username) }
    var editableDisplayName by remember { mutableStateOf(displayName) }
    var editableBio by remember { mutableStateOf(profile.bio ?: "") }

    LaunchedEffect(username) {
        settingsViewModel.loadProfile(username)
    }

    LaunchedEffect(profile) {
        editableBio = profile.bio ?: ""
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            settingsViewModel.clearMessage()
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            settingsViewModel.updateProfilePicture(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(title = LanguageManager.getString("settings", isPersian))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val bitmap = profile.avatar?.let { ImageUtils.decodeBase64ToBitmap(it) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPersian) "تغییر تصویر پروفایل" else "Change Profile Picture",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                value = editableDisplayName,
                onValueChange = { editableDisplayName = it },
                label = LanguageManager.getString("display_name", isPersian)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = editableUsername,
                onValueChange = { editableUsername = it },
                label = LanguageManager.getString("username", isPersian)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = editableBio,
                onValueChange = { editableBio = it },
                label = LanguageManager.getString("bio", isPersian),
                isSingleLine = false,
                modifier = Modifier.height(90.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Switches Card
            CustomCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.getString("dark_mode", isPersian),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.getString("language", isPersian),
                        style = MaterialTheme.typography.titleMedium
                    )
                    CustomOutlinedButton(
                        onClick = {
                            val next = if (language == "fa") "en" else "fa"
                            settingsViewModel.toggleLanguage(next)
                        },
                        height = 36.dp
                    ) {
                        Text(if (language == "fa") "English" else "فارسی")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.getString("allow_group_invites", isPersian),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = allowInvites,
                        onCheckedChange = { settingsViewModel.toggleGroupInvites(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CustomButton(
                onClick = {
                    settingsViewModel.saveSettings(
                        username = editableUsername,
                        displayName = editableDisplayName,
                        bio = editableBio
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(LanguageManager.getString("save_settings", isPersian))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedButton(
                onClick = onLogout,
                contentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isPersian) "خروج از حساب کاربری" else "Logout")
            }
        }
    }
}
