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

package app.diol.dialer.calldetails;

import android.content.Context;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.BuildCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.calldetails.CallDetailsEntries.CallDetailsEntry;
import app.diol.dialer.calllogutils.CallLogDates;
import app.diol.dialer.calllogutils.CallLogDurations;
import app.diol.dialer.calllogutils.CallTypeHelper;
import app.diol.dialer.calllogutils.CallTypeIconsView;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.enrichedcall.historyquery.proto.HistoryResult;
import app.diol.dialer.enrichedcall.historyquery.proto.HistoryResult.Type;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.oem.MotorolaUtils;
import app.diol.dialer.util.DialerUtils;
import app.diol.dialer.util.IntentUtil;

/**
 * ViewHolder for call entries in {@link OldCallDetailsActivity} or {@link CallDetailsActivity}.
 */
public class CallDetailsEntryViewHolder extends ViewHolder {

    private final CallDetailsEntryListener callDetailsEntryListener;
    private final CallTypeIconsView callTypeIcon;
    private final TextView callTypeText;
    private final TextView callTime;
    private final TextView callDuration;
    private final View multimediaImageContainer;
    private final View multimediaDetailsContainer;
    private final View multimediaDivider;
    private final TextView multimediaDetails;
    private final TextView postCallNote;
    private final TextView rttTranscript;
    private final ImageView multimediaImage;
    // TODO(maxwelb): Display this when location is stored - a bug
    @SuppressWarnings("unused")
    private final TextView multimediaAttachmentsNumber;
    private final Context context;

    public CallDetailsEntryViewHolder(
            View container, CallDetailsEntryListener callDetailsEntryListener) {
        super(container);
        context = container.getContext();

        callTypeIcon = (CallTypeIconsView) container.findViewById(R.id.call_direction);
        callTypeText = (TextView) container.findViewById(R.id.call_type);
        callTime = (TextView) container.findViewById(R.id.call_time);
        callDuration = (TextView) container.findViewById(R.id.call_duration);

        multimediaImageContainer = container.findViewById(R.id.multimedia_image_container);
        multimediaDetailsContainer = container.findViewById(R.id.ec_container);
        multimediaDivider = container.findViewById(R.id.divider);
        multimediaDetails = (TextView) container.findViewById(R.id.multimedia_details);
        postCallNote = (TextView) container.findViewById(R.id.post_call_note);
        multimediaImage = (ImageView) container.findViewById(R.id.multimedia_image);
        multimediaAttachmentsNumber =
                (TextView) container.findViewById(R.id.multimedia_attachments_number);
        rttTranscript = container.findViewById(R.id.rtt_transcript);
        this.callDetailsEntryListener = callDetailsEntryListener;
    }

    private static boolean isIncoming(@NonNull HistoryResult historyResult) {
        return historyResult.getType() == Type.INCOMING_POST_CALL
                || historyResult.getType() == Type.INCOMING_CALL_COMPOSER;
    }

    private static @ColorInt
    int getColorForCallType(Context context, int callType) {
        switch (callType) {
            case Calls.OUTGOING_TYPE:
            case Calls.VOICEMAIL_TYPE:
            case Calls.BLOCKED_TYPE:
            case Calls.INCOMING_TYPE:
            case Calls.ANSWERED_EXTERNALLY_TYPE:
            case Calls.REJECTED_TYPE:
                return ContextCompat.getColor(context, R.color.dialer_secondary_text_color);
            case Calls.MISSED_TYPE:
            default:
                // It is possible for users to end up with calls with unknown call types in their
                // call history, possibly due to 3rd party call log implementations (e.g. to
                // distinguish between rejected and missed calls). Instead of crashing, just
                // assume that all unknown call types are missed calls.
                return ContextCompat.getColor(context, R.color.dialer_red);
        }
    }

    void setCallDetails(
            String number,
            String primaryText,
            PhotoInfo photoInfo,
            CallDetailsEntry entry,
            CallTypeHelper callTypeHelper,
            boolean showMultimediaDivider) {
        int callType = entry.getCallType();
        boolean isVideoCall = (entry.getFeatures() & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO;
        boolean isPulledCall =
                (entry.getFeatures() & Calls.FEATURES_PULLED_EXTERNALLY)
                        == Calls.FEATURES_PULLED_EXTERNALLY;
        boolean isDuoCall = entry.getIsDuoCall();
        boolean isRttCall =
                BuildCompat.isAtLeastP()
                        && (entry.getFeatures() & Calls.FEATURES_RTT) == Calls.FEATURES_RTT;

        callTime.setTextColor(getColorForCallType(context, callType));
        callTypeIcon.clear();
        callTypeIcon.add(callType);
        callTypeIcon.setShowVideo(isVideoCall);
        callTypeIcon.setShowHd(
                (entry.getFeatures() & Calls.FEATURES_HD_CALL) == Calls.FEATURES_HD_CALL);
        callTypeIcon.setShowWifi(
                MotorolaUtils.shouldShowWifiIconInCallLog(context, entry.getFeatures()));
        if (BuildCompat.isAtLeastP()) {
            callTypeIcon.setShowRtt((entry.getFeatures() & Calls.FEATURES_RTT) == Calls.FEATURES_RTT);
        }

        callTypeText.setText(
                callTypeHelper.getCallTypeText(callType, isVideoCall, isPulledCall, isDuoCall));
        callTime.setText(CallLogDates.formatDate(context, entry.getDate()));

        if (CallTypeHelper.isMissedCallType(callType)) {
            callDuration.setVisibility(View.GONE);
        } else {
            callDuration.setVisibility(View.VISIBLE);
            callDuration.setText(
                    CallLogDurations.formatDurationAndDataUsage(
                            context, entry.getDuration(), entry.getDataUsage()));
            callDuration.setContentDescription(
                    CallLogDurations.formatDurationAndDataUsageA11y(
                            context, entry.getDuration(), entry.getDataUsage()));
        }
        setMultimediaDetails(number, entry, showMultimediaDivider);
        if (isRttCall) {
            if (entry.getHasRttTranscript()) {
                rttTranscript.setText(R.string.rtt_transcript_link);
                rttTranscript.setTextAppearance(R.style.RttTranscriptLink);
                rttTranscript.setClickable(true);
                rttTranscript.setOnClickListener(
                        v ->
                                callDetailsEntryListener.showRttTranscript(
                                        entry.getCallMappingId(), primaryText, photoInfo));
            } else {
                rttTranscript.setText(R.string.rtt_transcript_not_available);
                rttTranscript.setTextAppearance(R.style.RttTranscriptMessage);
                rttTranscript.setClickable(false);
            }
            rttTranscript.setVisibility(View.VISIBLE);
        } else {
            rttTranscript.setVisibility(View.GONE);
        }
    }

    private void setMultimediaDetails(String number, CallDetailsEntry entry, boolean showDivider) {
        multimediaDivider.setVisibility(showDivider ? View.VISIBLE : View.GONE);
        if (entry.getHistoryResultsList().isEmpty()) {
            LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "no data, hiding UI");
            multimediaDetailsContainer.setVisibility(View.GONE);
        } else {

            HistoryResult historyResult = entry.getHistoryResults(0);
            multimediaDetailsContainer.setVisibility(View.VISIBLE);
            multimediaDetailsContainer.setOnClickListener((v) -> startSmsIntent(context, number));
            multimediaImageContainer.setOnClickListener((v) -> startSmsIntent(context, number));
            multimediaImageContainer.setClipToOutline(true);

            if (!TextUtils.isEmpty(historyResult.getImageUri())) {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "setting image");
                multimediaImageContainer.setVisibility(View.VISIBLE);
                multimediaImage.setImageURI(Uri.parse(historyResult.getImageUri()));
                multimediaDetails.setText(
                        isIncoming(historyResult) ? R.string.received_a_photo : R.string.sent_a_photo);
            } else {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "no image");
            }

            // Set text after image to overwrite the received/sent a photo text
            if (!TextUtils.isEmpty(historyResult.getText())) {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "showing text");
                multimediaDetails.setText(
                        context.getString(R.string.message_in_quotes, historyResult.getText()));
            } else {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "no text");
            }

            if (entry.getHistoryResultsList().size() > 1
                    && !TextUtils.isEmpty(entry.getHistoryResults(1).getText())) {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "showing post call note");
                postCallNote.setVisibility(View.VISIBLE);
                postCallNote.setText(
                        context.getString(R.string.message_in_quotes, entry.getHistoryResults(1).getText()));
                postCallNote.setOnClickListener((v) -> startSmsIntent(context, number));
            } else {
                LogUtil.i("CallDetailsEntryViewHolder.setMultimediaDetails", "no post call note");
            }
        }
    }

    private void startSmsIntent(Context context, String number) {
        DialerUtils.startActivityWithErrorToast(context, IntentUtil.getSendSmsIntent(number));
    }

    /**
     * Listener for the call details header
     */
    interface CallDetailsEntryListener {
        /**
         * Shows RTT transcript.
         */
        void showRttTranscript(String transcriptId, String primaryText, PhotoInfo photoInfo);
    }
}
