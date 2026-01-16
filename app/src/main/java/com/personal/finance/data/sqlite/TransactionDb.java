package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.model.ReportRow;
import com.personal.finance.data.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                new String[] { String.valueOf(t.getId()) });
    }

    public int deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("transactions", "id=?", new String[] { String.valueOf(id) });
    }

    public int delete(Transaction t) {
        return deleteById(t.getId());
    }

    // ---------- Queries ----------
    public List<Transaction> getAllTransactions(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? ORDER BY date DESC",
                new String[] { email });
    }

    public List<Transaction> getIncomes(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? AND type='INCOME' ORDER BY date DESC",
                new String[] { email });
    }

    public List<Transaction> getExpenses(String email) {
        return queryTransactions(
                "SELECT id, amount, date, category, description, type, userEmail " +
                        "FROM transactions WHERE userEmail=? AND type='EXPENSE' ORDER BY date DESC",
                new String[] { email });
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
                new String[] { email });
    }

    public double getTotalExpense(String email) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='EXPENSE'",
                new String[] { email });
    }

    public double getTotalIncomeByDate(String email, long startDate, long endDate) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='INCOME' AND date>=? AND date<=?",
                new String[] { email, String.valueOf(startDate), String.valueOf(endDate) });
    }

    public double getTotalExpenseByDate(String email, long startDate, long endDate) {
        return querySum(
                "SELECT SUM(amount) FROM transactions WHERE userEmail=? AND type='EXPENSE' AND date>=? AND date<=?",
                new String[] { email, String.valueOf(startDate), String.valueOf(endDate) });
    }

    private double querySum(String sql, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            if (!c.moveToFirst() || c.isNull(0))
                return 0.0;
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

        String[] args = new String[] {
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

    public double[] getMonthlySumsByType(String email, String type, long startDate, long endDate) {
        double[] monthlySums = new double[12];
        SQLiteDatabase db = helper.getReadableDatabase();

        String sql = "SELECT strftime('%m', datetime(date/1000, 'unixepoch')) as month, SUM(amount) as total " +
                "FROM transactions " +
                "WHERE userEmail=? AND type=? AND date>=? AND date<=? " +
                "GROUP BY month";

        String[] args = new String[] {
                email, type, String.valueOf(startDate), String.valueOf(endDate)
        };

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                String mStr = c.getString(0);
                if (mStr != null) {
                    int monthIdx = Integer.parseInt(mStr) - 1;
                    double total = c.getDouble(1);
                    if (monthIdx >= 0 && monthIdx < 12) {
                        monthlySums[monthIdx] = total;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return monthlySums;
    }

    public double[] getDailySumsByType(String email, String type, long startDate, long endDate, int days) {
        double[] dailySums = new double[days];
        SQLiteDatabase db = helper.getReadableDatabase();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(startDate);
        int startDayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR);

        String sql = "SELECT date, amount FROM transactions WHERE userEmail=? AND type=? AND date>=? AND date<=?";
        String[] args = new String[] { email, type, String.valueOf(startDate), String.valueOf(endDate) };

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                long d = c.getLong(0);
                double amt = c.getDouble(1);

                cal.setTimeInMillis(d);
                int currentDayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR);
                int diff = currentDayOfYear - startDayOfYear;

                if (diff < 0)
                    diff += cal.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);

                if (diff >= 0 && diff < days) {
                    dailySums[diff] += amt;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dailySums;
    }

    // ================== REPORTS (Aggregates) ==================

    /**
     * Daily report rows inside [startDate, endDate].
     * label: "MMM dd"
     */
    public List<ReportRow> getDailyReport(String email, long startDate, long endDate) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<ReportRow> res = new ArrayList<>();

        // group by day
        String sql =
                "SELECT strftime('%Y-%m-%d', datetime(date/1000, 'unixepoch')) as d, " +
                        "SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) as incomeSum, " +
                        "SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) as expenseSum " +
                        "FROM transactions " +
                        "WHERE userEmail=? AND date>=? AND date<=? " +
                        "GROUP BY d " +
                        "ORDER BY d ASC";

        String[] args = new String[] { email, String.valueOf(startDate), String.valueOf(endDate) };

        SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd", Locale.getDefault());

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                String dayStr = c.getString(0);
                double inc = c.isNull(1) ? 0.0 : c.getDouble(1);
                double exp = c.isNull(2) ? 0.0 : c.getDouble(2);

                String label = dayStr;
                try {
                    java.util.Date d = inFmt.parse(dayStr);
                    if (d != null) label = outFmt.format(d);
                } catch (Exception ignored) {}

                res.add(new ReportRow(label, inc, exp));
            }
        }
        return res;
    }

    /**
     * Weekly report rows inside [startDate, endDate].
     * label: "YYYY-WW"
     */
    public List<ReportRow> getWeeklyReport(String email, long startDate, long endDate) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<ReportRow> res = new ArrayList<>();

        String sql =
                "SELECT strftime('%Y-%W', datetime(date/1000, 'unixepoch')) as yw, " +
                        "SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) as incomeSum, " +
                        "SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) as expenseSum " +
                        "FROM transactions " +
                        "WHERE userEmail=? AND date>=? AND date<=? " +
                        "GROUP BY yw " +
                        "ORDER BY yw ASC";

        String[] args = new String[] { email, String.valueOf(startDate), String.valueOf(endDate) };

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                String label = c.getString(0);
                double inc = c.isNull(1) ? 0.0 : c.getDouble(1);
                double exp = c.isNull(2) ? 0.0 : c.getDouble(2);
                res.add(new ReportRow(label, inc, exp));
            }
        }
        return res;
    }

    /**
     * Monthly report rows inside [startDate, endDate].
     * label: "MMM yyyy"
     */
    public List<ReportRow> getMonthlyReport(String email, long startDate, long endDate) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<ReportRow> res = new ArrayList<>();

        String sql =
                "SELECT strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as ym, " +
                        "SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) as incomeSum, " +
                        "SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) as expenseSum " +
                        "FROM transactions " +
                        "WHERE userEmail=? AND date>=? AND date<=? " +
                        "GROUP BY ym " +
                        "ORDER BY ym ASC";

        String[] args = new String[] { email, String.valueOf(startDate), String.valueOf(endDate) };

        SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat outFmt = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                String ym = c.getString(0);
                double inc = c.isNull(1) ? 0.0 : c.getDouble(1);
                double exp = c.isNull(2) ? 0.0 : c.getDouble(2);

                String label = ym;
                try {
                    java.util.Date d = inFmt.parse(ym);
                    if (d != null) label = outFmt.format(d);
                } catch (Exception ignored) {}

                res.add(new ReportRow(label, inc, exp));
            }
        }
        return res;
    }
}