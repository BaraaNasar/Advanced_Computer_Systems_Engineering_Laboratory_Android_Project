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

        // Show ALL transactions
        financeViewModel.getTransactions(email).observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions);
        });

        // Hide FAB as this is just for viewing history, or allow adding from here?
        // Let's keep it simple and hide FAB or show it and default to Expense.
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
                        .setPositiveButton("Delete", (dialog, which) -> financeViewModel.deleteTransaction(transaction))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void showEditDialog(String email, Transaction existingTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Transaction");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText etDescription = view.findViewById(R.id.etDescription);

        etAmount.setText(String.valueOf(existingTransaction.amount));
        etDescription.setText(existingTransaction.description);

        // Populate Spinner based on type
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        financeViewModel.getCategoriesByType(email, existingTransaction.type).observe(getViewLifecycleOwner(),
                categories -> {
                    adapter.clear();
                    int selectedIndex = 0;
                    for (int i = 0; i < categories.size(); i++) {
                        com.personal.finance.data.model.Category c = categories.get(i);
                        adapter.add(c.name);
                        if (c.name.equals(existingTransaction.category)) {
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
                existingTransaction.amount = Double.parseDouble(amountStr);
                existingTransaction.category = category;
                existingTransaction.description = description;
                financeViewModel.updateTransaction(existingTransaction);
            } else {
                Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
