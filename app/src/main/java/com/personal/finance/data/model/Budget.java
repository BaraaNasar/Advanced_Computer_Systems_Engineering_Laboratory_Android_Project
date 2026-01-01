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

    public Budget(long id, String category, double limitAmount, String userEmail) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.userEmail = userEmail;
    }

    public Budget(String category, double limitAmount, String userEmail) {
        this(0, category, limitAmount, userEmail);
    }

}