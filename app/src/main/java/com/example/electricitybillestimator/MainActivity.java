package com.example.electricitybillestimator; // Make sure this matches your package name

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.electricitybillestimator.BillContract.BillEntry;
import com.example.electricitybillestimator.BillDbHelper;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etKwh;
    private EditText etRebate;
    private Button btnCalculate;
    private TextView tvTotalCharges;
    private TextView tvFinalCost;
    private ListView lvBillHistory;

    private BillDbHelper dbHelper;
    private SimpleCursorAdapter cursorAdapter;

    // Define a constant for the Intent extra key
    public static final String EXTRA_BILL_ID = "com.yourdomain.electricitybillestimator.BILL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        spinnerMonth = findViewById(R.id.spinnerMonth);
        etKwh = findViewById(R.id.etKwh);
        etRebate = findViewById(R.id.etRebate);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        lvBillHistory = findViewById(R.id.lvBillHistory);

        // Initialize database helper
        dbHelper = new BillDbHelper(this);

        // Set up the SimpleCursorAdapter for the ListView
        String[] fromColumns = {BillEntry.COLUMN_MONTH, BillEntry.COLUMN_FINAL_COST};
        int[] toViews = {R.id.tvListItemMonth, R.id.tvListItemFinalCost};

        SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tvListItemFinalCost) {
                    double finalCost = cursor.getDouble(columnIndex);
                    ((TextView) view).setText(String.format("Final Cost: RM %.2f", finalCost));
                    return true; // Indicate that the data was bound
                }
                // For other columns, let the SimpleCursorAdapter handle it normally
                return false;
            }
        };


        cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_bill,
                null,
                fromColumns,
                toViews,
                0
        );
        cursorAdapter.setViewBinder(viewBinder); // Apply the custom view binder
        lvBillHistory.setAdapter(cursorAdapter);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAndSaveBill();
            }
        });

        lvBillHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, BillDetailActivity.class);
                intent.putExtra(EXTRA_BILL_ID, id);
                startActivity(intent);
            }
        });

        loadBillHistory();
    }

    private void calculateAndSaveBill() {
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String kwhString = etKwh.getText().toString();
        String rebateString = etRebate.getText().toString();

        if (kwhString.isEmpty()) {
            etKwh.setError("Please enter units used (kWh)");
            return;
        }
        if (rebateString.isEmpty()) {
            etRebate.setError("Please enter rebate percentage");
            return;
        }

        double kwh;
        double rebatePercentage;
        try {
            kwh = Double.parseDouble(kwhString);
            rebatePercentage = Double.parseDouble(rebateString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for kWh or Rebate", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rebatePercentage < 0 || rebatePercentage > 5) {
            etRebate.setError("Rebate must be between 0% and 5%");
            return;
        }

        double totalCharges = 0;
        double remainingKwh = kwh;

        if (remainingKwh > 0) {
            double block1Kwh = Math.min(remainingKwh, 200);
            totalCharges += block1Kwh * 0.218;
            remainingKwh -= block1Kwh;
        }

        if (remainingKwh > 0) {
            double block2Kwh = Math.min(remainingKwh, 100);
            totalCharges += block2Kwh * 0.334;
            remainingKwh -= block2Kwh;
        }

        if (remainingKwh > 0) {
            double block3Kwh = Math.min(remainingKwh, 300);
            totalCharges += block3Kwh * 0.516;
            remainingKwh -= block3Kwh;
        }

        if (remainingKwh > 0) {
            totalCharges += remainingKwh * 0.546;
        }

        double rebateAmount = totalCharges * (rebatePercentage / 100.0);
        double finalCost = totalCharges - rebateAmount;

        tvTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
        tvFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));

        // Save to database
        long newRowId = dbHelper.insertBill(selectedMonth, kwh, totalCharges, rebatePercentage, finalCost);

        if (newRowId != -1) {
            Toast.makeText(this, "Bill saved!", Toast.LENGTH_SHORT).show();
            loadBillHistory(); // Refresh the ListView after saving
        } else {
            Toast.makeText(this, "Error saving bill.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBillHistory() {
        Cursor cursor = dbHelper.getAllBills();
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }

        if (cursorAdapter != null && cursorAdapter.getCursor() != null) {
            cursorAdapter.getCursor().close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadBillHistory();
    }
}