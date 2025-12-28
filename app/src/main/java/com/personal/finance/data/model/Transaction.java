package com.personal.finance.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {

    private long id; // AUTOINCREMENT in SQLite
    private double amount;
    private long date; // timestamp
    private String category;
    private String description;
    private String type; // INCOME / EXPENSE
    private String userEmail;

    public Transaction(long id, double amount, long date,
                       String category, String description,
                       String type, String userEmail) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
        this.type = type;
        this.userEmail = userEmail;
    }

    // constructor for insert (no id yet)
    public Transaction(double amount, long date,
                       String category, String description,
                       String type, String userEmail) {
        this(0, amount, date, category, description, type, userEmail);
    }
}
