package com.example.incometracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.incometracker.model.AppDatabase
import com.example.incometracker.model.Transaction
import kotlinx.coroutines.launch

class EditTransactionActivity : AppCompatActivity() {
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val amountEdit = findViewById<EditText>(R.id.amountEdit)
        val dateEdit = findViewById<EditText>(R.id.dateEdit)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val descriptionEdit = findViewById<EditText>(R.id.descriptionEdit)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val addButton = findViewById<Button>(R.id.addButton)
        addButton.text = "Update Transaction"

        val categories = arrayOf("Food", "Bills", "Entertainment", "Salary", "Scholarship", "Other")
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val types = arrayOf("income", "expense")
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        transactionId = intent.getIntExtra("transaction_id", -1)
        val db = AppDatabase.getInstance(applicationContext)

        lifecycleScope.launch {
            val transaction = db.transactionDao().getTransactionById(transactionId)
            transaction?.let {
                amountEdit.setText(it.amount.toString())
                dateEdit.setText(it.date)
                categorySpinner.setSelection(categories.indexOf(it.category).takeIf { idx -> idx >= 0 } ?: 0)
                descriptionEdit.setText(it.description)
                typeSpinner.setSelection(types.indexOf(it.type).takeIf { idx -> idx >= 0 } ?: 0)
            }
        }

        addButton.setOnClickListener {
            val amount = amountEdit.text.toString().toDoubleOrNull() ?: 0.0
            val date = dateEdit.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val description = descriptionEdit.text.toString()
            val type = typeSpinner.selectedItem.toString()

            val updatedTransaction = Transaction(
                id = transactionId,
                amount = amount,
                date = date,
                category = category,
                description = description,
                type = type
            )

            lifecycleScope.launch {
                db.transactionDao().updateTransaction(updatedTransaction)
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
