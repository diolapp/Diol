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

package app.diol.dialer.precall.impl;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager.LayoutParams;

/**
 * A transparent activity to host dialogs for {@link PreCallCoordinatorImpl}
 */
public class PreCallActivity extends AppCompatActivity {

    private PreCallCoordinatorImpl preCallCoordinator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preCallCoordinator = new PreCallCoordinatorImpl(this);
        preCallCoordinator.onCreate(getIntent(), savedInstanceState);
        if (getSystemService(KeyguardManager.class).isKeyguardLocked()) {
            // Note:
            //
            // Flag LayoutParams.FLAG_TURN_SCREEN_ON was deprecated in O_MR1, but calling the new API
            // setTurnScreenOn(true) doesn't give us the expected behavior.
            //
            // Calling setTurnScreenOn(true) alone doesn't turn on the screen when the device is locked.
            // We must also call KeyguardManager#requestDismissKeyguard, which will bring up the lock
            // screen for the user to enter their credentials.
            //
            // If the Keyguard is not secure or the device is currently in a trusted state, calling
            // requestDismissKeyguard will immediately dismiss the Keyguard without any user interaction.
            // However, the lock screen will still pop up before it quickly disappears.
            //
            // If the Keyguard is secure and the device is not in a trusted state, the device will show
            // the lock screen and wait for the user's credentials.
            //
            // Therefore, to avoid showing the lock screen, we will continue using the deprecated flag in
            // O_MR1 and later Android versions.
            //
            // Flag LayoutParams.FLAG_SHOW_WHEN_LOCKED was also deprecated in O_MR1, and the new API
            // setShowWhenLocked(boolean) works. However, as the purpose of the two new APIs is to prevent
            // an unintentional double life-cycle event, only using one is ineffective.
            //
            // Therefore, to simplify code and make testing easier, we will also keep using
            // LayoutParams.FLAG_SHOW_WHEN_LOCKED.
            getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        preCallCoordinator.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        preCallCoordinator.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        preCallCoordinator.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        preCallCoordinator.onSaveInstanceState(outState);
    }
}
