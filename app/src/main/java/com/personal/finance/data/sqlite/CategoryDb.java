package com.personal.finance.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.personal.finance.data.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDb {

    private final DBHelper helper;

    public CategoryDb(Context ctx) {
        this.helper = new DBHelper(ctx);
    }

    // insert with ignore
    public long insert(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // insert or ignore for uniqueness
        String sql = "INSERT OR IGNORE INTO categories(name, type, userEmail) VALUES(?,?,?)";
        db.execSQL(sql, new Object[] {
                category.getName(),
                category.getType(),
                category.getUserEmail()
        });

        // return 1 if exists, else -1
        return exists(category.getName(), category.getType(), category.getUserEmail()) ? 1 : -1;
    }

    private boolean exists(String name, String type, String userEmail) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "SELECT 1 FROM categories WHERE name=? AND type=? AND userEmail=? LIMIT 1";
        try (Cursor c = db.rawQuery(q, new String[] { name, type, userEmail })) {
            return c.moveToFirst();
        }
    }

    // delete category
    public int deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("categories", "id=?", new String[] { String.valueOf(id) });
    }

    public int update(Category oldCategory, String newName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        return db.update("categories", cv, "id=?", new String[] { String.valueOf(oldCategory.getId()) });
    }

    public int delete(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("categories", "id=?", new String[] { String.valueOf(category.getId()) });
    }

    public List<Category> getAllCategories(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Category> res = new ArrayList<>();

        String sql = "SELECT id, name, type, userEmail FROM categories WHERE userEmail=?";
        try (Cursor c = db.rawQuery(sql, new String[] { email })) {
            int iId = c.getColumnIndexOrThrow("id");
            int iName = c.getColumnIndexOrThrow("name");
            int iType = c.getColumnIndexOrThrow("type");
            int iEmail = c.getColumnIndexOrThrow("userEmail");

            while (c.moveToNext()) {
                res.add(new Category(
                        c.getLong(iId),
                        c.isNull(iName) ? null : c.getString(iName),
                        c.isNull(iType) ? null : c.getString(iType),
                        c.isNull(iEmail) ? null : c.getString(iEmail)));
            }
        }
        return res;
    }

    public List<Category> getCategoriesByType(String email, String type) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Category> res = new ArrayList<>();

        String sql = "SELECT id, name, type, userEmail FROM categories WHERE userEmail=? AND type=?";
        try (Cursor c = db.rawQuery(sql, new String[] { email, type })) {
            int iId = c.getColumnIndexOrThrow("id");
            int iName = c.getColumnIndexOrThrow("name");
            int iType = c.getColumnIndexOrThrow("type");
            int iEmail = c.getColumnIndexOrThrow("userEmail");

            while (c.moveToNext()) {
                res.add(new Category(
                        c.getLong(iId),
                        c.isNull(iName) ? null : c.getString(iName),
                        c.isNull(iType) ? null : c.getString(iType),
                        c.isNull(iEmail) ? null : c.getString(iEmail)));
            }
        }
        return res;
    }

    public int getCategoryCount(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM categories WHERE userEmail=?";
        try (Cursor c = db.rawQuery(sql, new String[] { email })) {
            if (!c.moveToFirst())
                return 0;
            return c.getInt(0);
        }
    }
}