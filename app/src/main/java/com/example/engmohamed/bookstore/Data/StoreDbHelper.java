package com.example.engmohamed.bookstore.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.engmohamed.bookstore.Data.StoreContract.BookEntry;

/**
 * Customized DataBase helper class
 */
public class StoreDbHelper extends SQLiteOpenHelper {

    // Fixed DataBase Name
    private static String DATABASE_NAME = "BookStore";

    // Fixed DataBase version (needed when upgrade)
    private static int DATABASE_VERSION = 1;

    /* SQL query order the create a specific table
      CREATE TABLE BooksInformation (_id INTEGER PRIMARY KEY AUTOINCREMENT,
      ProductName TEXT NOT NULL,
      ProductPrice INTEGER NOT NULL DEFAULT 0,
      Quantity INTEGER NOT NULL DEFAULT 0,
      SupplierName TEXT NOT NULL,
      SupplierPhoneNumber INTEGER,
      ProductImage TEXT); */
    private static String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + BookEntry.TABLE_NAME + "( "
            + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + BookEntry.PRODUCT_NAME_COLUMN + " TEXT NOT NULL,"
            + BookEntry.PRICE_COLUMN + " INTEGER NOT NULL DEFAULT 0,"
            + BookEntry.QUANTITY_COLUMN + " INTEGER NOT NULL DEFAULT 0,"
            + BookEntry.SUPPLIER_NAME_COLUMN + " TEXT NOT NULL,"
            + BookEntry.SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL,"
            + BookEntry.PRODUCT_IMAGE_COLUMN + " TEXT);";

    /**
     * Customized constructor for DataBase using the default cursor object
     *
     * @param context for information
     */
    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the DataBase for the first time
     *
     * @param db to operate with DB
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Used when the DataBase get an update
     *
     * @param db         to operate with DB
     * @param oldVersion of DB
     * @param newVersion of DB
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
