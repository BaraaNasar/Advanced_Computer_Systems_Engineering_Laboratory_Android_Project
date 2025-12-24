package com.personal.finance.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions", foreignKeys = @ForeignKey(entity = User.class, parentColumns = "email", childColumns = "userEmail", onDelete = ForeignKey.CASCADE), indices = {
        @Index("userEmail") })
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public double amount;
    public long date; // Timestamp
    public String category;
    public String description;
    public String type; // "INCOME" or "EXPENSE"
    public String userEmail;

    public Transaction(double amount, long date, String category, String description, String type, String userEmail) {
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
        this.type = type;
        this.userEmail = userEmail;
    }
}
