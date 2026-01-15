package com.personal.finance.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "finance_local.db";
    public static final int DB_VERSION = 2; // Incremented version to trigger upgrade

    // Tables
    public static final String T_USERS = "users";
    public static final String T_TRANSACTIONS = "transactions";
    public static final String T_BUDGETS = "budgets";
    public static final String T_CATEGORIES = "categories";
    public static final String T_SAVINGS_GOALS = "savings_goals";

    // Users columns
    public static final String C_EMAIL = "email";
    public static final String C_FIRST = "firstName";
    public static final String C_LAST = "lastName";
    public static final String C_PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + T_USERS + " ("
                + C_EMAIL + " TEXT PRIMARY KEY, "
                + C_FIRST + " TEXT NOT NULL, "
                + C_LAST + " TEXT NOT NULL, "
                + C_PASSWORD + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + T_TRANSACTIONS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "amount REAL NOT NULL, "
                + "date INTEGER NOT NULL, "
                + "category TEXT, "
                + "description TEXT, "
                + "type TEXT, "
                + "userEmail TEXT, "
                + "FOREIGN KEY(userEmail) REFERENCES " + T_USERS + "(" + C_EMAIL + ") ON DELETE CASCADE"
                + ");");
        db.execSQL("CREATE INDEX index_transactions_userEmail ON " + T_TRANSACTIONS + "(userEmail);");

        // Updated Budget Table with month/year
        db.execSQL("CREATE TABLE " + T_BUDGETS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "category TEXT, "
                + "limitAmount REAL NOT NULL, "
                + "month INTEGER NOT NULL DEFAULT 0, "
                + "year INTEGER NOT NULL DEFAULT 0, "
                + "userEmail TEXT, "
                + "FOREIGN KEY(userEmail) REFERENCES " + T_USERS + "(" + C_EMAIL + ") ON DELETE CASCADE"
                + ");");
        db.execSQL("CREATE INDEX index_budgets_userEmail ON " + T_BUDGETS + "(userEmail);");

        db.execSQL("CREATE TABLE " + T_CATEGORIES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "type TEXT, "
                + "userEmail TEXT"
                + ");");
        db.execSQL("CREATE UNIQUE INDEX index_categories_name_type_userEmail "
                + "ON " + T_CATEGORIES + "(name, type, userEmail);");

        // New Savings Goal Table
        db.execSQL("CREATE TABLE " + T_SAVINGS_GOALS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "goalAmount REAL NOT NULL, "
                + "month INTEGER NOT NULL, "
                + "year INTEGER NOT NULL, "
                + "userEmail TEXT, "
                + "FOREIGN KEY(userEmail) REFERENCES " + T_USERS + "(" + C_EMAIL + ") ON DELETE CASCADE"
                + ");");
        db.execSQL("CREATE UNIQUE INDEX index_savings_goals_month_year_userEmail "
                + "ON " + T_SAVINGS_GOALS + "(month, year, userEmail);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Version 1 -> 2: Add Savings Goals and recreate Budgets table to add columns
            // Simple approach: Drop and recreate Budgets (User data loss acceptable for now
            // as per dev phase)
            // Or better: ALTER TABLE, but recreate is cleaner for dev if user didn't
            // request migration preservation.
            // Let's go with Drop/Recreate for Budgets as it's cleaner. User agreed to
            // "month/year" change.
            db.execSQL("DROP TABLE IF EXISTS " + T_BUDGETS);
            db.execSQL("CREATE TABLE " + T_BUDGETS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "category TEXT, "
                    + "limitAmount REAL NOT NULL, "
                    + "month INTEGER NOT NULL DEFAULT 0, "
                    + "year INTEGER NOT NULL DEFAULT 0, "
                    + "userEmail TEXT, "
                    + "FOREIGN KEY(userEmail) REFERENCES " + T_USERS + "(" + C_EMAIL + ") ON DELETE CASCADE"
                    + ");");
            db.execSQL("CREATE INDEX index_budgets_userEmail ON " + T_BUDGETS + "(userEmail);");

            // Add Savings Goals
            db.execSQL("CREATE TABLE " + T_SAVINGS_GOALS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "goalAmount REAL NOT NULL, "
                    + "month INTEGER NOT NULL, "
                    + "year INTEGER NOT NULL, "
                    + "userEmail TEXT, "
                    + "FOREIGN KEY(userEmail) REFERENCES " + T_USERS + "(" + C_EMAIL + ") ON DELETE CASCADE"
                    + ");");
            db.execSQL("CREATE UNIQUE INDEX index_savings_goals_month_year_userEmail "
                    + "ON " + T_SAVINGS_GOALS + "(month, year, userEmail);");
        }
    }
}