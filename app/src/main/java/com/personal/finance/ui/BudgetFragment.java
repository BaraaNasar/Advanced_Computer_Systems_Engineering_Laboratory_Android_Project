package com.personal.finance.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.R;
import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Category;
import com.personal.finance.ui.adapter.BudgetAdapter;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private BudgetAdapter adapter;
    private SessionManager sessionManager;

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

        RecyclerView recyclerView = view.findViewById(R.id.rvBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter();
        recyclerView.setAdapter(adapter);

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        loadBudgets(email);

        view.findViewById(R.id.fabAddBudget).setOnClickListener(v -> showAddDialog(email));
    }

    private long startOfMonth(int month1to12, int year) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, month1to12 - 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfMonth(int month1to12, int year) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, month1to12 - 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private void loadBudgets(String email) {
        new Thread(() -> {
            List<Budget> budgets = financeViewModel.getBudgets(email);

            Map<Long, Double> spentAmounts = new HashMap<>();
            for (Budget budget : budgets) {
                long start = startOfMonth(budget.getMonth(), budget.getYear());
                long end = endOfMonth(budget.getMonth(), budget.getYear());

                double spent = financeViewModel.getTotalExpenseForCategoryByDate(
                        email,
                        budget.getCategory(),
                        start,
                        end
                );

                spentAmounts.put(budget.getId(), spent);
            }

            requireActivity().runOnUiThread(() -> {
                adapter.setBudgets(budgets);
                adapter.setSpentAmounts(spentAmounts);

                // Alerts (مرة واحدة فقط)
                for (Budget budget : budgets) {
                    double spent = spentAmounts.getOrDefault(budget.getId(), 0.0);
                    double limit = budget.getLimitAmount();
                    int percentage = (limit <= 0) ? 0 : (int) ((spent / limit) * 100);

                    if (percentage >= 100 && budget.getAlert100Sent() == 0) {
                        Toast.makeText(requireContext(),
                                budget.getCategory() + " budget exceeded! (" + percentage + "%)",
                                Toast.LENGTH_LONG).show();

                        budget.setAlert100Sent(1);
                        financeViewModel.updateBudgetAlerts(budget.getId(), budget.getAlert50Sent(), 1);

                    } else if (percentage >= 50 && budget.getAlert50Sent() == 0) {
                        Toast.makeText(requireContext(),
                                budget.getCategory() + " budget alert: " + percentage + "% used",
                                Toast.LENGTH_SHORT).show();

                        budget.setAlert50Sent(1);
                        financeViewModel.updateBudgetAlerts(budget.getId(), 1, budget.getAlert100Sent());
                    }
                }
            });

        }).start();
    }

    private void showAddDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Budget");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(view);

        Spinner spinnerCategory = view.findViewById(R.id.spinnerBudgetCategory);
        EditText etLimit = view.findViewById(R.id.etBudgetLimit);

        // تجهيز Adapter للسبينر
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // تعبئة الـ Spinner من DB (Categories نوع EXPENSE)
        new Thread(() -> {
            List<Category> categories = financeViewModel.getCategoriesByType(email, "EXPENSE");
            List<String> names = new ArrayList<>();
            for (Category c : categories) names.add(c.getName());

            requireActivity().runOnUiThread(() -> {
                catAdapter.clear();
                catAdapter.addAll(names);
                catAdapter.notifyDataSetChanged();
            });
        }).start();

        builder.setPositiveButton("Save", (dialog, which) -> {
            String limitStr = etLimit.getText().toString().trim();

            // إذا السبنر فاضي
            if (spinnerCategory.getAdapter() == null || spinnerCategory.getAdapter().getCount() == 0) {
                Toast.makeText(getContext(),
                        "Please add a category first from Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String category = spinnerCategory.getSelectedItem() != null
                    ? spinnerCategory.getSelectedItem().toString()
                    : null;

            if (category == null || category.trim().isEmpty() || limitStr.isEmpty()) {
                Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }

            double limit;
            try {
                limit = Double.parseDouble(limitStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid limit value", Toast.LENGTH_SHORT).show();
                return;
            }

            if (limit <= 0) {
                Toast.makeText(getContext(), "Limit must be more than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            java.util.Calendar cal = java.util.Calendar.getInstance();
            int month = cal.get(java.util.Calendar.MONTH) + 1;
            int year = cal.get(java.util.Calendar.YEAR);

            Budget budget = new Budget(category, limit, email, month, year);
            financeViewModel.addBudget(budget);

            new android.os.Handler(android.os.Looper.getMainLooper())
                    .postDelayed(() -> loadBudgets(email), 150);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}