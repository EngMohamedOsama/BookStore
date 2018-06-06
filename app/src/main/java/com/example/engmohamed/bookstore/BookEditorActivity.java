package com.example.engmohamed.bookstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engmohamed.bookstore.Data.StoreContract.BookEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = BookEditorActivity.class.getSimpleName();

    // Valid Egyptian phone number length
    private static final int EGYPT_PHONE_NUMBER_LENGTH = 11;

    // Image Picker ActivityResult Code
    private static final int IMAGE_PICKER_REQUEST_CODE = 0;

    private static final int DATA_SET_LOADER_CODE = 0;
    // Detect the mode of the Activity
    private Uri modeUri;

    // References for Data Containers
    @BindView(R.id.book_name)
    EditText nameEditText;
    @BindView(R.id.book_price)
    EditText priceEditText;
    @BindView(R.id.quantity_num)
    TextView quantityText;
    @BindView(R.id.book_supplier)
    EditText supplierEditText;
    @BindView(R.id.book_supplier_number)
    EditText supplierNumberEditText;
    @BindView(R.id.book_image)
    ImageView bookImage;


    // References for errors TextViews
    @BindView(R.id.error_name)
    TextView nameError;
    @BindView(R.id.error_price)
    TextView priceError;
    @BindView(R.id.error_supplier_name)
    TextView supplierError;
    @BindView(R.id.error_supplier_number)
    TextView supplierNumberError;

    // References for Btn
    @BindView(R.id.minus_btn)
    Button minusBtn;
    @BindView(R.id.plus_btn)
    Button plusBtn;
    @BindView(R.id.supplier_order_btn)
    Button supplierOrderBtn;

    // number of products available
    private int quantity;

    private String imageLocation;

    // Detector Var Change when user touch specific items
    private boolean mBookHasChanged = false;

    // Touch Var to detect user touch
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // Detector Var Change when user touch
            mBookHasChanged = true;

            // Hide All Error Messages when user touch
            if (nameError.getVisibility() == View.VISIBLE) {
                nameError.setVisibility(View.GONE);
            }
            if (priceError.getVisibility() == View.VISIBLE) {
                priceError.setVisibility(View.GONE);
            }
            if (supplierError.getVisibility() == View.VISIBLE) {
                supplierError.setVisibility(View.GONE);
            }
            if (supplierNumberError.getVisibility() == View.VISIBLE) {
                supplierNumberError.setVisibility(View.GONE);
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_editor);
        ButterKnife.bind(this);

        // Store product quantity on quantity TextView
        quantity = Integer.valueOf(quantityText.getText().toString());

        // Receive Uri Data from BookStoreActivity which contain item that clicked on
        modeUri = getIntent().getData();

        // Detect Mode of the Activity
        if (modeUri == null) {
            setTitle(getString(R.string.new_book_title));

            // Hide Order Btn
            supplierOrderBtn.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.book_editor_title));

            // Start the loader
            getLoaderManager().initLoader(DATA_SET_LOADER_CODE, null, this);
        }

        // Set Touch Listener for all data containers
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        plusBtn.setOnTouchListener(mTouchListener);
        minusBtn.setOnTouchListener(mTouchListener);
        supplierEditText.setOnTouchListener(mTouchListener);
        supplierNumberEditText.setOnTouchListener(mTouchListener);
        bookImage.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_book_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (modeUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_book);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveBook();
                return true;
            case R.id.delete_book:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the Book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(BookEditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(BookEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                Uri imageSelectionUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + imageSelectionUri.toString());

                // Store Result location on imageLocation Var
                imageLocation = imageSelectionUri.toString();

                // Set Book Image with the new image location
                bookImage.setImageBitmap(getBitmapFromUri(imageSelectionUri));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create Cursor
        return new CursorLoader(this, modeUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            // IDs for all DataBase Columns
            int nameColumn = cursor.getColumnIndex(BookEntry.PRODUCT_NAME_COLUMN);
            int priceColumn = cursor.getColumnIndex(BookEntry.PRICE_COLUMN);
            int quantityColumn = cursor.getColumnIndex(BookEntry.QUANTITY_COLUMN);
            int supplierNameColumn = cursor.getColumnIndex(BookEntry.SUPPLIER_NAME_COLUMN);
            int supplierNumberColumn = cursor.getColumnIndex(BookEntry.SUPPLIER_PHONE_NUMBER);
            int imageColumn = cursor.getColumnIndex(BookEntry.PRODUCT_IMAGE_COLUMN);

            // Get needed Information from DataBase
            // Update Quantity with the current quantity from DB
            quantity = cursor.getInt(quantityColumn);
            String bookName = cursor.getString(nameColumn);
            String bookSupplierName = cursor.getString(supplierNameColumn);
            String bookSupplierNumber = cursor.getString(supplierNumberColumn);
            String bookPrice = String.valueOf(cursor.getInt(priceColumn));
            String bookQuantity = String.valueOf(quantity);
            String bookImageLocation = cursor.getString(imageColumn);

            // Set DB Information on data containers
            nameEditText.setText(bookName);
            priceEditText.setText(bookPrice);
            quantityText.setText(bookQuantity);
            supplierEditText.setText(bookSupplierName);
            supplierNumberEditText.setText(bookSupplierNumber);

            // Check the availability of image
            if (!(TextUtils.isEmpty(bookImageLocation))) {
                // Parse Image Location to Uri & set bitmap image
                bookImage.setImageBitmap(getBitmapFromUri(Uri.parse(bookImageLocation)));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText(null);
        priceEditText.setText(null);
        quantityText.setText(null);
        supplierEditText.setText(null);
        supplierEditText.setText(null);
        bookImage.setImageBitmap(null);
    }

    /**
     * Delete Specific book depend on modeUri
     * Return the result of operation
     */
    private void deleteBook() {
        int deleteResult = getContentResolver().delete(modeUri, null, null);
        if (deleteResult != 0) {
            finish();
            Toast.makeText(this, R.string.deleted_successfully, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Insert or Update Book Depend on Activity Mode
     */
    private void saveBook() {
        // Get needed Information from Data Containers
        String bookName = nameEditText.getText().toString();
        String bookSupplierName = supplierEditText.getText().toString();
        String bookPrice = priceEditText.getText().toString();
        String bookSupplierNumber = supplierNumberEditText.getText().toString();
        int bookQuantity = quantity;

        // Data Validation Section
        // Check if user entered required data
        // And Show Error Messages if needed
        if (TextUtils.isEmpty(bookName)) {
            // Show error message
            nameError.setText(R.string.name_error_message);
            nameError.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(bookPrice)) {
            priceError.setText(R.string.price_error_message);
            priceError.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(bookSupplierName)) {
            supplierError.setText(R.string.supplier_error_message);
            supplierError.setVisibility(View.VISIBLE);
            return;
        }

        if (bookSupplierNumber.length() != EGYPT_PHONE_NUMBER_LENGTH || TextUtils.isEmpty(bookSupplierNumber)) {
            supplierNumberError.setText(R.string.error_supplier_number_message);
            supplierNumberError.setVisibility(View.VISIBLE);
            return;
        }

        // Set Information into ContentValue to use it on insert or update
        ContentValues values = new ContentValues();
        values.put(BookEntry.PRODUCT_NAME_COLUMN, bookName);
        values.put(BookEntry.PRICE_COLUMN, Integer.valueOf(bookPrice));
        values.put(BookEntry.QUANTITY_COLUMN, bookQuantity);
        values.put(BookEntry.SUPPLIER_NAME_COLUMN, bookSupplierName);
        values.put(BookEntry.SUPPLIER_PHONE_NUMBER, bookSupplierNumber);

        // Check the availability of image location
        if (imageLocation != null) {
            values.put(BookEntry.PRODUCT_IMAGE_COLUMN, imageLocation);
        }

        // Check the mode of activity insert or update
        if (modeUri == null) {
            // Insert Mode
            // Get the result Uri & check the state of insertion
            Uri insertUriResult = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (insertUriResult != null) {
                Toast.makeText(this, R.string.book_inserted_successfully, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.insert_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update Mode
            // Get the result id & check the state of update
            int updateId = getContentResolver().update(modeUri, values, null, null);
            if (updateId != 0) {
                Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.update_fail, Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Convert Image Location Uri to bitmap image
     *
     * @param uri of image location
     * @return bitmap image
     */
    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = bookImage.getWidth();
        int targetH = bookImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
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

    /**
     * Build Alert Dialog for unsaved changes
     *
     * @param discardButtonClickListener for different discard behaviors
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        builder.create().show();
    }

    /**
     * Build Dialog for delete warning
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.create().show();
    }

    // Handle btnClicks Section
    @OnClick(R.id.book_image)
    void bookImage() {
        // Check SDK and preform intent action depending on it
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Show the required file types only(image)
        intent.setType("image/*");

        // Create Image Chooser with intent data, title, custom Code
        startActivityForResult(Intent.createChooser(intent, getString(R.string.img_picker_title)), IMAGE_PICKER_REQUEST_CODE);
    }

    @OnClick(R.id.plus_btn)
    void increaseQuantity() {
        // Increase the quantity by one
        quantity++;

        // Set the new Quantity on quantity TextView
        quantityText.setText(String.valueOf(quantity));

        // Check if minus btn is disabled
        if (!(minusBtn.isEnabled())) {
            // Enable minus btn
            minusBtn.setEnabled(true);
        }
    }

    @OnClick(R.id.minus_btn)
    void decreaseQuantity() {
        // Check if quantity more than zero
        if (quantity > 0) {
            // minus quantity by one
            quantity--;

            // Set the new quantity
            quantityText.setText(String.valueOf(quantity));

            // disable minus btn when quantity equal zero

        } else {
            minusBtn.setEnabled(false);
        }
    }

    @OnClick(R.id.supplier_order_btn)
    void supplierOrder() {
        // Store the current supplier phone number
        String supplierPhoneText = supplierNumberEditText.getText().toString();

        // Check if supplier phone number is valid
        if (supplierPhoneText.length() == EGYPT_PHONE_NUMBER_LENGTH) {
            // Create call intent with app
            Intent callIntent = new Intent(Intent.ACTION_DIAL);

            // Base Phone number Uri
            callIntent.setData(Uri.parse("tel:" + supplierPhoneText));
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                // Start Call Intent
                startActivity(callIntent);
            }
        } else {
            Toast.makeText(BookEditorActivity.this, R.string.supplier_btn_error, Toast.LENGTH_SHORT).show();
        }
    }
}
