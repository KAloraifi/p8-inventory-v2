package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getName();
    private InventoryCursorAdapter mCursorAdapter;
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        ListView inventoryList = findViewById(R.id.inventory_list);
        View emptyView = findViewById(R.id.empty_view);
        inventoryList.setEmptyView(emptyView);
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryList.setAdapter(mCursorAdapter);

        Button sale = findViewById(R.id.sale_button);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
    }

    // Inserts dummy data into the database.
    private void insertDummyData() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_NAME, getString(R.string.sample_name));
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 99.4965);
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 4);
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_NAME, getString(R.string.sample_supplier_name));
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, getString(R.string.sample_supplier_phone));

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues);
        if (newUri == null) {
            Toast.makeText(this, R.string.insert_error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.insert_success, Toast.LENGTH_SHORT).show();
        }
    }

    // Deletes all products.
    private void deleteAllProducts() {
        int deletedProductsCount = getContentResolver().delete(InventoryEntry.CONTENT_URI,
                null, null);
        if (deletedProductsCount == 0) {
            Toast.makeText(this, R.string.delete_all_products_error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_all_products_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_products_dialog_msg);

        // When the user clicks delete button
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllProducts();
            }
        });
        // When the user clicks cancel button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
