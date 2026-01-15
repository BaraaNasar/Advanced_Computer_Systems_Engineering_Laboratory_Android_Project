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

public class TransactionListFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;

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
        recyclerView.setHasFixedSize(true);

        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        loadTransactions(email);

        view.findViewById(R.id.fabAdd).setVisibility(View.GONE);

        adapter.setOnActionClickListener(new TransactionAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Transaction transaction) {
                showEditDialog(email, transaction);
            }

            @Override
            public void onDelete(Transaction transaction) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            financeViewModel.deleteTransaction(transaction);
                            loadTransactions(email);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void loadTransactions(String email) {
        if (email == null) return;

        new Thread(() -> {
            List<Transaction> list = financeViewModel.getTransactionsList(email);
            requireActivity().runOnUiThread(() -> adapter.setTransactions(list));
        }).start();
    }

    private long selectedDateTimestamp = System.currentTimeMillis();

    private void showEditDialog(String email, Transaction existingTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Transaction");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText etDescription = view.findViewById(R.id.etDescription);
        android.widget.TextView tvDate = view.findViewById(R.id.tvTransactionDate);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());

        // Fill existing values
        etAmount.setText(String.valueOf(existingTransaction.getAmount()));
        etDescription.setText(existingTransaction.getDescription());

        selectedDateTimestamp = existingTransaction.getDate();
        tvDate.setText(sdf.format(new java.util.Date(selectedDateTimestamp)));

        // Date picker with maxDate = today (no future)
        tvDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(selectedDateTimestamp);

            android.app.DatePickerDialog dp = new android.app.DatePickerDialog(
                    requireContext(),
                    (picker, year, month, dayOfMonth) -> {
                        java.util.Calendar newCal = java.util.Calendar.getInstance();
                        newCal.set(year, month, dayOfMonth, 0, 0, 0);
                        newCal.set(java.util.Calendar.MILLISECOND, 0);
                        selectedDateTimestamp = newCal.getTimeInMillis();
                        tvDate.setText(sdf.format(new java.util.Date(selectedDateTimestamp)));
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
            );

            dp.getDatePicker().setMaxDate(System.currentTimeMillis());
            dp.show();
        });

        // Spinner adapter
        android.widget.ArrayAdapter<String> catAdapter =
                new android.widget.ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, new java.util.ArrayList<>());
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Load categories in background
        new Thread(() -> {
            String tType = existingTransaction.getType();
            if (tType == null) tType = "EXPENSE";

            List<com.personal.finance.data.model.Category> categories =
                    financeViewModel.getCategoriesByType(email, tType);

            List<String> names = new java.util.ArrayList<>();
            int selectedIndex = 0;

            for (int i = 0; i < categories.size(); i++) {
                String name = categories.get(i).getName();
                names.add(name);
                if (name.equals(existingTransaction.getCategory())) {
                    selectedIndex = i;
                }
            }

            int finalSelectedIndex = selectedIndex;
            requireActivity().runOnUiThread(() -> {
                catAdapter.clear();
                catAdapter.addAll(names);
                catAdapter.notifyDataSetChanged();
                if (!names.isEmpty()) spinnerCategory.setSelection(finalSelectedIndex);
            });
        }).start();

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            // 1) categories empty
            if (spinnerCategory.getAdapter() == null || spinnerCategory.getAdapter().getCount() == 0) {
                Toast.makeText(getContext(),
                        "Please add a category first from Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // 2) category null/empty
            String category = spinnerCategory.getSelectedItem() != null
                    ? spinnerCategory.getSelectedItem().toString()
                    : null;

            if (category == null || category.trim().isEmpty()) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) amount validation
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

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

            // 4) save
            existingTransaction.setAmount(amount);
            existingTransaction.setCategory(category);
            existingTransaction.setDescription(etDescription.getText().toString().trim());
            existingTransaction.setDate(selectedDateTimestamp);

            financeViewModel.updateTransaction(existingTransaction);
            loadTransactions(email);

            dialog.dismiss();
        });
    }
}