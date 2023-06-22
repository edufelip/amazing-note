package com.edufelip.amazing_note.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.edufelip.amazing_note.data.models.Note
import com.edufelip.amazing_note.databinding.TrashRowLayoutBinding
import javax.inject.Inject

class TrashAdapter @Inject constructor(

): RecyclerView.Adapter<TrashAdapter.MyViewHolder>() {
    var noteList = mutableListOf<Note>()

    class MyViewHolder(private val binding: TrashRowLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.note = note
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TrashRowLayoutBinding.inflate(layoutInflater, parent, false)
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