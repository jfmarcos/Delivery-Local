package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class DeliveryRepository(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    // Flow of all products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    // Flow of all orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    // Combined Flow: Cart items with full product details
    fun getCartItemsWithProducts(): Flow<List<CartItemWithProduct>> {
        return cartDao.getCartItems().combine(productDao.getAllProducts()) { cartItems, products ->
            cartItems.mapNotNull { cartItem ->
                val product = products.find { it.id == cartItem.productId }
                if (product != null) {
                    CartItemWithProduct(cartItem, product)
                } else {
                    null
                }
            }
        }
    }

    // Prepopulate database with delicious Brazilian menu options if empty
    suspend fun checkAndPrepopulateMenu() {
        val currentProducts = allProducts.first()
        if (currentProducts.isEmpty()) {
            val sampleProducts = listOf(
                Product(
                    name = "Burguer Artesanal Cheddar",
                    description = "Pão de brioche, blend bovino de 150g suculento, bacon crocante, triplo cheddar cremoso e maionese defumada.",
                    price = 28.90,
                    category = "Lanches",
                    imageUrl = "burger"
                ),
                Product(
                    name = "Misto Quente Especial",
                    description = "Pão de forma tostado na chapa com muito queijo prato derretido, presunto cozido de alta qualidade e queijo provolone.",
                    price = 14.90,
                    category = "Lanches",
                    imageUrl = "sandwich"
                ),
                Product(
                    name = "Pizza Calabresa Espetacular",
                    description = "Massa fina artesanal, molho caseiro de tomate, muçarela, calabresa fatiada prime, cebola roxa e azeitonas pretas chilenas.",
                    price = 42.00,
                    category = "Pizzas",
                    imageUrl = "pizza"
                ),
                Product(
                    name = "Pizza Quatro Queijos Gourmet",
                    description = "Molho de tomate especial, muçarela, provolone defumado, catupiry original e lascas de queijo azul gorgonzola.",
                    price = 48.00,
                    category = "Pizzas",
                    imageUrl = "pizza"
                ),
                Product(
                    name = "Batata Frita Sabor Supreme",
                    description = "Porção generosa de fritas rústicas e sequinhas salpicadas com queijo parmesão ralado e salsa fresca picadinha.",
                    price = 22.00,
                    category = "Lanches",
                    imageUrl = "fries"
                ),
                Product(
                    name = "Petit Gâteau Premium",
                    description = "Bolinho de chocolate com farto recheio cremoso e quente de chocolate belga, acompanhado de sorvete premium de creme.",
                    price = 18.00,
                    category = "Sobremesas",
                    imageUrl = "cake"
                ),
                Product(
                    name = "Milkshake Cremoso Nutella",
                    description = "Bebida gelada ultra cremosa batida com doce de leite, gelato de creme artesanal e recheio original de Nutella (400ml).",
                    price = 16.50,
                    category = "Sobremesas",
                    imageUrl = "icecream"
                ),
                Product(
                    name = "Suco Natural de Laranja",
                    description = "Suco natural extraído de laranjas selecionadas na hora, super gelado e sem adição de açúcares (500ml).",
                    price = 8.50,
                    category = "Bebidas",
                    imageUrl = "juice"
                ),
                Product(
                    name = "Coca-Cola Lata Gelada",
                    description = "Refrigerante Coca-Cola original lata de 350ml servido na temperatura perfeita.",
                    price = 6.00,
                    category = "Bebidas",
                    imageUrl = "soda"
                )
            )
            productDao.insertProducts(sampleProducts)
        }
    }

    // Add or increment item in cart
    suspend fun addToCart(productId: Int) {
        val existingCart = cartDao.getCartItems().first()
        val match = existingCart.find { it.productId == productId }
        if (match != null) {
            cartDao.updateCartItemQuantity(match.id, match.quantity + 1)
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, quantity = 1))
        }
    }

    // Remove or decrement item from cart
    suspend fun decrementCartItem(cartItemWithProduct: CartItemWithProduct) {
        val currentQty = cartItemWithProduct.cartItem.quantity
        if (currentQty <= 1) {
            cartDao.deleteCartItem(cartItemWithProduct.cartItem.id)
        } else {
            cartDao.updateCartItemQuantity(cartItemWithProduct.cartItem.id, currentQty - 1)
        }
    }

    // Directly delete product from cart
    suspend fun deleteCartItem(cartItemId: Int) {
        cartDao.deleteCartItem(cartItemId)
    }

    // Checkout active cart, computing values and emptying the cart
    suspend fun checkout(
        customerName: String,
        customerAddress: String,
        customerPhone: String,
        deliveryNotes: String
    ): Long {
        val cartItems = getCartItemsWithProducts().first()
        if (cartItems.isEmpty()) return -1

        // Structure items summary
        val summary = cartItems.joinToString(", ") { "${it.product.name} (x${it.cartItem.quantity})" }
        val total = cartItems.sumOf { it.product.price * it.cartItem.quantity }

        val newOrder = Order(
            status = "Pendente",
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone,
            totalAmount = total,
            deliveryNotes = deliveryNotes,
            itemsSummary = summary
        )

        val id = orderDao.insertOrder(newOrder)
        // Clear active cart items
        cartDao.clearCart()
        return id
    }

    // Update product price (Admin function)
    suspend fun updateProductPrice(productId: Int, price: Double) {
        productDao.updateProductPrice(productId, price)
    }

    // Update product availability (Admin function)
    suspend fun updateProductAvailability(productId: Int, isAvailable: Boolean) {
        productDao.updateProductAvailability(productId, isAvailable)
    }

    // Create a new custom menu product (Admin function)
    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    // Claim order for delivery rider (Driver function)
    suspend fun claimOrder(orderId: Int, driverName: String) {
        orderDao.claimOrder(orderId, driverName, "Preparando")
    }

    // Update order state (Driver or System)
    suspend fun updateOrderStatus(orderId: Int, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }
}
