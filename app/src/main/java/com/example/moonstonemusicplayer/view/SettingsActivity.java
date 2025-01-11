package com.example.moonstonemusicplayer.view;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.GoogleDriveManager;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_AUTHORIZE = 771;
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void startAuthorizationRequest(){
            List<Scope> requestedScopes = Collections.singletonList(new Scope(DriveScopes.DRIVE_FILE));
            AuthorizationRequest authorizationRequest = AuthorizationRequest.builder().setRequestedScopes(requestedScopes).build();
            Identity.getAuthorizationClient(this)
                    .authorize(authorizationRequest)
                    .addOnSuccessListener(
                            authorizationResult -> {
                                if (authorizationResult.hasResolution()) {
                                    // Access needs to be granted by the user
                                    PendingIntent pendingIntent = authorizationResult.getPendingIntent();
                                    try {
                                        this.startIntentSenderForResult(pendingIntent.getIntentSender(),
                                                REQUEST_AUTHORIZE, null, 0, 0, 0, null);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG, "Couldn't start Authorization UI: " + e.getLocalizedMessage());
                                    }
                                } else {
                                    // Access already granted, continue with user action
                                    Toast.makeText(this, "Access already granted, continue with user action", Toast.LENGTH_LONG).show();
                                }
                            })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to authorize", e));
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZE) {
            settingsFragment.handleSignInResult(data);
        }
    }
}
