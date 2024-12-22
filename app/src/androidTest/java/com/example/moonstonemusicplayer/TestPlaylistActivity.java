package com.example.moonstonemusicplayer;

import android.app.Activity;
import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivityListener;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestPlaylistActivity {

  @Rule
  public ActivityScenarioRule<PlayListActivityListener> activityRule
          = new ActivityScenarioRule<>(PlayListActivityListener.class);


  @Test
  /** click all surface main elements (tabs) */
  public void playSong() {
    PlayListActivityListener playListActivity = (PlayListActivityListener) getCurrentActivity();
    //init playlistListener with songlist
    playListActivity.setPlayListActivityListener(new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(playListActivity, new Song[0] ,0, ""));
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