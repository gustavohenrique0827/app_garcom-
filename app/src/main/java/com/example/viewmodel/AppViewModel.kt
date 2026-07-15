package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.MainActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ViewType {
    CLIENTE, GARCOM, ADMIN, GTECH
}

enum class AdminPanelType {
    DASHBOARD, MESAS, CARDAPIO, GARCONS, RELATORIOS, ASSINATURA, CONFIG
}

enum class GtechPanelType {
    OVERVIEW, RESTAURANTES, PLANOS, CONFIG
}

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // Current View / Navigation
    private val _currentView = MutableStateFlow(ViewType.CLIENTE)
    val currentView: StateFlow<ViewType> = _currentView.asStateFlow()

    private val _currentAdminPanel = MutableStateFlow(AdminPanelType.DASHBOARD)
    val currentAdminPanel: StateFlow<AdminPanelType> = _currentAdminPanel.asStateFlow()

    private val _currentGtechPanel = MutableStateFlow(GtechPanelType.OVERVIEW)
    val currentGtechPanel: StateFlow<GtechPanelType> = _currentGtechPanel.asStateFlow()

    // Customer Specific State
    private val _selectedTableId = MutableStateFlow("07")
    val selectedTableId: StateFlow<String> = _selectedTableId.asStateFlow()

    // Database state flows
    val mesas: StateFlow<List<MesaEntity>> = repository.allMesas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCalls: StateFlow<List<WaiterCallEntity>> = repository.activeCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalls: StateFlow<List<WaiterCallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waiters: StateFlow<List<WaiterEntity>> = repository.allWaiters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tenants: StateFlow<List<PlatformTenantEntity>> = repository.allTenants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val platformActivities: StateFlow<List<PlatformActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Toast Messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Admin Settings State (in-memory, preloaded with values from prototype)
    var estName = MutableStateFlow("Bar do Zé")
    var estType = MutableStateFlow("Bar")
    var estPhone = MutableStateFlow("(11) 98888-1234")
    var estEmail = MutableStateFlow("contato@bardoze.com.br")
    var estHours = MutableStateFlow("Ter–Dom, 18h às 00h")
    var estMaxTime = MutableStateFlow("90 segundos")

    // Gtech Settings State
    var gtechName = MutableStateFlow("Gtech")
    var gtechDomain = MutableStateFlow("app.gtech.com.br")
    var gtechEmail = MutableStateFlow("suporte@gtech.com.br")
    var gtechTax = MutableStateFlow("2,9% + R$ 0,49")
    var gtechTrial = MutableStateFlow("14 dias")
    var gtechWebhook = MutableStateFlow("https://app.gtech.com.br/webhooks/billing")

    // Waiter Sound Settings
    val waiterSoundType = MutableStateFlow("Bip de Cozinha (Duplo)")
    val waiterSoundVolume = MutableStateFlow(0.8f)

    // Authentication States
    private val _isWaiterLoggedIn = MutableStateFlow(false)
    val isWaiterLoggedIn: StateFlow<Boolean> = _isWaiterLoggedIn.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Theme State (true = Dark, false = Light)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Browser Notifications States
    private val _waiterBrowserNotificationPermission = MutableStateFlow("default") // "default", "granted", "denied"
    val waiterBrowserNotificationPermission: StateFlow<String> = _waiterBrowserNotificationPermission.asStateFlow()

    private val _simulateBackgroundMode = MutableStateFlow(false)
    val simulateBackgroundMode: StateFlow<Boolean> = _simulateBackgroundMode.asStateFlow()

    private val _showBrowserPermissionDialog = MutableStateFlow(false)
    val showBrowserPermissionDialog: StateFlow<Boolean> = _showBrowserPermissionDialog.asStateFlow()

    private val _activeBrowserNotifications = MutableStateFlow<List<BrowserNotification>>(emptyList())
    val activeBrowserNotifications: StateFlow<List<BrowserNotification>> = _activeBrowserNotifications.asStateFlow()

    fun setBrowserNotificationPermission(permission: String) {
        _waiterBrowserNotificationPermission.value = permission
        _showBrowserPermissionDialog.value = false
        showToast("Notificações: " + when (permission) {
            "granted" -> "Permitidas (Push)"
            "denied" -> "Bloqueadas (Silenciado)"
            else -> "Padrão"
        })
    }

    fun requestBrowserNotificationPermission() {
        if (_waiterBrowserNotificationPermission.value == "default") {
            _showBrowserPermissionDialog.value = true
        } else {
            showToast("Permissão já definida como: ${if (_waiterBrowserNotificationPermission.value == "granted") "Permitida" else "Bloqueada"}")
        }
    }

    fun toggleSimulateBackgroundMode() {
        _simulateBackgroundMode.value = !_simulateBackgroundMode.value
        showToast("Segundo plano: ${if (_simulateBackgroundMode.value) "Ativado (Simulação)" else "Desativado"}")
    }

    fun dismissBrowserNotification(id: String) {
        _activeBrowserNotifications.value = _activeBrowserNotifications.value.filter { it.id != id }
    }

    fun triggerPushNotification(context: android.content.Context, title: String, body: String) {
        if (_waiterBrowserNotificationPermission.value == "granted") {
            val isInBackground = _currentView.value != ViewType.GARCOM || _simulateBackgroundMode.value
            
            if (isInBackground) {
                val newNotif = BrowserNotification(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    body = body
                )
                _activeBrowserNotifications.value = _activeBrowserNotifications.value + newNotif
                
                // Automatically dismiss simulated popup after 6 seconds
                viewModelScope.launch {
                    kotlinx.coroutines.delay(6000)
                    dismissBrowserNotification(newNotif.id)
                }
            }
            
            // Trigger actual native Android System Notification
            sendNativeAndroidNotification(context, title, body)
        }
    }

    private fun sendNativeAndroidNotification(context: android.content.Context, title: String, body: String) {
        try {
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "waiter_calls_channel"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Chamados de Clientes",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações de novos chamados de clientes"
                    enableLights(true)
                    lightColor = android.graphics.Color.YELLOW
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val intent = android.content.Intent(context, MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        showToast("Tema alterado para: ${if (_isDarkTheme.value) "Escuro" else "Claro"}")
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
        }
    }

    fun loginWaiter(user: String, pass: String): Boolean {
        return if (user.trim().lowercase() == "garcom" && pass == "123") {
            _isWaiterLoggedIn.value = true
            showToast("Garçom João Pedro logado com sucesso!")
            true
        } else {
            showToast("Dados inválidos! Use: garcom / 123")
            false
        }
    }

    fun logoutWaiter() {
        _isWaiterLoggedIn.value = false
        showToast("Sessão de garçom encerrada.")
    }

    fun loginAdmin(user: String, pass: String): Boolean {
        return if (user.trim().lowercase() == "admin" && pass == "123") {
            _isAdminLoggedIn.value = true
            showToast("Administrador logado com sucesso!")
            true
        } else {
            showToast("Dados inválidos! Use: admin / 123")
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        showToast("Sessão de administrador encerrada.")
    }

    fun playWaiterNotification(context: android.content.Context) {
        val vol = waiterSoundVolume.value
        val sound = waiterSoundType.value
        viewModelScope.launch {
            try {
                when (sound) {
                    "Notificação Padrão" -> {
                        val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                        val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
                        if (ringtone != null) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                ringtone.volume = vol
                            }
                            ringtone.play()
                        }
                    }
                    "Bip de Cozinha (Duplo)" -> {
                        val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, (vol * 100).toInt())
                        toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 250)
                    }
                    "Alerta Discreto (Suave)" -> {
                        val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, (vol * 100).toInt())
                        toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 200)
                    }
                    "Alarme Contínuo (Forte)" -> {
                        val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, (vol * 100).toInt())
                        toneGen.startTone(android.media.ToneGenerator.TONE_SUP_PIP, 400)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setView(view: ViewType) {
        _currentView.value = view
    }

    fun setAdminPanel(panel: AdminPanelType) {
        _currentAdminPanel.value = panel
    }

    fun setGtechPanel(panel: GtechPanelType) {
        _currentGtechPanel.value = panel
    }

    fun setSelectedTable(tableId: String) {
        _selectedTableId.value = tableId
        showToast("Mesa selecionada: $tableId")
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    // Actions
    fun callWaiter(reasonKey: String, reasonLabel: String, reasonIcon: String) {
        viewModelScope.launch {
            val tableId = _selectedTableId.value
            repository.insertCall(tableId, reasonKey, reasonLabel, reasonIcon)
            showToast("Chamado enviado da Mesa $tableId!")
        }
    }

    fun acceptCall(callId: Int, waiterId: Int, waiterName: String) {
        viewModelScope.launch {
            repository.acceptCall(callId, waiterId, waiterName)
            showToast("Chamado aceito por $waiterName")
        }
    }

    fun finishCall(callId: Int) {
        viewModelScope.launch {
            repository.finishCall(callId)
            showToast("Atendimento finalizado com sucesso!")
        }
    }

    fun updateMesaStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateMesaStatus(id, status)
            showToast("Status da Mesa $id atualizado para $status")
        }
    }

    fun addMesa(id: String, sector: String) {
        viewModelScope.launch {
            if (id.isBlank()) {
                showToast("Erro: ID da mesa inválido")
                return@launch
            }
            repository.addMesa(id, sector)
            showToast("Mesa $id cadastrada com sucesso!")
        }
    }

    fun addProduct(category: String, name: String, price: Double, emoji: String) {
        viewModelScope.launch {
            if (name.isBlank() || price <= 0.0) {
                showToast("Erro: Preencha os campos corretamente")
                return@launch
            }
            repository.addProduct(category, name, price, emoji)
            showToast("Produto '$name' adicionado ao cardápio!")
        }
    }

    fun addWaiter(name: String, sector: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showToast("Erro: Nome do garçom é obrigatório")
                return@launch
            }
            repository.addWaiter(name, sector)
            showToast("Garçom '$name' cadastrado com sucesso!")
        }
    }

    fun addTenant(name: String, plan: String, status: String, mrr: Double) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showToast("Erro: Nome é obrigatório")
                return@launch
            }
            repository.addTenant(name, plan, status, mrr)
            showToast("Estabelecimento '$name' cadastrado na Gtech!")
        }
    }

    fun saveAdminSettings() {
        showToast("Configurações do estabelecimento salvas com sucesso!")
    }

    fun saveGtechSettings() {
        showToast("Configurações da Gtech salvas com sucesso!")
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class BrowserNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)
