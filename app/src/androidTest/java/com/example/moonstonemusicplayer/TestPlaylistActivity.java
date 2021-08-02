package com.example.moonstonemusicplayer;

import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.google.android.material.tabs.TabLayout;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestPlaylistActivity {

  @Rule
  public ActivityScenarioRule<PlayListActivity> activityRule
          = new ActivityScenarioRule<>(PlayListActivity.class);


  @Test
  /** click all surface main elements (tabs) */
  public void updateAndSelectAllTabs() {
    //update library
    //Open the overflow menu, open the options menu,
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    // Click the item and click OK (button1) in displayed alert dialog
    onView(withText(R.string.update_library)).perform(click());
    onView(withId(android.R.id.button1)).perform(click());

    //select all tabs
    for(int tabId=0; tabId<5; tabId++){
      onView(withId(R.id.mainactivity_tabs)).perform(selectTabAtPosition(tabId));
      //search something
      onView(withId(R.id.miSearch)).perform(click());
      typeSearchViewText("a random text 3239592395395");
      //click through hierarchy
    }
  }



}