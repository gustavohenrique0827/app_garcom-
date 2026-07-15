package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun ClienteScreen(viewModel: AppViewModel) {
    val activeCalls by viewModel.activeCalls.collectAsState()
    val mesas by viewModel.mesas.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedTableId by viewModel.selectedTableId.collectAsState()

    var showTableDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showReasonsSheet by remember { mutableStateOf(false) }
    var showWaterOptions by remember { mutableStateOf(false) }

    // Check if there is an active call for the currently selected table
    val currentTableCall = activeCalls.firstOrNull { it.tableId == selectedTableId && it.status != "finished" }

    // Coroutine to animate ringing pulse
    var ringScale by remember { mutableStateOf(1f) }
    var ringAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(currentTableCall) {
        if (currentTableCall != null && currentTableCall.status == "pending") {
            while (true) {
                ringScale = 1f
                ringAlpha = 0.6f
                animate(
                    initialValue = 1f,
                    targetValue = 1.6f,
                    animationSpec = tween(1200, easing = LinearEasing)
                ) { value, _ ->
                    ringScale = value
                    ringAlpha = 1f - (value - 1f) / 0.6f
                }
                delay(200)
            }
        } else {
            ringScale = 1f
            ringAlpha = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Ink2, Ink)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BrassBright, Brass)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "B",
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = viewModel.estName.collectAsState().value,
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Chama Garçom",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                    }
                }

                // Table Selector Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Ink3)
                        .clickable { showTableDialog = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MESA $selectedTableId",
                        color = BrassBright,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Call Status Banner
            AnimatedVisibility(
                visible = currentTableCall != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                currentTableCall?.let { call ->
                    var elapsedSeconds by remember { mutableStateOf(0L) }
                    LaunchedEffect(call.createdAt) {
                        while (true) {
                            elapsedSeconds = (System.currentTimeMillis() - call.createdAt) / 1000
                            delay(1000)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Ink3)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Pulsing Dot
                        val dotColor = if (call.status == "accepted") Teal else BrassBright
                        var dotAlpha by remember { mutableStateOf(1f) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                dotAlpha = 0.4f
                                delay(600)
                                dotAlpha = 1f
                                delay(600)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(dotAlpha)
                                .clip(CircleShape)
                                .background(dotColor)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (call.status == "accepted") "João Pedro aceitou!" else call.reasonLabel,
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (call.status == "accepted") "A caminho da sua mesa..." else "Aguardando garçom...",
                                color = TextDim,
                                fontSize = 11.sp
                            )
                        }

                        // Elapsed Time Text
                        Text(
                            text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                            color = BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hero section
            Text(
                text = "BEM-VINDO",
                color = TextDimmer,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Precisa de algo?\nToque no sino.",
                color = Paper,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            // Centered Bell Action Zone
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Ringing wave animations
                if (currentTableCall != null && currentTableCall.status == "pending") {
                    Box(
                        modifier = Modifier
                            .size(172.dp)
                            .scale(ringScale)
                            .clip(CircleShape)
                            .background(BrassBright.copy(alpha = ringAlpha))
                    )
                }

                // Bell Button
                Box(
                    modifier = Modifier
                        .size(172.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BrassBright, Brass),
                                radius = 240f
                            )
                        )
                        .clickable(enabled = currentTableCall == null) {
                            showReasonsSheet = true
                            showWaterOptions = false
                        }
                        .testTag("bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Sino de chamada",
                        tint = Ink,
                        modifier = Modifier.size(56.dp)
                    )
                }

                // Small helpful subtitle inside center zone
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentTableCall != null) "Chamado enviado" else "Chamar Garçom",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentTableCall != null) "Aguarde, seu garçom já foi notificado." else "Toque para escolher o que você precisa.",
                        color = TextDimmer,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(220.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Link Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMenuDialog = true },
                colors = CardDefaults.cardColors(containerColor = Ink3),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver Cardápio",
                        color = Paper,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Abrir cardápio",
                        tint = BrassBright
                    )
                }
            }
        }

        // ================= BOTTOM SHEET OVERLAY (REASONS SHEET) =================
        if (showReasonsSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
                    .clickable { showReasonsSheet = false }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Ink2)
                        .clickable(enabled = false) {} // Disable clicks bypassing
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Line)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!showWaterOptions) {
                            Text(
                                text = "O que você precisa?",
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = "Mesa $selectedTableId · escolha uma opção e o garçom será avisado",
                                color = TextDimmer,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Reasons List
                            ReasonOptionItem(
                                emoji = "🍽️",
                                title = "Fazer um pedido",
                                description = "Quero pedir algo do cardápio",
                                onClick = {
                                    viewModel.callWaiter("pedido", "Fazer um pedido", "🍽️")
                                    showReasonsSheet = false
                                }
                            )
                            ReasonOptionItem(
                                emoji = "💧",
                                title = "Pedir água",
                                description = "Água com ou sem gás",
                                onClick = { showWaterOptions = true }
                            )
                            ReasonOptionItem(
                                emoji = "🧾",
                                title = "Pedir a conta",
                                description = "Fechar a comanda da mesa",
                                onClick = {
                                    viewModel.callWaiter("conta", "Pedir a conta", "🧾")
                                    showReasonsSheet = false
                                }
                            )
                            ReasonOptionItem(
                                emoji = "🔔",
                                title = "Só chamar o garçom",
                                description = "Outro assunto",
                                onClick = {
                                    viewModel.callWaiter("garcom", "Chamar garçom", "🔔")
                                    showReasonsSheet = false
                                }
                            )
                        } else {
                            // Water sub options
                            Text(
                                text = "💧 Como você prefere?",
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = "Mesa $selectedTableId · escolha o tipo de água",
                                color = TextDimmer,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            ReasonOptionItem(
                                emoji = "🚰",
                                title = "Sem gás",
                                description = "Água natural",
                                onClick = {
                                    viewModel.callWaiter("agua_sem", "Água sem gás", "🚰")
                                    showReasonsSheet = false
                                }
                            )
                            ReasonOptionItem(
                                emoji = "🫧",
                                title = "Com gás",
                                description = "Água gaseificada",
                                onClick = {
                                    viewModel.callWaiter("agua_com", "Água com gás", "🫧")
                                    showReasonsSheet = false
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { showWaterOptions = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "← Voltar", color = TextDim, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showReasonsSheet = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Cancelar", color = TextDim, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ================= DIALOG: SELECT TABLE =================
        if (showTableDialog) {
            Dialog(onDismissRequest = { showTableDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink2)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selecione a sua Mesa",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Escolha de qual mesa quer enviar chamados",
                            color = TextDimmer,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid of 12 tables
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(mesas) { mesa ->
                                val isSelected = mesa.id == selectedTableId
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Brass else Ink3)
                                        .clickable {
                                            viewModel.setSelectedTable(mesa.id)
                                            showTableDialog = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mesa.id,
                                        color = if (isSelected) Ink else Paper,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { showTableDialog = false }) {
                            Text(text = "Fechar", color = BrassBright)
                        }
                    }
                }
            }
        }

        // ================= DIALOG: MENU / CARDAPIO =================
        if (showMenuDialog) {
            Dialog(onDismissRequest = { showMenuDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp)
                        .height(520.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink2)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Cardápio Digital",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Escolha seus favoritos e chame o garçom para pedir",
                            color = TextDimmer,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val categories = products.groupBy { it.category }
                            categories.forEach { (catName, catList) ->
                                item {
                                    Text(
                                        text = catName,
                                        color = BrassBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                items(catList) { prod ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Ink3)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Ink),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = prod.emoji, fontSize = 24.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prod.name,
                                                color = Paper,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = String.format("R$ %.2f", prod.price),
                                                color = BrassBright,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        // Status dot
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(if (prod.isAvailable) Teal.copy(alpha = 0.15f) else Coral.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (prod.isAvailable) "Ativo" else "Falta",
                                                color = if (prod.isAvailable) Teal else Coral,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showMenuDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Fechar Cardápio", color = Ink, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReasonOptionItem(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Ink3)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Ink),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Paper,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
            Text(
                text = description,
                color = TextDimmer,
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Selecionar",
            tint = TextDimmer,
            modifier = Modifier.size(16.dp)
        )
    }
}
