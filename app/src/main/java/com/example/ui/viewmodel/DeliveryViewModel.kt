package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppRole {
    CLIENTE,
    ENTREGADOR,
    ADMIN
}

enum class ClienteTab {
    CARDAPIO,
    CARRINHO,
    PEDIDOS
}

class DeliveryViewModel(private val repository: DeliveryRepository) : ViewModel() {

    // Roles and Tabs navigation states
    private val _currentRole = MutableStateFlow(AppRole.CLIENTE)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _clienteTab = MutableStateFlow(ClienteTab.CARDAPIO)
    val clienteTab: StateFlow<ClienteTab> = _clienteTab.asStateFlow()

    // Active Category Filter for Cardapio
    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Observable states from database
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.getCartItemsWithProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived states
    val cartTotalPrice: StateFlow<Double> = cartItems.map { list ->
        list.sumOf { it.product.price * it.cartItem.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = cartItems.map { list ->
        list.sumOf { it.cartItem.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Checkout Notification State
    private val _checkoutSuccessMessage = MutableStateFlow<String?>(null)
    val checkoutSuccessMessage: StateFlow<String?> = _checkoutSuccessMessage.asStateFlow()

    init {
        // Automatically check and prepopulate menu on start
        viewModelScope.launch {
            repository.checkAndPrepopulateMenu()
        }
    }

    // Navigation and Role Switching Actions
    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun setClienteTab(tab: ClienteTab) {
        _clienteTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearCheckoutNotification() {
        _checkoutSuccessMessage.value = null
    }

    // Client Actions
    fun addToCart(productId: Int) {
        viewModelScope.launch {
            repository.addToCart(productId)
        }
    }

    fun decrementCartQuantity(item: CartItemWithProduct) {
        viewModelScope.launch {
            repository.decrementCartItem(item)
        }
    }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItemId)
        }
    }

    fun placeOrder(
        customerName: String,
        customerAddress: String,
        customerPhone: String,
        deliveryNotes: String
    ) {
        viewModelScope.launch {
            if (customerName.isBlank() || customerAddress.isBlank() || customerPhone.isBlank()) return@launch
            val orderId = repository.checkout(customerName, customerAddress, customerPhone, deliveryNotes)
            if (orderId != -1L) {
                _checkoutSuccessMessage.value = "Pedido #$orderId enviado com sucesso!"
                _clienteTab.value = ClienteTab.PEDIDOS
            }
        }
    }

    // Admin Actions
    fun updateProductPrice(productId: Int, price: Double) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, price)
        }
    }

    fun updateProductAvailability(productId: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateProductAvailability(productId, isAvailable)
        }
    }

    fun addNewProduct(name: String, description: String, price: Double, category: String) {
        viewModelScope.launch {
            if (name.isBlank() || category.isBlank() || price <= 0.0) return@launch
            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                category = category
            )
            repository.addProduct(newProduct)
        }
    }

    // Driver/Entregador Actions
    fun claimOrderForDelivery(orderId: Int, driverName: String) {
        viewModelScope.launch {
            if (driverName.isBlank()) return@launch
            repository.claimOrder(orderId, driverName)
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // Factory Class
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
                val database = AppDatabase.getDatabase(context)
                val repository = DeliveryRepository(
                    productDao = database.productDao(),
                    cartDao = database.cartDao(),
                    orderDao = database.orderDao()
                )
                return DeliveryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
