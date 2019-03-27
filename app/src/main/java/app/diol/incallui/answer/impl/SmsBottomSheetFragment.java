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

package app.diol.incallui.answer.impl;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.diol.R;
import app.diol.dialer.common.DpUtil;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.incallui.incalluilock.InCallUiLock;

/**
 * Shows options for rejecting call with SMS
 */
public class SmsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_OPTIONS = "options";

    private InCallUiLock inCallUiLock;

    public static SmsBottomSheetFragment newInstance(@Nullable ArrayList<CharSequence> options) {
        SmsBottomSheetFragment fragment = new SmsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putCharSequenceArrayList(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        List<CharSequence> items = getArguments().getCharSequenceArrayList(ARG_OPTIONS);
        if (items != null) {
            for (CharSequence item : items) {
                layout.addView(newTextViewItem(item));
            }
        }
        layout.addView(newTextViewItem(null));
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentUtils.checkParent(this, SmsSheetHolder.class);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LogUtil.i("SmsBottomSheetFragment.onCreateDialog", null);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        inCallUiLock =
                FragmentUtils.getParentUnsafe(SmsBottomSheetFragment.this, SmsSheetHolder.class)
                        .acquireInCallUiLock("SmsBottomSheetFragment");
        return dialog;
    }

    private TextView newTextViewItem(@Nullable final CharSequence text) {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        Context context = new ContextThemeWrapper(getContext(), getTheme());
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        Drawable background = typedArray.getDrawable(0);
        // noinspection ResourceType
        typedArray.recycle();

        TextView textView = new TextView(context);
        textView.setText(text == null ? getString(R.string.call_incoming_message_custom) : text);
        int padding = (int) DpUtil.dpToPx(context, 16);
        textView.setPadding(padding, padding, padding, padding);
        textView.setBackground(background);
        textView.setTextColor(context.getColor(R.color.blue_grey_100));
        textView.setTextAppearance(R.style.TextAppearance_AppCompat_Widget_PopupMenu_Large);

        LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);

        textView.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentUtils.getParentUnsafe(SmsBottomSheetFragment.this, SmsSheetHolder.class)
                                .smsSelected(text);
                        dismiss();
                    }
                });
        return textView;
    }

    @Override
    public int getTheme() {
        return R.style.Theme_Design_Light_BottomSheetDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        FragmentUtils.getParentUnsafe(this, SmsSheetHolder.class).smsDismissed();
        inCallUiLock.release();
    }

    /**
     * Callback interface for {@link SmsBottomSheetFragment}
     */
    public interface SmsSheetHolder {

        InCallUiLock acquireInCallUiLock(String tag);

        void smsSelected(@Nullable CharSequence text);

        void smsDismissed();
    }
}
