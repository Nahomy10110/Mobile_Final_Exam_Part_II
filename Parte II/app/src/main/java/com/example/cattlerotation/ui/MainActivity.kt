package com.example.cattlerotation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.cattlerotation.R
import com.example.cattlerotation.data.AppDatabase
import com.example.cattlerotation.data.Paddock
import com.example.cattlerotation.util.applyStatusBarTopPadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: PaddockAdapter
    private lateinit var emptyView: View
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        toolbar.applyStatusBarTopPadding()
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_rotation) {
                startActivity(Intent(this, RotationActivity::class.java)); true
            } else false
        }

        emptyView = findViewById(R.id.emptyView)

        adapter = PaddockAdapter(
            onClick = { p ->
                startActivity(Intent(this, PaddockFormActivity::class.java)
                    .putExtra(PaddockFormActivity.EXTRA_ID, p.id))
            },
            onLongClick = { p -> confirmDelete(p) }
        )
        findViewById<RecyclerView>(R.id.recyclerPaddocks).adapter = adapter

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, PaddockFormActivity::class.java))
        }
        findViewById<RecyclerView>(R.id.recyclerPaddocks)
            .addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) fab.shrink() else fab.extend()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        loadPaddocks()
    }

    private fun loadPaddocks() = lifecycleScope.launch {
        val list = withContext(Dispatchers.IO) {
            seedIfEmpty()
            db.paddockDao().getAll()
        }
        adapter.submit(list)
        emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        // Subtitle con conteo [H1 — estado del sistema]
        toolbar.subtitle = if (list.isEmpty()) "" else
            resources.getQuantityString(R.plurals.paddock_count, list.size, list.size)
    }

    private suspend fun seedIfEmpty() {
        if (db.paddockDao().count() == 0) {
            val now = System.currentTimeMillis()
            for (i in 1..5) {
                db.paddockDao().insert(
                    Paddock(name = "Lote $i", areaM2 = 1000.0 * i, creationDate = now)
                )
            }
        }
    }

    private fun confirmDelete(p: Paddock) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_msg)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { db.paddockDao().delete(p) }
                    loadPaddocks()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}