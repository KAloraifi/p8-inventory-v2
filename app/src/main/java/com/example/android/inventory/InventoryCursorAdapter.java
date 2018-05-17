package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.text.DecimalFormat;

/**
 * Created by Aloraifi on 15/05/2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView productName = view.findViewById(R.id.product_name);
        TextView productPrice = view.findViewById(R.id.product_price);
        TextView productQty = view.findViewById(R.id.main_product_qty);
        Button sale = view.findViewById(R.id.sale_button);
        // the id will be used to identify the row/product that the sale button resides in.
        final int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME));
        float price = cursor.getFloat(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE));
        // the quantity will be used as a reference to decrease it in decrementProductQuantity method.
        final int qty = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        productName.setText(name);
        productPrice.setText(formatPrice(price));
        productQty.setText(String.valueOf(qty));

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the corresponding product's URI.
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                decrementProductQuantity(context, currentProductUri, qty);
            }
        });
    }

    // Helper method for Sale Button which decreases a product's quantity by 1.
    private void decrementProductQuantity(Context context, Uri currentProductUri, int qty) {
        if (qty == 0) {
            Toast.makeText(context, R.string.min_qty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        int newQty = qty - 1;
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQty);

        int updatedProductCount = context.getContentResolver().update(
                currentProductUri, values, null, null);
        if (updatedProductCount == 0) {
            Toast.makeText(context, R.string.sale_error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.sale_success, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to format price as a 2 decimal point.
    private String formatPrice(float price) {
        DecimalFormat formatted = new DecimalFormat("SAR.##");
        return formatted.format(price);
    }
}
