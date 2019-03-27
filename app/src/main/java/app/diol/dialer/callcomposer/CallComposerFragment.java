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

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;

/**
 * Base fragment with fields and methods needed for all fragments in the call compose UI.
 */
public abstract class CallComposerFragment extends Fragment {

    protected static final int CAMERA_PERMISSION = 1;
    protected static final int STORAGE_PERMISSION = 2;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (FragmentUtils.getParent(this, CallComposerListener.class) == null) {
            LogUtil.e(
                    "CallComposerFragment.onAttach",
                    "Container activity must implement CallComposerListener.");
            Assert.fail();
        }
    }

    @Nullable
    public CallComposerListener getListener() {
        return FragmentUtils.getParent(this, CallComposerListener.class);
    }

    public abstract boolean shouldHide();

    public abstract void clearComposer();

    /**
     * Interface used to listen to CallComposeFragments
     */
    public interface CallComposerListener {
        /**
         * Let the listener know when a call is ready to be composed.
         */
        void composeCall(CallComposerFragment fragment);

        /**
         * Let the listener know when the layout has changed to full screen
         */
        void showFullscreen(boolean show);

        /**
         * True is the listener is in fullscreen.
         */
        boolean isFullscreen();

        /**
         * True if the layout is in landscape mode.
         */
        boolean isLandscapeLayout();

        /**
         * Tell the listener that call composition is done and we should start the call.
         */
        void sendAndCall();
    }
}
