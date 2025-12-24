package com.personal.finance.data.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories", indices = { @Index(value = { "name", "type", "userEmail" }, unique = true) })
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String type; // "INCOME" or "EXPENSE"
    public String userEmail; // To support per-user categories

    public Category(String name, String type, String userEmail) {
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }
}
