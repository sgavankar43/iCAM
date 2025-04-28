package com.example.icam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FinalFmcg extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinalProductsDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PRODUCTS = "final_products";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PRICE = "price";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_DESCRIPTION = "description";

    public FinalFmcg(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " TEXT," +
                KEY_PRICE + " TEXT," +
                KEY_QUANTITY + " TEXT," +
                KEY_DESCRIPTION + " TEXT)");

        insertSampleProducts(db);
        updateCocaColaCanPrice(db);// ✅ Update Coca Cola Can price
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        addProduct(db, "Colgate Maximum Cavity Protection", "10", "16g", "Anticavity toothpaste with calcium boost. Strengthens teeth. Not edible.");
        addProduct(db, "Kurkure Chutney Chaska", "20", "68 g", "Rice meal, corn meal, edible oil, chilli chatka flavoring. Contains gluten.");
        addProduct(db, "Lays Masala", "10-78", "28-115g", "Potatoes, vegetable oil, sweet chilli seasoning. May contain traces of dairy.");
        addProduct(db, "Tapal Danedar", "120-150", "200-250g", "Premium black tea granules. No allergens.");
        addProduct(db, "Supreme Tea", "100-130", "200-250g", "Blended black tea. 100% natural. No allergens.");
        addProduct(db, "Coca Cola Can", "30", "300ml", "Carbonated water, sugar, caffeine, caramel color. Contains caffeine.");
        addProduct(db, "Lipton Yellow Label Tea", "150-180", "200-250g", "Black tea leaves. Natural source of antioxidants.");
        addProduct(db, "Fanta", "20-100", "250-750ml", "Carbonated water, sugar, orange flavoring. Contains added flavor (natural & nature identical).");
        addProduct(db, "Lifebuoy Total Protect Soap", "30-40", "120-130g", "Germ protection soap with active silver formula. For external use only.");
        addProduct(db, "LU Prince Biscuit", "8-12", "25-35g", "Wheat flour, sugar, cream filling. Contains gluten, milk, and soy.");
        addProduct(db, "Nestle Fruita Vitals Red Grapes", "90-110", "1L", "Red grape juice concentrate, water, vitamin C. No added preservatives.");
        addProduct(db, "Vaseline Healthy White Lotion", "250-280", "350-400ml", "With vitamin B3 and triple sunscreens. For external use only.");
        addProduct(db, "LU Oreo Biscuit", "10-145", "25-500g", "Cocoa biscuit with vanilla creme. Contains gluten, milk, and soy.");
        addProduct(db, "Sunsilk Shampoo Soft & Smooth", "200-230", "300-350ml", "With 5 natural oils. For dry hair. Avoid contact with eyes.");
    }

    private void addProduct(SQLiteDatabase db, String name, String price, String quantity, String description) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_PRICE, price);
        values.put(KEY_QUANTITY, quantity);
        values.put(KEY_DESCRIPTION, description);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // ✅ Update Coca Cola Can price and quantity
//    private void updateCocaColaPrice(SQLiteDatabase db) {
//        ContentValues values = new ContentValues();
//        values.put(KEY_PRICE, "40");
//        values.put(KEY_QUANTITY, "300ml");
//        String whereClause = KEY_NAME + " = ?";
//        String[] whereArgs = new String[]{"Coca Cola Can"};
//        db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);
//    }

    private void updateCocaColaCanPrice(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_PRICE, "40");
        values.put(KEY_QUANTITY, "300ml");
        String whereClause = KEY_NAME + " = ?";
        String[] whereArgs = new String[]{"Coca Cola Can"};
        db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    // ✅ Get product by name
    public ProductModel getProductByName(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ProductModel product = null;

        Cursor cursor = db.query(TABLE_PRODUCTS,
                new String[]{KEY_ID, KEY_NAME, KEY_PRICE, KEY_QUANTITY, KEY_DESCRIPTION},
                KEY_NAME + "=?",
                new String[]{productName},
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            product = new ProductModel();
            product.id = cursor.getInt(0);
            product.name = cursor.getString(1);
            product.price = cursor.getString(2);
            product.quantity = cursor.getString(3);
            product.description = cursor.getString(4);
            cursor.close();
        }

        return product;
    }

    // ✅ Model class
    public static class ProductModel {
        public int id;
        public String name;
        public String price;
        public String quantity;
        public String description;

        public ProductModel() {}

        public int getId() { return id; }
        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getQuantity() { return quantity; }
        public String getDescription() { return description; }

        public String getImageName() {
            return name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        }

        public String getImage() {
            return getImageName();
        }

        public String getPriceRange() {
            return price;
        }
    }
}
