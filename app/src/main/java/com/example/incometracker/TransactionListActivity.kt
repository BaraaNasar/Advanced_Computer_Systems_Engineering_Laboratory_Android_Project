package com.example.incometracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incometracker.model.AppDatabase
import com.example.incometracker.model.Transaction
import kotlinx.coroutines.launch

class TransactionListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = TransactionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val db = AppDatabase.getInstance(applicationContext)

        fun refreshList() {
            lifecycleScope.launch {
                val transactions = db.transactionDao().getAllTransactions()
                adapter.submitList(transactions)
            }
        }

        refreshList()

        adapter.onDeleteClick = { transaction ->
            lifecycleScope.launch {
                db.transactionDao().deleteTransaction(transaction)
                refreshList()
            }
        }

        adapter.onEditClick = { transaction ->
            val intent = android.content.Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivityForResult(intent, 1001)
        }

        val addTransactionButton = findViewById<android.widget.Button>(R.id.addTransactionButton)
        addTransactionButton.setOnClickListener {
            val intent = android.content.Intent(this, AddTransactionActivity::class.java)
            startActivityForResult(intent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // تحديث القائمة عند العودة من إضافة أو تعديل
        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            val transactions = db.transactionDao().getAllTransactions()
            adapter.submitList(transactions)
        }
    }
}
