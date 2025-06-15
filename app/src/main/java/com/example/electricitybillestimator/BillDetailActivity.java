package com.example.electricitybillestimator;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.electricitybillestimator.BillContract;
import com.example.electricitybillestimator.BillDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    private TextView tvDetailMonth;
    private TextView tvDetailKwhUsed;
    private TextView tvDetailTotalCharges;
    private TextView tvDetailRebatePercentage;
    private TextView tvDetailFinalCost;
    private TextView tvDetailTimestamp;

    private BillDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Initialize UI elements
        tvDetailMonth = findViewById(R.id.tvDetailMonth);
        tvDetailKwhUsed = findViewById(R.id.tvDetailKwhUsed);
        tvDetailTotalCharges = findViewById(R.id.tvDetailTotalCharges);
        tvDetailRebatePercentage = findViewById(R.id.tvDetailRebatePercentage);
        tvDetailFinalCost = findViewById(R.id.tvDetailFinalCost);
        tvDetailTimestamp = findViewById(R.id.tvDetailTimestamp);

        dbHelper = new BillDbHelper(this);

        // Get the bill ID passed from MainActivity
        long billId = getIntent().getLongExtra(MainActivity.EXTRA_BILL_ID, -1);

        if (billId != -1) {
            displayBillDetails(billId);
        } else {
            Toast.makeText(this, "Error: Bill ID not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no valid ID is passed
        }
    }

    private void displayBillDetails(long billId) {
        Cursor cursor = dbHelper.getBillById(billId);

        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve data from the cursor
            String month = cursor.getString(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_MONTH));
            double kwhUsed = cursor.getDouble(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_KWH_USED));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_TOTAL_CHARGES));
            double rebatePercentage = cursor.getDouble(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_REBATE_PERCENTAGE));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_FINAL_COST));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(BillContract.BillEntry.COLUMN_TIMESTAMP));

            // Populate TextViews
            tvDetailMonth.setText("Month: " + month);
            tvDetailKwhUsed.setText(String.format(Locale.getDefault(), "Units Used (kWh): %.2f", kwhUsed));
            tvDetailTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
            tvDetailRebatePercentage.setText(String.format(Locale.getDefault(), "Rebate Percentage: %.2f%%", rebatePercentage));
            tvDetailFinalCost.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));

            // Format timestamp for display
            // The timestamp from SQLite is usually in "YYYY-MM-DD HH:MM:SS" format
            try {
                SimpleDateFormat sqliteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sqliteFormat.parse(timestamp);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                tvDetailTimestamp.setText("Recorded On: " + displayFormat.format(date));
            } catch (Exception e) {
                tvDetailTimestamp.setText("Recorded On: " + timestamp); // Fallback
            }

        } else {
            Toast.makeText(this, "Bill details not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if details couldn't be loaded
        }

        // Always close the cursor after you're done with it to prevent memory leaks.
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close(); // Close the database helper when activity is destroyed
        }
    }
}