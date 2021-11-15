package com.example.amazing_note.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.amazing_note.R
import com.example.amazing_note.databinding.FragmentTrashNoteBinding
import com.example.amazing_note.others.Status
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.viewmodels.SharedViewModel
import com.example.amazing_note.ui.viewmodels.TrashViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrashNoteFragment @Inject constructor(
    private val adapter: ListAdapter,
    var mTrashViewModel: TrashViewModel? = null
): Fragment() {
    private var _binding: FragmentTrashNoteBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashNoteBinding.inflate(inflater, container, false)
        binding.note = args.currentNote

        mTrashViewModel = mTrashViewModel ?: ViewModelProvider(requireActivity()).get(TrashViewModel::class.java)
        setListeners()
        subscribeObserver()
        val toolbar = binding.trashNoteToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        binding.prioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener
        binding.prioritiesSpinner.isEnabled = false
        return binding.root
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun subscribeObserver() {
        mTrashViewModel?.deleteNoteStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), this.getString(R.string.perma_deleted), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        })

        mTrashViewModel?.recoverNoteStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), this.getString(R.string.note_recovered), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        })
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
        builder.setPositiveButton(this.getString(R.string.yes)) { _, _ ->
            mTrashViewModel?.permaDeleteNote(args.currentNote)
        }
        builder.setNegativeButton(this.getString(R.string.no)) { _, _ ->
        }
        builder.setTitle(this.getString(R.string.delete) + " '${args.currentNote.title}'?")
        builder.setMessage(this.getString(R.string.once_deleted_permanent))
        builder.create().show()
    }

    private fun recoverItem() {
        val updatedNote = args.currentNote
        updatedNote.deleted = false
        mTrashViewModel?.recoverNote(updatedNote)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}