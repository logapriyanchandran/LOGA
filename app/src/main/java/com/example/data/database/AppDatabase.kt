package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserSession::class,
        Product::class,
        TradeRequirement::class,
        ChatMessage::class,
        Rfq::class,
        LogisticsInquiry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun productDao(): ProductDao
    abstract fun tradeRequirementDao(): TradeRequirementDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun rfqDao(): RfqDao
    abstract fun logisticsInquiryDao(): LogisticsInquiryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exim_connect_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
