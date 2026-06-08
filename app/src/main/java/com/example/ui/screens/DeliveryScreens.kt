package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItemWithProduct
import com.example.data.Order
import com.example.data.Product
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.LightBg
import com.example.ui.theme.SecondaryOrange
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.ClienteTab
import com.example.ui.viewmodel.DeliveryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDeliveryApp(viewModel: DeliveryViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val clienteTab by viewModel.clienteTab.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val checkoutMessage by viewModel.checkoutSuccessMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Show a snackbar when order checkouts successfully
    LaunchedEffect(checkoutMessage) {
        checkoutMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCheckoutNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Delivery Local 🛵",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Quick stats badge or info
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Informações do APP e Política de Privacidade",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Role switcher bottom bar to cleanly simulate standard roles
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentRole == AppRole.CLIENTE,
                    onClick = { viewModel.setRole(AppRole.CLIENTE) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0 && currentRole != AppRole.CLIENTE) {
                                    Badge { Text(cartItemCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cliente")
                        }
                    },
                    label = { Text("Mesa/Cliente", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentRole == AppRole.ENTREGADOR,
                    onClick = { viewModel.setRole(AppRole.ENTREGADOR) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Entregador") },
                    label = { Text("Entregador", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentRole == AppRole.ADMIN,
                    onClick = { viewModel.setRole(AppRole.ADMIN) },
                    icon = { Icon(Icons.Filled.Build, contentDescription = "Painel de Preços") },
                    label = { Text("Gerência", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated role container
            AnimatedContent(
                targetState = currentRole,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "role_content_transition"
            ) { role ->
                when (role) {
                    AppRole.CLIENTE -> ClienteRoleContainer(viewModel)
                    AppRole.ENTREGADOR -> EntregadorRoleScreen(viewModel)
                    AppRole.ADMIN -> AdminRoleScreen(viewModel)
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Política de Privacidade 🛡️", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Este aplicativo preza pela segurança dos seus dados e foi desenvolvido de acordo com as diretrizes da LGPD (Lei Geral de Proteção de Dados) para as simulações locais do app.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "1. Coleta e Uso de Informações",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "O app processa apenas dados voluntários fornecidos para a entrega simulada, como endereço fictício e itens selecionados no cardápio de padaria/orgânicos.",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "2. Armazenamento Seguro no Seu Dispositivo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Para sua segurança total, todos os registros de pedidos e de entregas são salvos unicamente no banco de dados local Room, persistido no próprio armazenamento interno do seu aparelho Android.",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "3. Compartilhamento e Terceiros",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Nenhuma informação de rota, itens ou nome inseridos é compartilhada com servidores do desenvolvedor ou vendida para terceiros/redes de anúncios.",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "4. Controle Total do Usuário",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Caso deseje apagar todas as informações instantaneamente, basta clicar em 'Redefinir Banco de Dados' na aba Gerência ou limpar o cache/dados do app nas configurações do Android.",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = "Versão 1.1 • Natural Tones Design",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Aceitar e Voltar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// 1. CLIENTE ROLE (Cardapio, Carrinho, Pedidos)
// ==========================================

@Composable
fun ClienteRoleContainer(viewModel: DeliveryViewModel) {
    val activeTab by viewModel.clienteTab.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Inner client screen switcher tabs
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = activeTab == ClienteTab.CARDAPIO,
                onClick = { viewModel.setClienteTab(ClienteTab.CARDAPIO) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cardápio", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activeTab == ClienteTab.CARRINHO,
                onClick = { viewModel.setClienteTab(ClienteTab.CARRINHO) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(cartItemCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Carrinho", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activeTab == ClienteTab.PEDIDOS,
                onClick = { viewModel.setClienteTab(ClienteTab.PEDIDOS) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pedidos", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Display current client screen
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeTab) {
                ClienteTab.CARDAPIO -> CardapioScreen(viewModel)
                ClienteTab.CARRINHO -> CarrinhoScreen(viewModel)
                ClienteTab.PEDIDOS -> ClientePedidosScreen(viewModel)
            }
        }
    }
}

@Composable
fun CardapioScreen(viewModel: DeliveryViewModel) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("Todos", "Lanches", "Pizzas", "Bebidas", "Sobremesas")

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero / Banner Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_delivery_hero),
                    contentDescription = "Delivery banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay to make text more readable
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
                // Overlay text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Seu Delivery Local Favorito 🛵",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Produtos orgânicos e lanches rápidos fresquinhos!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        // Horizontal Scroll category list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedCategory(category) },
                    label = { Text(category, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        // Filtering list based on selected category & active menu availability
        val filteredProducts = products.filter { product ->
            (selectedCategory == "Todos" || product.category == selectedCategory) && product.isAvailable
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Cardápio Vazio ou Itens Indisponíveis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "Volte mais tarde ou tente escolher outra categoria.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(product = product, onAddToCart = { viewModel.addToCart(product.id) })
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual food category representation icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                SecondaryOrange.copy(alpha = 0.25f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (product.category) {
                    "Lanches" -> "🍔"
                    "Pizzas" -> "🍕"
                    "Bebidas" -> "🍹"
                    "Sobremesas" -> "🍰"
                    else -> "🍽️"
                }
                Text(emoji, fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale("pt", "BR"), "R$ %.2f", product.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Quick add button
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .testTag("add_to_cart_btn_${product.id}")
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Adicionar ao carrinho",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pedir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CarrinhoScreen(viewModel: DeliveryViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.cartTotalPrice.collectAsState()

    // Form Fields
    var clientName by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var deliveryNotes by remember { mutableStateOf("") }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛒", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Seu carrinho está vazio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Explore o nosso cardápio e adicione lanches saborosos para prosseguir!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.setClienteTab(ClienteTab.CARDAPIO) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Cardápio", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    "Itens Escolhidos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            }

            // Cart Items List
            items(cartItems) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                String.format(Locale("pt", "BR"), "Subtotal: R$ %.2f", item.product.price * item.cartItem.quantity),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Quantity modifications
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.decrementCartQuantity(item) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(LightBg, RoundedCornerShape(4.dp))
                            ) {
                                Text("-", fontWeight = FontWeight.Black, fontSize = 16.sp, color = WarmOrange)
                            }

                            Text(
                                item.cartItem.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepCharcoal,
                                modifier = Modifier.testTag("cart_quantity_${item.product.id}")
                            )

                            IconButton(
                                onClick = { viewModel.addToCart(item.product.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(LightBg, RoundedCornerShape(4.dp))
                            ) {
                                Text("+", fontWeight = FontWeight.Black, fontSize = 16.sp, color = WarmOrange)
                            }

                            IconButton(
                                onClick = { viewModel.removeFromCart(item.cartItem.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remover",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Total summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total do Pedido:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale("pt", "BR"), "R$ %.2f", totalPrice),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.testTag("cart_total")
                        )
                    }
                }
            }

            // Form Title
            item {
                Text(
                    "Informações de Entrega (Mesa / Endereço)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            }

            // Client Input Fields
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Seu Nome") },
                            placeholder = { Text("Ex: João Silva") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_input_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = clientAddress,
                            onValueChange = { clientAddress = it },
                            label = { Text("Mesa ou Endereço Completo") },
                            placeholder = { Text("Ex: Mesa 4 ou Rua das Flores, 123") },
                            singleLine = false,
                            maxLines = 2,
                            leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_input_address"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Telefone / WhatsApp") },
                            placeholder = { Text("Ex: (11) 99999-9999") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Filled.Call, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_input_phone"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = deliveryNotes,
                            onValueChange = { deliveryNotes = it },
                            label = { Text("Observação do Pedido (Opcional)") },
                            placeholder = { Text("Ex: Sem cebola, trazer troco para R$ 50") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val formValid = clientName.isNotBlank() && clientAddress.isNotBlank() && clientPhone.isNotBlank()

                        Button(
                            onClick = {
                                viewModel.placeOrder(
                                    customerName = clientName,
                                    customerAddress = clientAddress,
                                    customerPhone = clientPhone,
                                    deliveryNotes = deliveryNotes
                                )
                            },
                            enabled = formValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Enviar Pedido de Delivery 🚀",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!formValid) {
                            Text(
                                "* Preencha Nome, Endereço/Mesa e Telefone para liberar o fechamento.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientePedidosScreen(viewModel: DeliveryViewModel) {
    val orders by viewModel.orders.collectAsState()

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📋", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Nenhum pedido feito ainda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    "Quando você fizer pedidos aparecerá o status real aqui!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    "Seus Pedidos Recentes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            }

            items(orders) { order ->
                OrderClientCard(order = order)
            }
        }
    }
}

@Composable
fun OrderClientCard(order: Order) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale("pt", "BR")) }
    val formattedTime = formatter.format(Date(order.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pedido #${order.id}",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = DeepCharcoal
                )

                OrderBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LightBg)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Itens: ${order.itemsSummary}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DeepCharcoal
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Mesa/Endereço: ${order.customerAddress}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Timeline Indicator
            OrderStatusProgressBar(status = order.status)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LightBg)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Horário: $formattedTime", fontSize = 12.sp, color = Color.Gray)
                    if (order.driverName != null) {
                        Text("🛵 Entregador: ${order.driverName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Aguardando Entregador", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Text(
                    String.format(Locale("pt", "BR"), "Pago: R$ %.2f", order.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OrderBadge(status: String) {
    val (color, text) = when (status) {
        "Pendente" -> Color(0xFFFF9100) to "Pendente"
        "Preparando" -> Color(0xFFFFCC00) to "Na Cozinha"
        "Em Rota" -> Color(0xFF1E88E5) to "Saiu pra Entrega"
        "Entregue" -> Color(0xFF4CAF50) to "Entregue!"
        else -> Color.Gray to status
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
fun OrderStatusProgressBar(status: String) {
    val step = when (status) {
        "Pendente" -> 1
        "Preparando" -> 2
        "Em Rota" -> 3
        "Entregue" -> 4
        else -> 1
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { (step / 4.0f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = LightBg
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enviado", fontSize = 11.sp, fontWeight = if (step >= 1) FontWeight.Bold else FontWeight.Normal, color = if (step >= 1) MaterialTheme.colorScheme.primary else Color.Gray)
            Text("Cozinha", fontSize = 11.sp, fontWeight = if (step >= 2) FontWeight.Bold else FontWeight.Normal, color = if (step >= 2) MaterialTheme.colorScheme.primary else Color.Gray)
            Text("Em Rota", fontSize = 11.sp, fontWeight = if (step >= 3) FontWeight.Bold else FontWeight.Normal, color = if (step >= 3) MaterialTheme.colorScheme.primary else Color.Gray)
            Text("Entregue", fontSize = 11.sp, fontWeight = if (step >= 4) FontWeight.Bold else FontWeight.Normal, color = if (step >= 4) MaterialTheme.colorScheme.primary else Color.Gray)
        }
    }
}

// ==========================================
// 2. ENTREGADOR ROLE (Rider Screen)
// ==========================================

@Composable
fun EntregadorRoleScreen(viewModel: DeliveryViewModel) {
    val orders by viewModel.orders.collectAsState()
    var driverNameInput by remember { mutableStateOf("") }
    var activeDriverName by remember { mutableStateOf("") }

    if (activeDriverName.isBlank()) {
        // Simple Delivery profile registration
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🛵 Area do Entregador", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Escreva seu nome para se registrar na rodada de entregas.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)

                    OutlinedTextField(
                        value = driverNameInput,
                        onValueChange = { driverNameInput = it },
                        label = { Text("Nome do Entregador") },
                        placeholder = { Text("Ex: Carlos Motoboy") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("driver_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = {
                            if (driverNameInput.isNotBlank()) {
                                activeDriverName = driverNameInput
                            }
                        },
                        enabled = driverNameInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("driver_enter_btn")
                    ) {
                        Text("Iniciar Rodada de Trabalho", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Driver workspace active
        val availableOrders = orders.filter { it.status == "Pendente" && it.driverName == null }
        val myClaimedOrders = orders.filter { it.driverName == activeDriverName && it.status != "Entregue" }
        val completedDeliveriesCount = orders.count { it.driverName == activeDriverName && it.status == "Entregue" }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Stats & Header block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Rider Ativo: $activeDriverName 🛵", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Entregas feitas hoje: $completedDeliveriesCount", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = { activeDriverName = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Sair", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 1: Claimed & Active Deliveries (High priority!)
            item {
                Text(
                    "Minhas Entregas Ativas (${myClaimedOrders.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            }

            if (myClaimedOrders.isEmpty()) {
                item {
                    Text(
                        "Nenhuma entrega ativa no momento. Aceite pedidos pendentes abaixo!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(myClaimedOrders) { order ->
                    ActiveDeliveryDriverCard(order = order, onUpdateStatus = { nextStatus ->
                        viewModel.updateOrderStatus(order.id, nextStatus)
                    })
                }
            }

            // Section 2: Incoming Available Deliveries in Restaurant
            item {
                Text(
                    "Chamadas para Coleta (${availableOrders.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            }

            if (availableOrders.isEmpty()) {
                item {
                    Text(
                        "Sem pedidos disponíveis no momento. Aguarde novos pedidos de clientes...",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(availableOrders) { order ->
                    AvailableDeliveryCard(order = order, onClaim = {
                        viewModel.claimOrderForDelivery(order.id, activeDriverName)
                    })
                }
            }
        }
    }
}

@Composable
fun AvailableDeliveryCard(order: Order, onClaim: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("available_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pedido #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    String.format(Locale("pt", "BR"), "R$ %.2f", order.totalAmount),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LightBg)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Coletar em: Cozinha Central 🍟", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Entregar para: ${order.customerName}", fontSize = 13.sp, color = Color.Gray)
            Text("Endereço: ${order.customerAddress}", fontSize = 13.sp, color = Color.Gray)
            Text("Itens do pedido: ${order.itemsSummary}", fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClaim,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("claim_btn_${order.id}"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aceitar & Cozinhar pedido", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveDeliveryDriverCard(order: Order, onUpdateStatus: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pedido #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OrderBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LightBg)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Cliente: ${order.customerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Endereço: ${order.customerAddress}", fontSize = 13.sp, color = Color.Gray)
            Text("Contato: ${order.customerPhone}", fontSize = 13.sp, color = Color.Gray)
            if (order.deliveryNotes.isNotBlank()) {
                Text("Observações: ${order.deliveryNotes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action dispatcher based on current status transition
            when (order.status) {
                "Preparando" -> {
                    Button(
                        onClick = { onUpdateStatus("Em Rota") },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                        modifier = Modifier.fillMaxWidth().testTag("transit_btn_${order.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Rota de Entrega 🛵", fontWeight = FontWeight.Bold)
                    }
                }
                "Em Rota" -> {
                    Button(
                        onClick = { onUpdateStatus("Entregue") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth().testTag("deliver_btn_${order.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Entrega Realizada! ✅", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. ADMIN ROLE (Gerência & Painel de Preços)
// ==========================================

@Composable
fun AdminRoleScreen(viewModel: DeliveryViewModel) {
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Analytics computation
    val totalRevenue = orders.filter { it.status == "Entregue" }.sumOf { it.totalAmount }
    val completedCount = orders.count { it.status == "Entregue" }
    val activeCount = orders.count { it.status != "Entregue" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Business statistics banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Painel Financeiro & Pedidos", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepCharcoal)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Receita (Entregues)", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                String.format(Locale("pt", "BR"), "R$ %.2f", totalRevenue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.testTag("admin_revenue")
                            )
                        }
                        Column {
                            Text("Entregas", fontSize = 12.sp, color = Color.Gray)
                            Text("$completedCount salvas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                        }
                        Column {
                            Text("Em Produção", fontSize = 12.sp, color = Color.Gray)
                            Text("$activeCount ativos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Section header with functional action to add products
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Controle de Preços & Menu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )

                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Menu manager listing
        items(products) { product ->
            AdminProductEditableCard(
                product = product,
                onUpdatePrice = { price -> viewModel.updateProductPrice(product.id, price) },
                onUpdateAvailability = { active -> viewModel.updateProductAvailability(product.id, active) }
            )
        }
    }

    // Modal to create a new Menu Product
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priceInput by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Lanches") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Adicionar Produto", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do item") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_product_name")
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Preço sugerido (ex: 29.90)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_product_price")
                    )

                    // Simple select category
                    Text("Categoria:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val cats = listOf("Lanches", "Pizzas", "Bebidas", "Sobremesas")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cats.forEach { cat ->
                            Button(
                                onClick = { category = cat },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (category == cat) MaterialTheme.colorScheme.primary else LightBg,
                                    contentColor = if (category == cat) Color.White else Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(cat, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val valPrice = priceInput.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        if (name.isNotBlank() && valPrice > 0.0) {
                            viewModel.addNewProduct(name, description, valPrice, category)
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && valPrice > 0.0,
                    modifier = Modifier.testTag("add_product_confirm")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AdminProductEditableCard(
    product: Product,
    onUpdatePrice: (Double) -> Unit,
    onUpdateAvailability: (Boolean) -> Unit
) {
    var priceEditState by remember { mutableStateOf(false) }
    var inputPriceText by remember { mutableStateOf(product.price.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("admin_product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(product.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Active status switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(if (product.isAvailable) "Disponível" else "Esgotado", fontSize = 11.sp, color = if (product.isAvailable) Color(0xFF4CAF50) else Color.Red, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = onUpdateAvailability,
                        modifier = Modifier
                            .scale(0.81f)
                            .testTag("availability_switch_${product.id}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LightBg)
            Spacer(modifier = Modifier.height(8.dp))

            // Price adjuster layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (priceEditState) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputPriceText,
                            onValueChange = { inputPriceText = it },
                            placeholder = { Text("Preço") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(120.dp)
                                .height(50.dp)
                                .testTag("price_input_${product.id}"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        IconButton(
                            onClick = {
                                val valNewPrice = inputPriceText.toDoubleOrNull()
                                if (valNewPrice != null && valNewPrice >= 0.0) {
                                    onUpdatePrice(valNewPrice)
                                    priceEditState = false
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .testTag("price_save_btn_${product.id}")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Salvar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                inputPriceText = product.price.toString()
                                priceEditState = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(LightBg, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Text(
                        String.format(Locale("pt", "BR"), "Preço Atual: R$ %.2f", product.price),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )

                    Button(
                        onClick = { priceEditState = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBg),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("price_edit_trigger_${product.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar Preço", modifier = Modifier.size(14.dp), tint = DeepCharcoal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Atualizar Preço", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    }
                }
            }
        }
    }
}

// Relying on standard androidx.compose.ui.draw.scale import
