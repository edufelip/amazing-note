package com.edufelipe.amazing_note.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.edufelipe.amazing_note.DataBindingIdlingResource
import com.edufelipe.amazing_note.R
import com.edufelipe.amazing_note.data.models.Note
import com.edufelipe.amazing_note.data.models.Priority
import com.edufelipe.amazing_note.getOrAwaitValue
import com.edufelipe.amazing_note.launchFragmentInHiltContainer
import com.edufelipe.amazing_note.others.Status
import com.edufelipe.amazing_note.ui.MainActivity
import com.edufelipe.amazing_note.ui.TestMainFragmentFactory
import com.edufelipe.amazing_note.ui.viewmodels.TrashViewModel
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class TrashNoteFragmentTest {
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
    fun clickOnBackButton_navigateBack() {
        val navController = Mockito.mock(NavController::class.java)
        val note = Note(0, "random title", Priority.HIGH, "random description", true)

        launchFragmentInHiltContainer<TrashNoteFragment> (
            fragmentArgs = bundleOf(Pair("currentNote", note)),
            fragmentFactory = testFragmentFactory
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.back_button)).perform(click())
        verify(navController).navigateUp()
    }

    @Test
    fun clickOnRecoverButton_successfullyRecover() {
        val navController = Mockito.mock(NavController::class.java)
        val note = Note(0, "random title", Priority.HIGH, "random description", true)
        var testViewModel: TrashViewModel? = null

        launchFragmentInHiltContainer<TrashNoteFragment> (fragmentArgs = bundleOf(Pair("currentNote", note)), fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mTrashViewModel
        }

        onView(withId(R.id.action_more)).perform(click())
        onView(withText(R.string.recover)).perform(click())
        verify(navController).navigateUp()
        val value = testViewModel?.recoverNoteStatus?.getOrAwaitValue()
        assertThat(value?.peekContent()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun clickOnDeleteButton_successfullyRemove() {
        val navController = Mockito.mock(NavController::class.java)
        val note = Note(0, "random title", Priority.HIGH, "random description", true)
        var testViewModel: TrashViewModel? = null

        launchFragmentInHiltContainer<TrashNoteFragment> (fragmentArgs = bundleOf(Pair("currentNote", note)), fragmentFactory = testFragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            testViewModel = mTrashViewModel
        }

        onView(withId(R.id.action_more)).perform(click())
        onView(withText(R.string.delete)).perform(click())
        onView(withText(R.string.yes)).perform(click())
        val value = testViewModel?.deleteNoteStatus?.getOrAwaitValue()
        assertThat(value?.peekContent()?.status).isEqualTo(Status.SUCCESS)
    }
}