package com.personal.finance.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.R;
import com.personal.finance.data.model.Budget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgets = new ArrayList<>();
    private Map<Long, Double> spentAmounts = new HashMap<>();

    private OnItemClickListener listener;

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        double limit = budget.getLimitAmount();
        double spent = spentAmounts.getOrDefault(budget.getId(), 0.0);
        double remaining = limit - spent;
        int percentage = (limit <= 0) ? 0 : (int) ((spent / limit) * 100);

        // Set basic info
        holder.tvCategory.setText(budget.getCategory());
        holder.tvLimit.setText(String.format(Locale.getDefault(), "$%.2f", limit));
        holder.tvSpent.setText(String.format(Locale.getDefault(), "$%.2f", spent));
        holder.tvRemaining.setText(String.format(Locale.getDefault(), "$%.2f", Math.max(0, remaining)));
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%d%% used", Math.min(100, percentage)));

        // Set progress bar
        holder.progressBar.setProgress(Math.min(100, percentage));

        // Color coding based on percentage
        int progressColor;
        if (percentage < 50) {
            // Green: Under 50%
            progressColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_income);
        } else if (percentage < 90) {
            // Yellow/Orange: 50-90%
            progressColor = Color.parseColor("#FFA726"); // Orange
        } else {
            // Red: Over 90%
            progressColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_expense);
        }
        holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(progressColor));

        // Show alert icon if >= 50%
        if (percentage >= 50) {
            holder.ivAlert.setVisibility(View.VISIBLE);
            if (percentage >= 100) {
                // Red alert for exceeded budget
                holder.ivAlert
                        .setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_expense));
            } else {
                // Orange alert for 50%+
                holder.ivAlert.setColorFilter(Color.parseColor("#FFA726"));
            }
        } else {
            holder.ivAlert.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    public void setSpentAmounts(Map<Long, Double> spentAmounts) {
        this.spentAmounts = spentAmounts;
        notifyDataSetChanged();
    }

    public Budget getBudgetAt(int position) {
        return budgets.get(position);
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvLimit, tvSpent, tvRemaining, tvPercentage;
        ProgressBar progressBar;
        ImageView ivAlert;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvLimit = itemView.findViewById(R.id.tvBudgetLimit);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            tvPercentage = itemView.findViewById(R.id.tvBudgetPercentage);
            progressBar = itemView.findViewById(R.id.progressBudget);
            ivAlert = itemView.findViewById(R.id.ivBudgetAlert);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(budgets.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Budget budget);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
