package com.example.engmohamed.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engmohamed.bookstore.Data.StoreContract.BookEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

public class BookStoreCursorAdaptor extends CursorAdapter {
    private String LOG_TAG = BookStoreCursorAdaptor.class.getSimpleName();
    private static final int FREE_PRODUCT_PRICE = 0;
    private static final int OUT_STOCK = 0;

    /**
     * Custom Constructor pass data to super
     *
     * @param context for the Activity
     * @param cursor  used to get data
     */
    BookStoreCursorAdaptor(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // IDs for all DataBase Columns
        int idColumn = cursor.getColumnIndex(BookEntry._ID);
        int nameColumn = cursor.getColumnIndex(BookEntry.PRODUCT_NAME_COLUMN);
        int priceColumn = cursor.getColumnIndex(BookEntry.PRICE_COLUMN);
        final int quantityColumn = cursor.getColumnIndex(BookEntry.QUANTITY_COLUMN);
        int imageColumn = cursor.getColumnIndex(BookEntry.PRODUCT_IMAGE_COLUMN);

        // Get All Item Information needed
        // Get ID with buyNow Btn to append id with Uri
        final int id = cursor.getInt(idColumn);

        // Get item name
        String name = cursor.getString(nameColumn);

        // Get item price
        int price = cursor.getInt(priceColumn);

        // Get item quantity used with buyNow Btn
        int quantity = cursor.getInt(quantityColumn);

        // Get image location used to set product image
        String imageLocation = cursor.getString(imageColumn);

        // Find & set product name
        TextView productName = view.findViewById(R.id.item_name);
        productName.setText(name);

        // Find & set product price
        TextView productPrice = view.findViewById(R.id.item_price);

        if (price == FREE_PRODUCT_PRICE) {
            productPrice.setText(R.string.price_free);
        } else {
            productPrice.setText(NumberFormat.getCurrencyInstance().format(price));
        }

        // Find Reference for item Btn
        Button buyNowBtn = view.findViewById(R.id.buy_now);

        // Check if BuyNow Btn is enabled
        if (buyNowBtn.isEnabled()) {
            buyNowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get current quantity at the moment of click
                    int quantity = cursor.getInt(quantityColumn);

                    // Create a Uri for current item on the list using id
                    Uri bookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                    // update the quantity as needed
                    quantityUpdate(context, bookUri, quantity);
                }
            });
        }

        // Find Reference for item quantity
        TextView productQuantity = view.findViewById(R.id.item_quantity);

        // Quantity Logic Implementation
        if (quantity != OUT_STOCK) {
            buyNowBtn.setEnabled(true);
            // When quantity more than 0 item_quantity text color change to green
            productQuantity.setTextColor(context.getResources().getColor(R.color.inStock));

            // Show the remain Quantity
            productQuantity.setText(context.getString(R.string.in_stock) + quantity + " " + context.getString(R.string.remain));
        } else {
            // When Quantity equal zero buy now btn got disabled
            buyNowBtn.setEnabled(false);

            // Item_quantity text color change to red
            productQuantity.setTextColor(context.getResources().getColor(R.color.priceColor));

            // Show out of stock
            productQuantity.setText(R.string.out_of_stock);
        }

        // Check the availability of image
        if (!(TextUtils.isEmpty(imageLocation))) {
            // Find Reference for Product Image
            ImageView productImage = view.findViewById(R.id.item_image);

            // Get the image from given location
            Bitmap bookImage = getBitmapFromUri(imageLocation, context);

            // Set the image
            productImage.setImageBitmap(bookImage);
        }
    }

    /**
     * Lower the quantity by one
     * Update the database with new quantity
     * after update ContentResolver Notify the change & item got recreated
     * when the item got recreated it check the new quantity & if it equal zero
     * The BuyNow Btn got disabled & no negative quantities are displayed
     *
     * @param context  for get ContentResolver
     * @param bookUri  to update DB
     * @param quantity to get current item quantity
     */
    private void quantityUpdate(Context context, Uri bookUri, int quantity) {
        quantity--;
        ContentValues values = new ContentValues();
        values.put(BookEntry.QUANTITY_COLUMN, quantity);
        long updateResult = context.getContentResolver().update(bookUri, values, null, null);
        if (updateResult < 0) {
            Toast.makeText(context, R.string.quantity_update_fail, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert image location to bitmap
     *
     * @param imageLocation place of image on device
     * @param context       to get ContextResolver
     * @return bitmap image
     */
    private Bitmap getBitmapFromUri(String imageLocation, Context context) {
        // Convert image location to Uri
        Uri uri = Uri.parse(imageLocation);

        // Check the content of Uri
        if (uri == null || uri.toString().isEmpty())
            return null;

        // Fixed Dimension from item_image View
        int targetW = (int) context.getResources().getDimension(R.dimen.space88);
        int targetH = (int) context.getResources().getDimension(R.dimen.space100);

        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            // Main image dimensions
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}
