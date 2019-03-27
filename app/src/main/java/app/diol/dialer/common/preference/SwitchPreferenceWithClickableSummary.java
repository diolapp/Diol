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

package app.diol.dialer.common.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.common.Assert;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Utility to allow the summary of a {@link SwitchPreference} to be clicked and opened via a browser
 * to the specified {@link urlToOpen} attribute while maintaining all other aspects of a {@link
 * SwitchPreference}.
 *
 * <p>Example usage:
 *
 * <pre>
 *   <app.diol.dialer.common.preference.SwitchPreferenceWithClickableSummary
 *          android:dependency="...."
 *          android:key="...."
 *          android:title="...."
 *          app:urlToOpen="...."/>
 * </pre>
 */
public class SwitchPreferenceWithClickableSummary extends SwitchPreference {
    private final String urlToOpen;

    public SwitchPreferenceWithClickableSummary(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SwitchPreferenceWithClickableSummary);
        urlToOpen =
                String.valueOf(
                        typedArray.getText(R.styleable.SwitchPreferenceWithClickableSummary_urlToOpen));
    }

    public SwitchPreferenceWithClickableSummary(
            Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, defStyleAttr);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SwitchPreferenceWithClickableSummary);
        urlToOpen =
                String.valueOf(
                        typedArray.getText(R.styleable.SwitchPreferenceWithClickableSummary_urlToOpen));
    }

    public SwitchPreferenceWithClickableSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SwitchPreferenceWithClickableSummary);
        urlToOpen =
                String.valueOf(
                        typedArray.getText(R.styleable.SwitchPreferenceWithClickableSummary_urlToOpen));
    }

    public SwitchPreferenceWithClickableSummary(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Assert.checkArgument(
                urlToOpen != null,
                "must have a urlToOpen attribute when using SwitchPreferenceWithClickableSummary");
        view.findViewById(android.R.id.summary)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                                startActivity(view.getContext(), intent, null);
                            }
                        });
    }
}
