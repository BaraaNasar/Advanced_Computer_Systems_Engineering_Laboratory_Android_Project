package com.personal.finance.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static final String PREF_NAME = "FinanceAppPrefs";

    // Session
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";

    // Remember me (email only)
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    // Settings
    private static final String KEY_THEME = "theme"; // "LIGHT" or "DARK"
    private static final String KEY_DEFAULT_PERIOD = "default_period";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Session
    public void setLogin(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    // Remember me (store email only)
    public void setRememberedEmail(String emailOrNull) {
        if (emailOrNull == null || emailOrNull.trim().isEmpty()) {
            editor.remove(KEY_REMEMBER_EMAIL);
        } else {
            editor.putString(KEY_REMEMBER_EMAIL, emailOrNull.trim());
        }
        editor.apply();
    }

    public String getRememberedEmail() {
        return sharedPreferences.getString(KEY_REMEMBER_EMAIL, null);
    }

    public void logout() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_EMAIL);
        editor.apply();
        // IMPORTANT: do NOT clear remembered email here
    }

    // Settings
    public void setTheme(String theme) {
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, "LIGHT");
    }

    public void setDefaultPeriod(String period) {
        editor.putString(KEY_DEFAULT_PERIOD, period);
        editor.apply();
    }

    public String getDefaultPeriod() {
        return sharedPreferences.getString(KEY_DEFAULT_PERIOD, "Month");
    }
}