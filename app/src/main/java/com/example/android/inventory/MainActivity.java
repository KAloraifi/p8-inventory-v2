package com.example.android.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;
import com.example.android.inventory.data.InventoryDbHelper;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button insertButton = findViewById(R.id.insert_button);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
                displayDatabaseInfo();
            }
        });

        mDbHelper = new InventoryDbHelper(this);
        displayDatabaseInfo();
    }

    // Inserts dummy data into the database.
    private void insertData() {
        // Gets the data repository in read and write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Test Product");
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 99.4965);
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 4);
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "Best Supplier");
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, 1234567);

        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, contentValues);
        Log.v(LOG_TAG, "The new row ID: " + newRowId);
    }

    // Queries the data which is equivalent to "SELECT * FROM inventory"
    private Cursor queryData() {
        // Gets the data repository in read mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        return db.query(
                InventoryEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    // Displays the inserted data
    private void displayDatabaseInfo() {
        Cursor cursor = queryData();

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.display);
            displayView.setText(getString(R.string.product_count, cursor.getCount()));
            displayView.append("\n\n");

            while (cursor.moveToNext()) {

                int idIndex = cursor.getColumnIndex(InventoryEntry._ID);
                int productNameIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
                int productPriceIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
                int productQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
                int supplierNameIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
                int supplierPhoneIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE);

                int id = cursor.getInt(idIndex);
                String productName = cursor.getString(productNameIndex);
                float productPrice = cursor.getFloat(productPriceIndex);
                int productQuantity = cursor.getInt(productQuantityIndex);
                String supplierName = cursor.getString(supplierNameIndex);
                int supplierPhone = cursor.getInt(supplierPhoneIndex);

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                float formattedPrice = Float.valueOf(decimalFormat.format(productPrice));

                displayView.append(
                        "ID: " + id + "\n" +
                                "Product Name: " + productName + "\n" +
                                "Product Price: " + formattedPrice + "\n" +
                                "Product Quantity: " + productQuantity + "\n" +
                                "Supplier Name: " + supplierName + "\n" +
                                "Supplier Phone: " + supplierPhone + "\n\n");
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
}
