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

        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog(email));
    }

    private void showAddDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Income");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory); // Changed
        EditText etDescription = view.findViewById(R.id.etDescription);

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
            for (com.personal.finance.data.model.Category c : categories) {
                adapter.add(c.name);
            }
            adapter.notifyDataSetChanged();
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
                Transaction transaction = new Transaction(
                        amount, System.currentTimeMillis(), category, description, "INCOME", email);
                financeViewModel.addTransaction(transaction);
            } else {
                Toast.makeText(getContext(), "Invalid Input or missing category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
