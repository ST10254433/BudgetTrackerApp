package com.example.budgettrackerapp

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        // Toolbar with back arrow
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.expenseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getDatabase(this)

        // Load expenses
        CoroutineScope(Dispatchers.IO).launch {
            val expenses = db.expenseDao().getAllExpenses()
            runOnUiThread {
                adapter = ExpenseAdapter(expenses)
                recyclerView.adapter = adapter
                attachSwipeGestures()
            }
        }
    }

    private fun attachSwipeGestures() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expense = adapter.expenses[position]

                if (direction == ItemTouchHelper.LEFT) {
                    // Delete expense
                    CoroutineScope(Dispatchers.IO).launch {
                        db.expenseDao().deleteExpense(expense)
                        val updated = db.expenseDao().getAllExpenses()
                        runOnUiThread {
                            adapter.updateData(updated)
                        }
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Edit expense
                    val intent = Intent(this@ExpenseListActivity, ExpenseEntryActivity::class.java)
                    intent.putExtra("expenseId", expense.id)
                    startActivity(intent)
                    adapter.notifyItemChanged(position) // reset swipe state
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background: ColorDrawable?

                if (dX > 0) {
                    // Swiping right → edit (green)
                    background = ColorDrawable(ContextCompat.getColor(this@ExpenseListActivity, android.R.color.holo_green_dark))
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else if (dX < 0) {
                    // Swiping left → delete (red)
                    background = ColorDrawable(ContextCompat.getColor(this@ExpenseListActivity, android.R.color.holo_red_dark))
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                } else {
                    background = null
                }

                background?.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
