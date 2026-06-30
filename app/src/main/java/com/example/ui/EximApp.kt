package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EximApp(viewModel: EximViewModel) {
    val currentScreen = viewModel.currentScreen
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()

    MyApplicationTheme {
        if (currentScreen is Screen.Onboarding) {
            OnboardingScreen(onLogin = viewModel::handleLogin)
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EximConnect",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        actions = {
                            if (userSession != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    // Premium Badge / Toggle
                                    TextButton(
                                        onClick = { viewModel.togglePremium() },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (userSession?.isPremium == true) PendingAmber else MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            if (userSession?.isPremium == true) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (userSession?.isPremium == true) "Premium Active" else "Upgrade Plan",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.logout() },
                                        modifier = Modifier.testTag("logout_button")
                                    ) {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = "Log out",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                        is Screen.Marketplace -> MarketplaceScreen(viewModel = viewModel)
                        is Screen.ProductDetail -> ProductDetailScreen(
                            product = currentScreen.product,
                            viewModel = viewModel
                        )
                        is Screen.AddProduct -> AddProductScreen(viewModel = viewModel)
                        is Screen.TradeBoard -> TradeBoardScreen(viewModel = viewModel)
                        is Screen.TradeRequirementDetail -> TradeRequirementDetailScreen(
                            requirement = currentScreen.requirement,
                            viewModel = viewModel
                        )
                        is Screen.ChatList -> ChatListScreen(viewModel = viewModel)
                        is Screen.ActiveChat -> ActiveChatScreen(
                            partnerName = currentScreen.partnerName,
                            viewModel = viewModel
                        )
                        is Screen.RfqHub -> RfqHubScreen(viewModel = viewModel)
                        is Screen.CreateRfq -> CreateRfqScreen(viewModel = viewModel)
                        is Screen.LogisticsHub -> LogisticsHubScreen(viewModel = viewModel)
                        is Screen.LearningCenter -> LearningCenterScreen()
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(Screen.Dashboard, Icons.Default.Home, "Home"),
            Triple(Screen.Marketplace, Icons.Default.Storefront, "Marketplace"),
            Triple(Screen.TradeBoard, Icons.Default.Announcement, "Trade Board"),
            Triple(Screen.ChatList, Icons.Default.Forum, "Chats"),
            Triple(Screen.RfqHub, Icons.Default.Description, "RFQs"),
            Triple(Screen.LogisticsHub, Icons.Default.Explore, "Logistics")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = when (currentScreen) {
                is Screen.Dashboard -> screen is Screen.Dashboard
                is Screen.Marketplace -> screen is Screen.Marketplace
                is Screen.ProductDetail -> screen is Screen.Marketplace
                is Screen.AddProduct -> screen is Screen.Marketplace
                is Screen.TradeBoard -> screen is Screen.TradeBoard
                is Screen.TradeRequirementDetail -> screen is Screen.TradeBoard
                is Screen.ChatList -> screen is Screen.ChatList
                is Screen.ActiveChat -> screen is Screen.ChatList
                is Screen.RfqHub -> screen is Screen.RfqHub
                is Screen.CreateRfq -> screen is Screen.RfqHub
                is Screen.LogisticsHub -> screen is Screen.LogisticsHub
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.tertiary,
                    selectedTextColor = MaterialTheme.colorScheme.tertiary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}

// --- ONBOARDING / LOGIN ---
@Composable
fun OnboardingScreen(onLogin: (String, String, String, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("United States") }
    var expandedRole by remember { mutableStateOf(false) }
    val roles = listOf("Exporter", "Importer", "Manufacturer", "Logistics", "Customs Broker", "Admin")
    var selectedRole by remember { mutableStateOf("Exporter") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyPrimary, MaritimeSecondary)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Public,
            contentDescription = null,
            tint = TradeGreenAccent,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EXIMCONNECT",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Text(
            text = "Global B2B Commerce & Trade Hub",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Business Profile Setup",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LightGrayText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Your Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = DarkGrayText,
                        focusedTextColor = LightGrayText,
                        unfocusedTextColor = LightGrayText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("onboarding_username_input")
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = DarkGrayText,
                        focusedTextColor = LightGrayText,
                        unfocusedTextColor = LightGrayText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("onboarding_company_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Work Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = DarkGrayText,
                        focusedTextColor = LightGrayText,
                        unfocusedTextColor = LightGrayText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("onboarding_email_input")
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country of Operation") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = DarkGrayText,
                        focusedTextColor = LightGrayText,
                        unfocusedTextColor = LightGrayText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Role Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    OutlinedCard(
                        onClick = { expandedRole = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = SlateCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Select Corporate Role", fontSize = 12.sp, color = DarkGrayText)
                                Text(selectedRole, fontWeight = FontWeight.Bold, color = LightGrayText)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LightGrayText)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(SlateCard)
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, color = LightGrayText) },
                                onClick = {
                                    selectedRole = role
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (username.isNotEmpty() && companyName.isNotEmpty() && email.isNotEmpty()) {
                            onLogin(username, companyName, email, selectedRole, country)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_submit_button"),
                    enabled = username.isNotEmpty() && companyName.isNotEmpty() && email.isNotEmpty()
                ) {
                    Text("Enter Global Network", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// --- DASHBOARD / HOME ---
@Composable
fun DashboardScreen(viewModel: EximViewModel) {
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    val rfqsList by viewModel.rfqs.collectAsStateWithLifecycle()
    val msgsList by viewModel.messages.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (session?.username?.firstOrNull() ?: 'U').toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session?.companyName ?: "Atlas Trading",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = LightGrayText
                            )
                            if (session?.isVerified == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified company",
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Role: ${session?.role ?: "Exporter"} | ${session?.country ?: "United States"}",
                            fontSize = 13.sp,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ActiveGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Operational Status: Active", fontSize = 11.sp, color = ActiveGreen)
                        }
                    }
                }
            }
        }

        // Action Quick Navigation Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButtonWithText(
                        icon = Icons.Default.Add,
                        label = "Add Product",
                        onClick = { viewModel.navigateTo(Screen.AddProduct) }
                    )
                    IconButtonWithText(
                        icon = Icons.Default.Description,
                        label = "Create RFQ",
                        onClick = { viewModel.navigateTo(Screen.CreateRfq) }
                    )
                    IconButtonWithText(
                        icon = Icons.Default.MenuBook,
                        label = "Learn Center",
                        onClick = { viewModel.navigateTo(Screen.LearningCenter) }
                    )
                }
            }
        }

        // Stats overview cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Market Listings",
                    value = productsList.size.toString(),
                    icon = Icons.Default.Storefront,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Requests for Quote",
                    value = rfqsList.size.toString(),
                    icon = Icons.Default.ReceiptLong,
                    color = PendingAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Premium Upgrader Overlay / Metric Analytics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Corporate Traffic & Performance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = LightGrayText
                        )
                        if (session?.isPremium == false) {
                            Badge(containerColor = PendingAmber) {
                                Text("LOCKED", color = Color.Black, modifier = Modifier.padding(2.dp))
                            }
                        } else {
                            Badge(containerColor = ActiveGreen) {
                                Text("LIVE", color = Color.White, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (session?.isPremium == true) {
                        Text(
                            text = "Analytics Overview (B2B Leads vs Port Visitors)",
                            fontSize = 12.sp,
                            color = LightGrayText.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        // Custom canvas drawing representing B2B monthly leads
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(SlateCard, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawAnalyticsChart()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF1B6A97)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Monthly Visitors", fontSize = 11.sp, color = LightGrayText)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF059669)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Business Inquiries", fontSize = 11.sp, color = LightGrayText)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateCard, shape = RoundedCornerShape(8.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = PendingAmber,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Premium Analytics Disabled",
                                fontWeight = FontWeight.SemiBold,
                                color = LightGrayText,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Upgrade to analyze company views, supplier clicks, and lead statistics directly.",
                                color = DarkGrayText,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.togglePremium() },
                                colors = ButtonDefaults.buttonColors(containerColor = PendingAmber)
                            ) {
                                Text("Unlock Premium Tools", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Operational Trade Alerts & News Feed
        item {
            Text(
                text = "Operational Trade Alerts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LightGrayText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val alerts = listOf(
            "US Port Congestion alert: Shanghai to LA sea route average transit time increased to 22 days due to weather delays.",
            "HS Code Update: Customs administration clarifies classification of solar-powered battery cells under tariff 8507.20.",
            "Incoterms update: Baltic freight index shows a 4% reduction in dry bulk maritime cost for July operations."
        )

        items(alerts) { alert ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = alert,
                        fontSize = 12.sp,
                        color = LightGrayText
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAnalyticsChart() {
    val barWidth = 32f
    val spacing = 80f
    val originX = 50f
    val originY = size.height - 20f
    
    // Draw Axis
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(originX, 10f),
        end = Offset(originX, originY),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(originX, originY),
        end = Offset(size.width - 10f, originY),
        strokeWidth = 2f
    )

    // Draw Bars
    val data = listOf(
        Pair(60f, 30f),  // Month 1
        Pair(95f, 45f),  // Month 2
        Pair(120f, 65f), // Month 3
        Pair(150f, 85f)  // Month 4
    )

    data.forEachIndexed { idx, pair ->
        val xVal = originX + 50f + (idx * spacing)
        
        // Month Visitors Bar (Maritime blue)
        drawRect(
            color = Color(0xFF1B6A97),
            topLeft = Offset(xVal, originY - pair.first),
            size = Size(barWidth, pair.first)
        )

        // Month Leads Bar (Green)
        drawRect(
            color = Color(0xFF059669),
            topLeft = Offset(xVal + barWidth + 6f, originY - pair.second),
            size = Size(barWidth, pair.second)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = DarkGrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LightGrayText)
        }
    }
}

@Composable
fun IconButtonWithText(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SlateCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = LightGrayText)
    }
}

// --- MARKETPLACE SCREEN ---
@Composable
fun MarketplaceScreen(viewModel: EximViewModel) {
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Agricultural Products", "Electrical Equipment", "Sustainable Materials")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Products, HS Codes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable categories row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Add Product Listing Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wholesale Catalog",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LightGrayText
            )
            Button(
                onClick = { viewModel.navigateTo(Screen.AddProduct) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("List Product", fontWeight = FontWeight.SemiBold)
            }
        }

        // Product Listings Grid/List
        val filtered = productsList.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.hsCode.contains(searchQuery))
        }

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.SearchOff, contentDescription = null, tint = DarkGrayText, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No matching products found", color = LightGrayText)
                Text("Try refining your filters or search keywords.", color = DarkGrayText, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filtered) { product ->
                    ProductCard(product = product, onClick = { viewModel.navigateTo(Screen.ProductDetail(product)) })
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Unsplash image simulation
            AsyncImage(
                model = product.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b" },
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LightGrayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${product.price} / unit",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "Category: ${product.category}",
                    fontSize = 11.sp,
                    color = DarkGrayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HS Code: ${product.hsCode}", fontSize = 12.sp, color = LightGrayText)
                        Text("Origin: ${product.countryOfOrigin}", fontSize = 12.sp, color = LightGrayText)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("MOQ: ${product.moq} units", fontSize = 12.sp, color = PendingAmber, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PendingAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(product.rating.toString(), fontSize = 12.sp, color = LightGrayText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ActiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.creatorCompanyName,
                            fontSize = 11.sp,
                            color = ActiveGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (product.isVerifiedSupplier) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Verified Exporter", fontSize = 10.sp, color = ActiveGreen)
                        }
                    }
                }
            }
        }
    }
}

// --- PRODUCT DETAIL SCREEN ---
@Composable
fun ProductDetailScreen(product: Product, viewModel: EximViewModel) {
    var inquiriesSent by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                AsyncImage(
                    model = product.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b" },
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = LightGrayText
                    )
                    Text(
                        text = "$${product.price} / unit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = PendingAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(product.rating.toString(), color = LightGrayText, fontSize = 13.sp)
                }
            }
        }

        // Technical Specifications Tab Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Technical Specifications & Logistics", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 15.sp)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = DarkGrayText.copy(alpha = 0.3f))

                    SpecificationRow(label = "Harmonized Code (HS)", value = product.hsCode)
                    SpecificationRow(label = "Minimum Order Quantity", value = "${product.moq} Units")
                    SpecificationRow(label = "Origin Country", value = product.countryOfOrigin)
                    SpecificationRow(label = "Available Lot Volume", value = "${product.availableQuantity} Units")
                    SpecificationRow(label = "Certificates Provided", value = product.certificates)
                }
            }
        }

        // Rich Description
        item {
            Column {
                Text("Product Overview", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    color = LightGrayText.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // Supplier Connection Details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(product.creatorCompanyName, fontWeight = FontWeight.Bold, color = LightGrayText)
                        Text("Business: ${product.creatorRole}", fontSize = 12.sp, color = DarkGrayText)
                    }
                    if (product.isVerifiedSupplier) {
                        Badge(containerColor = ActiveGreen) {
                            Text("VERIFIED SUPPLIER", color = Color.White, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.sendMessage(
                            product.creatorCompanyName,
                            "Hello! We are interested in your listing: '${product.name}' (HS: ${product.hsCode}). We would like to inquire about bulk ordering ${product.moq} units to our seaport. Can you provide freight details?"
                        )
                        viewModel.navigateTo(Screen.ActiveChat(product.creatorCompanyName))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inquire Bulk Order")
                }
            }
        }
    }
}

@Composable
fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = DarkGrayText, fontSize = 13.sp)
        Text(value, color = LightGrayText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// --- ADD PRODUCT SCREEN ---
@Composable
fun AddProductScreen(viewModel: EximViewModel) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Agricultural Products") }
    var price by remember { mutableStateOf("") }
    var moq by remember { mutableStateOf("") }
    var hsCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var certs by remember { mutableStateOf("") }
    var specifications by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Agricultural Products", "Electrical Equipment", "Sustainable Materials")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("List Wholesale Product", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LightGrayText)
            Text("Ensure description and HS Codes are highly accurate to speed up clearance.", fontSize = 12.sp, color = DarkGrayText)
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Title / Model") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        // Category dropdown
        item {
            Column {
                Text("Product Category", fontSize = 12.sp, color = DarkGrayText)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("FOB Unit Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
                OutlinedTextField(
                    value = moq,
                    onValueChange = { moq = it },
                    label = { Text("Min Order Qty (MOQ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hsCode,
                    onValueChange = { hsCode = it },
                    label = { Text("HS Tariff Code (6-digit)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country of Origin") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Available Stock Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
                OutlinedTextField(
                    value = certs,
                    onValueChange = { certs = it },
                    label = { Text("Certificates (SGS, CE...)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
            }
        }

        // Smart specs field
        item {
            OutlinedTextField(
                value = specifications,
                onValueChange = { specifications = it },
                label = { Text("Key specifications / Ingredients / Dimensions") },
                placeholder = { Text("e.g. Pure copper core, BS5467 standard, length 20m") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        // Description with Gemini draft button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wholesale B2B Description", fontWeight = FontWeight.SemiBold, color = LightGrayText, fontSize = 13.sp)
                        Button(
                            onClick = {
                                viewModel.generateProductDescription(
                                    productName = name,
                                    category = category,
                                    specs = specifications
                                ) { draft ->
                                    description = draft
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            enabled = name.isNotEmpty() && !viewModel.isGeneratingDescription,
                            modifier = Modifier.testTag("ai_generate_description_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (viewModel.isGeneratingDescription) "Generating..." else "AI Draft", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val finalPrice = price.toDoubleOrNull() ?: 1.0
                    val finalMoq = moq.toIntOrNull() ?: 100
                    val finalQty = qty.toIntOrNull() ?: 1000
                    viewModel.addProduct(
                        name = name,
                        category = category,
                        price = finalPrice,
                        moq = finalMoq,
                        hsCode = hsCode,
                        country = country,
                        qty = finalQty,
                        certs = certs,
                        desc = description
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotEmpty() && price.isNotEmpty() && moq.isNotEmpty()
            ) {
                Text("Publish to Marketplace", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TRADE BOARD SCREEN ---
@Composable
fun TradeBoardScreen(viewModel: EximViewModel) {
    val reqs by viewModel.requirements.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val requirementTypes = listOf("All", "Looking for Suppliers", "Looking for Buyers", "Partnership Opportunities")

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Global Requirement Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightGrayText
                )
                Text(
                    text = "Post supply needs, tender announcements, or export partnerships.",
                    fontSize = 12.sp,
                    color = DarkGrayText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Types filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    requirementTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Post Requirement Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active B2B Notices", fontWeight = FontWeight.Bold, color = LightGrayText)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Post Notice")
            }
        }

        val filtered = reqs.filter { selectedType == "All" || it.type == selectedType }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { req ->
                RequirementCard(requirement = req, onClick = { viewModel.navigateTo(Screen.TradeRequirementDetail(req)) })
            }
        }
    }

    if (showCreateDialog) {
        CreateRequirementDialog(
            onDismiss = { showCreateDialog = false },
            onPost = { title, type, desc, budget, hsCode ->
                viewModel.postRequirement(title, type, desc, budget, hsCode)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun RequirementCard(requirement: TradeRequirement, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    Text(requirement.type, color = Color.White, modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                }
                Text(
                    text = requirement.budget,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(requirement.title, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = requirement.description,
                color = LightGrayText.copy(alpha = 0.8f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(requirement.creatorCompanyName, fontSize = 11.sp, color = ActiveGreen, fontWeight = FontWeight.SemiBold)
                    Text("Country: ${requirement.creatorCountry}", fontSize = 10.sp, color = DarkGrayText)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// --- CREATE REQUIREMENT DIALOG ---
@Composable
fun CreateRequirementDialog(onDismiss: () -> Unit, onPost: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Looking for Suppliers") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var hsCode by remember { mutableStateOf("") }

    val types = listOf("Looking for Suppliers", "Looking for Buyers", "Partnership Opportunities")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Post Trade Notice", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 18.sp)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Notice Type", fontSize = 11.sp, color = DarkGrayText)
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.split(" ").last(), fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Specifications") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Target Budget (e.g. $5 / kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hsCode,
                    onValueChange = { hsCode = it },
                    label = { Text("Target HS Tariff Code (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkGrayText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onPost(title, selectedType, description, budget, hsCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        enabled = title.isNotEmpty() && description.isNotEmpty()
                    ) {
                        Text("Publish")
                    }
                }
            }
        }
    }
}

// --- TRADE REQUIREMENT DETAIL SCREEN ---
@Composable
fun TradeRequirementDetailScreen(requirement: TradeRequirement, viewModel: EximViewModel) {
    var offerSubmitted by remember { mutableStateOf(false) }
    var offerText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                            Text(requirement.type, color = Color.White, modifier = Modifier.padding(4.dp), fontSize = 11.sp)
                        }
                        Text(requirement.budget, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(requirement.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LightGrayText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = requirement.description,
                        color = LightGrayText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Posted By: ${requirement.creatorCompanyName}", fontWeight = FontWeight.SemiBold, color = LightGrayText, fontSize = 13.sp)
                        Text("Origin: ${requirement.creatorCountry}", color = DarkGrayText, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.sendMessage(
                                requirement.creatorCompanyName,
                                "Hello! We read your notice regarding '${requirement.title}'. We are a certified B2B trade supplier and would like to submit an official quotation. Let's exchange specs."
                            )
                            viewModel.navigateTo(Screen.ActiveChat(requirement.creatorCompanyName))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Chat Direct", fontSize = 12.sp)
                    }
                }
            }
        }

        // Bidding / Comments Box
        item {
            Text("Submit B2B Quotation Offer", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 15.sp)
        }

        item {
            if (offerSubmitted) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Offer Successfully Recorded!", fontWeight = FontWeight.SemiBold, color = LightGrayText)
                        Text("The buyer was notified and can view your details in their RFQ Hub.", fontSize = 12.sp, color = DarkGrayText, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = offerText,
                            onValueChange = { offerText = it },
                            placeholder = { Text("Specify unit pricing, lead time, certificates and delivery terms...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                        )

                        Button(
                            onClick = {
                                if (offerText.isNotEmpty()) {
                                    offerSubmitted = true
                                    // Send a message behind the scenes
                                    viewModel.sendMessage(
                                        requirement.creatorCompanyName,
                                        "🔔 QUOTATION BID SUBMITTED:\n$offerText"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Secure Quotation", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- CHAT LIST SCREEN ---
@Composable
fun ChatListScreen(viewModel: EximViewModel) {
    val msgs by viewModel.messages.collectAsStateWithLifecycle()

    // Find distinct conversation partners
    val partners = remember(msgs) {
        msgs.flatMap { listOf(it.senderId, it.receiverId) }
            .filter { it != "user" && it != "System" }
            .distinct()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "B2B Communication Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightGrayText
                )
                Text(
                    text = "Secure trading chats. Integrates real-time translation and AI Trade Assistant.",
                    fontSize = 12.sp,
                    color = DarkGrayText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (partners.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, tint = DarkGrayText, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No active B2B chats", color = LightGrayText)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(partners) { partner ->
                    val lastMsg = msgs.filter {
                        (it.senderId == "user" && it.receiverId == partner) ||
                                (it.senderId == partner && it.receiverId == "user")
                    }.maxByOrNull { it.timestamp }

                    val unreadCount = msgs.filter {
                        it.senderId == partner && it.receiverId == "user" && !it.isRead
                    }.size

                    ChatPartnerRow(
                        partnerName = partner,
                        lastMessage = lastMsg?.messageText ?: "Start trading conversation.",
                        unreadCount = unreadCount,
                        onClick = { viewModel.navigateTo(Screen.ActiveChat(partner)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatPartnerRow(partnerName: String, lastMessage: String, unreadCount: Int, onClick: () -> Unit) {
    val isAi = partnerName == "AI Trade Assistant"

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Icon Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isAi) Icons.Default.AutoAwesome else Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(partnerName, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isAi) ActiveGreen else Color.LightGray)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    color = LightGrayText.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ActiveGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(unreadCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ACTIVE CHAT SCREEN ---
@Composable
fun ActiveChatScreen(partnerName: String, viewModel: EximViewModel) {
    val allMsgs by viewModel.messages.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val chatHistory = remember(allMsgs, partnerName) {
        allMsgs.filter {
            (it.senderId == "user" && it.receiverId == partnerName) ||
                    (it.senderId == partnerName && it.receiverId == "user")
        }.sortedBy { it.timestamp }
    }

    // Scroll to bottom on updates
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Partner Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.ChatList) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightGrayText)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (partnerName == "AI Trade Assistant") Icons.Default.AutoAwesome else Icons.Default.Business,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(partnerName, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
                    Text(
                        text = if (partnerName == "AI Trade Assistant") "AI Core v3.5-flash | Active" else "Supplier Operational | Connected",
                        fontSize = 11.sp,
                        color = ActiveGreen
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatHistory) { msg ->
                val isMe = msg.senderId == "user"
                MessageBubble(message = msg, isMe = isMe)
            }

            if (viewModel.isSendingAiMessage) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Trade Assistant is drafting response...", fontSize = 12.sp, color = DarkGrayText)
                    }
                }
            }
        }

        // Media attachment bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateContainer)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                onClick = {
                    viewModel.sendMessage(
                        partnerName,
                        "📷 Sent a simulated Cargo Packing Inspection Photograph.",
                        fileMimeType = "image/jpeg",
                        fileName = "cargo_inspect_749.jpg"
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Attach Photo", fontSize = 10.sp, color = LightGrayText)
                }
            }

            OutlinedCard(
                onClick = {
                    viewModel.sendMessage(
                        partnerName,
                        "📄 Shared Trade Quote: 'proforma_invoice_EX843.pdf' (Standard Proforma Commercial Invoice).",
                        fileMimeType = "application/pdf",
                        fileName = "proforma_invoice_EX843.pdf"
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PendingAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Invoice", fontSize = 10.sp, color = LightGrayText)
                }
            }

            OutlinedCard(
                onClick = {
                    viewModel.sendMessage(
                        partnerName,
                        "🎙️ Simulated Audio Voice Memo: Cargo ETA update.",
                        fileMimeType = "audio/wav",
                        fileName = "voice_memo_ETA.wav"
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voice Memo", fontSize = 10.sp, color = LightGrayText)
                }
            }
        }

        // Chat Input Area
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Enter message, trade query...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotEmpty()) {
                            viewModel.sendMessage(partnerName, inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .testTag("send_chat_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.tertiary else SlateCard
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMe) 12.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 12.dp
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMe) "You" else message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.8f) else ActiveGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // PDF / Image Attachment layout
                if (message.fileMimeType != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepOceanBackground.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when {
                            message.fileMimeType.startsWith("image/") -> Icons.Default.Photo
                            message.fileMimeType == "application/pdf" -> Icons.Default.PictureAsPdf
                            else -> Icons.Default.Mic
                        }
                        Icon(icon, contentDescription = null, tint = if (isMe) Color.White else LightGrayText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(message.fileName ?: "Attachment", fontSize = 12.sp, color = if (isMe) Color.White else LightGrayText, fontWeight = FontWeight.Bold)
                            Text("Safe B2B Secure Vault Verified", fontSize = 10.sp, color = if (isMe) Color.White.copy(alpha = 0.6f) else DarkGrayText)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    color = if (isMe) Color.White else LightGrayText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// --- RFQ SCREEN ---
@Composable
fun RfqHubScreen(viewModel: EximViewModel) {
    val rfqsList by viewModel.rfqs.collectAsStateWithLifecycle()
    var selectedRfqForInvoicePdf by remember { mutableStateOf<Rfq?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Request for Quotation (RFQ) Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightGrayText
                )
                Text(
                    text = "Manage incoming supplier bids, accept agreements and export commercial contracts.",
                    fontSize = 12.sp,
                    color = DarkGrayText
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active RFQ Contracts", fontWeight = FontWeight.Bold, color = LightGrayText)
            Button(
                onClick = { viewModel.navigateTo(Screen.CreateRfq) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New RFQ")
            }
        }

        if (rfqsList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = DarkGrayText, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No RFQs submitted yet", color = LightGrayText)
                Text("Launch an RFQ to invite verified suppliers to quote.", color = DarkGrayText, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rfqsList) { rfq ->
                    RfqCard(
                        rfq = rfq,
                        onAccept = { viewModel.acceptRfqOffer(rfq.id) },
                        onReject = { viewModel.rejectRfqOffer(rfq.id) },
                        onDownloadPdf = { selectedRfqForInvoicePdf = rfq }
                    )
                }
            }
        }
    }

    if (selectedRfqForInvoicePdf != null) {
        InvoicePdfPreviewDialog(
            rfq = selectedRfqForInvoicePdf!!,
            onDismiss = { selectedRfqForInvoicePdf = null }
        )
    }
}

@Composable
fun RfqCard(rfq: Rfq, onAccept: () -> Unit, onReject: () -> Unit, onDownloadPdf: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rfq.title, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
                val (badgeColor, textColor) = when (rfq.status) {
                    "Accepted" -> Pair(ActiveGreen, Color.White)
                    "Quotes Received" -> Pair(PendingAmber, Color.Black)
                    else -> Pair(SlateCard, Color.White)
                }
                Badge(containerColor = badgeColor) {
                    Text(rfq.status, color = textColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Product Needed: ${rfq.targetProduct}", color = LightGrayText, fontSize = 13.sp)
            Text("Incoterms: ${rfq.incoterms} | Volume Needed: ${rfq.quantityRequired} units", color = LightGrayText, fontSize = 13.sp)
            Text("Target FOB Budget: $${rfq.targetPrice} / unit", color = PendingAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            if (rfq.status == "Quotes Received") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkGrayText.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Supplier Quote Bid arrived!", fontWeight = FontWeight.SemiBold, color = ActiveGreen, fontSize = 13.sp)
                Text("Supplier: Global Freight & Commodities. Offer: $${rfq.targetPrice * 0.95} / unit FOB (5% savings).", fontSize = 12.sp, color = LightGrayText)

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept Quote")
                    }
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Decline")
                    }
                }
            }

            if (rfq.status == "Accepted") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkGrayText.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDownloadPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Commercial Proforma (PDF)")
                }
            }
        }
    }
}

// --- CREATE RFQ SCREEN ---
@Composable
fun CreateRfqScreen(viewModel: EximViewModel) {
    var title by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var targetPrice by remember { mutableStateOf("") }
    var incoterms by remember { mutableStateOf("FOB - Free on Board") }
    var remarks by remember { mutableStateOf("") }
    var expandedIncoterms by remember { mutableStateOf(false) }

    val incotermsList = listOf("FOB - Free on Board", "CIF - Cost, Insurance & Freight", "EXW - Ex Works", "DDP - Delivered Duty Paid")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Create B2B RFQ Agreement", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LightGrayText)
            Text("Define targeting price and shipping incoterm to let suppliers submit bids directly.", fontSize = 12.sp, color = DarkGrayText)
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("RFQ Project Title") },
                placeholder = { Text("e.g. Supply of Green Coffee Beans for Q3") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        item {
            OutlinedTextField(
                value = product,
                onValueChange = { product = it },
                label = { Text("Target Product / Model Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity Needed") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = { targetPrice = it },
                    label = { Text("Target Price ($ / unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
            }
        }

        // Incoterms Selector
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = incoterms,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Shipping Incoterm Requirement") },
                    trailingIcon = { IconButton(onClick = { expandedIncoterms = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                )
                DropdownMenu(
                    expanded = expandedIncoterms,
                    onDismissRequest = { expandedIncoterms = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(SlateCard)
                ) {
                    incotermsList.forEach { term ->
                        DropdownMenuItem(
                            text = { Text(term, color = LightGrayText) },
                            onClick = {
                                incoterms = term
                                expandedIncoterms = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Specific packing instructions / Certification requirements") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        item {
            Button(
                onClick = {
                    val finalQty = qty.toIntOrNull() ?: 500
                    val finalPrice = targetPrice.toDoubleOrNull() ?: 5.0
                    viewModel.createRfq(title, product, finalQty, finalPrice, incoterms.split(" ").first(), remarks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = title.isNotEmpty() && product.isNotEmpty() && qty.isNotEmpty()
            ) {
                Text("Launch Official RFQ Campaign", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PDF INVOICE PREVIEW DIALOG ---
@Composable
fun InvoicePdfPreviewDialog(rfq: Rfq, onDismiss: () -> Unit) {
    val totalVal = rfq.quantityRequired * rfq.targetPrice

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EXPORT INVOICE PREVIEW", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                Divider(color = Color.LightGray)

                Text("COMMERCIAL AGREEMENT BILL", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)
                Text("Agreement: ${rfq.title}", color = Color.Black, fontSize = 14.sp)
                Text("Incoterm Type: ${rfq.incoterms}", color = Color.Black, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Item Description", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                    Text("Qty", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                    Text("Unit Price", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                    Text("Total (USD)", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(rfq.targetProduct, color = Color.Black, fontSize = 12.sp, modifier = Modifier.weight(1.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(rfq.quantityRequired.toString(), color = Color.Black, fontSize = 12.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text("$${rfq.targetPrice}", color = Color.Black, fontSize = 12.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                    Text("$$totalVal", color = Color.Black, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                Divider(color = Color.LightGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Grand Total: ", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                    Text("$$totalVal USD", fontWeight = FontWeight.Bold, color = Color(0xFF059669), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3B66)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Local Device Storage", color = Color.White)
                }
            }
        }
    }
}

// --- LOGISTICS HUB SCREEN ---
@Composable
fun LogisticsHubScreen(viewModel: EximViewModel) {
    var portOrigin by remember { mutableStateOf("") }
    var portDest by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var cargoVolume by remember { mutableStateOf("") }
    var containerType by remember { mutableStateOf("20ft Standard Container") }
    var comments by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Calculator, 1: Container Guide, 2: Incoterms Handbook

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateContainer),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Marine & Air Logistics Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightGrayText
                )
                Text(
                    text = "Calculate freight pricing rates, read packing guides and container specifications.",
                    fontSize = 12.sp,
                    color = DarkGrayText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Sub tabs selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateCard,
                    contentColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Freight Calc", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 0) MaterialTheme.colorScheme.tertiary else DarkGrayText)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Containers", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 1) MaterialTheme.colorScheme.tertiary else DarkGrayText)
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Incoterms", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = if (selectedTab == 2) MaterialTheme.colorScheme.tertiary else DarkGrayText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> FreightCalculatorLayout(
                origin = portOrigin,
                onOriginChange = { portOrigin = it },
                dest = portDest,
                onDestChange = { portDest = it },
                weight = cargoWeight,
                onWeightChange = { cargoWeight = it },
                volume = cargoVolume,
                onVolumeChange = { cargoVolume = it },
                containerType = containerType,
                onContainerTypeChange = { containerType = it },
                remarks = comments,
                onRemarksChange = { comments = it },
                onSubmit = {
                    viewModel.submitLogisticsInquiry(
                        origin = portOrigin,
                        destination = portDest,
                        weight = cargoWeight.toDoubleOrNull() ?: 1000.0,
                        volume = cargoVolume.toDoubleOrNull() ?: 5.0,
                        containerType = containerType,
                        incoterms = "FOB",
                        remarks = comments
                    )
                }
            )
            1 -> ContainerGuideLayout()
            2 -> IncotermsHandbookLayout()
        }
    }
}

@Composable
fun FreightCalculatorLayout(
    origin: String, onOriginChange: (String) -> Unit,
    dest: String, onDestChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    volume: String, onVolumeChange: (String) -> Unit,
    containerType: String, onContainerTypeChange: (String) -> Unit,
    remarks: String, onRemarksChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var estimateCalculated by remember { mutableStateOf(false) }
    var expandedContainer by remember { mutableStateOf(false) }
    val containers = listOf("20ft Standard Container", "40ft Standard Container", "40ft High Cube Container", "Less than Container Load (LCL)")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SlateContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Get Freight Logistics Quote", fontWeight = FontWeight.Bold, color = LightGrayText)

                    OutlinedTextField(
                        value = origin,
                        onValueChange = onOriginChange,
                        label = { Text("Port of Origin / Loading Port") },
                        placeholder = { Text("e.g. Shanghai Seaport, CN") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dest,
                        onValueChange = onDestChange,
                        label = { Text("Port of Destination / Discharge Port") },
                        placeholder = { Text("e.g. Port of New York, US") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = onWeightChange,
                            label = { Text("Cargo Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = volume,
                            onValueChange = onVolumeChange,
                            label = { Text("Cargo Vol (CBM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = containerType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Load Mode / Container Type") },
                            trailingIcon = { IconButton(onClick = { expandedContainer = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedContainer,
                            onDismissRequest = { expandedContainer = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(SlateCard)
                        ) {
                            containers.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = LightGrayText) },
                                    onClick = {
                                        onContainerTypeChange(type)
                                        expandedContainer = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = onRemarksChange,
                        label = { Text("Specific commodity packing requirements") },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )

                    Button(
                        onClick = {
                            if (origin.isNotEmpty() && dest.isNotEmpty()) {
                                onSubmit()
                                estimateCalculated = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Calculate Live Freight Quote", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (estimateCalculated) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Logistics Route Calculated!", fontWeight = FontWeight.Bold, color = LightGrayText)
                        Text("Estimated Shipping Cost: $4,320 USD", color = ActiveGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            text = "Ocean Freight Forwarder Oceanic Swift has logged this routing. A freight invoice has been sent to your Chat tab to finalize booking.",
                            fontSize = 11.sp,
                            color = LightGrayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContainerGuideLayout() {
    var selectedGuideIndex by remember { mutableStateOf(0) }
    val specs = listOf(
        Triple("20ft Standard", "Length: 5.9m | Width: 2.35m | Height: 2.39m", "Max Volume: 33 CBM | Max Payload Weight: 25,000 kg"),
        Triple("40ft Standard", "Length: 12.0m | Width: 2.35m | Height: 2.39m", "Max Volume: 67 CBM | Max Payload Weight: 27,600 kg"),
        Triple("40ft High Cube", "Length: 12.0m | Width: 2.35m | Height: 2.69m", "Max Volume: 76 CBM | Max Payload Weight: 28,200 kg")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Ocean Container Size Index", fontWeight = FontWeight.Bold, color = LightGrayText)
        }

        items(specs.size) { idx ->
            val spec = specs[idx]
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedGuideIndex = idx }
                    .border(
                        width = if (selectedGuideIndex == idx) 2.dp else 0.dp,
                        color = if (selectedGuideIndex == idx) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spec.first, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
                        Icon(Icons.Default.DirectionsBoat, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(spec.second, color = LightGrayText, fontSize = 12.sp)
                    Text(spec.third, color = PendingAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun IncotermsHandbookLayout() {
    var searchQuery by remember { mutableStateOf("") }
    val terms = listOf(
        Pair("FOB (Free on Board)", "Seller clears the goods for export and delivers them on board the vessel at the port of shipment. Buyer assumes risk of loss once loaded."),
        Pair("CIF (Cost, Insurance & Freight)", "Seller pays freight and insurance to bring the goods to the destination port. Risk transfers to the buyer once loaded."),
        Pair("EXW (Ex Works)", "The seller makes goods available at their factory. Buyer assumes all shipping, export clearance, and ocean transport logistics costs."),
        Pair("DDP (Delivered Duty Paid)", "Seller handles transport, custom duties, taxes, and import clearances, delivering goods right to the buyer's warehouse.")
    )

    val filtered = terms.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search FOB, EXW, CIF...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
        }

        items(filtered) { term ->
            Card(colors = CardDefaults.cardColors(containerColor = SlateContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(term.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(term.second, color = LightGrayText, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

// --- LEARNING CENTER ---
@Composable
fun LearningCenterScreen() {
    var quizStep by remember { mutableStateOf(0) } // 0: Start, 1: Q1, 2: Q2, 3: Completed
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Export-Import Academy", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 20.sp)
                    Text("Acquire official trade compliance knowledge, customs protocols, and earn certificates.", fontSize = 12.sp, color = LightGrayText.copy(alpha = 0.8f))
                }
            }
        }

        // Quiz Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interactive Trade Compliance Quiz", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    when (quizStep) {
                        0 -> {
                            Text("Test your international trade rules and compliance licensing knowledge in this quick test to unlock your specialist profile badge.", fontSize = 13.sp, color = LightGrayText)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { quizStep = 1; score = 0; selectedAnswer = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start 3-Step Specialist Exam")
                            }
                        }
                        1 -> {
                            Text("Question 1: Which Incoterm requires the seller to assume maximum delivery, custom clearance, and import tax duties?", fontWeight = FontWeight.SemiBold, color = LightGrayText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            QuizOption(text = "A. EXW (Ex Works)", isSelected = selectedAnswer == 0, onClick = { selectedAnswer = 0 })
                            QuizOption(text = "B. FOB (Free on Board)", isSelected = selectedAnswer == 1, onClick = { selectedAnswer = 1 })
                            QuizOption(text = "C. DDP (Delivered Duty Paid)", isSelected = selectedAnswer == 2, onClick = { selectedAnswer = 2 })

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (selectedAnswer == 2) score++
                                    selectedAnswer = null
                                    quizStep = 2
                                },
                                enabled = selectedAnswer != null,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next Question")
                            }
                        }
                        2 -> {
                            Text("Question 2: What standard digit length does a Harmonized Tariff Code (HS Code) have globally?", fontWeight = FontWeight.SemiBold, color = LightGrayText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            QuizOption(text = "A. 4 Digits", isSelected = selectedAnswer == 0, onClick = { selectedAnswer = 0 })
                            QuizOption(text = "B. 6 Digits", isSelected = selectedAnswer == 1, onClick = { selectedAnswer = 1 })
                            QuizOption(text = "C. 10 Digits", isSelected = selectedAnswer == 2, onClick = { selectedAnswer = 2 })

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (selectedAnswer == 1) score++
                                    quizStep = 3
                                },
                                enabled = selectedAnswer != null,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Finish Specialist Exam")
                            }
                        }
                        3 -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.School, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Compliance Exam Finished!", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
                                Text("Your Score: $score / 2 Correct", color = ActiveGreen, fontWeight = FontWeight.Bold)

                                if (score >= 2) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF0CA)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("INTERNATIONAL TRADE SPECIALIST", fontWeight = FontWeight.Bold, color = Color(0xFF0D3B66), fontSize = 12.sp)
                                            Text("This certifies your compliance competence in Incoterms 2020 & global tariff codes.", fontSize = 10.sp, color = Color.Black, textAlign = TextAlign.Center)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { quizStep = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Retake Test")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standard guides list
        item {
            Text("Featured Trade Guides", fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 16.sp)
        }

        val guides = listOf(
            Triple("Customs Documentation Checklist", "Learn how to prepare standard commercial invoices, bills of lading, and phytosanitary certificates securely.", Icons.Default.MenuBook),
            Triple("Incoterms 2020 Handbook", "Deep dive into the 11 standard Incoterms defining financial obligations and risks between seller and buyer.", Icons.Default.MenuBook),
            Triple("SGS Cargo Inspection Guide", "Step by step procedures to check load density, moisture limits and bulk grain packaging certifications.", Icons.Default.MenuBook)
        )

        items(guides) { guide ->
            Card(colors = CardDefaults.cardColors(containerColor = SlateContainer)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(guide.third, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(guide.first, fontWeight = FontWeight.Bold, color = LightGrayText, fontSize = 14.sp)
                        Text(guide.second, color = LightGrayText, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else SlateContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 2.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else LightGrayText, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
    }
}
