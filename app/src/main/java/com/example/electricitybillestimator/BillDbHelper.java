package com.example.electricitybillestimator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.electricitybillestimator.BillContract.BillEntry;

import java.util.ArrayList;
import java.util.List;

public class BillDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ElectricityBills.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BillEntry.TABLE_NAME + " (" +
                    BillEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BillEntry.COLUMN_MONTH + " TEXT NOT NULL," +
                    BillEntry.COLUMN_KWH_USED + " REAL NOT NULL," +
                    BillEntry.COLUMN_TOTAL_CHARGES + " REAL NOT NULL," +
                    BillEntry.COLUMN_REBATE_PERCENTAGE + " REAL NOT NULL," +
                    BillEntry.COLUMN_FINAL_COST + " REAL NOT NULL," +
                    BillEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BillEntry.TABLE_NAME;

    public BillDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertBill(String month, double kwhUsed, double totalCharges,
                           double rebatePercentage, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BillEntry.COLUMN_MONTH, month);
        values.put(BillEntry.COLUMN_KWH_USED, kwhUsed);
        values.put(BillEntry.COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(BillEntry.COLUMN_REBATE_PERCENTAGE, rebatePercentage);
        values.put(BillEntry.COLUMN_FINAL_COST, finalCost);
        long newRowId = db.insert(BillEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                BillEntry._ID,
                BillEntry.COLUMN_MONTH,
                BillEntry.COLUMN_KWH_USED,
                BillEntry.COLUMN_TOTAL_CHARGES,
                BillEntry.COLUMN_REBATE_PERCENTAGE,
                BillEntry.COLUMN_FINAL_COST,
                BillEntry.COLUMN_TIMESTAMP
        };
        String sortOrder = BillEntry.COLUMN_TIMESTAMP + " DESC";
        Cursor cursor = db.query(
                BillEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        return cursor;
    }

    /**
     * Retrieves a single bill record by its ID.
     * @param id The _ID of the bill to retrieve.
     * @return A Cursor containing the single row, or null if not found.
     */
    public Cursor getBillById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                BillEntry._ID,
                BillEntry.COLUMN_MONTH,
                BillEntry.COLUMN_KWH_USED,
                BillEntry.COLUMN_TOTAL_CHARGES,
                BillEntry.COLUMN_REBATE_PERCENTAGE,
                BillEntry.COLUMN_FINAL_COST,
                BillEntry.COLUMN_TIMESTAMP
        };

        String selection = BillEntry._ID + " = ?"; // WHERE clause
        String[] selectionArgs = { String.valueOf(id) }; // Value for the WHERE clause

        Cursor cursor = db.query(
                BillEntry.TABLE_NAME,   // The table to query
                projection,             // The columns to return
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // Don't group the rows
                null,                   // Don't filter by row groups
                null                    // The sort order
        );

        // Don't close the database here, as the Cursor needs it to be open.
        // The Cursor will be closed by the caller (BillDetailActivity).
        return cursor;
    }
}

