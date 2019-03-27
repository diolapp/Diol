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

package app.diol.dialer.main.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import app.diol.R;
import app.diol.dialer.calllog.CallLogComponent;
import app.diol.dialer.calllog.ui.NewCallLogFragment;
import app.diol.dialer.common.concurrent.DefaultFutureCallback;
import app.diol.dialer.main.MainActivityPeer;
import app.diol.dialer.main.impl.bottomnav.BottomNavBar;
import app.diol.dialer.main.impl.bottomnav.BottomNavBar.OnBottomNavTabSelectedListener;
import app.diol.dialer.main.impl.bottomnav.BottomNavBar.TabIndex;
import app.diol.dialer.voicemail.listui.NewVoicemailFragment;

/**
 * MainActivityPeer that implements the new fragments.
 */
public class NewMainActivityPeer implements MainActivityPeer {

    private final MainActivity mainActivity;

    public NewMainActivityPeer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onActivityCreate(Bundle saveInstanceState) {
        mainActivity.setContentView(R.layout.main_activity);
        MainBottomNavBarBottomNavTabListener bottomNavBarBottomNavTabListener =
                new MainBottomNavBarBottomNavTabListener(
                        mainActivity.getSupportFragmentManager(), mainActivity.getApplicationContext());
        BottomNavBar bottomNav = mainActivity.findViewById(R.id.bottom_nav_bar);
        bottomNav.addOnTabSelectedListener(bottomNavBarBottomNavTabListener);
        bottomNav.selectTab(TabIndex.SPEED_DIAL);
    }

    @Override
    public void onActivityResume() {
    }

    @Override
    public void onUserLeaveHint() {
    }

    @Override
    public void onActivityPause() {
    }

    @Override
    public void onActivityStop() {
    }

    @Override
    public void onActivityDestroyed() {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * Implementation of {@link OnBottomNavTabSelectedListener} that handles logic for showing each of
     * the main tabs.
     */
    private static final class MainBottomNavBarBottomNavTabListener
            implements OnBottomNavTabSelectedListener {

        private static final String SPEED_DIAL_TAG = "speed_dial";
        private static final String CALL_LOG_TAG = "call_log";
        private static final String VOICEMAIL_TAG = "voicemail";

        private final FragmentManager supportFragmentManager;
        private final Context appContext;

        private MainBottomNavBarBottomNavTabListener(
                FragmentManager supportFragmentManager, Context appContext) {
            this.supportFragmentManager = supportFragmentManager;
            this.appContext = appContext;
        }

        @Override
        public void onSpeedDialSelected() {
            hideAllFragments();
            // TODO(calderwoodra): Since we aren't using fragment utils in this peer, let's disable
            // speed dial until we figure out a solution.
            // SpeedDialFragment fragment =
            //     (SpeedDialFragment) supportFragmentManager.findFragmentByTag(SPEED_DIAL_TAG);
            // if (fragment == null) {
            //   supportFragmentManager
            //       .beginTransaction()
            //       .add(R.id.fragment_container, SpeedDialFragment.newInstance(), SPEED_DIAL_TAG)
            //       .commit();
            // } else {
            //   supportFragmentManager.beginTransaction().show(fragment).commit();
            // }
        }

        @Override
        public void onCallLogSelected() {
            hideAllFragments();
            NewCallLogFragment fragment =
                    (NewCallLogFragment) supportFragmentManager.findFragmentByTag(CALL_LOG_TAG);
            if (fragment == null) {
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new NewCallLogFragment(), CALL_LOG_TAG)
                        .commit();
            } else {
                supportFragmentManager.beginTransaction().show(fragment).commit();
            }
        }

        @Override
        public void onContactsSelected() {
            hideAllFragments();
            // TODO(calderwoodra): Implement ContactsFragment when FragmentUtils#getParent works
        }

        @Override
        public void onVoicemailSelected() {
            hideAllFragments();
            NewVoicemailFragment fragment =
                    (NewVoicemailFragment) supportFragmentManager.findFragmentByTag(VOICEMAIL_TAG);
            if (fragment == null) {
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new NewVoicemailFragment(), VOICEMAIL_TAG)
                        .commit();
            } else {
                supportFragmentManager.beginTransaction().show(fragment).commit();
            }
        }

        // TODO(calderwoodra): fix overlapping fragments issue
        private void hideAllFragments() {
            FragmentTransaction supportTransaction = supportFragmentManager.beginTransaction();
            Fragment speedDialFragment = supportFragmentManager.findFragmentByTag(SPEED_DIAL_TAG);
            if (speedDialFragment != null) {
                supportTransaction.hide(speedDialFragment);
            }

            Fragment callLogFragment = supportFragmentManager.findFragmentByTag(CALL_LOG_TAG);
            if (callLogFragment != null) {
                if (callLogFragment.isVisible()) {
                    // If the user taps any bottom nav button and the call log is showing, immediately cancel
                    // missed calls (unbold them and clear their notifications).
                    Futures.addCallback(
                            // TODO(zachh): Use dagger to create Peer and MainBottomNavBarBottomNavTabListener.
                            CallLogComponent.get(appContext).getClearMissedCalls().clearAll(),
                            new DefaultFutureCallback<>(),
                            MoreExecutors.directExecutor());
                }
                supportTransaction.hide(callLogFragment);
            }

            if (supportFragmentManager.findFragmentByTag(VOICEMAIL_TAG) != null) {
                supportTransaction.hide(supportFragmentManager.findFragmentByTag(VOICEMAIL_TAG));
            }
            supportTransaction.commit();
        }
    }
}
