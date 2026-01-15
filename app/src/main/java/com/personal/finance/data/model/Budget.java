package com.personal.finance.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Budget {
    private long id;           // AUTOINCREMENT
    private String category;
    private double limitAmount;
    private String userEmail;
    private int month;
    private int year;
    private int alert50Sent;  // 0/1
    private int alert100Sent; // 0/1


    public Budget(long id, String category, double limitAmount, String userEmail,
                  int month, int year, int alert50Sent, int alert100Sent) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.userEmail = userEmail;
        this.month = month;
        this.year = year;
        this.alert50Sent = alert50Sent;
        this.alert100Sent = alert100Sent;
    }

    public Budget(String category, double limitAmount, String userEmail,
                  int month, int year) {
        this(0, category, limitAmount, userEmail, month, year, 0, 0);
    }

}