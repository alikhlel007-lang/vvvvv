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
    version = 2,
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
            val items = listOf(
                // Burgers
                MenuItemEntity(
                    id = "b1",
                    categoryId = "cat_burgers",
                    nameAr = "برجر كلاسيك",
                    nameEn = "Classic Burger",
                    descriptionAr = "لحم بقري مشوي مع الجبن الطازج والخس والطماطم",
                    descriptionEn = "Grilled beef patty with cheese, fresh lettuce and tomatoes",
                    price = 4.50,
                    imageType = "burger",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "b2",
                    categoryId = "cat_burgers",
                    nameAr = "برجر باللحم المقدد",
                    nameEn = "Bacon Burger",
                    descriptionAr = "لحم بقري مع شرائح اللحم المقدد والجبن والخس",
                    descriptionEn = "Beef patty with bacon strips, melted cheese and lettuce",
                    price = 5.50,
                    imageType = "burger",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "b3",
                    categoryId = "cat_burgers",
                    nameAr = "برجر حار",
                    nameEn = "Spicy Jalapeno Burger",
                    descriptionAr = "لحم بقري مع صلصة حارة وفلفل هالبينو",
                    descriptionEn = "Spicy beef patty with jalapenos and hot sauce",
                    price = 5.00,
                    imageType = "burger",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "b4",
                    categoryId = "cat_burgers",
                    nameAr = "برجر دجاج",
                    nameEn = "Crispy Chicken Burger",
                    descriptionAr = "دجاج مقرمش مع الخس والصلصة الخاصة",
                    descriptionEn = "Crispy fried chicken fillet with special sauce and lettuce",
                    price = 4.00,
                    imageType = "burger",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "b5",
                    categoryId = "cat_burgers",
                    nameAr = "برجر بالمشروم",
                    nameEn = "Mushroom Swiss Burger",
                    descriptionAr = "لحم بقري مع المشروم والجبن",
                    descriptionEn = "Juicy beef patty topped with sautéed mushrooms and cheese",
                    price = 5.50,
                    imageType = "burger",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "b6",
                    categoryId = "cat_burgers",
                    nameAr = "دبل سماش برجر",
                    nameEn = "Double Smash Burger",
                    descriptionAr = "شريحتا لحم سماش مع صوص الشيدر السائل ومخلل",
                    descriptionEn = "Double smash beef with cheddar sauce and pickles",
                    price = 6.50,
                    imageType = "burger",
                    isAvailable = true
                ),

                // Appetizers
                MenuItemEntity(
                    id = "a1",
                    categoryId = "cat_appetizers",
                    nameAr = "بطاطس مقلية مقرمشة",
                    nameEn = "French Fries",
                    descriptionAr = "بطاطس ذهبية مقلية ببهارات الكافيه الخاصة",
                    descriptionEn = "Golden crispy fries with special seasoning",
                    price = 2.50,
                    imageType = "fries",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "a2",
                    categoryId = "cat_appetizers",
                    nameAr = "أصابع الموزاريلا",
                    nameEn = "Mozzarella Sticks",
                    descriptionAr = "أصابع جبنة موزاريلا ذهبية مع صوص المارينارا",
                    descriptionEn = "Golden fried mozzarella sticks with marinara dip",
                    price = 3.50,
                    imageType = "fries",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "a3",
                    categoryId = "cat_appetizers",
                    nameAr = "حلقات البصل المقرمشة",
                    nameEn = "Onion Rings",
                    descriptionAr = "حلقات بصل مقرمشة مع صوص الرانش اللذيذ",
                    descriptionEn = "Crispy battered onion rings served with ranch dip",
                    price = 3.00,
                    imageType = "fries",
                    isAvailable = true
                ),

                // Drinks
                MenuItemEntity(
                    id = "d1",
                    categoryId = "cat_drinks",
                    nameAr = "سبانش لاتيه بارد",
                    nameEn = "Iced Spanish Latte",
                    descriptionAr = "قهوة إسبريسو مع الحليب المكثف المحلى والثلج",
                    descriptionEn = "Rich espresso with condensed milk and ice",
                    price = 4.00,
                    imageType = "drink",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "d2",
                    categoryId = "cat_drinks",
                    nameAr = "آيس كراميل ماكياتو",
                    nameEn = "Iced Caramel Macchiato",
                    descriptionAr = "إسبريسو بارد مع الكراميل والحليب ورغوة ناعمة",
                    descriptionEn = "Chilled espresso with vanilla, milk, and caramel drizzle",
                    price = 4.50,
                    imageType = "drink",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "d3",
                    categoryId = "cat_drinks",
                    nameAr = "موهيتو ليمون ونعناع",
                    nameEn = "Lemon Mint Mojito",
                    descriptionAr = "مشروب صودا منعش بالليمون الأخضر وأوراق النعناع الطازجة",
                    descriptionEn = "Refreshing sparkling drink with lime and fresh mint",
                    price = 3.50,
                    imageType = "drink",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "d4",
                    categoryId = "cat_drinks",
                    nameAr = "عصير برتقال طبيعي",
                    nameEn = "Fresh Orange Juice",
                    descriptionAr = "عصير برتقال طازج معصور يومياً بدون سكر مضاف",
                    descriptionEn = "100% freshly squeezed pure orange juice",
                    price = 3.00,
                    imageType = "drink",
                    isAvailable = true
                ),

                // Meals
                MenuItemEntity(
                    id = "m1",
                    categoryId = "cat_meals",
                    nameAr = "وجبة ستيك دجاج مشوي",
                    nameEn = "Grilled Chicken Steak",
                    descriptionAr = "صدر دجاج متبل مشوي مع الخضار المشوية وصوص الفطر",
                    descriptionEn = "Seasoned grilled chicken breast with vegetables and sauce",
                    price = 7.50,
                    imageType = "meals",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "m2",
                    categoryId = "cat_meals",
                    nameAr = "باستا ألفريدو بالدجاج",
                    nameEn = "Chicken Alfredo Pasta",
                    descriptionAr = "باستا فيتوتشيني مع قطع الدجاج وصوص الكريمة الإيطالي",
                    descriptionEn = "Fettuccine pasta in rich creamy parmesan alfredo sauce",
                    price = 6.50,
                    imageType = "meals",
                    isAvailable = true
                ),

                // Desserts
                MenuItemEntity(
                    id = "s1",
                    categoryId = "cat_sweets",
                    nameAr = "تشيز كيك فراولة",
                    nameEn = "Strawberry Cheesecake",
                    descriptionAr = "تشيز كيك نيويورك الكلاسيكي مع صوص الفراولة الطازجة",
                    descriptionEn = "Classic baked cheesecake topped with fresh strawberry coulis",
                    price = 4.50,
                    imageType = "sweets",
                    isAvailable = true
                ),
                MenuItemEntity(
                    id = "s2",
                    categoryId = "cat_sweets",
                    nameAr = "وافل شوكولاتة بلجيكية",
                    nameEn = "Belgian Chocolate Waffle",
                    descriptionAr = "وافل ذهبي مقرمش مغطى بالشوكولاتة البلجيكية وآيسكريم الفانيليا",
                    descriptionEn = "Warm crispy waffle smothered in Belgian chocolate with vanilla ice cream",
                    price = 5.00,
                    imageType = "sweets",
                    isAvailable = true
                )
            )
            dao.insertMenuItems(items)

            // Initial Tables 1 to 8 with QR payloads
            val tables = (1..8).map { number ->
                TableEntity(
                    id = "table_$number",
                    tableNumber = number,
                    label = "طاولة $number",
                    capacity = if (number % 2 == 0) 4 else 2,
                    qrPayload = "https://alikhlel007-lang.github.io/vvvvv/?table=$number"
                )
            }
            dao.insertTables(tables)

            // Initial demo orders for the Cashier screen so the user immediately sees the cashier dashboard in action!
            val now = System.currentTimeMillis()
            val demoOrders = listOf(
                OrderEntity(
                    id = "ord_101",
                    tableNumber = 3,
                    customerName = "طاولة 3",
                    itemsSummary = "2x برجر كلاسيك، 1x سبانش لاتيه بارد",
                    itemsCount = 3,
                    totalPrice = 13.00,
                    status = "NEW",
                    timestamp = now - 180000,
                    notes = "بدون بصل في أحد البرجرات"
                ),
                OrderEntity(
                    id = "ord_102",
                    tableNumber = 5,
                    customerName = "طاولة 5",
                    itemsSummary = "1x برجر باللحم المقدد، 1x بطاطس مقلية مقرمشة، 1x موهيتو ليمون",
                    itemsCount = 3,
                    totalPrice = 11.50,
                    status = "PREPARING",
                    timestamp = now - 650000,
                    notes = "الصلصة جانباً"
                ),
                OrderEntity(
                    id = "ord_103",
                    tableNumber = 2,
                    customerName = "طاولة 2",
                    itemsSummary = "1x برجر حار، 1x وافل شوكولاتة بلجيكية",
                    itemsCount = 2,
                    totalPrice = 10.00,
                    status = "READY",
                    timestamp = now - 1200000,
                    notes = ""
                )
            )
            dao.insertOrder(demoOrders[0])
            dao.insertOrder(demoOrders[1])
            dao.insertOrder(demoOrders[2])
        }
    }
}
