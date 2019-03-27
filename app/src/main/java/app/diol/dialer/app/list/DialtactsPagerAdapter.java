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

package app.diol.dialer.app.list;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.IntDef;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.diol.dialer.app.calllog.CallLogFragment;
import app.diol.dialer.app.calllog.VisualVoicemailCallLogFragment;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.contactsfragment.ContactsFragment;
import app.diol.dialer.contactsfragment.ContactsFragment.Header;
import app.diol.dialer.database.CallLogQueryHandler;
import app.diol.dialer.util.ViewUtil;

/**
 * ViewPager adapter for {@link app.diol.dialer.app.DialtactsActivity}.
 */
public class DialtactsPagerAdapter extends FragmentPagerAdapter {

    public static final int TAB_INDEX_SPEED_DIAL = 0;
    public static final int TAB_INDEX_HISTORY = 1;
    public static final int TAB_INDEX_ALL_CONTACTS = 2;
    public static final int TAB_INDEX_VOICEMAIL = 3;
    public static final int TAB_COUNT_DEFAULT = 3;
    public static final int TAB_COUNT_WITH_VOICEMAIL = 4;
    private final List<Fragment> fragments = new ArrayList<>();
    private final String[] tabTitles;
    private OldSpeedDialFragment oldSpeedDialFragment;
    private CallLogFragment callLogFragment;
    private ContactsFragment contactsFragment;
    private CallLogFragment voicemailFragment;
    private boolean hasActiveVoicemailProvider;

    public DialtactsPagerAdapter(
            FragmentManager fm, String[] tabTitles, boolean hasVoicemailProvider) {
        super(fm);
        this.tabTitles = tabTitles;
        hasActiveVoicemailProvider = hasVoicemailProvider;
        fragments.addAll(Collections.nCopies(TAB_COUNT_WITH_VOICEMAIL, null));
    }

    @Override
    public long getItemId(int position) {
        return getRtlPosition(position);
    }

    @Override
    public Fragment getItem(int position) {
        LogUtil.d("ViewPagerAdapter.getItem", "position: %d", position);
        switch (getRtlPosition(position)) {
            case TAB_INDEX_SPEED_DIAL:
                if (oldSpeedDialFragment == null) {
                    oldSpeedDialFragment = new OldSpeedDialFragment();
                }
                return oldSpeedDialFragment;
            case TAB_INDEX_HISTORY:
                if (callLogFragment == null) {
                    callLogFragment = new CallLogFragment(CallLogQueryHandler.CALL_TYPE_ALL);
                }
                return callLogFragment;
            case TAB_INDEX_ALL_CONTACTS:
                if (contactsFragment == null) {
                    contactsFragment = ContactsFragment.newInstance(Header.ADD_CONTACT);
                }
                return contactsFragment;
            case TAB_INDEX_VOICEMAIL:
                if (voicemailFragment == null) {
                    voicemailFragment = new VisualVoicemailCallLogFragment();
                    LogUtil.v(
                            "ViewPagerAdapter.getItem",
                            "new VisualVoicemailCallLogFragment: %s",
                            voicemailFragment);
                }
                return voicemailFragment;
            default:
                throw Assert.createIllegalStateFailException("No fragment at position " + position);
        }
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        LogUtil.d("ViewPagerAdapter.instantiateItem", "position: %d", position);
        // On rotation the FragmentManager handles rotation. Therefore getItem() isn't called.
        // Copy the fragments that the FragmentManager finds so that we can store them in
        // instance variables for later.
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        if (fragment instanceof OldSpeedDialFragment) {
            oldSpeedDialFragment = (OldSpeedDialFragment) fragment;
        } else if (fragment instanceof CallLogFragment && position == TAB_INDEX_HISTORY) {
            callLogFragment = (CallLogFragment) fragment;
        } else if (fragment instanceof ContactsFragment) {
            contactsFragment = (ContactsFragment) fragment;
        } else if (fragment instanceof CallLogFragment && position == TAB_INDEX_VOICEMAIL) {
            voicemailFragment = (CallLogFragment) fragment;
            LogUtil.v("ViewPagerAdapter.instantiateItem", voicemailFragment.toString());
        }
        fragments.set(position, fragment);
        return fragment;
    }

    /**
     * When {@link android.support.v4.view.PagerAdapter#notifyDataSetChanged} is called, this method
     * is called on all pages to determine whether they need to be recreated. When the voicemail tab
     * is removed, the view needs to be recreated by returning POSITION_NONE. If notifyDataSetChanged
     * is called for some other reason, the voicemail tab is recreated only if it is active. All other
     * tabs do not need to be recreated and POSITION_UNCHANGED is returned.
     */
    @Override
    public int getItemPosition(Object object) {
        return !hasActiveVoicemailProvider && fragments.indexOf(object) == TAB_INDEX_VOICEMAIL
                ? POSITION_NONE
                : POSITION_UNCHANGED;
    }

    @Override
    public int getCount() {
        return hasActiveVoicemailProvider ? TAB_COUNT_WITH_VOICEMAIL : TAB_COUNT_DEFAULT;
    }

    @Override
    public CharSequence getPageTitle(@TabIndex int position) {
        return tabTitles[position];
    }

    public int getRtlPosition(int position) {
        if (ViewUtil.isRtl()) {
            return getCount() - 1 - position;
        }
        return position;
    }

    public void removeVoicemailFragment(FragmentManager manager) {
        if (voicemailFragment != null) {
            manager.beginTransaction().remove(voicemailFragment).commitAllowingStateLoss();
            voicemailFragment = null;
        }
    }

    public boolean hasActiveVoicemailProvider() {
        return hasActiveVoicemailProvider;
    }

    public void setHasActiveVoicemailProvider(boolean hasActiveVoicemailProvider) {
        this.hasActiveVoicemailProvider = hasActiveVoicemailProvider;
    }

    /**
     * IntDef for indices of ViewPager tabs.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAB_INDEX_SPEED_DIAL, TAB_INDEX_HISTORY, TAB_INDEX_ALL_CONTACTS, TAB_INDEX_VOICEMAIL})
    public @interface TabIndex {
    }
}
