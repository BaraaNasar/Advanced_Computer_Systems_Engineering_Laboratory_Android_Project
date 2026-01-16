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

    // check if user exists
    public boolean userExists(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT 1 FROM " + DBHelper.T_USERS +
                " WHERE " + DBHelper.C_EMAIL + "=? LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[] { email })) {
            return c.moveToFirst();
        }
    }

    // insert user
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

    // validate login
    public boolean validateLogin(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT 1 FROM " + DBHelper.T_USERS +
                " WHERE " + DBHelper.C_EMAIL + "=? AND " + DBHelper.C_PASSWORD + "=? LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[] { email, password })) {
            return c.moveToFirst();
        }
    }

    // get by email
    public com.personal.finance.data.model.User getUser(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " + DBHelper.T_USERS +
                " WHERE " + DBHelper.C_EMAIL + "=? LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[] { email })) {
            if (c.moveToFirst()) {
                String first = c.getString(c.getColumnIndexOrThrow(DBHelper.C_FIRST));
                String last = c.getString(c.getColumnIndexOrThrow(DBHelper.C_LAST));
                String pass = c.getString(c.getColumnIndexOrThrow(DBHelper.C_PASSWORD));
                return new com.personal.finance.data.model.User(email, first, last, pass);
            }
        }
        return null;
    }

    // update user
    public boolean updateUser(com.personal.finance.data.model.User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.C_FIRST, user.getFirstName());
        cv.put(DBHelper.C_LAST, user.getLastName());
        cv.put(DBHelper.C_PASSWORD, user.getPassword());

        int count = db.update(DBHelper.T_USERS, cv, DBHelper.C_EMAIL + "=?", new String[] { user.getEmail() });
        return count > 0;
    }
}