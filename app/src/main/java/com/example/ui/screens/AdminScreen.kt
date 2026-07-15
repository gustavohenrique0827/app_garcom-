package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.AdminPanelType
import com.example.viewmodel.AppViewModel

@Composable
fun AdminScreen(viewModel: AppViewModel) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (!isAdminLoggedIn) {
        AdminLoginView(viewModel)
    } else {
        AdminPanelContent(viewModel)
    }
}

@Composable
fun AdminPanelContent(viewModel: AppViewModel) {
    val currentAdminPanel by viewModel.currentAdminPanel.collectAsState()
    val mesas by viewModel.mesas.collectAsState()
    val products by viewModel.products.collectAsState()
    val waiters by viewModel.waiters.collectAsState()
    val allCalls by viewModel.allCalls.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Title Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brass),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "B", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = viewModel.estName.collectAsState().value,
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Painel Administrativo",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                    }
                }

                // Plano Pro badge & Sair Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Ink3)
                            .border(1.dp, Line, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Plano Pro",
                            color = BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    TextButton(
                        onClick = { viewModel.logoutAdmin() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Sair", color = Coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Horizontal Scrollable Navigation Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdminPanelType.values()) { panel ->
                    val isSelected = currentAdminPanel == panel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) Brass else Ink3)
                            .clickable { viewModel.setAdminPanel(panel) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (panel) {
                                AdminPanelType.DASHBOARD -> "Dashboard"
                                AdminPanelType.MESAS -> "Mesas & QRs"
                                AdminPanelType.CARDAPIO -> "Cardápio"
                                AdminPanelType.GARCONS -> "Garçons"
                                AdminPanelType.RELATORIOS -> "Relatórios"
                                AdminPanelType.ASSINATURA -> "Assinatura"
                                AdminPanelType.CONFIG -> "Configurações"
                            },
                            color = if (isSelected) Ink else Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Divider(color = Line, thickness = 1.dp)

            // Dynamic Panel Viewport Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentAdminPanel) {
                    AdminPanelType.DASHBOARD -> DashboardPanel(viewModel, allCalls, mesas, waiters)
                    AdminPanelType.MESAS -> MesasPanel(viewModel, mesas)
                    AdminPanelType.CARDAPIO -> CardapioPanel(viewModel, products)
                    AdminPanelType.GARCONS -> GarconsPanel(viewModel, waiters)
                    AdminPanelType.RELATORIOS -> RelatoriosPanel(viewModel, allCalls)
                    AdminPanelType.ASSINATURA -> AssinaturaPanel(viewModel)
                    AdminPanelType.CONFIG -> ConfigPanel(viewModel)
                }
            }
        }
    }
}

// ================================= SUB-PANELS =================================

@Composable
fun DashboardPanel(
    viewModel: AppViewModel,
    allCalls: List<WaiterCallEntity>,
    mesas: List<MesaEntity>,
    waiters: List<WaiterEntity>
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Dashboard", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Visão geral das operações de hoje", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.showToast("Todos os QR Codes das mesas foram baixados (Simulado)") },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "Baixar QRs (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // KPIs Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                num = allCalls.size.toString(),
                lbl = "Chamados hoje",
                delta = "↑ 12% vs ontem",
                isUp = true,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                num = "02:14",
                lbl = "Tempo médio",
                delta = "↓ 18s vs ontem",
                isUp = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val topWaiter = waiters.maxByOrNull { it.callsCount }?.name ?: "João P."
            KpiCard(
                num = if (topWaiter.length > 8) topWaiter.substring(0, 7) + "." else topWaiter,
                lbl = "Garçom mais ativo",
                delta = "11 atendimentos",
                isUp = true,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                num = "Mesa 07",
                lbl = "Mesa que mais chama",
                delta = "6 chamados",
                isUp = false,
                modifier = Modifier.weight(1f)
            )
        }

        // "Chamados por horário" chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Chamados por horário (Hoje)",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // drawn vertical bars
                val heights = listOf(0.22f, 0.35f, 0.28f, 0.48f, 0.65f, 1.0f, 0.82f, 0.58f, 0.40f)
                val labels = listOf("11h", "12h", "13h", "14h", "19h", "20h", "21h", "22h", "23h")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    heights.forEach { h ->
                        val isMax = h == 1.0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isMax) listOf(BrassBright, Brass) else listOf(
                                            Brass.copy(alpha = 0.5f),
                                            Brass.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { lbl ->
                        Text(
                            text = lbl,
                            color = TextDimmer,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Waiter Ranking
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Ranking de garçons",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val sortedWaiters = waiters.sortedByDescending { it.callsCount }
                sortedWaiters.take(3).forEachIndexed { index, waiter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%02d", index + 1),
                                color = TextDimmer,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(text = waiter.name, color = Paper, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Text(
                            text = "${waiter.callsCount} chamados",
                            color = BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (index < 2 && index < sortedWaiters.size - 1) {
                        Divider(color = Line, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    num: String,
    lbl: String,
    delta: String,
    isUp: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Ink3),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = num,
                color = Paper,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = lbl, color = TextDimmer, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = delta,
                color = if (isUp) Teal else Coral,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MesasPanel(viewModel: AppViewModel, mesas: List<MesaEntity>) {
    var showAddMesaDialog by remember { mutableStateOf(false) }
    var tableIdInput by remember { mutableStateOf("") }
    var sectorInput by remember { mutableStateOf("Salão") }
    var selectedMesaForQr by remember { mutableStateOf<MesaEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Mesas & QR Codes", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${mesas.size} mesas cadastradas na plataforma", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { showAddMesaDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ Nova Mesa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Table List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mesas) { mesa ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink3)
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
                        Text(
                            text = mesa.id,
                            color = BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = mesa.name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Setor: ${mesa.sector} • ${mesa.lastCallText}", color = TextDimmer, fontSize = 11.sp)
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (mesa.status == "Livre") Teal.copy(alpha = 0.12f) else BrassBright.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mesa.status == "Livre") "Livre" else "Ocupada",
                            color = if (mesa.status == "Livre") Teal else BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }

                    // QR Code dialog trigger button
                    IconButton(
                        onClick = { selectedMesaForQr = mesa },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "Ver QR", tint = BrassBright, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showAddMesaDialog) {
        Dialog(onDismissRequest = { showAddMesaDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Ink2),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Cadastrar Nova Mesa", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    TextField(
                        value = tableIdInput,
                        onValueChange = { tableIdInput = it },
                        label = { Text("Número da Mesa (ex: 13)") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = sectorInput,
                        onValueChange = { sectorInput = it },
                        label = { Text("Setor (ex: Salão, Varanda, Bar)") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showAddMesaDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancelar", color = TextDim)
                        }
                        Button(
                            onClick = {
                                viewModel.addMesa(tableIdInput, sectorInput)
                                showAddMesaDialog = false
                                tableIdInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Salvar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (selectedMesaForQr != null) {
        val mesa = selectedMesaForQr!!
        val domain = viewModel.gtechDomain.value.ifEmpty { "chama-garcom.gtech.com" }
        val mesaUrl = "https://$domain/mesa/${mesa.id}"

        Dialog(onDismissRequest = { selectedMesaForQr = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Ink2),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "QR Code - Mesa ${mesa.id}",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Aponte a câmera para simular o acesso do cliente",
                        color = TextDimmer,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    // Generated QR Code image using zxing-core
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        QRCodeImage(
                            text = mesaUrl,
                            modifier = Modifier.fillMaxSize(),
                            qrColor = Color(0xFF13161C),
                            backgroundColor = Color.White
                        )
                    }

                    Text(
                        text = mesaUrl,
                        color = BrassBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setSelectedTable(mesa.id)
                                viewModel.setView(com.example.viewmodel.ViewType.CLIENTE)
                                selectedMesaForQr = null
                                viewModel.showToast("Mesa ${mesa.id} selecionada!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Simular Leitura", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.showToast("QR Code da Mesa ${mesa.id} baixado! (Simulado)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Ink3, contentColor = Paper),
                            border = BorderStroke(1.dp, Line),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Baixar", fontSize = 11.sp)
                        }
                    }

                    TextButton(onClick = { selectedMesaForQr = null }) {
                        Text(text = "Fechar", color = TextDim)
                    }
                }
            }
        }
    }
}

@Composable
fun CardapioPanel(viewModel: AppViewModel, products: List<ProductEntity>) {
    var showAddProdDialog by remember { mutableStateOf(false) }
    var prodCategory by remember { mutableStateOf("Bebidas") }
    var prodName by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodEmoji by remember { mutableStateOf("🍺") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Cardápio & Produtos", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${products.size} produtos listados em 2 categorias", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { showAddProdDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ Novo Produto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

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
                            .clip(RoundedCornerShape(14.dp))
                            .background(Ink3)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Ink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = prod.emoji, fontSize = 22.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = prod.name, color = Paper, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = String.format("R$ %.2f", prod.price),
                                color = BrassBright,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (prod.isAvailable) Teal.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (prod.isAvailable) "Disponível" else "Indisponível",
                                color = if (prod.isAvailable) Teal else Coral,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddProdDialog) {
        Dialog(onDismissRequest = { showAddProdDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Ink2),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Adicionar Produto", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    TextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Nome do Produto") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Preço (ex: 24.90)") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = prodEmoji,
                        onValueChange = { prodEmoji = it },
                        label = { Text("Emoji Representativo (ex: 🍔)") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple category selector buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { prodCategory = "Bebidas" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prodCategory == "Bebidas") Brass else Ink3
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Bebidas", color = if (prodCategory == "Bebidas") Ink else Paper, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { prodCategory = "Petiscos" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prodCategory == "Petiscos") Brass else Ink3
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Petiscos", color = if (prodCategory == "Petiscos") Ink else Paper, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showAddProdDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancelar", color = TextDim)
                        }
                        Button(
                            onClick = {
                                val priceVal = prodPrice.toDoubleOrNull() ?: 0.0
                                viewModel.addProduct(prodCategory, prodName, priceVal, prodEmoji)
                                showAddProdDialog = false
                                prodName = ""
                                prodPrice = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Adicionar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GarconsPanel(viewModel: AppViewModel, waiters: List<WaiterEntity>) {
    var showAddWaiterDialog by remember { mutableStateOf(false) }
    var waiterNameInput by remember { mutableStateOf("") }
    var waiterSectorInput by remember { mutableStateOf("Salão 1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Equipe de Garçons", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${waiters.size} de 8 vagas de garçons ativas no plano", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { showAddWaiterDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ Convidar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(waiters) { waiter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink3)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3A4152), Ink)
                                )
                            )
                            .border(1.dp, Line, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = waiter.initials, color = Paper, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = waiter.name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Status bullet
                            val statColor = when (waiter.status) {
                                "Online" -> Teal
                                "Pausa" -> BrassBright
                                else -> TextDimmer
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(statColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = waiter.status, color = statColor, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(text = "${waiter.sector} • ativo desde jan/25", color = TextDimmer, fontSize = 11.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = waiter.callsCount.toString(),
                            color = BrassBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = "atendimentos", color = TextDimmer, fontSize = 9.sp)
                    }
                }
            }
        }
    }

    if (showAddWaiterDialog) {
        Dialog(onDismissRequest = { showAddWaiterDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Ink2),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Convidar Garçom", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    TextField(
                        value = waiterNameInput,
                        onValueChange = { waiterNameInput = it },
                        label = { Text("Nome Completo") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = waiterSectorInput,
                        onValueChange = { waiterSectorInput = it },
                        label = { Text("Setor de Atendimento") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showAddWaiterDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancelar", color = TextDim)
                        }
                        Button(
                            onClick = {
                                viewModel.addWaiter(waiterNameInput, waiterSectorInput)
                                showAddWaiterDialog = false
                                waiterNameInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Convidar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelatoriosPanel(viewModel: AppViewModel, allCalls: List<WaiterCallEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Relatórios", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Resumos estatísticos semanais", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.showToast("Relatório semanal exportado para CSV com sucesso (Simulado)") },
                colors = ButtonDefaults.buttonColors(containerColor = Ink3, contentColor = Paper),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                border = BorderStroke(1.dp, Line)
            ) {
                Text(text = "Exportar CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Stats cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Ink3),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "184", color = BrassBright, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "Chamados na semana", color = TextDimmer, fontSize = 10.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Ink3),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "02:31", color = Teal, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "Tempo médio", color = TextDimmer, fontSize = 10.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Ink3),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "20h às 21h", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Horário de pico", color = TextDimmer, fontSize = 10.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Ink3),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Sábado", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Dia mais movimentado", color = TextDimmer, fontSize = 10.sp)
                }
            }
        }

        // Horizontal Bars distribution of call reasons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Motivo dos chamados na semana",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                ReasonProgressBar(emoji = "🍽️", title = "Fazer um pedido", count = 76, fraction = 0.76f)
                ReasonProgressBar(emoji = "💧", title = "Pedir água", count = 54, fraction = 0.54f)
                ReasonProgressBar(emoji = "🧾", title = "Pedir a conta", count = 38, fraction = 0.38f)
                ReasonProgressBar(emoji = "🔔", title = "Só chamar o garçom", count = 16, fraction = 0.16f)
            }
        }
    }
}

@Composable
fun ReasonProgressBar(emoji: String, title: String, count: Int, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$emoji $title", color = Paper, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Text(text = "$count", color = BrassBright, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Ink)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Brass, BrassBright)))
            )
        }
    }
}

@Composable
fun AssinaturaPanel(viewModel: AppViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "Minha Assinatura", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Status: Pro • Renova em 12 dias • Cartão final 4821", color = TextDimmer, fontSize = 12.sp)
        }

        // Active plans carousel/cards
        PlanCard(name = "Start", price = "R$ 39,90", features = "Até 3 garçons • QR Codes • Cardápio", isCurrent = false)
        PlanCard(name = "Pro", price = "R$ 59,90", features = "Até 8 garçons • QR Codes • Cardápio • Relatórios", isCurrent = true)
        PlanCard(name = "Premium", price = "R$ 89,90", features = "Garçons ilimitados • Tudo do Pro • Suporte VIP", isCurrent = false)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Histórico de pagamentos",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PaymentRow(date = "15/06/2026", price = "R$ 59,90")
                Divider(color = Line, thickness = 0.5.dp)
                PaymentRow(date = "15/05/2026", price = "R$ 59,90")
                Divider(color = Line, thickness = 0.5.dp)
                PaymentRow(date = "15/04/2026", price = "R$ 59,90")
            }
        }
    }
}

@Composable
fun PlanCard(name: String, price: String, features: String, isCurrent: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Ink3),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isCurrent) Brass else Line)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 12.dp))
                        .background(Brass)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "ATUAL", color = Ink, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = price, color = BrassBright, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "/mês", color = TextDimmer, fontSize = 11.sp, modifier = Modifier.padding(start = 2.dp, bottom = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = features, color = TextDim, fontSize = 11.5.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun PaymentRow(date: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = date, color = Paper, fontSize = 13.sp)
        Text(text = price, color = BrassBright, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ConfigPanel(viewModel: AppViewModel) {
    var name by remember { mutableStateOf(viewModel.estName.value) }
    var type by remember { mutableStateOf(viewModel.estType.value) }
    var phone by remember { mutableStateOf(viewModel.estPhone.value) }
    var email by remember { mutableStateOf(viewModel.estEmail.value) }
    var hours by remember { mutableStateOf(viewModel.estHours.value) }
    var maxTime by remember { mutableStateOf(viewModel.estMaxTime.value) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "Configurações", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Dados cadastrais do estabelecimento", color = TextDimmer, fontSize = 12.sp)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Estabelecimento") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo (ex: Bar, Restaurante, Choperia)") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone de contato") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email de contato") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Horário de Funcionamento") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = maxTime,
                    onValueChange = { maxTime = it },
                    label = { Text("Tempo máx. de atendimento (Alerta)") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.estName.value = name
                        viewModel.estType.value = type
                        viewModel.estPhone.value = phone
                        viewModel.estEmail.value = email
                        viewModel.estHours.value = hours
                        viewModel.estMaxTime.value = maxTime
                        viewModel.saveAdminSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Salvar Alterações", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminLoginView(viewModel: AppViewModel) {
    var user by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }

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
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Login Admin",
                        tint = BrassBright,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Acesso Admin",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Faça login no painel de controle do estabelecimento",
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
                        text = "Dica para teste rápido:\nUsuário: admin | Senha: admin123",
                        color = TextDimmer,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.loginAdmin(user, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Acessar Painel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

