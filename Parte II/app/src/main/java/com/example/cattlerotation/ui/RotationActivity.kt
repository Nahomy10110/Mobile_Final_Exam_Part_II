package com.example.cattlerotation.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.cattlerotation.R
import com.example.cattlerotation.data.AppDatabase
import com.example.cattlerotation.data.Rotation
import com.example.cattlerotation.domain.RotationCalculator
import com.example.cattlerotation.util.applyStatusBarTopPadding
import com.example.cattlerotation.util.toDateString
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class RotationActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private val adapter = RotationAdapter()
    private var selectedDate: Long = System.currentTimeMillis()
    private lateinit var buttonDate: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotation)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.applyStatusBarTopPadding()
        toolbar.setNavigationOnClickListener { finish() }

        buttonDate = findViewById(R.id.buttonDate)
        buttonDate.setOnClickListener { pickDate() }

        findViewById<RecyclerView>(R.id.recyclerRotations).adapter = adapter
        findViewById<MaterialButton>(R.id.buttonLoadCattle).setOnClickListener { loadCattle() }

        updateDateButton()
        refresh()
    }

    private fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            selectedDate = cal.timeInMillis
            updateDateButton()
            refresh()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateButton() {
        buttonDate.text = "${getString(R.string.select_date_prefix)} ${selectedDate.toDateString()}"
    }

    private fun refresh() = lifecycleScope.launch {
        val paddocks = withContext(Dispatchers.IO) { db.paddockDao().getAll() }
        val rotations = withContext(Dispatchers.IO) { db.rotationDao().getAll() }
        val byPaddock = rotations.groupBy { it.paddockId }
        val rows = paddocks.map { p ->
            val rs = byPaddock[p.id] ?: emptyList()
            RotationRow(
                paddock = p,
                state = RotationCalculator.stateOnDate(rs, selectedDate),
                active = RotationCalculator.activeRotationOn(rs, selectedDate)
            )
        }
        adapter.submit(rows)
    }

    private fun loadCattle() {
        val ids = adapter.selectedIds()
        if (ids.isEmpty()) {
            Toast.makeText(this, R.string.select_at_least_one, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ids.forEach {
                    db.rotationDao().insert(Rotation(paddockId = it, startDate = selectedDate))
                }
            }
            adapter.clearSelection()
            Toast.makeText(this@RotationActivity, R.string.cattle_loaded, Toast.LENGTH_SHORT).show()
            refresh()
        }
    }
}