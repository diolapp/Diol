/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.dialer.voicemail.settings;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.R;

/**
 * Activity for recording a new voicemail greeting
 */
public class RecordVoicemailGreetingActivity extends AppCompatActivity implements OnClickListener {

    public static final int RECORD_GREETING_INIT = 1;
    public static final int RECORD_GREETING_RECORDING = 2;
    public static final int RECORD_GREETING_RECORDED = 3;
    public static final int RECORD_GREETING_PLAYING_BACK = 4;
    public static final int MAX_GREETING_DURATION_MS = 45000;
    private int currentState;
    private int duration;
    private RecordButton recordButton;
    private Button saveButton;
    private Button redoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voicemail_greeting);

        recordButton = findViewById(R.id.record_button);
        saveButton = findViewById(R.id.save_button);
        redoButton = findViewById(R.id.redo_button);

        duration = 0;
        setState(RECORD_GREETING_INIT);
        recordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == recordButton) {
            switch (currentState) {
                case RECORD_GREETING_INIT:
                    setState(RECORD_GREETING_RECORDING);
                    break;
                case RECORD_GREETING_RECORDED:
                    setState(RECORD_GREETING_PLAYING_BACK);
                    break;
                case RECORD_GREETING_RECORDING:
                case RECORD_GREETING_PLAYING_BACK:
                    setState(RECORD_GREETING_RECORDED);
                    break;
                default:
                    break;
            }
        }
    }

    private void setState(@ButtonState int state) {
        currentState = state;

        switch (state) {
            case RECORD_GREETING_INIT:
                recordButton.setState(state);
                recordButton.setTracks(0, 0);
                setSaveRedoButtonsEnabled(false);
                break;
            case RECORD_GREETING_PLAYING_BACK:
            case RECORD_GREETING_RECORDED:
                recordButton.setState(state);
                recordButton.setTracks(0, (float) duration / MAX_GREETING_DURATION_MS);
                setSaveRedoButtonsEnabled(true);
                break;
            case RECORD_GREETING_RECORDING:
                recordButton.setState(state);
                recordButton.setTracks(0, 1f);
                setSaveRedoButtonsEnabled(false);
                break;
            default:
                break;
        }
    }

    /**
     * Enables/Disables save and redo buttons in the layout
     */
    private void setSaveRedoButtonsEnabled(boolean enabled) {
        if (enabled) {
            saveButton.setVisibility(View.VISIBLE);
            redoButton.setVisibility(View.VISIBLE);
        } else {
            saveButton.setVisibility(View.GONE);
            redoButton.setVisibility(View.GONE);
        }
    }

    /**
     * Possible states of RecordButton and RecordVoicemailGreetingActivity
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RECORD_GREETING_INIT,
            RECORD_GREETING_RECORDING,
            RECORD_GREETING_RECORDED,
            RECORD_GREETING_PLAYING_BACK
    })
    public @interface ButtonState {
    }
}
