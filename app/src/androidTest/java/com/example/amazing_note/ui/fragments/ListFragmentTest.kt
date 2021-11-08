package com.example.amazing_note.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.example.amazing_note.DataBindingIdlingResource
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.data.repositories.FakeNoteRepositoryAndroidTest
import com.example.amazing_note.getOrAwaitValue
import com.example.amazing_note.launchFragmentInHiltContainer
import com.example.amazing_note.ui.MainActivity
import com.example.amazing_note.ui.TestMainFragmentFactory
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.viewmodels.NoteViewModel
import com.google.common.truth.Truth.assertThat
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
    fun clickOnNote_navigateUpdateFragment() {
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

        verify(navController).popBackStack()
//        assertThat(testViewModel.insertNoteStatus.getOrAwaitValue()).isEqualTo("")
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
}