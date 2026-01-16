package com.personal.finance.data.model;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
