package com.qfinopt.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onOpenServerSettings: () -> Unit = {}
) {
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val focusManager = LocalFocusManager.current

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRiskProfile by remember { mutableStateOf("Moderate") }
    var localError by remember { mutableStateOf<String?>(null) }

    val riskOptions = listOf("Conservative", "Moderate", "Aggressive")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Logo & Hero
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.15f),
                border = BorderStroke(2.dp, PrimaryBlue.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "📈",
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Q-FinOpt",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = "AI-Powered Mutual Fund & Wealth Advisor",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Segmented Switcher
                    TabRow(
                        selectedTabIndex = if (authMode == AuthMode.SIGN_IN) 0 else 1,
                        containerColor = CardDark,
                        indicator = {},
                        divider = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDark, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Tab(
                            selected = authMode == AuthMode.SIGN_IN,
                            onClick = {
                                authMode = AuthMode.SIGN_IN
                                localError = null
                            },
                            modifier = Modifier
                                .background(
                                    if (authMode == AuthMode.SIGN_IN) PrimaryBlue else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (authMode == AuthMode.SIGN_IN) Color.White else TextSecondary
                            )
                        }

                        Tab(
                            selected = authMode == AuthMode.SIGN_UP,
                            onClick = {
                                authMode = AuthMode.SIGN_UP
                                localError = null
                            },
                            modifier = Modifier
                                .background(
                                    if (authMode == AuthMode.SIGN_UP) PrimaryBlue else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (authMode == AuthMode.SIGN_UP) Color.White else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Input (Only on Sign Up)
                    AnimatedVisibility(
                        visible = authMode == AuthMode.SIGN_UP,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    localError = null
                                },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Sai Kumar") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Investor Risk Profile Selector
                            Text(
                                text = "INVESTOR RISK PROFILE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                riskOptions.forEach { opt ->
                                    val isSel = selectedRiskProfile == opt
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedRiskProfile = opt },
                                        label = { Text(opt, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.25f),
                                            selectedLabelColor = PrimaryBlue
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isSel) PrimaryBlue else CardBorder,
                                            enabled = true,
                                            selected = isSel
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            localError = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("name@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            localError = null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Error Message
                    val activeError = localError ?: errorMessage
                    if (activeError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠️ $activeError",
                            color = BearishRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                localError = "Please enter both email and password."
                                return@Button
                            }
                            if (authMode == AuthMode.SIGN_UP && nameInput.isBlank()) {
                                localError = "Please enter your name."
                                return@Button
                            }

                            if (authMode == AuthMode.SIGN_IN) {
                                viewModel.login(emailInput, passwordInput) { success, err ->
                                    if (!success) localError = err
                                }
                            } else {
                                viewModel.register(nameInput, emailInput, passwordInput, selectedRiskProfile) { success, err ->
                                    if (!success) localError = err
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isAuthLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (authMode == AuthMode.SIGN_IN) "Sign In to Q-FinOpt" else "Create Free Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider: OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = CardBorder)
                        Text(
                            text = "  OR  ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Divider(modifier = Modifier.weight(1f), color = CardBorder)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1-Tap Guest Access Button
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.continueAsGuest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ Continue as Guest (1-Tap)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Server Connection Link
            TextButton(
                onClick = onOpenServerSettings,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Backend Server Settings",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
