package com.edufelip.aqua_note.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.edufelip.aqua_note.R
import com.edufelip.aqua_note.databinding.FragmentAddBinding
import com.edufelip.aqua_note.others.Status
import com.edufelip.aqua_note.ui.adapters.ListAdapter
import com.edufelip.aqua_note.ui.viewmodels.NoteViewModel
import com.edufelip.aqua_note.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddFragment @Inject constructor(
    val adapter: ListAdapter,
    var mNoteViewModel: NoteViewModel? = null
) : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        mNoteViewModel = mNoteViewModel ?: ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        setListeners()
        subscribeObservers()
        setToolbar()
        binding.prioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_add) {
            insertDataToDb()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setToolbar() {
        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    private fun insertDataToDb() {
        val title = binding.titleEt.text.toString()
        val priority = binding.prioritiesSpinner.selectedItem.toString()
        val description = binding.descriptionEt.text.toString()
        mNoteViewModel?.insertNote(title, mSharedViewModel.parsePriority(priority), description)
    }

    private fun subscribeObservers() {
        mNoteViewModel?.insertNoteStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), this.getString(R.string.note_created), Toast.LENGTH_SHORT).show()
                        findNavController().navigate(AddFragmentDirections.actionAddFragmentToListFragment())
                    }
                    Status.ERROR -> {
                        when(result.message) {
                            "empty_field" -> {
                                Toast.makeText(requireContext(), this.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                        // Do nothing
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}