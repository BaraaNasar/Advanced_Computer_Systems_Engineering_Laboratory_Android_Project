package com.personal.finance.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.DateValidatorPointBackward;

import com.personal.finance.R;
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.data.model.ReportRow;
import com.personal.finance.ui.adapter.ReportAdapter;


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
    private RecyclerView rvReport;
    private ReportAdapter reportAdapter;
    private TextView tvReportTitle;
    private TextView barChartTitle;



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
        tvReportTitle = view.findViewById(R.id.tvReportTitle);
        rvReport = view.findViewById(R.id.rvReport);
        barChartTitle = view.findViewById(R.id.barChartTitle);


        rvReport.setLayoutManager(new LinearLayoutManager(requireContext()));
        reportAdapter = new ReportAdapter(new ArrayList<>());
        rvReport.setAdapter(reportAdapter);


        // default selected = Expense
        toggleChartType.check(R.id.btnToggleExpense);

        toggleChartType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnToggleIncome) {
                chartType = "INCOME";
                pieChart.setCenterText("Income");
            } else {
                chartType = "EXPENSE";
                pieChart.setCenterText("Expenses");
            }
            loadData();
        });

        setupSpinner();

        // Initial load using Default Period
        String defaultPeriod = sessionManager.getDefaultPeriod();
        if (defaultPeriod != null) {
            String[] periods = getResources().getStringArray(R.array.periods_array);
            for (int i = 0; i < periods.length; i++) {
                if (periods[i].equals(defaultPeriod)) {
                    spinnerPeriod.setSelection(i);
                    break;
                }
            }
            if (defaultPeriod.equals(currentPeriod)) {
                updateDateRange(defaultPeriod);
                loadData();
            }
        } else {
            updateDateRange("Month");
            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null) {
            String defaultPeriod = sessionManager.getDefaultPeriod();
            if (defaultPeriod != null && !defaultPeriod.equals(currentPeriod)) {
                String[] periods = getResources().getStringArray(R.array.periods_array);
                for (int i = 0; i < periods.length; i++) {
                    if (periods[i].equals(defaultPeriod)) {
                        spinnerPeriod.setSelection(i);
                        break;
                    }
                }
            } else {
                // حتى لو ما تغيرت، بدنا نعمل refresh لأنه ممكن انضاف دخل/صرف
                loadData();
            }
        }
    }

    private void setupSpinner() {
        android.widget.ArrayAdapter<CharSequence> adapter =
                android.widget.ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.periods_array,
                        android.R.layout.simple_spinner_item
                );
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
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        tvDateRange.setOnClickListener(v -> {
            if ("Custom".equals(currentPeriod)) {
                showDatePicker();
            }
        });
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
                endDate = endOfDay(now); // ✅ الأسبوع حتى اليوم الحالي
                break;

            case "Month":
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startDate = startOfDay(cal.getTimeInMillis());
                endDate = endOfDay(now); // ✅ الشهر حتى اليوم الحالي
                break;

            case "Year":
                cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startDate = startOfDay(cal.getTimeInMillis());
                endDate = endOfDay(now); // ✅ السنة حتى اليوم الحالي
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
            tvDateRange.setText(sdf.format(new java.util.Date(startDate)) + " - " +
                    sdf.format(new java.util.Date(endDate)));
        }
    }

    private void showDatePicker() {
        // ✅ منع المستقبل
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setCalendarConstraints(constraints)
                .setTheme(R.style.CustomDatePickerTheme)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                startDate = startOfDay(selection.first);
                endDate = endOfDay(selection.second);

                // حماية إضافية لو صار خطأ
                long todayEnd = endOfDay(System.currentTimeMillis());
                if (endDate > todayEnd) endDate = todayEnd;
                if (startDate > endDate) startDate = startOfDay(endDate);

                currentPeriod = "Custom";
                updateDateDisplay();
                loadData();
            }
        });

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
        if (email == null) return;

        new Thread(() -> {
            double income = financeViewModel.getTotalIncomeByDate(email, startDate, endDate);
            double expense = financeViewModel.getTotalExpenseByDate(email, startDate, endDate);
            List<CategorySum> sums = financeViewModel.getCategoryGroupedSums(email, chartType, startDate, endDate);

            boolean isYearly = "Year".equals(currentPeriod);

            double[] trendData;
            if (isYearly) {
                trendData = financeViewModel.getMonthlySumsByType(email, chartType, startDate, endDate);
            } else {
                int days;
                if ("Week".equals(currentPeriod)) {
                    days = 7;
                } else {
                    long diff = (endDate - startDate) / (1000L * 60 * 60 * 24) + 1;
                    days = (int) Math.min(diff, 366); // خليه أوسع للـ Custom
                }
                trendData = financeViewModel.getDailySumsByType(email, chartType, startDate, endDate, days);
            }
            List<ReportRow> reportRows;

            switch (currentPeriod) {
                case "Day":
                    reportRows = financeViewModel.getDailyReport(email, startDate, endDate);
                    break;
                case "Week":
                    reportRows = financeViewModel.getWeeklyReport(email, startDate, endDate);
                    break;
                case "Month":
                    reportRows = financeViewModel.getDailyReport(email, startDate, endDate); // أفضل عرض شهري داخل Home كـ daily rows
                    break;
                case "Year":
                    reportRows = financeViewModel.getMonthlyReport(email, startDate, endDate);
                    break;
                case "Custom":
                default:
                    // لو المدة كبيرة اختاري monthly بدل daily
                    long daysDiff = (endDate - startDate) / (1000L * 60 * 60 * 24) + 1;
                    if (daysDiff > 60) {
                        reportRows = financeViewModel.getMonthlyReport(email, startDate, endDate);
                    } else {
                        reportRows = financeViewModel.getDailyReport(email, startDate, endDate);
                    }
                    break;
            }
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                totalIncome = income;
                totalExpense = expense;
                updateBalanceUI();
                updateChart(sums);
                updateBarChart(trendData, isYearly);
                updateReportUI(reportRows);

                TextView barChartTitle = requireView().findViewById(R.id.barChartTitle);
                if (barChartTitle != null) {
                    barChartTitle.setText(isYearly ? "Monthly Trends" : "Daily Trends");
                }



            });
        }).start();
    }

    private void updateBalanceUI() {
        tvIncome.setText(String.format(Locale.getDefault(), "$%.2f", totalIncome));
        tvExpense.setText(String.format(Locale.getDefault(), "$%.2f", totalExpense));
        tvTotalBalance.setText(String.format(Locale.getDefault(), "$%.2f", totalIncome - totalExpense));
    }

    private void updateChart(List<CategorySum> categorySums) {
        if (pieChart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        if (categorySums != null) {
            for (CategorySum sum : categorySums) {
                if (sum.getTotalAmount() > 0 && sum.getCategory() != null) {
                    entries.add(new PieEntry((float) sum.getTotalAmount(), sum.getCategory()));
                }
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText(chartType.equals("INCOME")
                    ? "No income data recorded yet"
                    : "No expense data recorded yet");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = new int[]{
                Color.parseColor("#48C67D"),
                Color.parseColor("#133A2D"),
                Color.parseColor("#36B9FF"),
                Color.parseColor("#D1FAE5"),
                Color.parseColor("#059669")
        };
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText(chartType.equals("INCOME") ? "Income" : "Expenses");
        pieChart.setCenterTextSize(16f);

        pieChart.animateY(900);
        pieChart.invalidate();
    }

    private void updateBarChart(double[] trendData, boolean isYearly) {
        if (barChart == null) return;

        // 1) Entries
        List<BarEntry> entries = new ArrayList<>();
        boolean hasData = false;
        for (int i = 0; i < trendData.length; i++) {
            float v = (float) trendData[i];
            if (v > 0f) hasData = true;
            entries.add(new BarEntry(i, v));
        }

        if (!hasData) {
            barChart.clear();
            barChart.setNoDataText("No chart data available");
            barChart.invalidate();
            return;
        }

        // 2) Data
        String label = (isYearly ? "Monthly " : "Daily ")
                + (chartType.equals("INCOME") ? "Income" : "Expenses");
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        barChart.setData(data);

        // 3) Styling
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setTextColor(getResources().getColor(R.color.text_primary));
        barChart.setFitBars(true);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_primary));
        barChart.getAxisLeft().setDrawGridLines(true);

        // 4) X Axis
        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.text_primary));
        xAxis.setAvoidFirstLastClipping(true);

        int n = trendData.length;
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(n - 0.5f);

        final long DAY_MS = 24L * 60 * 60 * 1000;

        if (isYearly) {
            String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));

            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(11.5f);
            xAxis.setLabelCount(12, true);
            xAxis.setLabelRotationAngle(-25f);

        } else if ("Week".equals(currentPeriod)) {
            final java.text.SimpleDateFormat sdfWeek =
                    new java.text.SimpleDateFormat("EEE", Locale.getDefault());

            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(6.5f);
            xAxis.setLabelCount(7, true);

            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int i = Math.round(value);
                    if (i < 0 || i > 6) return "";
                    long d = startDate + (i * DAY_MS);
                    return sdfWeek.format(new java.util.Date(d));
                }
            });

        } else if ("Month".equals(currentPeriod)) {
            final int step = 5;

            xAxis.setLabelCount(Math.max(2, n / step), false);
            xAxis.setLabelRotationAngle(-25f);

            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int i = Math.round(value);
                    if (i < 0 || i >= n) return "";
                    int day = i + 1;           // D1...
                    if (day == 1 || day % step == 0) {
                        return "D" + day;
                    }
                    return "";
                }
            });

        } else {
            int step;
            if (n <= 15) step = 1;
            else if (n <= 30) step = 2;
            else if (n <= 60) step = 5;
            else if (n <= 120) step = 10;
            else if (n <= 200) step = 15;
            else step = 30;

            xAxis.setLabelCount(Math.max(2, n / step), false);
            xAxis.setLabelRotationAngle(-25f);

            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int i = Math.round(value);
                    if (i < 0 || i >= n) return "";
                    int day = i + 1;
                    if (day == 1 || day % step == 0) return "D" + day;
                    return "";
                }
            });
        }

        barChart.animateY(900);
        barChart.invalidate();
    }
    private void updateReportUI(List<ReportRow> rows) {
        if (tvReportTitle != null) {
            String title;
            switch (currentPeriod) {
                case "Day": title = "Detailed Report (Today)"; break;
                case "Week": title = "Detailed Report (This Week)"; break;
                case "Month": title = "Detailed Report (This Month)"; break;
                case "Year": title = "Detailed Report (This Year)"; break;
                case "Custom": title = "Detailed Report (Custom)"; break;
                default: title = "Detailed Report";
            }
            tvReportTitle.setText(title);
        }

        if (reportAdapter != null) {
            reportAdapter.submit(rows); // رح نعملها بالـAdapter
        }
    }

}