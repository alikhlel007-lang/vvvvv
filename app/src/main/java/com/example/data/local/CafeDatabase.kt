package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        MenuItemEntity::class,
        TableEntity::class,
        OrderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CafeDatabase : RoomDatabase() {
    abstract fun cafeDao(): CafeDao

    companion object {
        @Volatile
        private var INSTANCE: CafeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CafeDatabase::class.java,
                    "cafe_menu_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(CafeDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class CafeDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.cafeDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: CafeDao) {
            // Default user
            dao.insertUser(
                UserEntity(
                    id = "u_default",
                    fullName = "مدير الكافيه",
                    email = "admin@cafe.com",
                    passwordHash = "12345678",
                    cafeName = "CAFE LUXURY",
                    role = "owner"
                )
            )

            // Categories matching Screenshot 2
            val categories = listOf(
                CategoryEntity("cat_burgers", "البرجر", "Burgers", "burger", 1),
                CategoryEntity("cat_appetizers", "المقبلات", "Appetizers", "fries", 2),
                CategoryEntity("cat_drinks", "المشروبات", "Drinks", "drink", 3),
                CategoryEntity("cat_meals", "الوجبات", "Meals", "meals", 4),
                CategoryEntity("cat_sweets", "الحلويات", "Desserts", "sweets", 5)
            )
            dao.insertCategories(categories)

            // Menu Items exactly matching Screenshot 2
            // Menu Items - Exactly 1 item per category as requested by user (ضع في كل قسم صنف واحد)
            val items = listOf(
                // 1. Burgers
                MenuItemEntity(
                    id = "b1",
                    categoryId = "cat_burgers",
                    nameAr = "برجر كلاسيك",
                    nameEn = "Classic Burger",
                    descriptionAr = "لحم بقري مشوي مع الجبن الطازج والخس والطماطم مع البطاطس المقلية",
                    descriptionEn = "Grilled beef patty with cheese, fresh lettuce and tomatoes",
                    price = 7000.0,
                    currency = "د.ع",
                    imageType = "burger",
                    isAvailable = true,
                    isOffer = true,
                    offerDiscountPercent = 15,
                    offerTag = "عرض اليوم"
                ),

                // 2. Appetizers
                MenuItemEntity(
                    id = "a1",
                    categoryId = "cat_appetizers",
                    nameAr = "بطاطس مقلية مقرمشة",
                    nameEn = "French Fries",
                    descriptionAr = "بطاطس ذهبية مقلية ببهارات الكافيه الخاصة وصوص الكاتشب",
                    descriptionEn = "Golden crispy fries with special seasoning",
                    price = 3500.0,
                    currency = "د.ع",
                    imageType = "fries",
                    isAvailable = true
                ),

                // 3. Drinks
                MenuItemEntity(
                    id = "d1",
                    categoryId = "cat_drinks",
                    nameAr = "سبانش لاتيه بارد",
                    nameEn = "Iced Spanish Latte",
                    descriptionAr = "قهوة إسبريسو غنية مع الحليب المكثف المحلى والثلج المنعش",
                    descriptionEn = "Rich espresso with condensed milk and ice",
                    price = 4500.0,
                    currency = "د.ع",
                    imageType = "drink",
                    isAvailable = true
                ),

                // 4. Meals
                MenuItemEntity(
                    id = "m1",
                    categoryId = "cat_meals",
                    nameAr = "وجبة ستيك دجاج مشوي",
                    nameEn = "Grilled Chicken Steak",
                    descriptionAr = "صدر دجاج متبل مشوي مع الخضار المشوية وصوص الفطر اللذيذ",
                    descriptionEn = "Seasoned grilled chicken breast with vegetables and sauce",
                    price = 9000.0,
                    currency = "د.ع",
                    imageType = "meals",
                    isAvailable = true
                ),

                // 5. Desserts
                MenuItemEntity(
                    id = "s1",
                    categoryId = "cat_sweets",
                    nameAr = "تشيز كيك فراولة",
                    nameEn = "Strawberry Cheesecake",
                    descriptionAr = "تشيز كيك نيويورك الكلاسيكي الفاخر مع صوص الفراولة الطازجة",
                    descriptionEn = "Classic baked cheesecake topped with fresh strawberry coulis",
                    price = 5000.0,
                    currency = "د.ع",
                    imageType = "sweets",
                    isAvailable = true,
                    isOffer = true,
                    offerDiscountPercent = 20,
                    offerTag = "حلى مميز"
                )
            )
            dao.insertMenuItems(items)

            // الحساب الجديد وقاعدة البيانات تبدأ خالية من الباركودات حتى يقوم المستخدم بتوليدها من قسم الباركودات
            // (Tables start empty for new accounts until generated)

            // Initial demo orders for the Cashier screen in Iraqi Dinars
            val now = System.currentTimeMillis()
            val demoOrders = listOf(
                OrderEntity(
                    id = "ord_101",
                    tableNumber = 3,
                    customerName = "أحمد الربيعي",
                    itemsSummary = "2x برجر كلاسيك، 1x سبانش لاتيه بارد",
                    itemsCount = 3,
                    totalPrice = 12250.0,
                    subtotal = 12000.0,
                    serviceFee = 250.0,
                    currency = "د.ع",
                    status = "NEW",
                    timestamp = now - 180000,
                    notes = "بدون بصل"
                ),
                OrderEntity(
                    id = "ord_102",
                    tableNumber = 3,
                    customerName = "حيدر الكرخي",
                    itemsSummary = "1x برجر باللحم المقدد، 1x موهيتو ليمون",
                    itemsCount = 2,
                    totalPrice = 9250.0,
                    subtotal = 9000.0,
                    serviceFee = 250.0,
                    currency = "د.ع",
                    status = "NEW",
                    timestamp = now - 120000,
                    notes = "سكر خفيف"
                ),
                OrderEntity(
                    id = "ord_103",
                    tableNumber = 5,
                    customerName = "علي السعدي",
                    itemsSummary = "1x برجر حار، 1x بطاطس مقلية",
                    itemsCount = 2,
                    totalPrice = 8250.0,
                    subtotal = 8000.0,
                    serviceFee = 250.0,
                    currency = "د.ع",
                    status = "PREPARING",
                    timestamp = now - 650000,
                    notes = "الصلصة جانباً"
                ),
                OrderEntity(
                    id = "ord_104",
                    tableNumber = 2,
                    customerName = "سارة حسن",
                    itemsSummary = "1x وافل شوكولاتة بلجيكية، 1x كابتشينو",
                    itemsCount = 2,
                    totalPrice = 10250.0,
                    subtotal = 10000.0,
                    serviceFee = 250.0,
                    currency = "د.ع",
                    status = "READY",
                    timestamp = now - 1200000,
                    notes = ""
                )
            )
            demoOrders.forEach { dao.insertOrder(it) }
        }
    }
}
