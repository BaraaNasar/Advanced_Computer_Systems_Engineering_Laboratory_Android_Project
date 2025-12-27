package com.personal.finance.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.personal.finance.R;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private SessionManager sessionManager;
    private TextView tvTotalBalance, tvIncome, tvExpense, tvDateRange;
    private PieChart pieChart;
    private Spinner spinnerPeriod;

    private double totalIncome = 0;
    private double totalExpense = 0;
    private long startDate, endDate;
    private String currentPeriod = "Month"; // Default

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvIncome = view.findViewById(R.id.tvTotalIncome);
        tvExpense = view.findViewById(R.id.tvTotalExpense);
        tvDateRange = view.findViewById(R.id.tvDateRange);
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod);
        pieChart = view.findViewById(R.id.pieChart);

        setupSpinner();

        // Initial load using Default Period
        String defaultPeriod = sessionManager.getDefaultPeriod();
        if (defaultPeriod != null) {
            // Find index. Note: adapter is set in setupSpinner so we need to move this
            // logic or access adapter
            // Actually, setupSpinner is async/listener based, but setting selection
            // triggers listener?
            // Listener checks if !selected.equals(currentPeriod).
            // Let's rely on listener or manually call.
            // Better: Set selection on spinner, and let listener handle it if we want.
            // But valid pointer: Adapter needs to be accessible or finding index.
            // We know the array resource.
            String[] periods = getResources().getStringArray(R.array.periods_array);
            for (int i = 0; i < periods.length; i++) {
                if (periods[i].equals(defaultPeriod)) {
                    spinnerPeriod.setSelection(i);
                    break;
                }
            }
            // Whatever happens, let's force update if needed or let listener do it.
            // Listener calls loadData() if selection changes.
            // If selection doesn't change (e.g. Month is default and 0 is Month), listener
            // might not fire if we just set adapter.
            // Actually, setAdapter defaults to 0.
            // We should probably just call updateDateRange manually if we want to be safe,
            // or make sure listener fires.
            // Safest: set selection. If it's same as 0, listener might not fire?
            // Let's just update manually if it matches currentPeriod (which is "Month"
            // init).
            if (defaultPeriod.equals(currentPeriod)) {
                updateDateRange(defaultPeriod);
                loadData();
            }
        } else {
            updateDateRange("Month");
            loadData();
        }
    }

    private void setupSpinner() {
        android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(
                requireContext(),
                R.array.periods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);

        spinnerPeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals(currentPeriod) || startDate == 0) {
                    currentPeriod = selected;
                    if (selected.equals("Custom")) {
                        showDatePicker();
                    } else {
                        updateDateRange(selected);
                        loadData();
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        tvDateRange.setOnClickListener(v -> showDatePicker());
    }

    private void updateDateRange(String period) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // Reset to end of today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        endDate = calendar.getTimeInMillis();

        // Calculate start date
        calendar = java.util.Calendar.getInstance(); // Reset
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);

        switch (period) {
            case "Day":
                // Start is beginning of today
                break;
            case "Week":
                calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            case "Month":
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case "Custom":
                // Don't change if already set, or handled by picker
                return;
        }
        startDate = calendar.getTimeInMillis();
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDateRange
                .setText(sdf.format(new java.util.Date(startDate)) + " - " + sdf.format(new java.util.Date(endDate)));
    }

    private void showDatePicker() {
        // Simple implementation: Just pick start date, then maybe assume end is today
        // or pick end date.
        // For simplicity, let's pick Single date or Range.
        // Let's implement basic logic: Pick Start Date, then assume 7 days or ask for
        // End Date.
        // Actually, let's just use MaterialDatePicker if available, or simple
        // DatePickerDialog for Start Date.

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            java.util.Calendar startCal = java.util.Calendar.getInstance();
            startCal.set(year, month, dayOfMonth, 0, 0, 0);
            startDate = startCal.getTimeInMillis();

            // For End Date, we could show another dialog.
            // For now, let's just default End Date to Today if start is before today, or
            // same day.
            java.util.Calendar endCal = java.util.Calendar.getInstance();
            endCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            endCal.set(java.util.Calendar.MINUTE, 59);

            if (endCal.getTimeInMillis() < startDate) {
                endCal.setTimeInMillis(startDate);
                endCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                endCal.set(java.util.Calendar.MINUTE, 59);
            }
            endDate = endCal.getTimeInMillis();

            spinnerPeriod.setSelection(3); // Custom
            updateDateDisplay();
            loadData();

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void loadData() {
        String email = sessionManager.getUserEmail();

        // Remove observers to avoid duplicates if we were to re-attach (simplification)
        // In real app, we might switch LiveData source.
        // Here, simpler to just observe newly.

        financeViewModel.getTotalIncomeByDate(email, startDate, endDate).observe(getViewLifecycleOwner(), income -> {
            totalIncome = income != null ? income : 0.0;
            updateBalanceUI();
        });

        financeViewModel.getTotalExpenseByDate(email, startDate, endDate).observe(getViewLifecycleOwner(), expense -> {
            totalExpense = expense != null ? expense : 0.0;
            updateBalanceUI();
        });

        financeViewModel.getCategoryGroupedSums(email, "EXPENSE", startDate, endDate).observe(getViewLifecycleOwner(),
                this::updateChart);
    }

    private void updateBalanceUI() {
        tvIncome.setText(String.format(Locale.getDefault(), "$%.2f", totalIncome));
        tvExpense.setText(String.format(Locale.getDefault(), "$%.2f", totalExpense));
        tvTotalBalance.setText(String.format(Locale.getDefault(), "$%.2f", totalIncome - totalExpense));
    }

    private void updateChart(List<com.personal.finance.data.model.CategorySum> categorySums) {
        if (pieChart == null)
            return;

        List<PieEntry> entries = new ArrayList<>();
        if (categorySums != null) {
            for (com.personal.finance.data.model.CategorySum sum : categorySums) {
                if (sum.totalAmount > 0)
                    entries.add(new PieEntry((float) sum.totalAmount, sum.category));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data recorded yet");
            pieChart.setNoDataTextColor(getResources().getColor(R.color.text_secondary));
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Premium Emerald Color Palette for Chart
        int[] colors = new int[] {
                Color.parseColor("#48C67D"), // Emerald
                Color.parseColor("#133A2D"), // Forest
                Color.parseColor("#36B9FF"), // Cyan
                Color.parseColor("#D1FAE5"), // Mint
                Color.parseColor("#059669") // Deep Emerald
        };
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend()
                .setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setHorizontalAlignment(
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextColor(getResources().getColor(R.color.text_primary));
        pieChart.setCenterTextSize(16f);

        pieChart.animateY(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }
}
