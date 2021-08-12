package com.example.amazing_note.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.databinding.RowLayoutBinding
import com.example.amazing_note.ui.fragments.ListFragmentDirections

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    var noteList = emptyList<Note>()

    class MyViewHolder(private val rowBinding: RowLayoutBinding): RecyclerView.ViewHolder(rowBinding.root) {
        fun bind(note: Note) {
            rowBinding.rowTitle.text = note.title
            rowBinding.rowDescription.text = note.description
            when(note.priority) {
                Priority.HIGH -> rowBinding.rowPriorityIndicator.setCardBackgroundColor(ContextCompat.getColor(
                    this.itemView.context,
                    R.color.red))
                Priority.MEDIUM -> rowBinding.rowPriorityIndicator.setCardBackgroundColor(ContextCompat.getColor(
                    this.itemView.context,
                    R.color.yellow))
                Priority.LOW -> rowBinding.rowPriorityIndicator.setCardBackgroundColor(ContextCompat.getColor(
                    this.itemView.context,
                    R.color.green))
            }
            rowBinding.rowBackground.setOnClickListener {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(note)
                Navigation.findNavController(rowBinding.root).navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    fun setNotes(noteData: List<Note>) {
        this.noteList = noteData
        notifyDataSetChanged()
    }
}