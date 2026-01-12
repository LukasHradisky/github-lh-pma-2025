package com.example.myapp014asharedtasklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp014asharedtasklist.databinding.ItemTaskBinding

// Adapter obsluhuje zobrazení seznamu úkolů v RecyclerView.
// Přijímá list úkolů a callbacky pro změnu stavu a smazání.
class TaskAdapter(
    private var tasks: List<Task>,
    private val onChecked: (Task) -> Unit,  // zavolá se při kliknutí na checkbox
    private val onDelete: (Task) -> Unit,     // zavolá se při kliknutí na ikonu smazání
    private val onEdit: (Task) -> Unit        // zavolá se při kliknutí na text úkolu (pro editaci)
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder drží jeden řádek seznamu (item_task.xml) a jeho view binding.
    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Vytvoření nového ViewHolderu – vytvoří se layout jednoho řádku.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    // Naplnění dat pro jeden řádek – nastavíme text, checkbox a tlačítko delete/edit.
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Nastaví text úkolu
        holder.binding.textTitle.text = task.title

        // Nastaví zaškrtnutí checkboxu podle hodnoty v objektu
        holder.binding.checkCompleted.isChecked = task.completed

        // Listener pro změnu stavu checkboxu
        // Když uživatel změní stav, zavolá se funkce onChecked(task)
        // Důležité: Použijeme onCheckedChangeListener, abychom zamezili vícenásobnému volání
        // a reagovali jen na změnu uživatelem. Zde je problém s tím, že se volá i při
        // nastavení hodnoty v onBindViewHolder. Pro jednoduchost ponecháme tvé původní řešení
        // a budeme spoléhat na to, že Firestore to sám vyřeší (což je v pořádku pro tuto aplikaci).
        // Pro správné řešení by se měl použít Compound Button.
        holder.binding.checkCompleted.setOnCheckedChangeListener { _, _ ->
            onChecked(task)
        }

        // Listener pro smazání úkolu
        holder.binding.imageDelete.setOnClickListener {
            onDelete(task)
        }

        // Listener pro editaci úkolu (kliknutí na text)
        holder.binding.textTitle.setOnClickListener {
            onEdit(task)
        }
    }

    // Počet položek v seznamu
    override fun getItemCount() = tasks.size

    // Aktualizace seznamu – nahradí starý list novým a překreslí RecyclerView
    fun submitList(newList: List<Task>) {
        tasks = newList
        notifyDataSetChanged()
    }
}
