package com.personal.finance.data.model;

public class Budget {
    private long id;           // AUTOINCREMENT
    private String category;
    private double limitAmount;
    private String userEmail;

    public Budget(long id, String category, double limitAmount, String userEmail) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.userEmail = userEmail;
    }

    public Budget(String category, double limitAmount, String userEmail) {
        this(0, category, limitAmount, userEmail);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}