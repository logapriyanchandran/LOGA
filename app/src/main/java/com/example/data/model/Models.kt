package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val id: String = "current_session",
    val username: String,
    val companyName: String,
    val role: String, // Exporter, Importer, Manufacturer, Logistics, Customs Broker, Admin
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val email: String,
    val country: String = "United States",
    val website: String = "",
    val phone: String = "",
    val businessCertificates: String = "", // Comma-separated or description
    val portfolioGallery: String = "", // Comma-separated paths/names
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val currency: String = "USD",
    val moq: Int, // Minimum Order Quantity
    val hsCode: String,
    val countryOfOrigin: String,
    val availableQuantity: Int,
    val certificates: String,
    val imageUrl: String = "",
    val creatorCompanyName: String = "",
    val creatorRole: String = "Exporter",
    val isVerifiedSupplier: Boolean = false,
    val rating: Float = 4.5f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trade_requirements")
data class TradeRequirement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // Looking for Buyer, Looking for Supplier, Import Request, Export Opportunity, Tender Notice, Partnership
    val description: String,
    val budget: String = "Negotiable",
    val hsCode: String = "",
    val creatorCompanyName: String,
    val creatorRole: String,
    val creatorCountry: String,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String, // "user" or partner company name or "AI"
    val senderName: String,
    val receiverId: String, // Partner name or "AI"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileMimeType: String? = null, // "image/*", "application/pdf", or null
    val fileName: String? = null,
    val fileUri: String? = null,
    val isRead: Boolean = false
)

@Entity(tableName = "rfqs")
data class Rfq(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetProduct: String,
    val quantityRequired: Int,
    val targetPrice: Double,
    val hsCode: String = "",
    val incoterms: String = "FOB", // FOB, CIF, EXW, etc.
    val status: String = "Pending", // Pending, Quotes Received, Accepted, Rejected, Completed
    val targetCompany: String = "All Suppliers",
    val creatorCompany: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "logistics_inquiries")
data class LogisticsInquiry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val origin: String,
    val destination: String,
    val cargoWeight: Double, // in kg
    val cargoVolume: Double, // in cbm
    val containerType: String, // 20ft Standard, 40ft Standard, 40ft High Cube, Less than Container (LCL)
    val incoterms: String = "FOB",
    val remarks: String = "",
    val status: String = "Submitted", // Submitted, Quoted, Booked
    val estimatedCost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
