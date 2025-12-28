package com.personal.finance.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.finance.R;
import com.personal.finance.data.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private OnActionClickListener actionListener;
    private OnItemClickListener listener;

    public interface OnActionClickListener {
        void onEdit(Transaction transaction);
        void onDelete(Transaction transaction);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.tvCategory.setText(nullSafe(transaction.getCategory()));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", transaction.getAmount()));
        holder.tvDescription.setText(nullSafe(transaction.getDescription()));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(transaction.getDate())));

        if ("INCOME".equals(transaction.getType())) {
            holder.tvAmount.setTextColor(Color.GREEN);
        } else {
            holder.tvAmount.setTextColor(Color.RED);
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = (transactions == null) ? new ArrayList<>() : transactions;
        notifyDataSetChanged();
    }

    public Transaction getTransactionAt(int position) {
        return transactions.get(position);
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDescription, tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);

            TextView editBtn = itemView.findViewById(R.id.editButton);
            TextView deleteBtn = itemView.findViewById(R.id.deleteButton);

            editBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (actionListener != null && position != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(transactions.get(position));
                }
            });

            deleteBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (actionListener != null && position != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(transactions.get(position));
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(transactions.get(position));
                }
            });
        }
    }
}