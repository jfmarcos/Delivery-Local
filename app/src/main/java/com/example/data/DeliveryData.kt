package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Lanches", "Pizzas", "Bebidas", "Sobremesas"
    val isAvailable: Boolean = true,
    val imageUrl: String = "" // Placeholder or local identifier
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val status: String, // "Pendente" (Pending/Placed), "Preparando" (Preparing), "Em Rota" (In Route), "Entregue" (Delivered)
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val totalAmount: Double,
    val deliveryNotes: String,
    val itemsSummary: String, // e.g., "X-Burger x2, Refrigerante x1"
    val driverName: String? = null // Claims from delivering
)

// List helper to bind product info with cart quantity
data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("UPDATE products SET price = :price WHERE id = :id")
    suspend fun updateProductPrice(id: Int, price: Double)

    @Query("UPDATE products SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateProductAvailability(id: Int, isAvailable: Boolean)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartItemQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemByProductId(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE orders SET driverName = :driverName, status = :status WHERE id = :id")
    suspend fun claimOrder(id: Int, driverName: String, status: String)
}

// ==========================================
// 3. Database
// ==========================================

@Database(entities = [Product::class, CartItem::class, Order::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery_local_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
