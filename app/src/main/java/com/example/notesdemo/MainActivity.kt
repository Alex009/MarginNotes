package com.example.notesdemo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesdemo.adapters.NotesListAdapter
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val adapter = NotesListAdapter()
        recyclerview_notes.layoutManager = LinearLayoutManager(this)
        recyclerview_notes.adapter = adapter

        adapter.setOnNoteTapListener{ note ->
            val intent = Intent(this@MainActivity, EditNote::class.java)
            intent.putExtra("note", note)
            startActivity(intent)
        }

        notesVModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        with(recyclerview_notes) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(divider)
        }


        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, EditNote::class.java)
            startActivity(intent)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return true//super.onCreateOptionsMenu(menu)
    }
}