package com.example.amazing_note.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.databinding.FragmentListBinding
import com.example.amazing_note.helpers.hideKeyboard
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.utils.SwipeToDelete
import com.example.amazing_note.ui.viewmodels.NoteViewModel
import com.example.amazing_note.ui.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }
    private val mNoteViewModel: NoteViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mSharedViewModel = mSharedViewModel

        // set recyclerview
        setupRecyclerView()

        // observe livedata
        mNoteViewModel.noteList.observe(viewLifecycleOwner, { data ->
            adapter.setNotes(data)
        })

        setHasOptionsMenu(true)

        hideKeyboard(requireActivity())

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.listfragRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        swipeToDelete(recyclerView)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null) {
            searchDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null) {
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%${query}%"
        mNoteViewModel.searchNote(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setNotes(it)
            }
        })
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallBack = object: SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val noteDelete = adapter.noteList[viewHolder.adapterPosition]
                mNoteViewModel.deleteNote(noteDelete)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedNote(viewHolder.itemView, noteDelete)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedNote(view: View, deletedNote: Note) {
        val snackBar = Snackbar.make(
            view, this.getString(R.string.deleted) + " '${deletedNote.title}'", Snackbar.LENGTH_SHORT
        )
        snackBar.setAction(this.getString(R.string.undo)) {
            mNoteViewModel.insertNote(deletedNote)
        }
        snackBar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_priority_high -> mNoteViewModel.noteListAsc.observe(this, {
                adapter.setNotes(it)
            })
            R.id.menu_priority_low -> mNoteViewModel.noteListDes.observe(this, {
                adapter.setNotes(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}