package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionDb {

    private final DBHelper helper;

    public TransactionDb(Context ctx) {
        this.helper = new DBHelper(ctx);
    }

    // INSERT -> returns new row id or -1
    public long insert(Transaction t) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", t.getAmount());
        cv.put("date", t.getDate());
        cv.put("category", t.getCategory());
        cv.put("description", t.getDescription());
        cv.put("type", t.getType());
        cv.put("userEmail", t.getUserEmail());
        return db.insert("transactions", null, cv);
    }

    // UPDATE by id
    public int update(Transaction t) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", t.getAmount());
        cv.put("date", t.getDate());
        cv.put("category", t.getCategory());
        cv.put("description", t.getDescription());
        cv.put("type", t.getType());
        cv.put("userEmail", t.getUserEmail());

        return db.update("transactions", cv, "id=?",
                new String[]{String.valueOf(t.getId())});
    }

    // DELETE by id (أفضل من delete(object) مباشرة)
    public int deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("transactions", "id=?", new String[]{String.valueOf(id)});
    }

    // Optional: delete using object fields (لو حابة)
    public int delete(Transaction t) {
        return deleteById(t.getId());
    }

    // ---------- Queries ----------
    public List<Transaction> getAllTransactions(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? ORDER BY date DESC",
                new String[]{email}
        );
    }

    public List<Transaction> getIncomes(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? AND type='INCOME' ORDER BY date DESC",
                new String[]{email}
        );
    }

    public List<Transaction> getExpenses(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? AND type='EXPENSE' ORDER BY date DESC",
                new String[]{email}
        );
    }

    private List<Transaction> queryTransactions(String sql, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Transaction> res = new ArrayList<>();

        try (Cursor c = db.rawQuery(sql, args)) {
            int iId = c.getColumnIndexOrThrow("id");
            int iAmount = c.getColumnIndexOrThrow("amount");
            int iDate = c.getColumnIndexOrThrow("date");
            int iCategory = c.getColumnIndexOrThrow("category");
            int iDesc = c.getColumnIndexOrThrow("description");
            int iType = c.getColumnIndexOrThrow("type");
            int iEmail = c.getColumnIndexOrThrow("userEmail");

            while (c.moveToNext()) {
                long id = c.getLong(iId);
                double amount = c.getDouble(iAmount);
                long date = c.getLong(iDate);
                String category = c.isNull(iCategory) ? null : c.getString(iCategory);
                String desc = c.isNull(iDesc) ? null : c.getString(iDesc);
                String type = c.isNull(iType) ? null : c.getString(iType);
                String userEmail = c.isNull(iEmail) ? null : c.getString(iEmail);

                res.add(new Transaction(id, amount, date, category, desc, type, userEmail));
            }
        }

        return res;
    }

    // ---------- SUM helpers ----------
    public double getTotalIncome(String email) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='INCOME'",
                new String[]{email}
        );
    }

    public double getTotalExpense(String email) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='EXPENSE'",
                new String[]{email}
        );
    }

    public double getTotalIncomeByDate(String email, long startDate, long endDate) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='INCOME' AND date>=? AND date<=?",
                new String[]{email, String.valueOf(startDate), String.valueOf(endDate)}
        );
    }

    public double getTotalExpenseByDate(String email, long startDate, long endDate) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='EXPENSE' AND date>=? AND date<=?",
                new String[]{email, String.valueOf(startDate), String.valueOf(endDate)}
        );
    }

    private double querySum(String sql, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            if (!c.moveToFirst() || c.isNull(0)) return 0.0;
            return c.getDouble(0);
        }
    }

    // ---------- Category grouped sums ----------
    public List<CategorySum> getCategoryGroupedSums(String email, String type, long startDate, long endDate) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<CategorySum> res = new ArrayList<>();

        String sql = "SELECT category, SUM(amount) as totalAmount " +
                "FROM transactions " +
                "WHERE userEmail=? AND type=? AND date>=? AND date<=? " +
                "GROUP BY category";

        String[] args = new String[]{
                email, type, String.valueOf(startDate), String.valueOf(endDate)
        };

        try (Cursor c = db.rawQuery(sql, args)) {
            int iCategory = c.getColumnIndexOrThrow("category");
            int iTotal = c.getColumnIndexOrThrow("totalAmount");

            while (c.moveToNext()) {
                String category = c.isNull(iCategory) ? null : c.getString(iCategory);
                double total = c.isNull(iTotal) ? 0.0 : c.getDouble(iTotal);
                res.add(new CategorySum(category, total));
            }
        }

        return res;
    }
}