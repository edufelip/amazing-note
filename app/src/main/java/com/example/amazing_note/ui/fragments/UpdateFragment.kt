package com.example.amazing_note.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.amazing_note.R
import com.example.amazing_note.data.Converter
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.databinding.FragmentUpdateBinding
import com.example.amazing_note.ui.viewmodels.NoteViewModel
import com.example.amazing_note.ui.viewmodels.SharedViewModel

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

        setHasOptionsMenu(true)

        binding.updatePrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.udpate_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> deleteItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteItem() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mNoteViewModel.deleteNote(args.currentNote)
            Toast.makeText(requireContext(), "Note Removed", Toast.LENGTH_SHORT).show()
            findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
        }
        builder.setNegativeButton("No") { _, _ ->
        }
        builder.setTitle("Delete '${args.currentNote.title}'?")
        builder.setMessage("Once deleted the note is lost permanently")
        builder.create().show()
    }

    private fun updateItem() {
        val title = binding.updateTitleEt.text.toString()
        val description = binding.updateDescriptionEt.text.toString()
        val priority = binding.updatePrioritiesSpinner.selectedItem.toString()
        if(mSharedViewModel.checkEmptyInputs(title, description)) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val updatedNote = Note(args.currentNote.id, title, mSharedViewModel.parsePriority(priority), description)
        mNoteViewModel.updateNote(updatedNote)
        Toast.makeText(requireContext(), "Note Successfully Updated", Toast.LENGTH_SHORT).show()
        findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}