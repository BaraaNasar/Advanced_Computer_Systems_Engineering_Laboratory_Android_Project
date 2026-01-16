package com.personal.finance.ui;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;

    private long selectedDateTimestamp = System.currentTimeMillis();

    @Override
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

        loadExpenses(email);

        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog(email, null));

        adapter.setOnActionClickListener(new TransactionAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Transaction transaction) {
                showAddDialog(email, transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            financeViewModel.deleteTransaction(transaction);
                            loadExpenses(email); // refresh
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void loadExpenses(String email) {
        if (email == null)
            return;

        new Thread(() -> {
            List<Transaction> expenses = financeViewModel.getExpenses(email);
            requireActivity().runOnUiThread(() -> adapter.setTransactions(expenses));
        }).start();
    }

    private void showAddDialog(String email, @Nullable Transaction existingTransaction) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(existingTransaction == null ? "Add Expense" : "Edit Expense");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        android.widget.TextView tvDate = dialogView.findViewById(R.id.tvTransactionDate);
        com.google.android.material.card.MaterialCardView cardCategory = dialogView.findViewById(R.id.cardCategory);

        // click card to open spinner
        cardCategory.setOnClickListener(v -> spinnerCategory.performClick());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (existingTransaction != null) {
            etAmount.setText(String.valueOf(existingTransaction.getAmount()));
            etDescription.setText(existingTransaction.getDescription());
            selectedDateTimestamp = existingTransaction.getDate();
        } else {
            selectedDateTimestamp = System.currentTimeMillis();
        }
        tvDate.setText(sdf.format(new Date(selectedDateTimestamp)));

        com.google.android.material.card.MaterialCardView cardDate = dialogView.findViewById(R.id.cardDate);
        View.OnClickListener dateClickListener = v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(selectedDateTimestamp);
            new android.app.DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                java.util.Calendar newCal = java.util.Calendar.getInstance();
                newCal.set(year, month, dayOfMonth, 0, 0, 0);
                selectedDateTimestamp = newCal.getTimeInMillis();
                tvDate.setText(sdf.format(new Date(selectedDateTimestamp)));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        };

        tvDate.setOnClickListener(dateClickListener);
        cardDate.setOnClickListener(dateClickListener);

        // setup category spinner
        List<com.personal.finance.data.model.Category> categoryList = new ArrayList<>();
        android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        new Thread(() -> {
            List<com.personal.finance.data.model.Category> categories = financeViewModel.getCategoriesByType(email,
                    "EXPENSE");

            categoryList.clear();
            categoryList.addAll(categories);

            int selectedIndex = 0;
            List<String> names = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                String name = categories.get(i).getName();
                names.add(name);
                if (existingTransaction != null && name.equals(existingTransaction.getCategory())) {
                    selectedIndex = i;
                }
            }

            int finalSelectedIndex = selectedIndex;
            requireActivity().runOnUiThread(() -> {
                catAdapter.clear();
                catAdapter.addAll(names);
                catAdapter.notifyDataSetChanged();
                if (!names.isEmpty())
                    spinnerCategory.setSelection(finalSelectedIndex);
            });
        }).start();

        builder.setPositiveButton("Save", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            String category = spinnerCategory.getSelectedItem() != null
                    ? spinnerCategory.getSelectedItem().toString()
                    : null;

            if (!amountStr.isEmpty() && category != null) {
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (amount <= 0) {
                    Toast.makeText(getContext(), "Amount must be more than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (existingTransaction == null) {
                    Transaction t = new Transaction(
                            amount, selectedDateTimestamp, category, description, "EXPENSE", email);
                    financeViewModel.addTransaction(t);
                    checkBudgetAfterAdd(email, category, selectedDateTimestamp);
                } else {
                    existingTransaction.setAmount(amount);
                    existingTransaction.setCategory(category);
                    existingTransaction.setDescription(description);
                    existingTransaction.setDate(selectedDateTimestamp);
                    financeViewModel.updateTransaction(existingTransaction);
                    checkBudgetAfterAdd(email, category, selectedDateTimestamp);
                }

                loadExpenses(email); // refresh
            } else {
                Toast.makeText(getContext(), "Invalid input or missing category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkBudgetAfterAdd(String email, String category, long timestamp) {
        new Thread(() -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            int month = cal.get(java.util.Calendar.MONTH);
            int year = cal.get(java.util.Calendar.YEAR);

            String msg = financeViewModel.getBudgetAlertMessage(email, category, month, year);
            if (msg != null) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}