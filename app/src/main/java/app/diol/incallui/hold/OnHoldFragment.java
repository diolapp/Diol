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

package app.diol.incallui.hold;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.incallui.incall.protocol.SecondaryInfo;

/**
 * Shows banner UI for background call
 */
public class OnHoldFragment extends Fragment {

    private static final String ARG_INFO = "info";
    private boolean padTopInset = true;
    private int topInset;

    public static OnHoldFragment newInstance(@NonNull SecondaryInfo info) {
        OnHoldFragment fragment = new OnHoldFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_INFO, info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        final View view = layoutInflater.inflate(R.layout.incall_on_hold_banner, viewGroup, false);

        SecondaryInfo secondaryInfo = getArguments().getParcelable(ARG_INFO);
        secondaryInfo = Assert.isNotNull(secondaryInfo);

        ((TextView) view.findViewById(R.id.hold_contact_name))
                .setText(
                        secondaryInfo.nameIsNumber()
                                ? PhoneNumberUtils.createTtsSpannable(
                                BidiFormatter.getInstance()
                                        .unicodeWrap(secondaryInfo.name(), TextDirectionHeuristics.LTR))
                                : secondaryInfo.name());
        ((ImageView) view.findViewById(R.id.hold_phone_icon))
                .setImageResource(
                        secondaryInfo.isVideoCall()
                                ? R.drawable.quantum_ic_videocam_white_18
                                : R.drawable.quantum_ic_phone_paused_vd_theme_24);
        view.addOnAttachStateChangeListener(
                new OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        topInset = v.getRootWindowInsets().getSystemWindowInsetTop();
                        applyInset();
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                    }
                });
        return view;
    }

    public void setPadTopInset(boolean padTopInset) {
        this.padTopInset = padTopInset;
        applyInset();
    }

    private void applyInset() {
        if (getView() == null) {
            return;
        }

        int newPadding = padTopInset ? topInset : 0;
        if (newPadding != getView().getPaddingTop()) {
            TransitionManager.beginDelayedTransition(((ViewGroup) getView().getParent()));
            getView().setPadding(0, newPadding, 0, 0);
        }
    }
}
