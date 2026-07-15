package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WaiterCallEntity
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun GarcomScreen(viewModel: AppViewModel) {
    val isWaiterLoggedIn by viewModel.isWaiterLoggedIn.collectAsState()

    if (!isWaiterLoggedIn) {
        WaiterLoginView(viewModel)
    } else {
        GarcomPanel(viewModel)
    }
}

@Composable
fun GarcomPanel(viewModel: AppViewModel) {
    val activeCalls by viewModel.activeCalls.collectAsState()
    val waiters by viewModel.waiters.collectAsState()
    val allCalls by viewModel.allCalls.collectAsState()

    // Filter active calls to display
    val pendingCalls = activeCalls.filter { it.status == "pending" }
    val acceptedCalls = activeCalls.filter { it.status == "accepted" }
    val doneCountToday = allCalls.count { it.status == "finished" }

    // Automatic notification sound on new pending call
    val context = LocalContext.current
    var lastPendingSize by remember { mutableStateOf(pendingCalls.size) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pendingCalls) {
        if (pendingCalls.size > lastPendingSize) {
            viewModel.playWaiterNotification(context)
        }
        lastPendingSize = pendingCalls.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Waiter Profile Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3A4152), Ink3)
                                )
                            )
                            .border(1.dp, Line, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JP",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            text = "João Pedro",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Garçom · Salão 1",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                    }
                }

                // Settings & Logout Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações de Alerta",
                            tint = BrassBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    TextButton(
                        onClick = { viewModel.logoutWaiter() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Sair", color = Coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Online Pill Status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Teal.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Teal)
                            )
                            Text(
                                text = "Online",
                                color = Teal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queue Summary Row (3 stats cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QueueSummaryCard(
                    num = pendingCalls.size.toString(),
                    label = "Aguardando",
                    color = BrassBright,
                    modifier = Modifier.weight(1f)
                )
                QueueSummaryCard(
                    num = acceptedCalls.size.toString(),
                    label = "Em atendimento",
                    color = Paper,
                    modifier = Modifier.weight(1f)
                )
                QueueSummaryCard(
                    num = doneCountToday.toString(),
                    label = "Hoje",
                    color = Teal,
                    modifier = Modifier.weight(1f)
                )
            }

            val browserPermission by viewModel.waiterBrowserNotificationPermission.collectAsState()

            if (browserPermission == "default") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Brass.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Brass.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = BrassBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Ative as notificações do navegador para receber chamados em segundo plano.",
                            color = Paper,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f),
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = { viewModel.requestBrowserNotificationPermission() },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Ativar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FILA DE CHAMADOS",
                color = TextDimmer,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
            )

            // Fila de chamados list
            Box(modifier = Modifier.weight(1f)) {
                if (activeCalls.isEmpty()) {
                    // Empty Queue State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Fila vazia",
                            tint = TextDimmer,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Nenhum chamado no momento.\nTudo tranquilo por aqui.",
                            color = TextDimmer,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeCalls, key = { it.id }) { call ->
                            WaiterCallCard(call = call, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        WaiterSoundSettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun QueueSummaryCard(
    num: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Ink3),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = num,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextDimmer,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun WaiterCallCard(call: WaiterCallEntity, viewModel: AppViewModel) {
    // Timer calculation
    var elapsedSeconds by remember { mutableStateOf(0L) }
    val baseTime = if (call.status == "pending") call.createdAt else call.acceptedAt

    LaunchedEffect(call.status, baseTime) {
        while (true) {
            elapsedSeconds = (System.currentTimeMillis() - baseTime) / 1000
            delay(1000)
        }
    }

    val isUrgent = call.status == "pending" && elapsedSeconds > 90
    val cardBorderColor = when {
        call.status == "accepted" -> Teal.copy(alpha = 0.35f)
        isUrgent -> Coral.copy(alpha = 0.40f)
        else -> Line
    }

    val isPending = call.status == "pending"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseValue by if (isPending) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPending) {
            // High fidelity pulsing glow ring behind the card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1f + pulseValue * 0.05f
                        scaleY = 1f + pulseValue * 0.12f
                        alpha = 0.6f * (1f - pulseValue)
                    }
                    .background(
                        color = if (isUrgent) Coral.copy(alpha = 0.35f) else Brass.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("call_item_${call.id}"),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Table ID Circular Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink)
                    .border(1.dp, Line, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M${call.tableId}",
                    color = BrassBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${call.reasonIcon} ${call.reasonLabel} · Mesa ${call.tableId}",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (call.status == "pending") {
                        Text(
                            text = "Aguardando",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                            color = if (isUrgent) Coral else Paper,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        if (isUrgent) {
                            Text(
                                text = "⚠️ URGENTE",
                                color = Coral,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Teal)
                        )
                        Text(
                            text = "Em atendimento",
                            color = Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                            color = Teal,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // CTA action buttons
            if (call.status == "pending") {
                Button(
                    onClick = { viewModel.acceptCall(call.id, 1, "João Pedro") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brass,
                        contentColor = Ink
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Aceitar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.finishCall(call.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal.copy(alpha = 0.14f),
                        contentColor = Teal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Finalizar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
}

@Composable
fun WaiterLoginView(viewModel: AppViewModel) {
    var user by remember { mutableStateOf("garcom") }
    var password by remember { mutableStateOf("123") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Ink2),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Line),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header logo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brass.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Login Garçom",
                        tint = BrassBright,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Acesso Garçom",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Faça login para gerenciar chamados",
                        color = TextDimmer,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuário") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextDim) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink,
                        focusedLabelColor = BrassBright,
                        unfocusedLabelColor = TextDim
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextDim) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink,
                        focusedLabelColor = BrassBright,
                        unfocusedLabelColor = TextDim
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Ink3)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Dica para teste rápido:\nUsuário: garcom | Senha: 123",
                        color = TextDimmer,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.loginWaiter(user, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Entrar no Painel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun WaiterSoundSettingsDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val soundType by viewModel.waiterSoundType.collectAsState()
    val soundVolume by viewModel.waiterSoundVolume.collectAsState()
    val context = LocalContext.current

    val sounds = listOf(
        "Notificação Padrão",
        "Bip de Cozinha (Duplo)",
        "Alerta Discreto (Suave)",
        "Alarme Contínuo (Forte)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Ink2),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Line),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = BrassBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Configurações de Alerta",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                HorizontalDivider(color = Line)

                // Sound Selection
                Text(
                    text = "SOM DO ALERTA",
                    color = TextDimmer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sounds.forEach { s ->
                        val isSelected = s == soundType
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Ink.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable { viewModel.waiterSoundType.value = s }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = s,
                                color = if (isSelected) BrassBright else Paper,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.waiterSoundType.value = s },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BrassBright,
                                    unselectedColor = TextDim
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Volume slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "VOLUME DO ALERTA",
                        color = TextDimmer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${(soundVolume * 100).toInt()}%",
                        color = BrassBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = soundVolume,
                        onValueChange = { viewModel.waiterSoundVolume.value = it },
                        colors = SliderDefaults.colors(
                            thumbColor = Brass,
                            activeTrackColor = Brass,
                            inactiveTrackColor = Line
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = Line)

                // Browser Notifications Configuration (Web Push API)
                Text(
                    text = "NOTIFICAÇÕES DO NAVEGADOR (WEB PUSH)",
                    color = TextDimmer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val browserPermission by viewModel.waiterBrowserNotificationPermission.collectAsState()
                val simulateBackground by viewModel.simulateBackgroundMode.collectAsState()

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Permission State and Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ink3)
                            .border(1.dp, Line, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permissão Push API",
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = when (browserPermission) {
                                    "granted" -> "Permitido (Push Ativo)"
                                    "denied" -> "Bloqueado (Silenciado)"
                                    else -> "Não configurado"
                                },
                                color = when (browserPermission) {
                                    "granted" -> Teal
                                    "denied" -> Coral
                                    else -> TextDim
                                },
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                when (browserPermission) {
                                    "default" -> viewModel.requestBrowserNotificationPermission()
                                    "granted" -> viewModel.setBrowserNotificationPermission("denied")
                                    "denied" -> viewModel.setBrowserNotificationPermission("default")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (browserPermission == "default") Brass else Ink2,
                                contentColor = if (browserPermission == "default") Ink else Paper
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = when (browserPermission) {
                                    "default" -> "Ativar"
                                    "granted" -> "Bloquear"
                                    "denied" -> "Redefinir"
                                    else -> "Ativar"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Background Simulation Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ink3)
                            .border(1.dp, Line, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Simular Segundo Plano",
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Mostra notificações do navegador mesmo com a aba do garçom aberta.",
                                color = TextDimmer,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }

                        Switch(
                            checked = simulateBackground,
                            onCheckedChange = { viewModel.toggleSimulateBackgroundMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Brass,
                                checkedTrackColor = Brass.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextDim,
                                uncheckedTrackColor = Line
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.playWaiterNotification(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Ink3, contentColor = Paper),
                        border = BorderStroke(1.dp, Line),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Testar Alerta", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
