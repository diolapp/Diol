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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import app.diol.R;

/**
 * Fragment used to compose call with message fragment.
 */
public class MessageComposerFragment extends CallComposerFragment
        implements OnClickListener, TextWatcher, OnEditorActionListener {
    public static final int NO_CHAR_LIMIT = -1;
    private static final String CHAR_LIMIT_KEY = "char_limit";
    private EditText customMessage;
    private int charLimit;

    public static MessageComposerFragment newInstance(int charLimit) {
        MessageComposerFragment fragment = new MessageComposerFragment();
        Bundle args = new Bundle();
        args.putInt(CHAR_LIMIT_KEY, charLimit);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    public String getMessage() {
        return customMessage == null ? null : customMessage.getText().toString();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        charLimit = getArguments().getInt(CHAR_LIMIT_KEY, NO_CHAR_LIMIT);

        View view = inflater.inflate(R.layout.fragment_message_composer, container, false);
        TextView urgent = (TextView) view.findViewById(R.id.message_urgent);
        customMessage = (EditText) view.findViewById(R.id.custom_message);

        urgent.setOnClickListener(this);
        customMessage.addTextChangedListener(this);
        customMessage.setOnEditorActionListener(this);
        if (charLimit != NO_CHAR_LIMIT) {
            TextView remainingChar = (TextView) view.findViewById(R.id.remaining_characters);
            remainingChar.setText("" + charLimit);
            customMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(charLimit)});
            customMessage.addTextChangedListener(
                    new TextWatcher() {
                        @Override

                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            remainingChar.setText("" + (charLimit - editable.length()));
                        }
                    });
        }
        view.findViewById(R.id.message_chat).setOnClickListener(this);
        view.findViewById(R.id.message_question).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        customMessage.setText(((TextView) view).getText());
        customMessage.setSelection(customMessage.getText().length());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        getListener().composeCall(this);
    }

    @Override
    public boolean shouldHide() {
        return TextUtils.isEmpty(getMessage());
    }

    @Override
    public void clearComposer() {
        customMessage.getText().clear();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (getMessage() == null) {
            return false;
        }
        getListener().sendAndCall();
        return true;
    }
}
