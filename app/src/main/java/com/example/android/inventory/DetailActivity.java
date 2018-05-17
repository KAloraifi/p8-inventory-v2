package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private Uri mCurrentProductUri;
    private EditText mProductName;
    private EditText mProductPrice;
    private TextView mProductQty;
    private EditText mSupplierName;
    private EditText mSupplierPhone;
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Button contactSupplier = findViewById(R.id.contact_supplier);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            setTitle("Add a product");
            contactSupplier.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle("Product detail");
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

        mProductName = findViewById(R.id.edit_product_name);
        mProductPrice = findViewById(R.id.edit_product_price);
        mProductQty = findViewById(R.id.detail_product_qty);
        mSupplierName = findViewById(R.id.edit_supplier_name);
        mSupplierPhone = findViewById(R.id.edit_supplier_phone);

        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        Button minusQty = findViewById(R.id.detail_minus_qty);
        minusQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQty();
            }
        });
        Button plusQty = findViewById(R.id.detail_plus_qty);
        plusQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQty();
            }
        });

        contactSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierPhone = mSupplierPhone.getText().toString().trim();
                if (supplierPhone.length() != 10) {
                    Toast.makeText(DetailActivity.this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhone));
                startActivity(intent);
            }
        });
    }

    private void decrementQty() {
        int prevQty = Integer.parseInt(mProductQty.getText().toString());
        if (prevQty == 0) {
            Toast.makeText(this, R.string.min_qty_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        int newQty = prevQty - 1;
        mProductQty.setText(String.valueOf(newQty));
    }

    private void incrementQty() {
        int prevQty = Integer.parseInt(mProductQty.getText().toString());
        int newQty = prevQty + 1;
        mProductQty.setText(String.valueOf(newQty));
    }

    private void saveProduct() {
        String productName = mProductName.getText().toString().trim();
        String productPriceString = mProductPrice.getText().toString().trim();
        int productQuantity = Integer.parseInt(mProductQty.getText().toString());
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhone = mSupplierPhone.getText().toString().trim();
        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productPriceString)) {
            Toast.makeText(this, R.string.empty_field_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        float productPrice = Float.parseFloat(productPriceString);
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.insert_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.insert_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            int updatedProductCount = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (updatedProductCount == 0) {
                Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int deletedProductCount = getContentResolver().delete(mCurrentProductUri, null, null);
            if (deletedProductCount == 0) {
                Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
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
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE};
        return new CursorLoader(this, mCurrentProductUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String productName = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME));
            float productPrice = cursor.getFloat(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE));
            int productQty = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
            String supplierName = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME));
            String supplierPhone = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE));

            mProductName.setText(productName);
            mProductPrice.setText(String.valueOf(productPrice));
            mProductQty.setText(String.valueOf(productQty));
            mSupplierName.setText(supplierName);
            mSupplierPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mProductName.getText().clear();
        mProductPrice.getText().clear();
        mProductQty.setText(R.string.default_product_qty);
        mSupplierName.getText().clear();
        mSupplierPhone.getText().clear();
    }

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
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
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
}
