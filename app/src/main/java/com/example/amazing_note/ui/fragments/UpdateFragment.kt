package com.example.amazing_note.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.databinding.FragmentUpdateBinding
import com.example.amazing_note.ui.viewmodels.NoteViewModel
import com.example.amazing_note.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFragment : Fragment() {
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mNoteViewModel: NoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding.note = args.currentNote

        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        setListeners()

        binding.updatePrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> deleteItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteItem() {
        val builder = AlertDialog.Builder(requireContext(), R.style.MyCustomDialog)
        builder.setPositiveButton("Yes") { _, _ ->
            val updateNote = args.currentNote
            updateNote.deleted = true
            mNoteViewModel.updateNote(updateNote)
            Toast.makeText(requireContext(), this.getString(R.string.note_removed), Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
        builder.setNegativeButton("No") { _, _ ->
        }
        builder.setTitle(this.getString(R.string.delete) + " '${args.currentNote.title}'?")
        builder.setMessage(this.getString(R.string.once_deleted_permanent))
        builder.create().show()
    }

    private fun updateItem() {
        val title = binding.updateTitleEt.text.toString()
        val description = binding.updateDescriptionEt.text.toString()
        val priority = binding.updatePrioritiesSpinner.selectedItem.toString()
        if(mSharedViewModel.checkEmptyInputs(title, description)) {
            Toast.makeText(requireContext(), this.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        val updatedNote = Note(args.currentNote.id, title, mSharedViewModel.parsePriority(priority), description, false)
        mNoteViewModel.updateNote(updatedNote)
        Toast.makeText(requireContext(), this.getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}