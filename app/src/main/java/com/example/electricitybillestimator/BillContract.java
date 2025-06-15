package com.example.electricitybillestimator;

import android.provider.BaseColumns;

public final class BillContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BillContract() {}

    /**
     * Inner class that defines the table contents
     */
    public static class BillEntry implements BaseColumns {
        public static final String TABLE_NAME = "bills";
        public static final String COLUMN_MONTH = "month";
        public static final String COLUMN_KWH_USED = "kwh_used";
        public static final String COLUMN_TOTAL_CHARGES = "total_charges";
        public static final String COLUMN_REBATE_PERCENTAGE = "rebate_percentage";
        public static final String COLUMN_FINAL_COST = "final_cost";
        public static final String COLUMN_TIMESTAMP = "timestamp"; // To record when the entry was made
    }
}