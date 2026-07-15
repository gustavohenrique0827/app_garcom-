package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ClienteScreen
import com.example.ui.screens.GarcomScreen
import com.example.ui.screens.GtechScreen
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AppViewModelFactory
import com.example.viewmodel.ViewType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Build Room database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chama_garcom_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val repository = AppRepository(database.appDao())
        val viewModelFactory = AppViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: AppViewModel) {
    val currentView by viewModel.currentView.collectAsState()
    val activeCalls by viewModel.activeCalls.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isWaiterLoggedIn by viewModel.isWaiterLoggedIn.collectAsState()

    val pendingCount = activeCalls.count { it.status == "pending" }

    // Android Native Permission Request Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Native permission response handled gracefully
    }

    // Monitor new calls to trigger push alerts (and mock browser notifications)
    val context = LocalContext.current
    val pendingCalls = remember(activeCalls) { activeCalls.filter { it.status == "pending" } }
    var lastPendingSize by remember { mutableStateOf(pendingCalls.size) }

    LaunchedEffect(pendingCalls, isWaiterLoggedIn) {
        if (isWaiterLoggedIn && pendingCalls.size > lastPendingSize) {
            val newCall = pendingCalls.last()
            viewModel.triggerPushNotification(
                context = context,
                title = "Novo Chamado: Mesa ${newCall.tableId}",
                body = "Solicitou: ${newCall.reasonLabel} ${newCall.reasonIcon}"
            )
            viewModel.playWaiterNotification(context)
        }
        lastPendingSize = pendingCalls.size
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            // Elegant top view selector row representing the prototype switcher
            Surface(
                color = Ink,
                contentColor = Paper,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(ViewType.values()) { view ->
                            val isSelected = currentView == view
                            val buttonBg = if (isSelected) {
                                if (view == ViewType.GTECH) GtechPurple else Brass
                            } else {
                                Ink2
                            }
                            val textColor = if (isSelected) {
                                if (view == ViewType.GTECH) Color.White else Ink
                            } else {
                                TextDim
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(buttonBg)
                                    .clickable { viewModel.setView(view) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Notification Red Dot on Waiter tab if there are pending calls
                                    if (view == ViewType.GARCOM && pendingCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Coral)
                                        )
                                    }
                                    Text(
                                        text = when (view) {
                                            ViewType.CLIENTE -> "Cliente"
                                            ViewType.GARCOM -> "Garçom"
                                            ViewType.ADMIN -> "Admin"
                                            ViewType.GTECH -> "Gtech SaaS"
                                        },
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    }

                    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = BrassBright
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching with clean cross-fade transition
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { viewState ->
                when (viewState) {
                    ViewType.CLIENTE -> ClienteScreen(viewModel)
                    ViewType.GARCOM -> GarcomScreen(viewModel)
                    ViewType.ADMIN -> AdminScreen(viewModel)
                    ViewType.GTECH -> GtechScreen(viewModel)
                }
            }

            // High Fidelity Browser Notification Permission Banner Simulation
            val showBrowserPermissionDialog by viewModel.showBrowserPermissionDialog.collectAsState()
            AnimatedVisibility(
                visible = showBrowserPermissionDialog,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .zIndex(99f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Ink2),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brass.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = BrassBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "chama-garcom.gtech.com.br",
                                    color = Paper,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Deseja enviar notificações de chamados",
                                    color = TextDim,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.setBrowserNotificationPermission("denied") }
                            ) {
                                Text("Bloquear", color = Coral, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.setBrowserNotificationPermission("granted")
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Permitir", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            // High Fidelity sliding browser push notification alerts column
            val activeBrowserNotifications by viewModel.activeBrowserNotifications.collectAsState()
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .widthIn(max = 340.dp)
                    .zIndex(98f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeBrowserNotifications.forEach { notif ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Ink2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Line),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setView(ViewType.GARCOM)
                                viewModel.dismissBrowserNotification(notif.id)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = BrassBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "gtech-chama-garcom.com • Agora",
                                        color = TextDimmer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.dismissBrowserNotification(notif.id) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        tint = TextDim,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notif.title,
                                color = Paper,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.body,
                                color = TextDim,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            // High Fidelity floating custom Toast alert at the bottom
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                toastMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Ink3),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = Teal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                color = Paper,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

