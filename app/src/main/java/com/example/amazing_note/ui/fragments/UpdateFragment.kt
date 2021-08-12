package com.example.amazing_note.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.amazing_note.R
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
        val view = binding.root
        setHasOptionsMenu(true)

        binding.updateTitleEt.setText(args.currentNote.title)
        binding.updateDescriptionEt.setText(args.currentNote.description)
        binding.updatePrioritiesSpinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentNote.priority))
        binding.updatePrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.udpate_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_save) {
            updateItem()
        }
        return super.onOptionsItemSelected(item)
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
}