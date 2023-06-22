package com.edufelip.aqua_note.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.edufelip.aqua_note.R
import com.edufelip.aqua_note.databinding.FragmentListBinding
import com.edufelip.aqua_note.others.hideKeyboard
import com.edufelip.aqua_note.ui.adapters.ListAdapter
import com.edufelip.aqua_note.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListFragment @Inject constructor(
    val adapter: ListAdapter,
    var mNoteViewModel: NoteViewModel? = null
) : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        mNoteViewModel = mNoteViewModel ?: ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        getThemePreferences()
        setNavDrawer()
        setToolbar()
        subscribeObservers()
        setupRecyclerView()
        setListeners()
        hideKeyboard(requireActivity())

        return binding.root
    }

    private fun subscribeObservers() {
        mNoteViewModel?.noteList?.observe(viewLifecycleOwner, { data ->
            adapter.setNotes(data)
        })
    }

    private fun setToolbar() {
        val toolbar = binding.mainToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        binding.navView.menu.getItem(0).isChecked = true
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.listfragRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%${query}%"
        mNoteViewModel?.searchNote(searchQuery)?.observe(this, { list ->
            list?.let {
                adapter.setNotes(it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val menuMore = menu.findItem(R.id.menu_sortby)
        menuMore?.subMenu?.clearHeader()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_priority_high -> mNoteViewModel?.noteListAsc?.observe(this, {
                adapter.setNotes(it)
            })
            R.id.menu_priority_low -> mNoteViewModel?.noteListDes?.observe(this, {
                adapter.setNotes(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setListeners() {
        binding.hambMenuBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.searchView.findViewById<EditText>(R.id.search_input).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun afterTextChanged(query: Editable?) {
                searchDatabase(query.toString())
            }
        })

        binding.lightDarkSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                setTheme("Dark")
            } else {
                setTheme("Light")
            }
        }
    }

    private fun setNavDrawer() {
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menu_your_notes -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_trash -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    findNavController().navigate(ListFragmentDirections.actionListFragmentToTrashFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setTheme(theme: String) {
        val preferences: SharedPreferences = requireContext().getSharedPreferences("Theme", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putString("Theme", theme)
        editor.apply()
        changeTheme(theme)
    }

    private fun getThemePreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("Theme", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getString("Theme", "Default")
        if(theme.isNullOrEmpty() || theme == "Default") {
            val nightModeFlags = context!!.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    setTheme("Dark")
                    return
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    setTheme("Light")
                    return
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> Unit
            }
            return
        }

        setTheme(theme)
    }

    private fun changeTheme(theme: String) {
        when(theme) {
            "Dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.lightDarkSwitch.isChecked = true
            }
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}