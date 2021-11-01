package com.example.amazing_note.ui.adapters

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.amazing_note.data.models.Note
import androidx.recyclerview.widget.RecyclerView
import com.example.amazing_note.databinding.RowLayoutBinding
import javax.inject.Inject

class ListAdapter @Inject constructor(

): RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    var noteList = mutableListOf<Note>()

    class MyViewHolder(private val binding: RowLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.note = note
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = noteList[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    fun setNotes(noteData: List<Note>) {
        val noteDiffUtil = NoteDiffUtil(noteList, noteData)
        val noteDiffResult = DiffUtil.calculateDiff(noteDiffUtil)
        this.noteList = noteData.toMutableList()
        noteDiffResult.dispatchUpdatesTo(this)
    }
}