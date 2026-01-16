package com.personal.finance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.personal.finance.ui.viewmodel.FinanceViewModel;

import com.google.android.material.navigation.NavigationView;
import com.personal.finance.R;
import com.personal.finance.databinding.ActivityMainBinding;
import com.personal.finance.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);

        if (!sessionManager.isLoggedIn()) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Pass each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_income, R.id.nav_history, R.id.nav_expenses, R.id.nav_budgets,
                R.id.nav_profile,
                R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        // Correct way to get NavController with FragmentContainerView
        androidx.navigation.fragment.NavHostFragment navHostFragment = (androidx.navigation.fragment.NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Ensure default categories exist if none found
        FinanceViewModel financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        String userEmail = sessionManager.getUserEmail();
        if (userEmail != null) {
            financeViewModel.initializeUserData(userEmail);
            checkBudgetAlertsOnLogin(userEmail, financeViewModel);
        }

        // Handle Logout manually as it's not a destination
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            sessionManager.logout();

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        });

        // Set User Email in Header
        View headerView = navigationView.getHeaderView(0);
        TextView navUserEmail = headerView.findViewById(R.id.tvHeaderEmail);
        if (userEmail != null) {
            navUserEmail.setText(userEmail);
        }
    }

    private void checkBudgetAlertsOnLogin(String email, FinanceViewModel viewModel) {
        new Thread(() -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int month = cal.get(java.util.Calendar.MONTH);
            int year = cal.get(java.util.Calendar.YEAR);
            java.util.List<String> alerts = viewModel.getAllBudgetAlerts(email, month, year);

            if (!alerts.isEmpty()) {
                StringBuilder sb = new StringBuilder("Summary:\n");
                for (String alert : alerts) {
                    sb.append("• ").append(alert).append("\n");
                }
                String message = sb.toString().trim();

                runOnUiThread(() -> {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                            .setTitle("Proactive Budget Alert 🚨")
                            .setMessage(message)
                            .setPositiveButton("I'll check", null)
                            .show();
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        androidx.navigation.fragment.NavHostFragment navHostFragment = (androidx.navigation.fragment.NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
