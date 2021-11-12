package com.example.amazing_note.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.example.amazing_note.DataBindingIdlingResource
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.launchFragmentInHiltContainer
import com.example.amazing_note.ui.MainActivity
import com.example.amazing_note.ui.TestMainFragmentFactory
import com.example.amazing_note.ui.adapters.ListAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class TrashFragmentTest {
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
    fun clickOnNote_navigateToTrashNoteFragment() {
        val navController = Mockito.mock(NavController::class.java)
        val note = Note(0, "random title", Priority.HIGH, "random description", true)

        launchFragmentInHiltContainer<TrashFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            adapter.noteList = mutableListOf(note)
        }

        Espresso.onView(withId(R.id.listfrag_recyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ListAdapter.MyViewHolder>(
                0,
                ViewActions.click()
            )
        )
        Mockito.verify(navController).navigate(TrashFragmentDirections.actionTrashFragmentToTrashNoteFragment(note))
    }

    @Test
    fun clickOnBackButton_navigateBack() {
        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInHiltContainer<TrashFragment> (
            fragmentFactory = testFragmentFactory
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }

        Espresso.onView(withId(R.id.back_button)).perform(ViewActions.click())
        Mockito.verify(navController).navigateUp()
    }
}