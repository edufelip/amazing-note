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
import com.example.amazing_note.databinding.FragmentTrashNoteBinding
import com.example.amazing_note.ui.viewmodels.TrashViewModel

class TrashNoteFragment : Fragment() {
    private var _binding: FragmentTrashNoteBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()
    private val mTrashViewModel: TrashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashNoteBinding.inflate(inflater, container, false)
        binding.note = args.currentNote

        val toolbar = binding.trashNoteToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        setListeners()

        binding.updatePrioritiesSpinner.isEnabled = false
        return binding.root
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.trash_note_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_recover -> recoverItem()
            R.id.menu_delete_perm -> permaDeleteItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun permaDeleteItem() {
        val builder = AlertDialog.Builder(requireContext(), R.style.MyCustomDialog)
        builder.setPositiveButton("Yes") { _, _ ->
            mTrashViewModel.deleteNote(args.currentNote)
            Toast.makeText(requireContext(), "Note permanently deleted", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
        builder.setNegativeButton("No") { _, _ ->
        }
        builder.setTitle(this.getString(R.string.delete) + " '${args.currentNote.title}'?")
        builder.setMessage(this.getString(R.string.once_deleted_permanent))
        builder.create().show()
    }

    private fun recoverItem() {
        val updatedNote = args.currentNote
        updatedNote.deleted = false
        mTrashViewModel.recoverNote(updatedNote)
        Toast.makeText(requireContext(), this.getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}