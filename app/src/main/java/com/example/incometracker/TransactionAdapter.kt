package com.example.incometracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.incometracker.model.Transaction

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
        }
    }

    var onEditClick: ((Transaction) -> Unit)? = null
    var onDeleteClick: ((Transaction) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
        holder.editButton.setOnClickListener { onEditClick?.invoke(transaction) }
        holder.deleteButton.setOnClickListener { onDeleteClick?.invoke(transaction) }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountText: TextView = itemView.findViewById(R.id.amountText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val editButton: View = itemView.findViewById(R.id.editButton)
        val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        fun bind(transaction: Transaction) {
            amountText.text = transaction.amount.toString()
            dateText.text = transaction.date
            categoryText.text = transaction.category
            descriptionText.text = transaction.description
            typeText.text = transaction.type
        }
    }
}
