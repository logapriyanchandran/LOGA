package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class EximRepository(private val db: AppDatabase) {

    val userSession: Flow<UserSession?> = db.userSessionDao().getSessionFlow()
    val allProducts: Flow<List<Product>> = db.productDao().getAllProductsFlow()
    val allRequirements: Flow<List<TradeRequirement>> = db.tradeRequirementDao().getAllRequirementsFlow()
    val allMessages: Flow<List<ChatMessage>> = db.chatMessageDao().getAllMessagesFlow()
    val allRfqs: Flow<List<Rfq>> = db.rfqDao().getAllRfqsFlow()
    val allLogisticsInquiries: Flow<List<LogisticsInquiry>> = db.logisticsInquiryDao().getAllInquiriesFlow()

    fun getChatHistory(user: String, partner: String): Flow<List<ChatMessage>> {
        return db.chatMessageDao().getChatHistoryFlow(user, partner)
    }

    suspend fun getSession(): UserSession? = db.userSessionDao().getSession()

    suspend fun saveSession(session: UserSession) {
        db.userSessionDao().insertSession(session)
    }

    suspend fun updateSession(session: UserSession) {
        db.userSessionDao().updateSession(session)
    }

    suspend fun logout() {
        db.userSessionDao().clearSession()
    }

    suspend fun insertProduct(product: Product) {
        db.productDao().insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        db.productDao().deleteProduct(product)
    }

    suspend fun insertRequirement(req: TradeRequirement) {
        db.tradeRequirementDao().insertRequirement(req)
    }

    suspend fun deleteRequirement(req: TradeRequirement) {
        db.tradeRequirementDao().deleteRequirement(req)
    }

    suspend fun insertMessage(message: ChatMessage) {
        db.chatMessageDao().insertMessage(message)
    }

    suspend fun markMessagesAsRead(user: String, partner: String) {
        db.chatMessageDao().markAsRead(user, partner)
    }

    suspend fun insertRfq(rfq: Rfq) {
        db.rfqDao().insertRfq(rfq)
    }

    suspend fun updateRfqStatus(id: Long, status: String) {
        db.rfqDao().updateRfqStatus(id, status)
    }

    suspend fun deleteRfq(rfq: Rfq) {
        db.rfqDao().deleteRfq(rfq)
    }

    suspend fun insertLogisticsInquiry(inquiry: LogisticsInquiry) {
        db.logisticsInquiryDao().insertInquiry(inquiry)
    }

    suspend fun updateLogisticsInquiryStatus(id: Long, status: String) {
        db.logisticsInquiryDao().updateInquiryStatus(id, status)
    }

    suspend fun prePopulateIfEmpty() {
        val currentProducts = allProducts.first()
        if (currentProducts.isEmpty()) {
            // Populate standard products
            val starterProducts = listOf(
                Product(
                    name = "Premium Jasmine Rice AAA",
                    category = "Agricultural Products",
                    description = "High-grade fragrant Jasmine Rice from the Mekong Delta. Extensively polished, moisture content max 14%, broken grain max 5%. Phytosanitary certificate and certificate of origin provided.",
                    price = 580.0,
                    currency = "USD",
                    moq = 12,
                    hsCode = "1006.30",
                    countryOfOrigin = "Vietnam",
                    availableQuantity = 25000,
                    certificates = "SGS, ISO 22000, HACCP, Halal",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=600&auto=format&fit=crop",
                    creatorCompanyName = "Mekong Delta Agricultural Export Co.",
                    creatorRole = "Exporter",
                    isVerifiedSupplier = true,
                    rating = 4.8f
                ),
                Product(
                    name = "Heavy Duty Industrial Armored Copper Cable",
                    category = "Electrical Equipment",
                    description = "XLPE insulated steel wire armored (SWA) multi-core copper power cables for medium to heavy industrial operations. Approved to BS5467 standard. Rated voltage 0.6/1kV.",
                    price = 14.50,
                    currency = "USD",
                    moq = 500,
                    hsCode = "8544.49",
                    countryOfOrigin = "China",
                    availableQuantity = 100000,
                    certificates = "CE, RoHS, VDE, UL Listed",
                    imageUrl = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?q=80&w=600&auto=format&fit=crop",
                    creatorCompanyName = "Apex Industrial Wire & Cable Ltd",
                    creatorRole = "Manufacturer",
                    isVerifiedSupplier = true,
                    rating = 4.7f
                ),
                Product(
                    name = "Organic Fairtrade Arabica Coffee Beans (Green)",
                    category = "Agricultural Products",
                    description = "Single-origin unroasted specialty Arabica coffee beans harvested from high altitudes of Sidama. Grade 1 specialty. Handpicked, wet processed, washed.",
                    price = 4.25,
                    currency = "USD",
                    moq = 2000,
                    hsCode = "0901.11",
                    countryOfOrigin = "Ethiopia",
                    availableQuantity = 15000,
                    certificates = "Fairtrade, USDA Organic, UTZ",
                    imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?q=80&w=600&auto=format&fit=crop",
                    creatorCompanyName = "Sidama Highlands Coffee Union",
                    creatorRole = "Exporter",
                    isVerifiedSupplier = true,
                    rating = 4.9f
                ),
                Product(
                    name = "Biodegradable Bamboo Fibre Drinking Straws",
                    category = "Sustainable Materials",
                    description = "Eco-friendly, reusable, and 100% compostable bamboo straws. Made from premium untreated wild bamboo stalks. Length 20cm, diverse diameters. Perfect for hot and cold beverages.",
                    price = 0.08,
                    currency = "USD",
                    moq = 10000,
                    hsCode = "1401.10",
                    countryOfOrigin = "Indonesia",
                    availableQuantity = 500000,
                    certificates = "FDA Approved, LFGB, SGS Biodegradable",
                    imageUrl = "https://images.unsplash.com/photo-1592861438114-7c0b89566f16?q=80&w=600&auto=format&fit=crop",
                    creatorCompanyName = "Nusantara Bamboo Goods",
                    creatorRole = "Manufacturer",
                    isVerifiedSupplier = false,
                    rating = 4.4f
                )
            )
            for (product in starterProducts) {
                db.productDao().insertProduct(product)
            }
        }

        val currentReqs = allRequirements.first()
        if (currentReqs.isEmpty()) {
            val starterReqs = listOf(
                TradeRequirement(
                    title = "Urgent Supply of Organic Green Coffee Beans",
                    type = "Looking for Suppliers",
                    description = "We are seeking organic-certified green Arabica coffee beans for long-term supply contract in Hamburg. Looking for 10-15 tons per month. Must have EU Organic certificate.",
                    budget = "$4.00 - $4.50 / kg",
                    hsCode = "0901.11",
                    creatorCompanyName = "EuroBrew Imports GmbH",
                    creatorRole = "Importer",
                    creatorCountry = "Germany"
                ),
                TradeRequirement(
                    title = "Seeking Freight Forwarder for Shanghai to New York",
                    type = "Partnership Opportunities",
                    description = "Need regular logistics quote for 5x 40ft High Cube containers monthly. Electronics cargo. Direct shipment preferred. Seeking competitive rates for high-season contracts.",
                    budget = "Target: $4,500 / container",
                    creatorCompanyName = "Apex Industrial Wire & Cable Ltd",
                    creatorRole = "Manufacturer",
                    creatorCountry = "China"
                ),
                TradeRequirement(
                    title = "Logistics customs broker support in Port of Los Angeles",
                    type = "Tender Notices",
                    description = "Looking for an experienced US Customs Broker to handle clearing of machinery parts incoming from Kobe, Japan. Immediate assistance needed.",
                    budget = "Standard Broker Fees Apply",
                    creatorCompanyName = "Californian Assembly Corp",
                    creatorRole = "Importer",
                    creatorCountry = "United States"
                )
            )
            for (req in starterReqs) {
                db.tradeRequirementDao().insertRequirement(req)
            }
        }

        val currentMessages = allMessages.first()
        if (currentMessages.isEmpty()) {
            val starterMessages = listOf(
                ChatMessage(
                    senderId = "Oceanic Swift Logistics",
                    senderName = "Oceanic Swift Logistics",
                    receiverId = "user",
                    messageText = "Hello! We saw your freight inquiry for Shanghai to New York. We can offer you a rate of $4,300 per 40ft HC with direct shipping. Would you like us to prepare an official RFQ?",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                ChatMessage(
                    senderId = "user",
                    senderName = "My Company",
                    receiverId = "Oceanic Swift Logistics",
                    messageText = "Hi Oceanic Swift! That is a competitive rate. Does it include local port terminal charges in NY?",
                    timestamp = System.currentTimeMillis() - 1800000
                ),
                ChatMessage(
                    senderId = "Oceanic Swift Logistics",
                    senderName = "Oceanic Swift Logistics",
                    receiverId = "user",
                    messageText = "Yes, it includes Destination Terminal Handling Charges (DTHC). We can send you a detailed breakdown of Incoterms if needed.",
                    timestamp = System.currentTimeMillis() - 900000,
                    isRead = false
                ),
                // AI Help Chat
                ChatMessage(
                    senderId = "AI",
                    senderName = "AI Trade Assistant",
                    receiverId = "user",
                    messageText = "Welcome to EximConnect AI Assistant! 🤝\n\nI can help you with:\n1. Generating professional email proposals\n2. Recommending HS Harmonized Tariff Codes\n3. Answering complex international trade regulation Q&As\n4. Writing compelling, search-optimized product descriptions\n\nWhat are you exporting or importing today?",
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )
            for (msg in starterMessages) {
                db.chatMessageDao().insertMessage(msg)
            }
        }

        // Add a default user session so that they don't have to register from scratch on startup (though they can)
        val currentSession = db.userSessionDao().getSession()
        if (currentSession == null) {
            db.userSessionDao().insertSession(
                UserSession(
                    username = "John Trade",
                    companyName = "Atlas Trading Co.",
                    role = "Exporter",
                    isVerified = true,
                    isPremium = false,
                    email = "john@atlastrading.com",
                    country = "United States",
                    website = "www.atlastrading.com",
                    phone = "+1 (555) 321-4567",
                    businessCertificates = "FDA, ISO 9001, US Custom Export Registration",
                    isLoggedIn = true
                )
            )
        }
    }
}
