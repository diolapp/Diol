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
import android.view.View;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.calllogutils.CallTypeIconsView;
import app.diol.dialer.widget.BidiTextView;

/**
 * Encapsulates the views that are used to display the details of a phone call in the call log.
 */
public final class PhoneCallDetailsViews {

    public final BidiTextView nameView;
    public final View callTypeView;
    public final CallTypeIconsView callTypeIcons;
    public final TextView callLocationAndDate;
    public final View transcriptionView;
    public final TextView voicemailTranscriptionView;
    public final TextView voicemailTranscriptionBrandingView;
    public final View voicemailTranscriptionRatingView;
    public final TextView callAccountLabel;

    private PhoneCallDetailsViews(
            BidiTextView nameView,
            View callTypeView,
            CallTypeIconsView callTypeIcons,
            TextView callLocationAndDate,
            View transcriptionView,
            TextView voicemailTranscriptionView,
            TextView voicemailTranscriptionBrandingView,
            View voicemailTranscriptionRatingView,
            TextView callAccountLabel) {
        this.nameView = nameView;
        this.callTypeView = callTypeView;
        this.callTypeIcons = callTypeIcons;
        this.callLocationAndDate = callLocationAndDate;
        this.transcriptionView = transcriptionView;
        this.voicemailTranscriptionView = voicemailTranscriptionView;
        this.voicemailTranscriptionBrandingView = voicemailTranscriptionBrandingView;
        this.voicemailTranscriptionRatingView = voicemailTranscriptionRatingView;
        this.callAccountLabel = callAccountLabel;
    }

    /**
     * Create a new instance by extracting the elements from the given view.
     *
     * <p>The view should contain three text views with identifiers {@code R.id.name}, {@code
     * R.id.date}, and {@code R.id.number}, and a linear layout with identifier {@code
     * R.id.call_types}.
     */
    public static PhoneCallDetailsViews fromView(View view) {
        return new PhoneCallDetailsViews(
                (BidiTextView) view.findViewById(R.id.name),
                view.findViewById(R.id.call_type),
                (CallTypeIconsView) view.findViewById(R.id.call_type_icons),
                (TextView) view.findViewById(R.id.call_location_and_date),
                view.findViewById(R.id.transcription),
                (TextView) view.findViewById(R.id.voicemail_transcription),
                (TextView) view.findViewById(R.id.voicemail_transcription_branding),
                view.findViewById(R.id.voicemail_transcription_rating),
                (TextView) view.findViewById(R.id.call_account_label));
    }

    public static PhoneCallDetailsViews createForTest(Context context) {
        return new PhoneCallDetailsViews(
                new BidiTextView(context),
                new View(context),
                new CallTypeIconsView(context),
                new TextView(context),
                new View(context),
                new TextView(context),
                new TextView(context),
                new View(context),
                new TextView(context));
    }
}
