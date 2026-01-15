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
        cv.put("month", budget.getMonth());
        cv.put("year", budget.getYear());
        cv.put("alert50Sent", budget.getAlert50Sent());
        cv.put("alert100Sent", budget.getAlert100Sent());
        return db.insert("budgets", null, cv);
    }

    // UPDATE (by id)
    public int update(Budget budget) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", budget.getCategory());
        cv.put("limitAmount", budget.getLimitAmount());
        cv.put("userEmail", budget.getUserEmail());
        cv.put("month", budget.getMonth());
        cv.put("year", budget.getYear());
        cv.put("alert50Sent", budget.getAlert50Sent());
        cv.put("alert100Sent", budget.getAlert100Sent());

        return db.update(
                "budgets",
                cv,
                "id=?",
                new String[]{String.valueOf(budget.getId())}
        );
    }

    public int updateAlerts(long budgetId, int alert50Sent, int alert100Sent) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("alert50Sent", alert50Sent);
        cv.put("alert100Sent", alert100Sent);

        return db.update("budgets", cv, "id=?", new String[]{String.valueOf(budgetId)});
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

        String sql = "SELECT id, category, limitAmount, userEmail, month, year, alert50Sent, alert100Sent " +
                "FROM budgets WHERE userEmail=?";


        try (Cursor c = db.rawQuery(sql, new String[]{email})) {
            int iId = c.getColumnIndexOrThrow("id");
            int iCat = c.getColumnIndexOrThrow("category");
            int iLim = c.getColumnIndexOrThrow("limitAmount");
            int iEmail = c.getColumnIndexOrThrow("userEmail");
            int iMonth = c.getColumnIndexOrThrow("month");
            int iYear = c.getColumnIndexOrThrow("year");
            int iA50 = c.getColumnIndexOrThrow("alert50Sent");
            int iA100 = c.getColumnIndexOrThrow("alert100Sent");

            while (c.moveToNext()) {
                long id = c.getLong(iId);
                String category = c.isNull(iCat) ? null : c.getString(iCat);
                double limitAmount = c.getDouble(iLim);
                String userEmail = c.isNull(iEmail) ? null : c.getString(iEmail);

                int month = c.getInt(iMonth);
                int year = c.getInt(iYear);
                int a50 = c.getInt(iA50);
                int a100 = c.getInt(iA100);

                res.add(new Budget(id, category, limitAmount, userEmail, month, year, a50, a100));

            }
        }

        return res;
    }
}