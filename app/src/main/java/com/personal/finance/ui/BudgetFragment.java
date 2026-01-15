package com.personal.finance.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.R;
import com.personal.finance.data.model.Budget;
import com.personal.finance.ui.adapter.BudgetAdapter;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private BudgetAdapter adapter;
    private SessionManager sessionManager;

    private int selectedMonth;
    private int selectedYear;
    private java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMMM yyyy",
            java.util.Locale.getDefault());
    private android.widget.TextView tvSelectedMonth;
    private android.widget.TextView tvGoalAmount, tvSavedAmount, tvGoalStatus;
    private android.widget.ProgressBar progressSavings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getUserEmail();

        if (email == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Init Calendar to current month
        java.util.Calendar cal = java.util.Calendar.getInstance();
        selectedMonth = cal.get(java.util.Calendar.MONTH);
        selectedYear = cal.get(java.util.Calendar.YEAR);

        // UI Refs
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);

        // Initial update
        updateMonthDisplay();

        RecyclerView recyclerView = view.findViewById(R.id.rvBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnBudgetActionListener(new BudgetAdapter.OnBudgetActionListener() {
            @Override
            public void onEdit(Budget budget) {
                showUpdateLimitDialog(budget);
            }

            @Override
            public void onDelete(Budget budget) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Budget")
                        .setMessage("Are you sure you want to delete this budget?")
                        .setPositiveButton("Yes", (d, w) -> {
                            new Thread(() -> {
                                financeViewModel.deleteBudget(budget);
                                requireActivity().runOnUiThread(() -> loadData(sessionManager.getUserEmail()));
                            }).start();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        // Listeners
        view.findViewById(R.id.cardMonthSelector).setOnClickListener(v -> showMonthPicker());
        view.findViewById(R.id.fabAddBudget).setOnClickListener(v -> showAddDialog(email));

        loadData(email);
    }

    private void updateMonthDisplay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.MONTH, selectedMonth);
        cal.set(java.util.Calendar.YEAR, selectedYear);
        tvSelectedMonth.setText(monthFormat.format(cal.getTime()));
    }

    private void showMonthPicker() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        final android.widget.NumberPicker monthPicker = dialogView.findViewById(R.id.pickerMonth);
        final android.widget.NumberPicker yearPicker = dialogView.findViewById(R.id.pickerYear);

        // Setup Month Picker
        String[] months = new java.text.DateFormatSymbols().getShortMonths();
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(selectedMonth);
        monthPicker.setDescendantFocusability(android.widget.NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // Setup Year Picker
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 10);
        yearPicker.setMaxValue(currentYear + 10);
        yearPicker.setValue(selectedYear);
        yearPicker.setDescendantFocusability(android.widget.NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedMonth = monthPicker.getValue();
            selectedYear = yearPicker.getValue();
            updateMonthDisplay();
            loadData(sessionManager.getUserEmail());
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void loadData(String email) {
        new Thread(() -> {
            // 1. Budgets
            List<Budget> budgets = financeViewModel.getBudgets(email, selectedMonth, selectedYear);
            Map<String, Double> spentAmounts = new HashMap<>();
            for (Budget budget : budgets) {
                double spent = financeViewModel.getSpentAmountForCategory(email, budget.getCategory(), selectedMonth,
                        selectedYear);
                spentAmounts.put(budget.getCategory(), spent);
            }

            requireActivity().runOnUiThread(() -> {
                // Update Budgets List
                adapter.setBudgets(budgets);
                adapter.setSpentAmounts(spentAmounts);

                // Budget Alerts (same as before)
                checkBudgetAlerts(budgets, spentAmounts);
            });
        }).start();
    }

    private void checkBudgetAlerts(List<Budget> budgets, Map<String, Double> spentAmounts) {
        // Only show alerts if we are viewing the *current* month? Or maybe always
        // useful.
        // Let's stick to current month to avoid annoying toasts for history.
        java.util.Calendar now = java.util.Calendar.getInstance();
        if (now.get(java.util.Calendar.MONTH) != selectedMonth || now.get(java.util.Calendar.YEAR) != selectedYear) {
            return;
        }

        for (Budget budget : budgets) {
            double spent = spentAmounts.getOrDefault(budget.getCategory(), 0.0);
            double limit = budget.getLimitAmount();
            int percentage = (int) ((spent / limit) * 100);
            if (percentage >= 100) {
                Toast.makeText(requireContext(), "Alert: " + budget.getCategory() + " budget exceeded!",
                        Toast.LENGTH_LONG).show();
            } else if (percentage >= 50) {
                Toast.makeText(requireContext(), "Alert: " + budget.getCategory() + " at " + percentage + "%",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUpdateLimitDialog(Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Limit for " + budget.getCategory());

        final EditText input = new EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(budget.getLimitAmount()));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                double newLimit = Double.parseDouble(val);
                budget.setLimitAmount(newLimit);
                new Thread(() -> {
                    financeViewModel.updateBudget(budget);
                    requireActivity().runOnUiThread(() -> loadData(sessionManager.getUserEmail()));
                }).start();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Custom layout logic with Spinner
        View view = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(view);

        android.widget.Spinner spinner = view.findViewById(R.id.spinnerBudgetCategory);
        android.widget.EditText etLimit = view.findViewById(R.id.etBudgetLimit);
        android.widget.TextView tvPeriod = view.findViewById(R.id.tvBudgetPeriod);

        // Show selected period
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(selectedYear, selectedMonth, 1);
        tvPeriod.setText("For: " + monthFormat.format(cal.getTime()));

        // Make card clickable to open spinner
        view.findViewById(R.id.cardBudgetCategory).setOnClickListener(v -> spinner.performClick());

        // Populate Spinner with Expense Categories
        new Thread(() -> {
            List<com.personal.finance.data.model.Category> categories = financeViewModel.getCategoriesByType(email,
                    "EXPENSE");
            // Extract names
            List<String> names = new java.util.ArrayList<>();
            for (com.personal.finance.data.model.Category c : categories)
                names.add(c.getName());

            requireActivity().runOnUiThread(() -> {
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            });
        }).start();

        builder.setPositiveButton("Save", (dialog, which) -> {
            Object selectedItem = spinner.getSelectedItem();
            String category = (selectedItem != null) ? selectedItem.toString() : "";
            String limitStr = etLimit.getText().toString().trim();

            if (!category.isEmpty() && !limitStr.isEmpty()) {
                double limit;
                try {
                    limit = Double.parseDouble(limitStr);
                    Budget budget = new Budget(category, limit, selectedMonth, selectedYear, email);
                    financeViewModel.addBudget(budget);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> loadData(email), 200);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please select category and enter limit", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}