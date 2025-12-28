package com.personal.finance.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "finance_local.db";
    public static final int DB_VERSION = 1;

    // Tables
    public static final String T_USERS = "users";
    public static final String T_TRANSACTIONS = "transactions";
    public static final String T_BUDGETS = "budgets";
    public static final String T_CATEGORIES = "categories";

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

        db.execSQL("CREATE TABLE " + T_BUDGETS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "category TEXT, "
                + "limitAmount REAL NOT NULL, "
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + T_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }
}