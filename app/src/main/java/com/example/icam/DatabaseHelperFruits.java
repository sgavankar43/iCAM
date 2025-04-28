package com.example.icam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperFruits extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ProduceDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PRODUCE = "produce_info";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";

    public DatabaseHelperFruits(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PRODUCE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " TEXT," +
                KEY_DESCRIPTION + " TEXT)");  // Removed the image column

        insertSampleProduce(db);  // Insert sample produce into the database
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCE);
        onCreate(db);
    }

    private void insertSampleProduce(SQLiteDatabase db) {
        // Insert sample produce items with descriptions
        addProduce(db, "Apple Crimson Snow", "Sweet and crisp. Rich in dietary fiber and vitamin C. Contains trace amounts of potassium and antioxidants.");
        addProduce(db, "Apple Golden", "Soft and sweet apple. Good source of carbohydrates, vitamin C, and soluble fiber.");
        addProduce(db, "Apple Red", "Classic red apple. Provides vitamin C, potassium, and antioxidants such as quercetin.");
        addProduce(db, "Apple Red 1", "Similar to Red Delicious. Contains fiber, vitamin C, and polyphenols.");
        addProduce(db, "Banana", "High in potassium, vitamin B6, and vitamin C. Contains carbs (mainly sugars and starch).");
        addProduce(db, "Capsicum Red", "Most ripe and sweet. Highest in vitamin C, beta-carotene (vitamin A), and antioxidants.");
        addProduce(db, "Capsicum Green", "Least ripe. Slightly bitter. Good in fiber, vitamin C, and E.");
        addProduce(db, "Capsicum Yellow", "Sweet and juicy. Contains vitamin A, B6, and folate.");
        addProduce(db, "Onion Red", "Contains anthocyanins, vitamin C, B6, and fiber. Anti-inflammatory effects.");
        addProduce(db, "Onion White", "Less pungent, offers sulfur compounds, vitamin C, and manganese.");
        addProduce(db, "Potato White", "High in starch. Contains potassium, vitamin C, and B6.");
        addProduce(db, "Tomato", "Great source of vitamin C, potassium, folate, and lycopene.");
        addProduce(db, "Tomato Yellow", "Lower in acidity. Contains vitamin C, A, and lutein.");
        addProduce(db, "Tomato not Ripened", "Contains less lycopene. Still provides vitamin C and potassium.");
        // Add more sample products if needed
    }

    private void addProduce(SQLiteDatabase db, String name, String description) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_DESCRIPTION, description);
        db.insert(TABLE_PRODUCE, null, values);
    }

    // Retrieve produce by name
    public ProduceModel getProduceByName(String produceName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ProduceModel produce = null;

        Cursor cursor = db.query(TABLE_PRODUCE,
                new String[]{KEY_ID, KEY_NAME, KEY_DESCRIPTION},  // Excluding image column
                KEY_NAME + "=?",
                new String[]{produceName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            produce = new ProduceModel();
            produce.id = cursor.getInt(0);
            produce.name = cursor.getString(1);
            produce.description = cursor.getString(2);
            cursor.close();
        }

        db.close();
        return produce;
    }

    // Model class to represent produce item
    public static class ProduceModel {
        public int id;
        public String name;
        public String description;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
