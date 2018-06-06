package com.example.engmohamed.bookstore.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contain all final vars that is related to DB
 */

public final class StoreContract {

    // Application Authority Information used on UriMatcher
    static final String CONTENT_AUTHORITY = "com.example.engmohamed.bookstore";
    static final String PATH_BOOKS_INFORMATION = "BooksInformation";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Contract should never be callean throw constructor
     */
    private StoreContract() {
    }

    /**
     * Class that simulate BookInformation Table at the DB
     */
    public static class BookEntry implements BaseColumns {
        public static String TABLE_NAME = "BooksInformation";
        public static String _ID = BaseColumns._ID;
        public static String PRODUCT_NAME_COLUMN = "ProductName";
        public static String PRICE_COLUMN = "Price";
        public static String QUANTITY_COLUMN = "Quantity";
        public static String SUPPLIER_NAME_COLUMN = "SupplierName";
        public static String SUPPLIER_PHONE_NUMBER = "SupplierPhoneNumber";
        public static String PRODUCT_IMAGE_COLUMN = "ProductImage";

        // BookInformation Table URI (used to connect to the table)
        public static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS_INFORMATION);
    }

}
