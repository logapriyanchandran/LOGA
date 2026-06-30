package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE id = 'current_session' LIMIT 1")
    fun getSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE id = 'current_session' LIMIT 1")
    suspend fun getSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Update
    suspend fun updateSession(session: UserSession)

    @Query("DELETE FROM user_sessions")
    suspend fun clearSession()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY timestamp DESC")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Dao
interface TradeRequirementDao {
    @Query("SELECT * FROM trade_requirements ORDER BY timestamp DESC")
    fun getAllRequirementsFlow(): Flow<List<TradeRequirement>>

    @Query("SELECT * FROM trade_requirements WHERE type = :type ORDER BY timestamp DESC")
    fun getRequirementsByTypeFlow(type: String): Flow<List<TradeRequirement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequirement(req: TradeRequirement)

    @Delete
    suspend fun deleteRequirement(req: TradeRequirement)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :user AND receiverId = :partner) OR (senderId = :partner AND receiverId = :user) ORDER BY timestamp ASC")
    fun getChatHistoryFlow(user: String, partner: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE senderId = :partner AND receiverId = :user")
    suspend fun markAsRead(user: String, partner: String)
}

@Dao
interface RfqDao {
    @Query("SELECT * FROM rfqs ORDER BY timestamp DESC")
    fun getAllRfqsFlow(): Flow<List<Rfq>>

    @Query("SELECT * FROM rfqs WHERE creatorCompany = :company ORDER BY timestamp DESC")
    fun getMyRfqsFlow(company: String): Flow<List<Rfq>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRfq(rfq: Rfq)

    @Query("UPDATE rfqs SET status = :status WHERE id = :id")
    suspend fun updateRfqStatus(id: Long, status: String)

    @Delete
    suspend fun deleteRfq(rfq: Rfq)
}

@Dao
interface LogisticsInquiryDao {
    @Query("SELECT * FROM logistics_inquiries ORDER BY timestamp DESC")
    fun getAllInquiriesFlow(): Flow<List<LogisticsInquiry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: LogisticsInquiry)

    @Query("UPDATE logistics_inquiries SET status = :status WHERE id = :id")
    suspend fun updateInquiryStatus(id: Long, status: String)
}
