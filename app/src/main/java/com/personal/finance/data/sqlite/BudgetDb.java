package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.personal.finance.data.model.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetDb {

    private final DBHelper helper;

    public BudgetDb(Context ctx) {
        this.helper = new DBHelper(ctx);
    }

    // insert or update budget
    public long insert(Budget budget) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", budget.getCategory());
        cv.put("limitAmount", budget.getLimitAmount());
        cv.put("month", budget.getMonth());
        cv.put("year", budget.getYear());
        cv.put("userEmail", budget.getUserEmail());

        // see if it exists
        String selection = "category = ? AND month = ? AND year = ? AND userEmail = ?";
        String[] args = {
                budget.getCategory(),
                String.valueOf(budget.getMonth()),
                String.valueOf(budget.getYear()),
                budget.getUserEmail()
        };

        try (Cursor cursor = db.query("budgets", new String[] { "id" }, selection, args, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // update if found
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                db.update("budgets", cv, "id=?", new String[] { String.valueOf(id) });
                return id;
            } else {
                // or just insert new
                return db.insert("budgets", null, cv);
            }
        }
    }

    // update logic
    public int update(Budget budget) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", budget.getCategory());
        cv.put("limitAmount", budget.getLimitAmount());
        cv.put("month", budget.getMonth());
        cv.put("year", budget.getYear());
        cv.put("userEmail", budget.getUserEmail());

        return db.update(
                "budgets",
                cv,
                "id=?",
                new String[] { String.valueOf(budget.getId()) });
    }

    // delete logic
    public int deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("budgets", "id=?", new String[] { String.valueOf(id) });
    }

    // get operations
    public List<Budget> getAllBudgets(String email) {
        return getBudgetsForMonth(email, -1, -1);
    }

    public List<Budget> getBudgetsForMonth(String email, int month, int year) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Budget> res = new ArrayList<>();

        String sql;
        String[] args;
        if (month == -1 || year == -1) {
            sql = "SELECT id, category, limitAmount, month, year, userEmail FROM budgets WHERE userEmail=?";
            args = new String[] { email };
        } else {
            sql = "SELECT id, category, limitAmount, month, year, userEmail FROM budgets WHERE userEmail=? AND month=? AND year=?";
            args = new String[] { email, String.valueOf(month), String.valueOf(year) };
        }

        try (Cursor c = db.rawQuery(sql, args)) {
            int iId = c.getColumnIndexOrThrow("id");
            int iCat = c.getColumnIndexOrThrow("category");
            int iLim = c.getColumnIndexOrThrow("limitAmount");
            int iMonth = c.getColumnIndexOrThrow("month");
            int iYear = c.getColumnIndexOrThrow("year");
            int iEmail = c.getColumnIndexOrThrow("userEmail");

            while (c.moveToNext()) {
                res.add(new Budget(
                        c.getLong(iId),
                        c.isNull(iCat) ? null : c.getString(iCat),
                        c.getDouble(iLim),
                        c.getInt(iMonth),
                        c.getInt(iYear),
                        c.isNull(iEmail) ? null : c.getString(iEmail)));
            }
        }
        return res;
    }
}