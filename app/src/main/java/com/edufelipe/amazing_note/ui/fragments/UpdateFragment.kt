package com.edufelipe.amazing_note.ui.fragments

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
import com.edufelipe.amazing_note.R
import com.edufelipe.amazing_note.databinding.FragmentUpdateBinding
import com.edufelipe.amazing_note.others.parsePriority
import com.edufelipe.amazing_note.others.Status
import com.edufelipe.amazing_note.ui.adapters.ListAdapter
import com.edufelipe.amazing_note.ui.viewmodels.NoteViewModel
import com.edufelipe.amazing_note.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpdateFragment @Inject constructor(
    private val adapter: ListAdapter,
    var mNoteViewModel: NoteViewModel? = null
) : Fragment() {
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding.note = args.currentNote
        mNoteViewModel = mNoteViewModel ?: ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        setClickListeners()
        subscribeObservers()
        setToolbar()
        binding.updatePrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener
        return binding.root
    }

    private fun setToolbar() {
        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    private fun setClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun subscribeObservers() {
        mNoteViewModel?.updateNoteStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {
                        findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
                        Toast.makeText(requireContext(), this.getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
                    }
                    Status.ERROR -> {
                        when(result.message) {
                            "empty_field" -> {
                                Toast.makeText(requireContext(), this.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Status.LOADING -> Unit
                }
            }
        })
        mNoteViewModel?.deleteNoteStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
                        Toast.makeText(requireContext(), this.getString(R.string.note_removed), Toast.LENGTH_SHORT).show()
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        })
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
        builder.setPositiveButton(this.getString(R.string.yes)) { _, _ ->
            mNoteViewModel?.deleteNote(args.currentNote, true)
        }
        builder.setNegativeButton(this.getString(R.string.no)) { _, _ ->
        }
        builder.setTitle(this.getString(R.string.delete) + " '${args.currentNote.title}'?")
        builder.setMessage(this.getString(R.string.once_deleted_permanent))
        builder.create().show()
    }

    private fun updateItem() {
        val title = binding.updateTitleEt.text.toString()
        val description = binding.updateDescriptionEt.text.toString()
        val priority = binding.updatePrioritiesSpinner.selectedItem.toString()
        mNoteViewModel?.updateNote(args.currentNote.id, title, parsePriority(priority), description, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}