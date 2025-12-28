package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDb {

    private final DBHelper helper;

    public UserDb(Context ctx) {
        this.helper = new DBHelper(ctx);
    }

    // Check if email already exists (PK rule)
    public boolean userExists(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT 1 FROM " + DBHelper.T_USERS +
                " WHERE " + DBHelper.C_EMAIL + "=? LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[]{email})) {
            return c.moveToFirst();
        }
    }

    // Insert new user
    public boolean insertUser(String email, String first, String last, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.C_EMAIL, email);
        cv.put(DBHelper.C_FIRST, first);
        cv.put(DBHelper.C_LAST, last);
        cv.put(DBHelper.C_PASSWORD, password);

        long res = db.insert(DBHelper.T_USERS, null, cv);
        return res != -1;
    }

    // Validate login (email + password)
    public boolean validateLogin(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT 1 FROM " + DBHelper.T_USERS +
                " WHERE " + DBHelper.C_EMAIL + "=? AND " + DBHelper.C_PASSWORD + "=? LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[]{email, password})) {
            return c.moveToFirst();
        }
    }
}