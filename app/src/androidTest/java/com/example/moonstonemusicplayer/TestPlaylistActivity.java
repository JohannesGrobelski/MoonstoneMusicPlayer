package com.example.moonstonemusicplayer;

import android.app.Activity;
import android.content.res.Resources;
import android.media.audiofx.DynamicsProcessing;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.moonstonemusicplayer.controller.MainActivity.ArtistFragment.ArtistFragmentListener;
import com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener;
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
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
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
  public void playSong() {
    PlayListActivity playListActivity = (PlayListActivity) getCurrentActivity();
    //init playlistListener with songlist
    playListActivity.setPlayListActivityListener(new PlayListActivityListener(playListActivity, ,0));
  }

  private Activity getCurrentActivity() {
    final Activity[] activity = new Activity[1];
    onView(isRoot()).check(new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        activity[0] = (Activity) view.getContext();
      }
    });
    return activity[0];
  }

}