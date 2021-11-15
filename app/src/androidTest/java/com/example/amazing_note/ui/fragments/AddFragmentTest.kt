package com.example.amazing_note.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.example.amazing_note.DataBindingIdlingResource
import com.example.amazing_note.R
import com.example.amazing_note.getOrAwaitValue
import com.example.amazing_note.launchFragmentInHiltContainer
import com.example.amazing_note.others.Status
import com.example.amazing_note.ui.MainActivity
import com.example.amazing_note.ui.TestMainFragmentFactory
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
class AddFragmentTest {
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
    fun clickBackButton_navigateToListFragment() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddFragment>(fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
        }
        onView(withId(R.id.back_button)).perform(click())
        verify(navController).navigateUp()
    }

    @Test
    fun clickDoneButton_displayEmptyErrorMessage() {
        val navController = mock(NavController::class.java)
        var testViewModel: NoteViewModel? = null

        launchFragmentInHiltContainer<AddFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mNoteViewModel
        }
        onView(withId(R.id.menu_add)).perform(click())
        val value = testViewModel?.insertNoteStatus?.getOrAwaitValue()?.peekContent()?.status
        assertThat(value).isEqualTo(Status.ERROR)
    }

    @Test
    fun clickDoneButton_displayEmptyError_emptyTitle() {
        val navController = mock(NavController::class.java)
        var testViewModel: NoteViewModel? = null

        launchFragmentInHiltContainer<AddFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mNoteViewModel
        }

        onView(withId(R.id.description_et)).perform(replaceText("random description"))
        onView(withId(R.id.menu_add)).perform(click())
        val value = testViewModel?.insertNoteStatus?.getOrAwaitValue()?.peekContent()?.status
        assertThat(value).isEqualTo(Status.ERROR)
    }

    @Test
    fun clickDoneButton_displayEmptyError_emptyDescription() {
        val navController = mock(NavController::class.java)
        var testViewModel: NoteViewModel? = null

        launchFragmentInHiltContainer<AddFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mNoteViewModel
        }
        onView(withId(R.id.title_et)).perform(replaceText("random title"))
        onView(withId(R.id.menu_add)).perform(click())
        val value = testViewModel?.insertNoteStatus?.getOrAwaitValue()?.peekContent()?.status
        assertThat(value).isEqualTo(Status.ERROR)
    }

    @Test
    fun clickDoneButton_successfullyCreateNote() {
        val navController = mock(NavController::class.java)
        var testViewModel: NoteViewModel? = null

        launchFragmentInHiltContainer<AddFragment> (fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mNoteViewModel
        }

        onView(withId(R.id.title_et)).perform(replaceText("random title"))
        onView(withId(R.id.description_et)).perform(replaceText("random description"))
        onView(withId(R.id.menu_add)).perform(click())
        val value = testViewModel?.insertNoteStatus?.getOrAwaitValue()?.peekContent()?.status
        assertThat(value).isEqualTo(Status.SUCCESS)
    }
}