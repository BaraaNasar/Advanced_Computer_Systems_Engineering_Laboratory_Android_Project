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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.personal.finance.R;
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.sqlite.TransactionDb;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private SessionManager sessionManager;
    private TextView tvTotalBalance, tvIncome, tvExpense, tvDateRange;
    private PieChart pieChart;
    private BarChart barChart;
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
        barChart = view.findViewById(R.id.barChart);

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
                    if (selected.equals("Custom")) {
                        showDatePicker();
                    } else {
                        currentPeriod = selected;
                        updateDateRange(selected);
                        loadData();
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Make date range clickable to re-open picker when in Custom mode
        tvDateRange.setOnClickListener(v -> {
            if ("Custom".equals(currentPeriod)) {
                showDatePicker();
            }
        });
    }

    private void updateDateRange(String period) {
        long now = System.currentTimeMillis();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);

        switch (period) {
            case "Day":
                startDate = startOfDay(now);
                endDate = endOfDay(now);
                break;

            case "Week":
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = startOfDay(cal.getTimeInMillis());
                cal.add(java.util.Calendar.DAY_OF_YEAR, 6);
                endDate = endOfDay(cal.getTimeInMillis());
                break;

            case "Month":
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startDate = startOfDay(cal.getTimeInMillis());
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                endDate = endOfDay(cal.getTimeInMillis());
                break;

            case "Year":
                cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startDate = startOfDay(cal.getTimeInMillis());
                cal.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 31);
                endDate = endOfDay(cal.getTimeInMillis());
                break;

            case "Custom":
                return;
        }

        updateDateDisplay();
    }

    private void updateDateDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if ("Day".equals(currentPeriod)) {
            tvDateRange.setText(sdf.format(new java.util.Date(startDate)));
        } else {
            tvDateRange.setText(
                    sdf.format(new java.util.Date(startDate)) + " - " + sdf.format(new java.util.Date(endDate)));
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(new Pair<>(startDate, endDate))
                .setTheme(R.style.CustomDatePickerTheme)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                // MaterialDatePicker returns UTC. Convert to local start/end of day.
                startDate = startOfDay(selection.first);
                endDate = endOfDay(selection.second);
                currentPeriod = "Custom";

                updateDateDisplay();
                loadData();
            }
        });

        // Revert spinner if cancelled to keep UI in sync
        picker.addOnCancelListener(dialog -> revertSpinner());
        picker.addOnNegativeButtonClickListener(v -> revertSpinner());

        picker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void revertSpinner() {
        String[] periods = getResources().getStringArray(R.array.periods_array);
        for (int i = 0; i < periods.length; i++) {
            if (periods[i].equals(currentPeriod) && !currentPeriod.equals("Custom")) {
                spinnerPeriod.setSelection(i);
                break;
            }
        }
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

            double[] trendData;
            boolean isYearly = "Year".equals(currentPeriod);

            if (isYearly) {
                trendData = financeViewModel.getMonthlySumsByType(email, chartType, startDate, endDate);
            } else {
                long diff = (endDate - startDate) / (1000 * 60 * 60 * 24) + 1;
                int days = (int) Math.min(diff, 31); // Cap at 31
                trendData = financeViewModel.getDailySumsByType(email, chartType, startDate, endDate, days);
            }

            requireActivity().runOnUiThread(() -> {
                totalIncome = income;
                totalExpense = expense;
                updateBalanceUI();
                updateChart(sums);
                updateBarChart(trendData, isYearly);
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
        dataSet.setValueTextSize(16f); // Larger text on slices
        dataSet.setValueTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        // Show only numbers on slices, not category names
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false); // Don't draw category names on slices - only show numbers!

        // Configure legend to show category names below the chart
        com.github.mikephil.charting.components.Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(14f); // Larger legend text
        legend.setTextColor(getResources().getColor(R.color.text_primary)); // Better visibility in Dark Mode
        legend.setFormSize(12f); // Larger color indicators
        legend.setXEntrySpace(12f); // More spacing between legend items
        legend.setYEntrySpace(8f);
        legend.setWordWrapEnabled(true);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText(chartType.equals("INCOME") ? "Income" : "Expenses");
        pieChart.setCenterTextColor(getResources().getColor(R.color.text_primary));
        pieChart.setCenterTextSize(16f);
        pieChart.setExtraBottomOffset(10f); // Extra space for legend

        pieChart.animateY(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void updateBarChart(double[] trendData, boolean isYearly) {
        if (barChart == null)
            return;

        List<BarEntry> entries = new ArrayList<>();
        boolean hasData = false;
        for (int i = 0; i < trendData.length; i++) {
            if (trendData[i] > 0)
                hasData = true;
            entries.add(new BarEntry(i, (float) trendData[i]));
        }

        if (!hasData) {
            barChart.clear();
            barChart.setNoDataText("No trend data for this period");
            barChart.setNoDataTextColor(getResources().getColor(R.color.text_secondary));
            barChart.invalidate();
            return;
        }

        String label = (isYearly ? "Monthly " : "Daily ") + (chartType.equals("INCOME") ? "Income" : "Expenses");
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(Color.parseColor("#48C67D")); // Emerald primary
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setTextColor(getResources().getColor(R.color.text_primary));

        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        if (isYearly) {
            String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
                    "Nov", "Dec" };
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
            xAxis.setLabelCount(12, true); // Force all 12 labels to show!!
        } else {
            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "D" + ((int) value + 1);
                }
            });
            xAxis.setLabelCount(Math.min(trendData.length, 7), false);
        }

        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.text_primary));

        // Y-Axis
        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_primary));
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.LTGRAY);

        // Update card title if needed (lookup by ID)
        View card = getView().findViewById(R.id.cardBarChart);
        if (card != null) {
            TextView title = card.findViewById(R.id.barChartTitle);
            if (title != null) {
                title.setText(isYearly ? "Monthly Trends" : "Daily Trends");
            }
        }

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
