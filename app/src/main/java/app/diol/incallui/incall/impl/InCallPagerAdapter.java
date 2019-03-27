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

package app.diol.incallui.incall.impl;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import app.diol.dialer.common.Assert;
import app.diol.dialer.multimedia.MultimediaData;
import app.diol.incallui.sessiondata.MultimediaFragment;

/**
 * View pager adapter for in call ui.
 */
public class InCallPagerAdapter extends FragmentStatePagerAdapter {

    private final boolean showInCallButtonGrid;
    @Nullable
    private MultimediaData attachments;

    public InCallPagerAdapter(
            FragmentManager fragmentManager,
            @Nullable MultimediaData attachments,
            boolean showInCallButtonGrid) {
        super(fragmentManager);
        this.attachments = attachments;
        this.showInCallButtonGrid = showInCallButtonGrid;
    }

    @Override
    public Fragment getItem(int position) {
        if (!showInCallButtonGrid) {
            // TODO(calderwoodra): handle fragment invalidation for when the data changes.
            return MultimediaFragment.newInstance(
                    attachments, true /* isInteractive */, false /* showAvatar */, false /* isSpam */);

        } else if (position == getButtonGridPosition()) {
            return InCallButtonGridFragment.newInstance();

        } else {
            return MultimediaFragment.newInstance(
                    attachments, true /* isInteractive */, false /* showAvatar */, false /* isSpam */);
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        if (showInCallButtonGrid) {
            count++;
        }
        if (attachments != null && attachments.hasData()) {
            count++;
        }
        Assert.checkArgument(count > 0, "InCallPager adapter doesn't have any pages.");
        return count;
    }

    public void setAttachments(@Nullable MultimediaData attachments) {
        if (this.attachments != attachments) {
            this.attachments = attachments;
            notifyDataSetChanged();
        }
    }

    public int getButtonGridPosition() {
        return getCount() - 1;
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }
}
