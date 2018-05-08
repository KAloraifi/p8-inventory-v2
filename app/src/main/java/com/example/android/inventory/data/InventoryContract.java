package com.example.android.inventory.data;

import android.provider.BaseColumns;

/**
 * Created by Aloraifi on 08/05/2018.
 */

public final class InventoryContract {

    public static final class InventoryEntry implements BaseColumns {

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "p_name";
        public static final String COLUMN_PRODUCT_PRICE = "p_price";
        public static final String COLUMN_PRODUCT_QUANTITY = "p_quantity";
        public static final String COLUMN_SUPPLIER_NAME = "s_name";
        public static final String COLUMN_SUPPLIER_PHONE = "s_phone";

    }
}
