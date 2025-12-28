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

    // INSERT
    public long insert(Budget budget) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", budget.getCategory());
        cv.put("limitAmount", budget.getLimitAmount());
        cv.put("userEmail", budget.getUserEmail());
        return db.insert("budgets", null, cv); // يرجع id أو -1
    }

    // UPDATE (by id)
    public int update(Budget budget) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", budget.getCategory());
        cv.put("limitAmount", budget.getLimitAmount());
        cv.put("userEmail", budget.getUserEmail());

        return db.update(
                "budgets",
                cv,
                "id=?",
                new String[]{String.valueOf(budget.getId())}
        );
    }

    // DELETE (by id)
    public int deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("budgets", "id=?", new String[]{String.valueOf(id)});
    }

    // GET ALL budgets for user
    public List<Budget> getAllBudgets(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Budget> res = new ArrayList<>();

        String sql = "SELECT id, category, limitAmount, userEmail FROM budgets WHERE userEmail=?";

        try (Cursor c = db.rawQuery(sql, new String[]{email})) {
            int iId = c.getColumnIndexOrThrow("id");
            int iCat = c.getColumnIndexOrThrow("category");
            int iLim = c.getColumnIndexOrThrow("limitAmount");
            int iEmail = c.getColumnIndexOrThrow("userEmail");

            while (c.moveToNext()) {
                long id = c.getLong(iId);
                String category = c.isNull(iCat) ? null : c.getString(iCat);
                double limitAmount = c.getDouble(iLim);
                String userEmail = c.isNull(iEmail) ? null : c.getString(iEmail);

                res.add(new Budget(id, category, limitAmount, userEmail));
            }
        }

        return res;
    }
}