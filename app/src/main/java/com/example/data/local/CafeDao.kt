package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CafeDao {
    // Categories
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // Menu Items
    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId")
    fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsSync(): List<MenuItemEntity>

    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items")
    suspend fun deleteAllMenuItems()

    // Tables
    @Query("SELECT * FROM cafe_tables ORDER BY tableNumber ASC")
    fun getAllTables(): Flow<List<TableEntity>>

    @Query("SELECT * FROM cafe_tables ORDER BY tableNumber ASC")
    suspend fun getAllTablesSync(): List<TableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Update
    suspend fun updateTable(table: TableEntity)

    @Delete
    suspend fun deleteTable(table: TableEntity)

    @Query("DELETE FROM cafe_tables")
    suspend fun deleteAllTables()

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE tableNumber = :tableNumber AND status NOT IN ('PAID', 'CANCELLED', 'REJECTED') ORDER BY timestamp DESC")
    suspend fun getActiveOrdersForTable(tableNumber: Int): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE status = 'PAID'")
    suspend fun getPaidOrdersSync(): List<OrderEntity>

    @Query("DELETE FROM orders WHERE status = 'PAID'")
    suspend fun deleteAllPaidOrders()

    // Users
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}
