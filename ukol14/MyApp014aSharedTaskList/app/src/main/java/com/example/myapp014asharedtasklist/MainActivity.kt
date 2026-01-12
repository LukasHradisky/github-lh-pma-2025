package com.example.myapp014asharedtasklist

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp014asharedtasklist.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        // Nastavení adapteru
        adapter = TaskAdapter(
            tasks = emptyList(),
            onChecked = { task -> toggleCompleted(task) },
            onDelete = { task -> deleteTask(task) },
            onEdit = { task -> showEditDialog(task) } // Přidána obsluha pro editaci
        )

        binding.recyclerViewTasks.adapter = adapter
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Přidání úkolu
        binding.buttonAdd.setOnClickListener {
            val title = binding.inputTask.text.toString().trim()
            if (title.isNotEmpty()) {
                addTask(title)
                binding.inputTask.text.clear()
            }
        }

        // Realtime sledování Firestore
        listenForTasks()
    }

    private fun addTask(title: String) {
        println("DEBUG: addTask called with title = $title")
        // Vytvoříme nový Task. ID bude prázdné, ale Firestore mu ho přiřadí.
        val task = Task(title = title, completed = false)
        db.collection("tasks").add(task)
    }

    private fun toggleCompleted(task: Task) {
        if (task.id.isNotEmpty()) {
            db.collection("tasks")
                .document(task.id) // Používáme unikátní ID!
                .update("completed", !task.completed)
        }
    }

    private fun deleteTask(task: Task) {
        // --- OPRAVA BUGU: Místo whereEqualTo("title", ...) použijeme unikátní ID. ---
        // Smažeme dokument se známým ID.
        if (task.id.isNotEmpty()) {
            db.collection("tasks")
                .document(task.id)
                .delete()
        }
    }

    // --- NOVÁ FUNKCE: Editace úkolu ---
    private fun showEditDialog(task: Task) {
        val editText = EditText(this)
        editText.setText(task.title) // Předvyplníme aktuální název

        AlertDialog.Builder(this)
            .setTitle("Upravit úkol")
            .setView(editText)
            .setPositiveButton("Uložit") { dialog, _ ->
                val newTitle = editText.text.toString().trim()
                if (newTitle.isNotEmpty() && newTitle != task.title) {
                    editTask(task, newTitle)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Zrušit") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun editTask(task: Task, newTitle: String) {
        // Aktualizujeme pouze název úkolu na dokumentu se známým ID.
        if (task.id.isNotEmpty()) {
            db.collection("tasks")
                .document(task.id)
                .update("title", newTitle)
        }
    }


    private fun listenForTasks() {
        db.collection("tasks")
            // Sleduje kolekci tasks v reálném čase
            .addSnapshotListener { snapshots, _ ->
                // Převede dokumenty z Firestore na seznam objektů Task
                // Díky @DocumentId v Task.kt se nyní naplní i id úkolu.
                val taskList = snapshots?.toObjects(Task::class.java) ?: emptyList()
                // Aktualizuje RecyclerView novým seznamem úkolů
                adapter.submitList(taskList)
            }
    }
}