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
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.sqlite.TransactionDb;
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
    private com.google.android.material.button.MaterialButtonToggleGroup toggleChartType;
    private String chartType = "EXPENSE"; // default

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

        toggleChartType = view.findViewById(R.id.toggleChartType);

        // default selected = Expense
        toggleChartType.check(R.id.btnToggleExpense);

        toggleChartType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked)
                return;

            if (checkedId == R.id.btnToggleIncome) {
                chartType = "INCOME";
                pieChart.setCenterText("Income");
            } else {
                chartType = "EXPENSE";
                pieChart.setCenterText("Expenses");
            }
            loadData(); // refresh chart for selected type
        });

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

    private long startOfDay(long millis) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long endOfDay(long millis) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if default period changed while we were away (e.g. in Settings)
        if (sessionManager != null) {
            String defaultPeriod = sessionManager.getDefaultPeriod();
            // If current period is different from default, but the user hasn't explicitly
            // changed it
            // (or to simply enforce default on resume per requirement), we can update it.
            // Best logic: If the stored default period is different from what we are
            // showing, update it.
            if (defaultPeriod != null && !defaultPeriod.equals(currentPeriod)) {
                // Find index and select
                String[] periods = getResources().getStringArray(R.array.periods_array);
                for (int i = 0; i < periods.length; i++) {
                    if (periods[i].equals(defaultPeriod)) {
                        spinnerPeriod.setSelection(i);
                        break;
                    }
                }
                // Update logic will be handled by OnItemSelectedListener
            }
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
        long now = System.currentTimeMillis();
        endDate = endOfDay(now);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);

        switch (period) {
            case "Day":
                startDate = startOfDay(now);
                break;

            case "Week":
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = startOfDay(cal.getTimeInMillis());
                break;

            case "Month":
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startDate = startOfDay(cal.getTimeInMillis());
                break;

            case "Custom":
                return;
        }

        updateDateDisplay();
    }

    private void updateDateDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDateRange
                .setText(sdf.format(new java.util.Date(startDate)) + " - " + sdf.format(new java.util.Date(endDate)));
    }

    private void showDatePicker() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        final long maxDate = endOfDay(System.currentTimeMillis()); // آخر لحظة باليوم الحالي

        // Pick START
        android.app.DatePickerDialog startDialog = new android.app.DatePickerDialog(requireContext(),
                (v1, y1, m1, d1) -> {

                    java.util.Calendar startCal = java.util.Calendar.getInstance();
                    startCal.set(y1, m1, d1, 0, 0, 0);
                    startCal.set(java.util.Calendar.MILLISECOND, 0);
                    final long startPicked = startCal.getTimeInMillis();

                    // Pick END
                    java.util.Calendar now2 = java.util.Calendar.getInstance();
                    android.app.DatePickerDialog endDialog = new android.app.DatePickerDialog(requireContext(),
                            (v2, y2, m2, d2) -> {

                                java.util.Calendar endCal = java.util.Calendar.getInstance();
                                endCal.set(y2, m2, d2, 23, 59, 59);
                                endCal.set(java.util.Calendar.MILLISECOND, 999);
                                final long endPicked = endCal.getTimeInMillis();

                                // normalize order
                                final long normStart = Math.min(startPicked, endPicked);
                                final long normEnd = Math.max(startPicked, endPicked);

                                startDate = startOfDay(normStart);
                                endDate = endOfDay(normEnd);

                                // set spinner to Custom
                                String[] periods = getResources().getStringArray(R.array.periods_array);
                                for (int i = 0; i < periods.length; i++) {
                                    if ("Custom".equals(periods[i])) {
                                        spinnerPeriod.setSelection(i);
                                        break;
                                    }
                                }

                                updateDateDisplay();
                                loadData();

                            }, now2.get(java.util.Calendar.YEAR), now2.get(java.util.Calendar.MONTH),
                            now2.get(java.util.Calendar.DAY_OF_MONTH));

                    endDialog.getDatePicker().setMaxDate(maxDate);
                    endDialog.show();

                }, now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH),
                now.get(java.util.Calendar.DAY_OF_MONTH));

        startDialog.getDatePicker().setMaxDate(maxDate);
        startDialog.show();
    }

    private void loadData() {
        String email = sessionManager.getUserEmail();
        if (email == null)
            return;

        TransactionDb txDb = new TransactionDb(requireContext());

        new Thread(() -> {
            double income = txDb.getTotalIncomeByDate(email, startDate, endDate);
            double expense = txDb.getTotalExpenseByDate(email, startDate, endDate);
            List<CategorySum> sums = txDb.getCategoryGroupedSums(email, chartType, startDate, endDate);

            requireActivity().runOnUiThread(() -> {
                totalIncome = income;
                totalExpense = expense;
                updateBalanceUI();
                updateChart(sums);
            });
        }).start();
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
                if (sum.getTotalAmount() > 0)
                    entries.add(new PieEntry((float) sum.getTotalAmount(), sum.getCategory()));

            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText(chartType.equals("INCOME")
                    ? "No income data recorded yet"
                    : "No expense data recorded yet");

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
