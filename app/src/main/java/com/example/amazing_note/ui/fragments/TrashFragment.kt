package com.example.amazing_note.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.amazing_note.R
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.example.amazing_note.databinding.FragmentTrashBinding
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.adapters.TrashAdapter
import com.example.amazing_note.ui.viewmodels.TrashViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrashFragment @Inject constructor(
    private val adapter: TrashAdapter
) : Fragment() {
    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private val mTrashViewModel: TrashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val toolbar = binding.mainToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        setupRecyclerView()
        setListeners()

        mTrashViewModel.deletedNoteList.observe(viewLifecycleOwner, { data ->
            adapter.setNotes(data)
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.listfragRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}