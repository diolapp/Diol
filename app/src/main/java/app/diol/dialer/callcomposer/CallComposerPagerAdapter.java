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

package app.diol.dialer.callcomposer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.diol.dialer.common.Assert;

/**
 * ViewPager adapter for call compose UI.
 */
public class CallComposerPagerAdapter extends FragmentPagerAdapter {

    public static final int INDEX_CAMERA = 0;
    public static final int INDEX_GALLERY = 1;
    public static final int INDEX_MESSAGE = 2;

    private final int messageComposerCharLimit;

    public CallComposerPagerAdapter(FragmentManager fragmentManager, int messageComposerCharLimit) {
        super(fragmentManager);
        this.messageComposerCharLimit = messageComposerCharLimit;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_MESSAGE:
                return MessageComposerFragment.newInstance(messageComposerCharLimit);
            case INDEX_GALLERY:
                return GalleryComposerFragment.newInstance();
            case INDEX_CAMERA:
                return new CameraComposerFragment();
            default:
                Assert.fail();
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
