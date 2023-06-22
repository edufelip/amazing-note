package com.edufelip.aqua_note.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.edufelip.aqua_note.DataBindingIdlingResource
import com.edufelip.aqua_note.R
import com.edufelip.aqua_note.data.models.Note
import com.edufelip.aqua_note.data.models.Priority
import com.edufelip.aqua_note.launchFragmentInHiltContainer
import com.edufelip.aqua_note.ui.MainActivity
import com.edufelip.aqua_note.ui.TestMainFragmentFactory
import com.edufelip.aqua_note.ui.adapters.ListAdapter
import com.edufelip.aqua_note.ui.viewmodels.NoteViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class ListFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var testFragmentFactory: TestMainFragmentFactory

    @get:Rule
    val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val idlingResource = DataBindingIdlingResource(activityRule)

    @Before
    fun setup() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun clickOnNote_navigateToUpdateFragment() {
        val navController = mock(NavController::class.java)
        val note = Note(0, "random title", Priority.HIGH, "random description", false)
        var testViewModel: NoteViewModel? = null

        launchFragmentInHiltContainer<ListFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            adapter.noteList = mutableListOf(note)
            testViewModel = mNoteViewModel
        }

        onView(withId(R.id.listfrag_recyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ListAdapter.MyViewHolder>(
                0,
                click()
            )
        )

        verify(navController).navigate(ListFragmentDirections.actionListFragmentToUpdateFragment(note))
    }

    @Test
    fun clickAddNoteButton_navigateToAddFragment() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<ListFragment> (
            fragmentFactory = testFragmentFactory
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.floatingActionButton)).perform(click())

        verify(navController).navigate(
            ListFragmentDirections.actionListFragmentToAddFragment()
        )
    }

    @Test
    fun clickTrashButton_navigateToTrashFragment() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<ListFragment> (
            fragmentFactory = testFragmentFactory
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.hamb_menu_btn)).perform(click())
        onView(withText(R.string.trash)).perform(click())

        verify(navController).navigate(
            ListFragmentDirections.actionListFragmentToTrashFragment()
        )
    }
}