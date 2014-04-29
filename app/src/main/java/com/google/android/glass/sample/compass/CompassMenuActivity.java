/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.compass;

import com.google.android.glass.sample.compass.util.MathUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import cz.kotu.glass.hackathon.app1.R;

/**
 * This activity manages the options menu that appears when the user taps on the compass's live
 * card.
 */
public class CompassMenuActivity extends Activity {

    public static final int SPEECH_REQUEST = 0;
    private CompassService.CompassBinder mCompassService;
    private boolean mResumed;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof CompassService.CompassBinder) {
                mCompassService = (CompassService.CompassBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, CompassService.class), mConnection, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
        if (mResumed && mCompassService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_recognizer:
//                displaySpeechRecognizer();
                scanQrCode();
                return true;
            case R.id.read_aloud:
                mCompassService.readHeadingAloud();
                return true;
            case R.id.stop:
                stopService(new Intent(this, CompassService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scanQrCode() {
        Intent objIntent = new Intent("com.google.zxing.client.android.SCAN");
        objIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(objIntent, 0);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        unbindService(mConnection);

        // We must call finish() from this method to ensure that the activity ends either when an
        // item is selected from the menu or when the menu is dismissed by swiping down.
        finish();
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText.
            Toast.makeText(this, spokenText, Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
