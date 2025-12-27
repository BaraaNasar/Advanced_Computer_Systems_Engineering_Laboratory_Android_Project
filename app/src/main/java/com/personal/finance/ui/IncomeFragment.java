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
import com.personal.finance.data.model.Transaction;
import com.personal.finance.ui.adapter.TransactionAdapter;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class IncomeFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getUserEmail();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        // Filter for Income only: We need to filter the list from the general
        // observation or add a specific query
        // The ViewModel has getTransactions() returning all.
        // Ideally we should add getIncomes() to Repo/ViewModel, but for "A-Z"
        // completeness let's do it right.
        // Wait, I didn't add getIncomes() to ViewModel but I added it to DAO.
        // I'll filter manually here for simplicity as I can't edit ViewModel easily
        // without re-writing.
        // Actually, I should probably update ViewModel or just filter client side.
        // Client side filtering is fine for this scale.

        financeViewModel.getTransactions(email).observe(getViewLifecycleOwner(), transactions -> {
            List<Transaction> incomes = new ArrayList<>();
            for (Transaction t : transactions) {
                if ("INCOME".equals(t.type)) {
                    incomes.add(t);
                }
            }
            adapter.setTransactions(incomes);
        });

        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog(email, null));

        adapter.setOnActionClickListener(new TransactionAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Transaction transaction) {
                showAddDialog(email, transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this income?")
                        .setPositiveButton("Delete", (dialog, which) -> financeViewModel.deleteTransaction(transaction))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private long selectedDateTimestamp = System.currentTimeMillis();

    private void showAddDialog(String email, @Nullable Transaction existingTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(existingTransaction == null ? "Add Income" : "Edit Income");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText etDescription = view.findViewById(R.id.etDescription);
        android.widget.TextView tvDate = view.findViewById(R.id.tvTransactionDate);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());

        if (existingTransaction != null) {
            etAmount.setText(String.valueOf(existingTransaction.amount));
            etDescription.setText(existingTransaction.description);
            selectedDateTimestamp = existingTransaction.date;
        } else {
            selectedDateTimestamp = System.currentTimeMillis();
        }
        tvDate.setText(sdf.format(new java.util.Date(selectedDateTimestamp)));

        tvDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(selectedDateTimestamp);
            new android.app.DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                java.util.Calendar newCal = java.util.Calendar.getInstance();
                newCal.set(year, month, dayOfMonth);
                selectedDateTimestamp = newCal.getTimeInMillis();
                tvDate.setText(sdf.format(new java.util.Date(selectedDateTimestamp)));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        // Populate Spinner
        List<com.personal.finance.data.model.Category> categoryList = new ArrayList<>();
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        financeViewModel.getCategoriesByType(email, "INCOME").observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            adapter.clear();
            int selectedIndex = 0;
            for (int i = 0; i < categories.size(); i++) {
                com.personal.finance.data.model.Category c = categories.get(i);
                adapter.add(c.name);
                if (existingTransaction != null && c.name.equals(existingTransaction.category)) {
                    selectedIndex = i;
                }
            }
            adapter.notifyDataSetChanged();
            spinnerCategory.setSelection(selectedIndex);
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String amountStr = etAmount.getText().toString();
            String description = etDescription.getText().toString();

            String category = null;
            if (spinnerCategory.getSelectedItem() != null) {
                category = spinnerCategory.getSelectedItem().toString();
            }

            if (!amountStr.isEmpty() && category != null) {
                double amount = Double.parseDouble(amountStr);
                if (existingTransaction == null) {
                    Transaction transaction = new Transaction(
                            amount, selectedDateTimestamp, category, description, "INCOME", email);
                    financeViewModel.addTransaction(transaction);
                } else {
                    existingTransaction.amount = amount;
                    existingTransaction.category = category;
                    existingTransaction.description = description;
                    existingTransaction.date = selectedDateTimestamp;
                    financeViewModel.updateTransaction(existingTransaction);
                }
            } else {
                Toast.makeText(getContext(), "Invalid Input or missing category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
