package com.personal.finance.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "FinanceAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_THEME = "theme"; // "LIGHT" or "DARK"
    private static final String KEY_REMEMBER_EMAIL = "remember_email";
    private static final String KEY_REMEMBER_ME = "remember_me";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLogin(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public void setRememberMe(boolean remember, String email) {
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) {
            editor.putString(KEY_REMEMBER_EMAIL, email);
        } else {
            editor.remove(KEY_REMEMBER_EMAIL);
        }
        editor.apply();
    }

    public String getRememberedEmail() {
        return sharedPreferences.getString(KEY_REMEMBER_EMAIL, "");
    }

    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void logout() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_EMAIL);
        editor.apply();
    }

    public void setTheme(String theme) {
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, "LIGHT");
    }

    public void setDefaultPeriod(String period) {
        editor.putString("default_period", period);
        editor.apply();
    }

    public String getDefaultPeriod() {
        return sharedPreferences.getString("default_period", "Month");
    }
}
