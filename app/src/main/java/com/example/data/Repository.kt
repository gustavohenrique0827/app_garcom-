package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allMesas: Flow<List<MesaEntity>> = appDao.getAllMesas()
    val activeCalls: Flow<List<WaiterCallEntity>> = appDao.getActiveCalls()
    val allCalls: Flow<List<WaiterCallEntity>> = appDao.getAllCalls()
    val allWaiters: Flow<List<WaiterEntity>> = appDao.getAllWaiters()
    val allProducts: Flow<List<ProductEntity>> = appDao.getAllProducts()
    val allTenants: Flow<List<PlatformTenantEntity>> = appDao.getAllTenants()
    val allActivities: Flow<List<PlatformActivityEntity>> = appDao.getAllActivities()

    suspend fun seedDatabaseIfNeeded() {
        if (appDao.getMesasCount() == 0) {
            val mesas = listOf(
                MesaEntity("01", "MESA 01", "Salão", "Livre", "há 2h14"),
                MesaEntity("02", "MESA 02", "Salão", "Livre", "há 4h"),
                MesaEntity("03", "MESA 03", "Salão", "Ocupada", "há 6 min"),
                MesaEntity("04", "MESA 04", "Varanda", "Livre", "há 1 dia"),
                MesaEntity("05", "MESA 05", "Varanda", "Livre", "há 3h"),
                MesaEntity("06", "MESA 06", "Varanda", "Ocupada", "há 20 min"),
                MesaEntity("07", "MESA 07", "Salão", "Livre", "sem chamados"),
                MesaEntity("08", "MESA 08", "Salão", "Livre", "sem chamados"),
                MesaEntity("09", "MESA 09", "Salão", "Livre", "sem chamados"),
                MesaEntity("10", "MESA 10", "Varanda", "Livre", "sem chamados"),
                MesaEntity("11", "MESA 11", "Varanda", "Livre", "sem chamados"),
                MesaEntity("12", "MESA 12", "Bar", "Livre", "há 1 dia")
            )
            appDao.insertMesas(mesas)
        }

        if (appDao.getProductsCount() == 0) {
            val products = listOf(
                ProductEntity(category = "Bebidas", name = "Chopp Pilsen 500ml", price = 14.90, emoji = "🍺", isAvailable = true),
                ProductEntity(category = "Bebidas", name = "Caipirinha", price = 19.90, emoji = "🍹", isAvailable = true),
                ProductEntity(category = "Bebidas", name = "Refrigerante lata", price = 7.00, emoji = "🥤", isAvailable = false),
                ProductEntity(category = "Petiscos", name = "Batata Frita", price = 24.90, emoji = "🍟", isAvailable = true),
                ProductEntity(category = "Petiscos", name = "Frango a Passarinho", price = 32.90, emoji = "🍗", isAvailable = true),
                ProductEntity(category = "Petiscos", name = "Tábua de Frios", price = 45.00, emoji = "🧀", isAvailable = true)
            )
            appDao.insertProducts(products)
        }

        if (appDao.getWaitersCount() == 0) {
            val waiters = listOf(
                WaiterEntity(1, "João Pedro", "JP", "Salão 1", "Online", 11),
                WaiterEntity(2, "Marina Alves", "MA", "Varanda", "Online", 9),
                WaiterEntity(3, "Diego Souza", "DS", "Salão 1", "Pausa", 7),
                WaiterEntity(4, "Camila Lima", "CL", "Bar", "Offline", 0)
            )
            appDao.insertWaiters(waiters)
        }

        if (appDao.getTenantsCount() == 0) {
            val tenants = listOf(
                PlatformTenantEntity(name = "Bar do Zé", plan = "Pro", status = "Ativo", mrr = 59.90, joinedDate = "jan/2026"),
                PlatformTenantEntity(name = "Sabor Caseiro", plan = "Start", status = "Trial", mrr = 0.00, joinedDate = "jul/2026"),
                PlatformTenantEntity(name = "Choperia Central", plan = "Premium", status = "Ativo", mrr = 89.90, joinedDate = "mar/2025"),
                PlatformTenantEntity(name = "Hamburgueria 32", plan = "Pro", status = "Inadimplente", mrr = 59.90, joinedDate = "nov/2025"),
                PlatformTenantEntity(name = "Pizzaria do Bairro", plan = "Start", status = "Cancelado", mrr = 0.00, joinedDate = "ago/2025"),
                PlatformTenantEntity(name = "Café da Praça", plan = "Start", status = "Ativo", mrr = 39.90, joinedDate = "fev/2026")
            )
            appDao.insertTenants(tenants)
        }

        if (appDao.getActivitiesCount() == 0) {
            val activities = listOf(
                PlatformActivityEntity(text = "Bar do Zé assinou o plano Pro", timeAgo = "há 6 min"),
                PlatformActivityEntity(text = "Restaurante Sabor Caseiro iniciou trial", timeAgo = "há 2h"),
                PlatformActivityEntity(text = "Choperia Central fez upgrade para Premium", timeAgo = "há 5h"),
                PlatformActivityEntity(text = "Pizzaria do Bairro cancelou a assinatura", timeAgo = "ontem")
            )
            appDao.insertActivities(activities)
        }
    }

    suspend fun insertCall(tableId: String, reasonKey: String, reasonLabel: String, reasonIcon: String): Long {
        val call = WaiterCallEntity(
            tableId = tableId,
            reasonKey = reasonKey,
            reasonLabel = reasonLabel,
            reasonIcon = reasonIcon,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        appDao.updateMesaStatus(tableId, "Ocupada")
        appDao.updateMesaLastCall(tableId, "agora")
        return appDao.insertCall(call)
    }

    suspend fun acceptCall(callId: Int, waiterId: Int, waiterName: String) {
        appDao.acceptCall(callId, "accepted", System.currentTimeMillis(), waiterId, waiterName)
    }

    suspend fun finishCall(callId: Int) {
        val call = appDao.getCallById(callId)
        if (call != null) {
            appDao.updateCallStatus(callId, "finished")
            appDao.incrementWaiterCalls(call.waiterId)
            appDao.updateMesaStatus(call.tableId, "Livre")
        }
    }

    suspend fun updateMesaStatus(id: String, status: String) {
        appDao.updateMesaStatus(id, status)
    }

    suspend fun addMesa(id: String, sector: String) {
        val name = "MESA $id"
        appDao.insertMesas(listOf(MesaEntity(id, name, sector, "Livre", "sem chamados")))
    }

    suspend fun addProduct(category: String, name: String, price: Double, emoji: String) {
        appDao.insertProduct(ProductEntity(category = category, name = name, price = price, emoji = emoji, isAvailable = true))
    }

    suspend fun addWaiter(name: String, sector: String) {
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
        val id = (System.currentTimeMillis() % 100000).toInt()
        appDao.insertWaiters(listOf(WaiterEntity(id, name, initials, sector, "Online", 0)))
    }

    suspend fun addTenant(name: String, plan: String, status: String, mrr: Double) {
        appDao.insertTenant(PlatformTenantEntity(name = name, plan = plan, status = status, mrr = mrr, joinedDate = "agora"))
        appDao.insertActivity(PlatformActivityEntity(text = "$name iniciou trial no plano $plan", timeAgo = "agora"))
    }
}
