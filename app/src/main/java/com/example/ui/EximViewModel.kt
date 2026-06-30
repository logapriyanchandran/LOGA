package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiHelper
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.EximRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Onboarding : Screen()
    object Dashboard : Screen()
    object Marketplace : Screen()
    data class ProductDetail(val product: Product) : Screen()
    object AddProduct : Screen()
    object TradeBoard : Screen()
    data class TradeRequirementDetail(val requirement: TradeRequirement) : Screen()
    object ChatList : Screen()
    data class ActiveChat(val partnerName: String) : Screen()
    object RfqHub : Screen()
    object CreateRfq : Screen()
    object LogisticsHub : Screen()
    object LearningCenter : Screen()
}

class EximViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EximRepository(db)

    // Room Flows
    val userSession: StateFlow<UserSession?> = repository.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val requirements: StateFlow<List<TradeRequirement>> = repository.allRequirements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val rfqs: StateFlow<List<Rfq>> = repository.allRfqs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val inquiries: StateFlow<List<LogisticsInquiry>> = repository.allLogisticsInquiries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation State
    var currentScreen by mutableStateOf<Screen>(Screen.Dashboard)
        private set

    // Temporary active detail selections
    var selectedProductForDetail by mutableStateOf<Product?>(null)
    var selectedRequirementForDetail by mutableStateOf<TradeRequirement?>(null)
    var selectedPartnerForChat by mutableStateOf<String?>(null)

    // UI state states
    var isGeneratingDescription by mutableStateOf(false)
        private set
    var isSendingAiMessage by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
            // If userSession exists and isLoggedIn, start at Dashboard, else Onboarding
            val session = repository.getSession()
            currentScreen = if (session != null && session.isLoggedIn) {
                Screen.Dashboard
            } else {
                Screen.Onboarding
            }
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        when (screen) {
            is Screen.ProductDetail -> selectedProductForDetail = screen.product
            is Screen.TradeRequirementDetail -> selectedRequirementForDetail = screen.requirement
            is Screen.ActiveChat -> {
                selectedPartnerForChat = screen.partnerName
                markMessagesAsRead(screen.partnerName)
            }
            else -> {}
        }
    }

    // Onboarding Actions
    fun handleLogin(username: String, companyName: String, email: String, role: String, country: String) {
        viewModelScope.launch {
            val session = UserSession(
                username = username,
                companyName = companyName,
                email = email,
                role = role,
                country = country,
                isVerified = true,
                isPremium = false,
                isLoggedIn = true
            )
            repository.saveSession(session)
            currentScreen = Screen.Dashboard
        }
    }

    fun togglePremium() {
        viewModelScope.launch {
            val session = repository.getSession()
            if (session != null) {
                val updated = session.copy(isPremium = !session.isPremium)
                repository.saveSession(updated)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            currentScreen = Screen.Onboarding
        }
    }

    // Marketplace Actions
    fun addProduct(name: String, category: String, price: Double, moq: Int, hsCode: String, country: String, qty: Int, certs: String, desc: String) {
        viewModelScope.launch {
            val session = repository.getSession()
            val creatorCompany = session?.companyName ?: "Independent Exporter"
            val creatorRole = session?.role ?: "Exporter"
            val isVerified = session?.isVerified ?: false

            val newProduct = Product(
                name = name,
                category = category,
                description = desc,
                price = price,
                moq = moq,
                hsCode = hsCode,
                countryOfOrigin = country,
                availableQuantity = qty,
                certificates = certs,
                creatorCompanyName = creatorCompany,
                creatorRole = creatorRole,
                isVerifiedSupplier = isVerified,
                imageUrl = getRandomProductImage(category)
            )
            repository.insertProduct(newProduct)
            navigateTo(Screen.Marketplace)
        }
    }

    // Ask Gemini to generate Product Description
    fun generateProductDescription(productName: String, category: String, specs: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            isGeneratingDescription = true
            val prompt = """
                Generate a professional and appealing global trade product description for:
                Product Name: $productName
                Category: $category
                Key Specifications: $specs
                
                Keep the tone professional, persuasive, business-to-business (B2B), and focus on exporter standards, certifications, and reliability. Length should be around 3-4 concise paragraphs.
            """.trimIndent()

            val systemInstruction = "You are a professional copywriter specialized in global B2B supply chain trade descriptions."
            val response = GeminiHelper.generateTradeResponse(prompt, systemInstruction)
            onCompleted(response)
            isGeneratingDescription = false
        }
    }

    // Trade Board Actions
    fun postRequirement(title: String, type: String, desc: String, budget: String, hsCode: String) {
        viewModelScope.launch {
            val session = repository.getSession()
            val newReq = TradeRequirement(
                title = title,
                type = type,
                description = desc,
                budget = budget,
                hsCode = hsCode,
                creatorCompanyName = session?.companyName ?: "Global Import-Export Co.",
                creatorRole = session?.role ?: "Importer",
                creatorCountry = session?.country ?: "United States"
            )
            repository.insertRequirement(newReq)
            navigateTo(Screen.TradeBoard)
        }
    }

    // Chat Actions
    fun sendMessage(partnerName: String, text: String, fileMimeType: String? = null, fileName: String? = null) {
        viewModelScope.launch {
            val session = repository.getSession()
            val userName = session?.username ?: "User"

            val message = ChatMessage(
                senderId = "user",
                senderName = userName,
                receiverId = partnerName,
                messageText = text,
                fileMimeType = fileMimeType,
                fileName = fileName,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(message)

            // Simulate automatic partner responses or trigger Gemini AI Chat
            if (partnerName == "AI Trade Assistant") {
                triggerAiResponse(text)
            } else {
                simulatePartnerReply(partnerName, text)
            }
        }
    }

    private fun markMessagesAsRead(partnerName: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead("user", partnerName)
        }
    }

    // Gemini AI Chat Assistant
    private fun triggerAiResponse(userText: String) {
        viewModelScope.launch {
            isSendingAiMessage = true
            val prompt = """
                The user has sent the following query related to international trade:
                "$userText"
                
                Provide a helpful, precise, professional, and knowledgeable response. If they are asking for HS Codes, suggest the standard 6-digit Harmonized System codes. If they are asking for business emails or proposals, draft a polished template. If they ask about trade regulations or logistics, give an accurate breakdown.
            """.trimIndent()

            val systemInstruction = """
                You are EximConnect AI Trade Assistant. You are an expert in global commerce, Harmonized System (HS) Tariff Codes, custom duties, Incoterms 2020, marine logistics, freight calculation, export documentation (such as Bill of Lading, Certificate of Origin, Proforma Invoice), and international business communication. Always reply in a structured, professional, and polite manner.
            """.trimIndent()

            val response = GeminiHelper.generateTradeResponse(prompt, systemInstruction)
            val aiMessage = ChatMessage(
                senderId = "AI",
                senderName = "AI Trade Assistant",
                receiverId = "user",
                messageText = response,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(aiMessage)
            isSendingAiMessage = false
        }
    }

    // Human partner simulator to make the chat feel alive and realistic
    private fun simulatePartnerReply(partnerName: String, userText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                kotlinx.coroutines.delay(1500) // typing delay
            }
            val replyText = when {
                userText.contains("price", ignoreCase = true) || userText.contains("cost", ignoreCase = true) -> {
                    "Our base FOB price is listed, but we can offer a 5% discount for orders exceeding 2 containers. Let's discuss payment terms."
                }
                userText.contains("ship", ignoreCase = true) || userText.contains("delivery", ignoreCase = true) || userText.contains("freight", ignoreCase = true) -> {
                    "We typically ship within 14-21 days after receipt of LC (Letter of Credit) or 30% advance payment. We work with major ocean liners."
                }
                userText.contains("certificate", ignoreCase = true) || userText.contains("cert", ignoreCase = true) -> {
                    "All our products are certified by SGS and come with standard phytosanitary/CE certificates. I can share the PDF documentation here."
                }
                else -> {
                    "Thank you for the message. Let me review this with our sales manager and get back to you with an official quotation shortly."
                }
            }

            val reply = ChatMessage(
                senderId = partnerName,
                senderName = partnerName,
                receiverId = "user",
                messageText = replyText,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(reply)
        }
    }

    // RFQ Actions
    fun createRfq(title: String, targetProduct: String, qty: Int, targetPrice: Double, incoterms: String, remarks: String) {
        viewModelScope.launch {
            val session = repository.getSession()
            val company = session?.companyName ?: "Atlas Trading Co."

            val newRfq = Rfq(
                title = title,
                description = remarks,
                targetProduct = targetProduct,
                quantityRequired = qty,
                targetPrice = targetPrice,
                incoterms = incoterms,
                creatorCompany = company,
                status = "Pending"
            )
            repository.insertRfq(newRfq)
            navigateTo(Screen.RfqHub)

            // Simulate automatic supplier quotations arriving after short delay!
            simulateSupplierQuotations(newRfq)
        }
    }

    private fun simulateSupplierQuotations(rfq: Rfq) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                kotlinx.coroutines.delay(3000)
            }
            // Update RFQ status to Quotes Received
            val allCurrent = repository.allRfqs.first()
            val created = allCurrent.find { it.targetProduct == rfq.targetProduct && it.creatorCompany == rfq.creatorCompany }
            if (created != null) {
                repository.updateRfqStatus(created.id, "Quotes Received")
                
                // Also send a chat message notifying
                val systemNotify = ChatMessage(
                    senderId = "System",
                    senderName = "System Trade Alerts",
                    receiverId = "user",
                    messageText = "🔔 Quote Received! A supplier has submitted a competitive quotation for your RFQ '${rfq.title}'. Head over to the RFQ Hub to review and compare details.",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(systemNotify)
            }
        }
    }

    fun acceptRfqOffer(rfqId: Long) {
        viewModelScope.launch {
            repository.updateRfqStatus(rfqId, "Accepted")
        }
    }

    fun rejectRfqOffer(rfqId: Long) {
        viewModelScope.launch {
            repository.updateRfqStatus(rfqId, "Rejected")
        }
    }

    // Logistics Hub Actions
    fun submitLogisticsInquiry(origin: String, destination: String, weight: Double, volume: Double, containerType: String, incoterms: String, remarks: String) {
        viewModelScope.launch {
            val cost = calculateSimulatedLogisticsCost(weight, volume, containerType)
            val inquiry = LogisticsInquiry(
                origin = origin,
                destination = destination,
                cargoWeight = weight,
                cargoVolume = volume,
                containerType = containerType,
                incoterms = incoterms,
                remarks = remarks,
                status = "Quoted",
                estimatedCost = cost
            )
            repository.insertLogisticsInquiry(inquiry)

            // Insert system chat notification
            val alert = ChatMessage(
                senderId = "Oceanic Swift Logistics",
                senderName = "Oceanic Swift Logistics",
                receiverId = "user",
                messageText = "Hello! We have automatically generated a freight quotation for your route $origin to $destination. Estimated ocean freight rate is $$cost. Let's schedule a call to finalize booking.",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(alert)
        }
    }

    private fun calculateSimulatedLogisticsCost(weight: Double, volume: Double, containerType: String): Double {
        val baseRate = when {
            containerType.contains("20ft") -> 2800.0
            containerType.contains("40ft Standard") -> 3900.0
            containerType.contains("40ft High Cube") -> 4300.0
            else -> 150.0 * volume // LCL based on CBM
        }
        val weightSurcharge = if (weight > 15000) 450.0 else 0.0
        return baseRate + weightSurcharge
    }

    // Helper Unsplash Photos for marketplace cards
    private fun getRandomProductImage(category: String): String {
        return when (category) {
            "Agricultural Products" -> "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?q=80&w=600&auto=format&fit=crop"
            "Electrical Equipment" -> "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?q=80&w=600&auto=format&fit=crop"
            "Sustainable Materials" -> "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?q=80&w=600&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?q=80&w=600&auto=format&fit=crop"
        }
    }
}
