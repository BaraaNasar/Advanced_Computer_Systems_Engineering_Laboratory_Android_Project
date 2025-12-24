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

public class BudgetFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private BudgetAdapter adapter;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getUserEmail();

        RecyclerView recyclerView = view.findViewById(R.id.rvBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter();
        recyclerView.setAdapter(adapter);

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        financeViewModel.getBudgets(email).observe(getViewLifecycleOwner(), budgets -> {
            adapter.setBudgets(budgets);
            // Here we could also check logic for alerts if spending > budget
        });

        view.findViewById(R.id.fabAddBudget).setOnClickListener(v -> showAddDialog(email));
    }

    private void showAddDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Budget");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(view);

        EditText etCategory = view.findViewById(R.id.etBudgetCategory);
        EditText etLimit = view.findViewById(R.id.etBudgetLimit);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String category = etCategory.getText().toString();
            String limitStr = etLimit.getText().toString();

            if (!category.isEmpty() && !limitStr.isEmpty()) {
                double limit = Double.parseDouble(limitStr);
                Budget budget = new Budget(category, limit, email);
                financeViewModel.addBudget(budget);
            } else {
                Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
