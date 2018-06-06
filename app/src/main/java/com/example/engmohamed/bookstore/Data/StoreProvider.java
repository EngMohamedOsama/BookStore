package com.example.engmohamed.bookstore.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static com.example.engmohamed.bookstore.Data.StoreContract.BookEntry;
import static com.example.engmohamed.bookstore.Data.StoreContract.CONTENT_AUTHORITY;
import static com.example.engmohamed.bookstore.Data.StoreContract.PATH_BOOKS_INFORMATION;

/**
 * Ask Monitor
 * getType() more details
 * Mime Data Type
 * Deference int & Integer why == null
 */

public class StoreProvider extends ContentProvider {

    // Custom Code for hole BooksInformation Table
    private static final int BOOKS = 100;
    // Custom Code for a raw on BooksInformation Table
    private static final int BOOK_ID = 101;
    // Initialize UriMatcher with no root
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Run First when class is used
    static {
        // Add Information to the UriMatcher
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS_INFORMATION, BOOKS);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS_INFORMATION + "/#", BOOK_ID);
    }

    private String LOG_TAG = StoreProvider.class.getSimpleName();
    // DataBase Customized helper class
    private StoreDbHelper mStoreDbHelper;

    @Override
    public boolean onCreate() {
        // Instantiate Customized DataBase Helper
        mStoreDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get a readable DataBase Object
        SQLiteDatabase db = mStoreDbHelper.getReadableDatabase();

        // Initialize Cursor
        Cursor cursor;

        // Get the Uri Code
        int match = uriMatcher.match(uri);

        // Check the Uri Code
        switch (match) {
            case BOOKS:
                // Read hole BooksInformation table
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // Read an row on BooksInformation table
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query URI: " + uri);
        }
        // Auto Update UI when DataBase is Changing
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Get the Uri Code
        int match = uriMatcher.match(uri);
        // Check the Uri Code
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for Uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get Writable DataBase Object
        SQLiteDatabase db = mStoreDbHelper.getWritableDatabase();

        // Get the uri code
        int match = uriMatcher.match(uri);

        // Check the Uri code
        switch (match) {
            case BOOKS:
                // Notify DataBase Change
                getContext().getContentResolver().notifyChange(uri, null);

                // Delete Full Table
                return db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            case BOOK_ID:
                // Select based on Uri ID
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Notify DataBase Change
                getContext().getContentResolver().notifyChange(uri, null);

                // Delete the data with selected ID
                return db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete Failed for Uri" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get the Uri Code
        int match = uriMatcher.match(uri);

        // Check the Uri Code
        switch (match) {
            case BOOKS:
                // Update Full Table
                return updateBooks(values, selection, selectionArgs, uri);
            case BOOK_ID:
                // Select Based on Uri Id
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Update Date with Selected ID
                return updateBooks(values, selection, selectionArgs, uri);
            default:
                throw new IllegalArgumentException("Cannot Update Uri : " + uri);
        }


    }

    /**
     * Update Book Details and check Information Validation
     *
     * @param values        new book information
     * @param selection     where new information will be updated
     * @param selectionArgs the where value
     * @return updated book ID
     */
    private int updateBooks(ContentValues values, String selection, String[] selectionArgs, Uri uri) {
        if (values.containsKey(BookEntry.PRODUCT_NAME_COLUMN)) {
            String name = values.getAsString(BookEntry.PRODUCT_NAME_COLUMN);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException("Book Must Have a Name");
            }
        }
        if (values.containsKey(BookEntry.PRICE_COLUMN)) {
            Integer price = values.getAsInteger(BookEntry.PRICE_COLUMN);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book Must Have a Valid Price");
            }
        }

        if (values.containsKey(BookEntry.QUANTITY_COLUMN)) {
            Integer quantity = values.getAsInteger(BookEntry.QUANTITY_COLUMN);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Book Must Have Valid Quantity");
            }
        }

        if (values.containsKey(BookEntry.SUPPLIER_NAME_COLUMN)) {
            String supplier = values.getAsString(BookEntry.SUPPLIER_NAME_COLUMN);
            if (supplier == null || supplier.equals("")) {
                throw new IllegalArgumentException("You Should Write The Supplier Name");
            }
        }
        // Get Writable DataBase Object
        SQLiteDatabase db = mStoreDbHelper.getWritableDatabase();

        // Notify DataBase Change
        getContext().getContentResolver().notifyChange(uri, null);

        // Update DataBase
        return db.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Insert a book into BooksInformation Table
     * Validate Inserted Information
     *
     * @param uri    to append it with the Id
     * @param values that will be inserted
     * @return Uri appended with the Id
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        String name = values.getAsString(BookEntry.PRODUCT_NAME_COLUMN);
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Book Must Have a Name");
        }

        Integer price = values.getAsInteger(BookEntry.PRICE_COLUMN);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Book Must Have a Valid Price");
        }

        Integer quantity = values.getAsInteger(BookEntry.QUANTITY_COLUMN);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Book Must Have Valid Quantity");
        }

        String supplier = values.getAsString(BookEntry.SUPPLIER_NAME_COLUMN);
        if (supplier == null || supplier.equals("")) {
            throw new IllegalArgumentException("You Should Write The Supplier Name");
        }
        // Get a readable DataBase Object
        SQLiteDatabase db = mStoreDbHelper.getWritableDatabase();

        // Insert Data to the DataBase
        long id = db.insert(BookEntry.TABLE_NAME, null, values);
        // Check the state of insertion
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;

        }
        // Notify DataBase Change
        getContext().getContentResolver().notifyChange(uri, null);

        // Uri with new item Id
        return ContentUris.withAppendedId(uri, id);
    }
}
