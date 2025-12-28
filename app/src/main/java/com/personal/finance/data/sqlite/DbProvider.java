package com.personal.finance.data.sqlite;

import android.content.Context;

public class DbProvider {
    private DbProvider() {}

    public static UserDb userDb(Context c) { return new UserDb(c); }
    public static TransactionDb transactionDb(Context c) { return new TransactionDb(c); }
    public static BudgetDb budgetDb(Context c) { return new BudgetDb(c); }
    public static CategoryDb categoryDb(Context c) { return new CategoryDb(c); }
}