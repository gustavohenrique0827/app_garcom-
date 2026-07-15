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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.GtechPanelType
import com.example.viewmodel.AppViewModel

@Composable
fun GtechScreen(viewModel: AppViewModel) {
    val currentGtechPanel by viewModel.currentGtechPanel.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val platformActivities by viewModel.platformActivities.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gtech Title Row
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
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GtechPurpleBright, GtechPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Gtech",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Painel da Plataforma (SaaS)",
                            color = TextDimmer,
                            fontSize = 11.sp
                        )
                    }
                }

                // Internal Access Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GtechPurple.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "INTERNO",
                        color = GtechPurpleBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Gtech Navigation Tabs (Overview, Restaurantes, Planos, Config)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GtechPanelType.values()) { panel ->
                    val isSelected = currentGtechPanel == panel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) GtechPurple else Ink3)
                            .clickable { viewModel.setGtechPanel(panel) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (panel) {
                                GtechPanelType.OVERVIEW -> "Visão Geral"
                                GtechPanelType.RESTAURANTES -> "Restaurantes"
                                GtechPanelType.PLANOS -> "Planos"
                                GtechPanelType.CONFIG -> "Configurações"
                            },
                            color = if (isSelected) Color.White else Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Divider(color = Line, thickness = 1.dp)

            // Gtech Main Screen Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentGtechPanel) {
                    GtechPanelType.OVERVIEW -> GtechOverviewPanel(viewModel, tenants, platformActivities)
                    GtechPanelType.RESTAURANTES -> GtechRestaurantesPanel(viewModel, tenants)
                    GtechPanelType.PLANOS -> GtechPlanosPanel(viewModel)
                    GtechPanelType.CONFIG -> GtechConfigPanel(viewModel)
                }
            }
        }
    }
}

@Composable
fun GtechOverviewPanel(
    viewModel: AppViewModel,
    tenants: List<PlatformTenantEntity>,
    activities: List<PlatformActivityEntity>
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
                Text(text = "Visão Geral", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Métricas agregadas globais do SaaS", color = TextDimmer, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GtechPurple.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "Gtech SaaS", color = GtechPurpleBright, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }

        // Gtech Purple themed KPIs row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GtechKpiCard(
                num = tenants.size.toString(),
                lbl = "Restaurantes ativos",
                delta = "↑ 23 este mês",
                modifier = Modifier.weight(1f)
            )
            val totalMrr = tenants.sumOf { it.mrr }
            GtechKpiCard(
                num = String.format("R$ %.0f", totalMrr),
                lbl = "MRR total",
                delta = "↑ 9% vs mês anterior",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GtechKpiCard(
                num = "2,1%",
                lbl = "Churn mensal",
                delta = "↓ 0,4pp vs mês anterior",
                modifier = Modifier.weight(1f)
            )
            GtechKpiCard(
                num = "31",
                lbl = "Restaurantes em Trial",
                delta = "7 dias restantes (méd)",
                modifier = Modifier.weight(1f)
            )
        }

        // New restaurants monthly chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Novas assinaturas por mês (2026)",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val heights = listOf(0.30f, 0.42f, 0.38f, 0.55f, 0.64f, 0.72f, 1.0f)
                val labels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    heights.forEachIndexed { idx, h ->
                        val isCurrentMonth = idx == heights.size - 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isCurrentMonth) listOf(
                                            GtechPurpleBright,
                                            GtechPurple
                                        ) else listOf(
                                            GtechPurple.copy(alpha = 0.5f),
                                            GtechPurple.copy(alpha = 0.1f)
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

        // Recent Platform Activities
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Ink3),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Line)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Atividade recente na plataforma",
                    color = Paper,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                activities.forEach { act ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GtechPurpleBright)
                            )
                            Text(text = act.text, color = Paper, fontSize = 12.5.sp)
                        }
                        Text(
                            text = act.timeAgo,
                            color = TextDimmer,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                    Divider(color = Line, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun GtechKpiCard(
    num: String,
    lbl: String,
    delta: String,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = delta, color = GtechPurpleBright, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
        }
    }
}

@Composable
fun GtechRestaurantesPanel(viewModel: AppViewModel, tenants: List<PlatformTenantEntity>) {
    var showAddTenantDialog by remember { mutableStateOf(false) }
    var tenantNameInput by remember { mutableStateOf("") }
    var tenantPlanInput by remember { mutableStateOf("Pro") }
    var tenantMrrInput by remember { mutableStateOf("59.90") }

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
                Text(text = "Restaurantes", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${tenants.size} estabelecimentos cadastrados no SaaS", color = TextDimmer, fontSize = 12.sp)
            }
            Button(
                onClick = { showAddTenantDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GtechPurple, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ Cadastrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tenants) { tenant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink3)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = tenant.name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(text = "Plano: ${tenant.plan} • Desde: ${tenant.joinedDate}", color = TextDimmer, fontSize = 11.sp)
                    }

                    // Status Badge
                    val badgeColor = when (tenant.status) {
                        "Ativo" -> Teal
                        "Trial" -> GtechPurpleBright
                        "Inadimplente" -> Coral
                        else -> TextDimmer
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tenant.status,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = String.format("R$ %.2f", tenant.mrr),
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showAddTenantDialog) {
        Dialog(onDismissRequest = { showAddTenantDialog = false }) {
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
                    Text(text = "Cadastrar Estabelecimento", color = Paper, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    TextField(
                        value = tenantNameInput,
                        onValueChange = { tenantNameInput = it },
                        label = { Text("Nome do Restaurante/Bar") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = tenantMrrInput,
                        onValueChange = { tenantMrrInput = it },
                        label = { Text("Valor Mensal MRR (R$)") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Paper,
                            unfocusedTextColor = Paper,
                            focusedContainerColor = Ink,
                            unfocusedContainerColor = Ink
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Plan selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Start", "Pro", "Premium").forEach { p ->
                            val isSel = tenantPlanInput == p
                            Button(
                                onClick = {
                                    tenantPlanInput = p
                                    tenantMrrInput = when (p) {
                                        "Start" -> "39.90"
                                        "Pro" -> "59.90"
                                        else -> "89.90"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) GtechPurple else Ink3
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = p, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showAddTenantDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancelar", color = TextDim)
                        }
                        Button(
                            onClick = {
                                val mrrVal = tenantMrrInput.toDoubleOrNull() ?: 59.90
                                viewModel.addTenant(tenantNameInput, tenantPlanInput, "Ativo", mrrVal)
                                showAddTenantDialog = false
                                tenantNameInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GtechPurple, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cadastrar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GtechPlanosPanel(viewModel: AppViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "Planos & Faturamento", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Configuração de planos gerais do SaaS Gtech", color = TextDimmer, fontSize = 12.sp)
        }

        // Plans Details
        GtechPlanPricingCard(name = "Start", price = "R$ 39,90", count = "92 assinantes")
        GtechPlanPricingCard(name = "Pro", price = "R$ 59,90", count = "71 assinantes")
        GtechPlanPricingCard(name = "Premium", price = "R$ 89,90", count = "21 assinantes")
    }
}

@Composable
fun GtechPlanPricingCard(name: String, price: String, count: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Ink3),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = count, color = TextDimmer, fontSize = 11.sp)
            }
            Text(text = price, color = GtechPurpleBright, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun GtechConfigPanel(viewModel: AppViewModel) {
    var name by remember { mutableStateOf(viewModel.gtechName.value) }
    var domain by remember { mutableStateOf(viewModel.gtechDomain.value) }
    var email by remember { mutableStateOf(viewModel.gtechEmail.value) }
    var tax by remember { mutableStateOf(viewModel.gtechTax.value) }
    var trial by remember { mutableStateOf(viewModel.gtechTrial.value) }
    var webhook by remember { mutableStateOf(viewModel.gtechWebhook.value) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(text = "Configurações da Plataforma", color = Paper, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Dados internos e webhooks da Gtech", color = TextDimmer, fontSize = 12.sp)
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
                    label = { Text("Nome da Empresa SaaS") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domínio da Plataforma") },
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
                    label = { Text("Email de suporte") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = tax,
                    onValueChange = { tax = it },
                    label = { Text("Taxa de processamento de pagamento") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = trial,
                    onValueChange = { trial = it },
                    label = { Text("Dias de trial gratuito") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Paper,
                        unfocusedTextColor = Paper,
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = webhook,
                    onValueChange = { webhook = it },
                    label = { Text("Webhook de cobrança") },
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
                        viewModel.gtechName.value = name
                        viewModel.gtechDomain.value = domain
                        viewModel.gtechEmail.value = email
                        viewModel.gtechTax.value = tax
                        viewModel.gtechTrial.value = trial
                        viewModel.gtechWebhook.value = webhook
                        viewModel.saveGtechSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GtechPurple, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Salvar Alterações SaaS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
