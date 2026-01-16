package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.personal.finance.data.model.SavingsGoal;

public class SavingsGoalDb {

    private final DBHelper helper;

    public SavingsGoalDb(Context ctx) {
        this.helper = new DBHelper(ctx);
    }

    public long insert(SavingsGoal g) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("goalAmount", g.getGoalAmount());
        cv.put("month", g.getMonth());
        cv.put("year", g.getYear());
        cv.put("userEmail", g.getUserEmail());

        // replace if already exists
        return db.insertWithOnConflict("savings_goals", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public SavingsGoal getGoalForMonth(String email, int month, int year) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT id, goalAmount, month, year, userEmail FROM savings_goals WHERE userEmail=? AND month=? AND year=?";
        try (Cursor c = db.rawQuery(sql, new String[] { email, String.valueOf(month), String.valueOf(year) })) {
            if (c.moveToFirst()) {
                long id = c.getLong(c.getColumnIndexOrThrow("id"));
                double amount = c.getDouble(c.getColumnIndexOrThrow("goalAmount"));
                int m = c.getInt(c.getColumnIndexOrThrow("month"));
                int y = c.getInt(c.getColumnIndexOrThrow("year"));
                String e = c.getString(c.getColumnIndexOrThrow("userEmail"));
                return new SavingsGoal(id, amount, m, y, e);
            }
        }
        return null;
    }
}
