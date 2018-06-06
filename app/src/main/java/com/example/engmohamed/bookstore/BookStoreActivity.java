package com.example.engmohamed.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.engmohamed.bookstore.Data.StoreContract.BookEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookStoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CURSOR_LOADER_CODE = 0;
    private BookStoreCursorAdaptor bookAdaptor;

    // Find ListView Reference
    @BindView(R.id.list)
    ListView bookList;

    // Find reference for empty view
    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    @OnClick(R.id.add_book_floating_btn)
    void addBook() {
        // Initialize intent to NewBookActivity
        Intent newBookIntent = new Intent(BookStoreActivity.this, BookEditorActivity.class);

        // Start the activity
        startActivity(newBookIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_store);
        ButterKnife.bind(this);

        // Initialize Custom Cursor Adaptor
        bookAdaptor = new BookStoreCursorAdaptor(this, null);

        // Set Empty View
        bookList.setEmptyView(emptyView);

        // Set ListView Adaptor
        bookList.setAdapter(bookAdaptor);

        // Active on item click
        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Initialize intent to NewBookActivity
                Intent editBookIntent = new Intent(BookStoreActivity.this, BookEditorActivity.class);

                // Send Uri with item id
                editBookIntent.setData(ContentUris.withAppendedId(BookEntry.CONTENT_URI, id));

                // Start the activity
                startActivity(editBookIntent);
            }
        });

        // Start the loader on background to read cursor
        getLoaderManager().initLoader(CURSOR_LOADER_CODE, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu xml
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate Menu Icon
        switch (item.getItemId()) {
            case R.id.delete_books:
                // Delete All Table Info
                // TODO: Fix ID Bug (No Reset for AUTOINCREMENT)
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, BookEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Initialize Custom Cursor Adaptor
        bookAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdaptor.swapCursor(null);
    }

    /**
     * Build Dialog for delete warning
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_books);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the books.
                getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
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
}
