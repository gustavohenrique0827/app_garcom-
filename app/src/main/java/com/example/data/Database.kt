package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mesas")
data class MesaEntity(
    @PrimaryKey val id: String, // e.g. "01", "02", "07"
    val name: String, // e.g. "MESA 01"
    val sector: String, // e.g. "Salão", "Varanda", "Bar"
    val status: String, // e.g. "Livre", "Ocupada"
    val lastCallText: String = "sem chamados"
)

@Entity(tableName = "waiter_calls")
data class WaiterCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableId: String,
    val reasonKey: String,
    val reasonLabel: String,
    val reasonIcon: String,
    val status: String, // "pending", "accepted", "finished"
    val createdAt: Long,
    val acceptedAt: Long = 0,
    val waiterId: Int = 1,
    val waiterName: String = "João Pedro"
)

@Entity(tableName = "waiters")
data class WaiterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val initials: String,
    val sector: String,
    val status: String, // "Online", "Pausa", "Offline"
    val callsCount: Int
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Bebidas", "Petiscos"
    val name: String,
    val price: Double,
    val emoji: String,
    val isAvailable: Boolean
)

@Entity(tableName = "platform_tenants")
data class PlatformTenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val plan: String, // "Start", "Pro", "Premium"
    val status: String, // "Ativo", "Trial", "Inadimplente", "Cancelado"
    val mrr: Double,
    val joinedDate: String // e.g. "jan/2026"
)

@Entity(tableName = "platform_activities")
data class PlatformActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timeAgo: String
)

@Dao
interface AppDao {
    // Mesas
    @Query("SELECT * FROM mesas ORDER BY id ASC")
    fun getAllMesas(): Flow<List<MesaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMesas(mesas: List<MesaEntity>)

    @Query("UPDATE mesas SET status = :status WHERE id = :id")
    suspend fun updateMesaStatus(id: String, status: String)

    @Query("UPDATE mesas SET lastCallText = :lastCallText WHERE id = :id")
    suspend fun updateMesaLastCall(id: String, lastCallText: String)

    @Query("SELECT COUNT(*) FROM mesas")
    suspend fun getMesasCount(): Int

    // Waiter Calls
    @Query("SELECT * FROM waiter_calls WHERE status != 'finished' ORDER BY createdAt ASC")
    fun getActiveCalls(): Flow<List<WaiterCallEntity>>

    @Query("SELECT * FROM waiter_calls ORDER BY createdAt DESC")
    fun getAllCalls(): Flow<List<WaiterCallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: WaiterCallEntity): Long

    @Query("UPDATE waiter_calls SET status = :status, acceptedAt = :acceptedAt, waiterId = :waiterId, waiterName = :waiterName WHERE id = :id")
    suspend fun acceptCall(id: Int, status: String, acceptedAt: Long, waiterId: Int, waiterName: String)

    @Query("UPDATE waiter_calls SET status = :status WHERE id = :id")
    suspend fun updateCallStatus(id: Int, status: String)

    @Query("SELECT * FROM waiter_calls WHERE id = :id")
    suspend fun getCallById(id: Int): WaiterCallEntity?

    // Waiters
    @Query("SELECT * FROM waiters ORDER BY id ASC")
    fun getAllWaiters(): Flow<List<WaiterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaiters(waiters: List<WaiterEntity>)

    @Query("UPDATE waiters SET callsCount = callsCount + 1 WHERE id = :id")
    suspend fun incrementWaiterCalls(id: Int)

    @Query("SELECT COUNT(*) FROM waiters")
    suspend fun getWaitersCount(): Int

    // Products
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductsCount(): Int

    // Tenants
    @Query("SELECT * FROM platform_tenants ORDER BY id ASC")
    fun getAllTenants(): Flow<List<PlatformTenantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenants(tenants: List<PlatformTenantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: PlatformTenantEntity)

    @Query("SELECT COUNT(*) FROM platform_tenants")
    suspend fun getTenantsCount(): Int

    // Platform Activities
    @Query("SELECT * FROM platform_activities ORDER BY id DESC")
    fun getAllActivities(): Flow<List<PlatformActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<PlatformActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: PlatformActivityEntity)

    @Query("SELECT COUNT(*) FROM platform_activities")
    suspend fun getActivitiesCount(): Int
}

@Database(
    entities = [
        MesaEntity::class,
        WaiterCallEntity::class,
        WaiterEntity::class,
        ProductEntity::class,
        PlatformTenantEntity::class,
        PlatformActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
