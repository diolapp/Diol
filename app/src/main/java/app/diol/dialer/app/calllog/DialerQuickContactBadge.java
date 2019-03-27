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

package app.diol.dialer.app.calllog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.QuickContactBadge;

import app.diol.dialer.app.calllog.CallLogAdapter.OnActionModeStateChangedListener;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * Allows us to click the contact badge for non multi select mode.
 */
class DialerQuickContactBadge extends QuickContactBadge {

    private View.OnClickListener extraOnClickListener;
    private OnActionModeStateChangedListener onActionModeStateChangeListener;

    public DialerQuickContactBadge(Context context) {
        super(context);
    }

    public DialerQuickContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DialerQuickContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(View v) {
        if (extraOnClickListener != null
                && onActionModeStateChangeListener.isActionModeStateEnabled()) {
            Logger.get(v.getContext())
                    .logImpression(DialerImpression.Type.MULTISELECT_SINGLE_PRESS_TAP_VIA_CONTACT_BADGE);
            extraOnClickListener.onClick(v);
        } else {
            super.onClick(v);
        }
    }

    public void setMulitSelectListeners(
            View.OnClickListener extraOnClickListener,
            OnActionModeStateChangedListener actionModeStateChangeListener) {
        this.extraOnClickListener = extraOnClickListener;
        onActionModeStateChangeListener = actionModeStateChangeListener;
    }
}
