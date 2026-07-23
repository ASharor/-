package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomOutlinedButton
import com.example.ui.components.CustomTextField
import com.example.utils.LanguageManager
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()
    val isPersian by authViewModel.sessionManager.language.collectAsState(initial = "fa")

    // false = Has account ("بله"), true = Needs account ("خیر")
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val currentLangIsPersian = isPersian == "fa"

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onLoginSuccess()
                authViewModel.clearState()
            }
            is AuthUiState.Error -> {
                val key = (uiState as AuthUiState.Error).messageKey
                snackbarHostState.showSnackbar(
                    LanguageManager.getString(key, currentLangIsPersian)
                )
                authViewModel.clearState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language selector on top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    val nextLang = if (currentLangIsPersian) "en" else "fa"
                    coroutineScope.launch {
                        authViewModel.sessionManager.updateLanguage(nextLang)
                    }
                }) {
                    Text(
                        text = if (currentLangIsPersian) "English" else "فارسی",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Logo
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = LanguageManager.getString("app_name", currentLangIsPersian),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = LanguageManager.getString("subtitle", currentLangIsPersian),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Option card: "قبلا حساب داشتین؟ بله / خیر"
            CustomCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = LanguageManager.getString("have_account_question", currentLangIsPersian),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // "بله" (Has Account -> Login)
                        if (!isRegisterMode) {
                            CustomButton(
                                onClick = {
                                    isRegisterMode = false
                                    usernameError = null
                                    passwordError = null
                                },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            ) {
                                Text(
                                    text = LanguageManager.getString("yes", currentLangIsPersian),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            CustomOutlinedButton(
                                onClick = {
                                    isRegisterMode = false
                                    usernameError = null
                                    passwordError = null
                                },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            ) {
                                Text(
                                    text = LanguageManager.getString("yes", currentLangIsPersian)
                                )
                            }
                        }

                        // "خیر" (No Account -> Register)
                        if (isRegisterMode) {
                            CustomButton(
                                onClick = {
                                    isRegisterMode = true
                                    usernameError = null
                                    passwordError = null
                                },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            ) {
                                Text(
                                    text = LanguageManager.getString("no", currentLangIsPersian),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            CustomOutlinedButton(
                                onClick = {
                                    isRegisterMode = true
                                    usernameError = null
                                    passwordError = null
                                },
                                modifier = Modifier.weight(1f),
                                height = 44.dp
                            ) {
                                Text(
                                    text = LanguageManager.getString("no", currentLangIsPersian)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isRegisterMode) {
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = LanguageManager.getString("name", currentLangIsPersian),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            CustomTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = if (it.length < 5 && it.isNotEmpty()) {
                        LanguageManager.getString("username_min_length", currentLangIsPersian)
                    } else null
                },
                label = LanguageManager.getString("username", currentLangIsPersian),
                errorMessage = usernameError,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_groups),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.length < 4 && it.isNotEmpty()) {
                        LanguageManager.getString("password_min_length", currentLangIsPersian)
                    } else null
                },
                label = LanguageManager.getString("password", currentLangIsPersian),
                errorMessage = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                onClick = {
                    if (isRegisterMode) {
                        authViewModel.register(username, password, name)
                    } else {
                        authViewModel.login(username, password)
                    }
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) LanguageManager.getString("register", currentLangIsPersian)
                        else LanguageManager.getString("login", currentLangIsPersian),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Support & Forgot Password Section
            CustomCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = LanguageManager.getString("forgot_password", currentLangIsPersian),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = LanguageManager.getString("support_contact", currentLangIsPersian),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val phone = LanguageManager.getString("support_phone", currentLangIsPersian)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09168476659"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "Phone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
