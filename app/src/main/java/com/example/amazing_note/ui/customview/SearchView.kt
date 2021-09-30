package com.example.amazing_note.ui.customview

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.amazing_note.R
import com.example.amazing_note.databinding.ViewSearchBinding
import com.example.amazing_note.helpers.hideKeyboard
import com.example.amazing_note.helpers.showKeyboard

class SearchView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search, this, true)

        binding.openSearchButton.setOnClickListener {
            openSearch()
        }

        binding.closeSearchButton.setOnClickListener {
            closeSearch()
        }
    }

    private fun openSearch() {
        binding.searchInput.text.clear()
        binding.searchOpenedView.visibility = View.VISIBLE
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            binding.searchOpenedView,
            (binding.openSearchButton.right + binding.openSearchButton.left) / 2,
            (binding.openSearchButton.top + binding.openSearchButton.bottom) / 2,
            0f,
            width.toFloat()
        )
        circularReveal.duration = 150
        circularReveal.start()
        binding.searchInput.requestFocus()

        showKeyboard(context, binding.searchInput)
    }

    private fun closeSearch() {
        val circularConceal = ViewAnimationUtils.createCircularReveal(
            binding.searchOpenedView,
            (binding.openSearchButton.right + binding.openSearchButton.left) / 2,
            (binding.openSearchButton.top + binding.openSearchButton.bottom) / 2,
            width.toFloat(),
            0f
        )
        circularConceal.duration = 150
        circularConceal.start()
        circularConceal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) = Unit
            override fun onAnimationRepeat(p0: Animator?) = Unit
            override fun onAnimationCancel(p0: Animator?) = Unit
            override fun onAnimationEnd(p0: Animator?) {
                binding.searchOpenedView.visibility = View.INVISIBLE
                binding.searchInput.text.clear()
                circularConceal.removeAllListeners()
            }
        })

        hideKeyboard(context, binding.searchInput)
    }
}