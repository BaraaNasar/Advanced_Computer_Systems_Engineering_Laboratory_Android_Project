package com.personal.finance.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.R;
import com.personal.finance.data.model.ReportRow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    private final List<ReportRow> items = new ArrayList<>();

    public ReportAdapter(List<ReportRow> initial) {
        if (initial != null)
            items.addAll(initial);
    }

    public void submit(List<ReportRow> rows) {
        items.clear();
        if (rows != null)
            items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReportRow r = items.get(position);

        String label = r.getLabel();
        // format week key to date range if needed
        if (isWeekKey(label)) {
            label = formatWeekKeyToRange(label);
        }

        h.tvLabel.setText(label);
        h.tvIncome.setText(String.format(Locale.getDefault(), "Income: $%.2f", r.getIncome()));
        h.tvExpense.setText(String.format(Locale.getDefault(), "Expense: $%.2f", r.getExpense()));
        h.tvBalance.setText(String.format(Locale.getDefault(), "Balance: $%.2f", r.getBalance()));
    }

    private boolean isWeekKey(String label) {
        // week key like "2026-02"
        return label != null && label.matches("\\d{4}-\\d{2}");
    }

    private String formatWeekKeyToRange(String weekKey) {
        try {
            String[] parts = weekKey.split("-");
            int year = Integer.parseInt(parts[0]);
            int week = Integer.parseInt(parts[1]);

            Calendar cal = Calendar.getInstance(Locale.getDefault());

            // week starts Monday
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);

            // move to first day
            cal.clear();
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.WEEK_OF_YEAR, week);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            Date start = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 6);
            Date end = cal.getTime();

            SimpleDateFormat sdfStart = new SimpleDateFormat("MMM d", Locale.getDefault());
            SimpleDateFormat sdfEnd = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

            return sdfStart.format(start) + " - " + sdfEnd.format(end);

        } catch (Exception e) {
            return weekKey; // fallback
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLabel, tvIncome, tvExpense, tvBalance;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}