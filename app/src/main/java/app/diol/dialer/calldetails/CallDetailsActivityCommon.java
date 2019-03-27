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

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.diol.R;
import app.diol.dialer.assisteddialing.ui.AssistedDialingSettingActivity;
import app.diol.dialer.calldetails.CallDetailsEntries.CallDetailsEntry;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.FailureListener;
import app.diol.dialer.common.concurrent.DialerExecutor.SuccessListener;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.concurrent.UiListener;
import app.diol.dialer.common.database.Selection;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.enrichedcall.EnrichedCallManager;
import app.diol.dialer.enrichedcall.historyquery.proto.HistoryResult;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.logging.UiAction;
import app.diol.dialer.performancereport.PerformanceReport;
import app.diol.dialer.postcall.PostCall;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.rtt.RttTranscriptActivity;
import app.diol.dialer.rtt.RttTranscriptUtil;
import app.diol.dialer.theme.base.ThemeComponent;

/**
 * Contains common logic shared between {@link OldCallDetailsActivity} and {@link
 * CallDetailsActivity}.
 */
abstract class CallDetailsActivityCommon extends AppCompatActivity {

    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_HAS_ENRICHED_CALL_DATA = "has_enriched_call_data";
    public static final String EXTRA_CAN_REPORT_CALLER_ID = "can_report_caller_id";
    public static final String EXTRA_CAN_SUPPORT_ASSISTED_DIALING = "can_support_assisted_dialing";

    private final CallDetailsEntryViewHolder.CallDetailsEntryListener callDetailsEntryListener =
            new CallDetailsEntryListener(this);
    private final CallDetailsHeaderViewHolder.CallDetailsHeaderListener callDetailsHeaderListener =
            new CallDetailsHeaderListener(this);
    private final CallDetailsFooterViewHolder.DeleteCallDetailsListener deleteCallDetailsListener =
            new DeleteCallDetailsListener(this);
    private final CallDetailsFooterViewHolder.ReportCallIdListener reportCallIdListener =
            new ReportCallIdListener(this);
    private final EnrichedCallManager.HistoricalDataChangedListener
            enrichedCallHistoricalDataChangedListener =
            new EnrichedCallHistoricalDataChangedListener(this);

    private CallDetailsAdapterCommon adapter;
    private CallDetailsEntries callDetailsEntries;
    private UiListener<ImmutableSet<String>> checkRttTranscriptAvailabilityListener;

    /**
     * Handles the intent that launches {@link OldCallDetailsActivity} or {@link CallDetailsActivity},
     * e.g., extract data from intent extras, start loading data, etc.
     */
    protected abstract void handleIntent(Intent intent);

    /**
     * Creates an adapter for {@link OldCallDetailsActivity} or {@link CallDetailsActivity}.
     */
    protected abstract CallDetailsAdapterCommon createAdapter(
            CallDetailsEntryViewHolder.CallDetailsEntryListener callDetailsEntryListener,
            CallDetailsHeaderViewHolder.CallDetailsHeaderListener callDetailsHeaderListener,
            CallDetailsFooterViewHolder.ReportCallIdListener reportCallIdListener,
            CallDetailsFooterViewHolder.DeleteCallDetailsListener deleteCallDetailsListener);

    /**
     * Returns the phone number of the call details.
     */
    protected abstract String getNumber();

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeComponent.get(this).theme().getApplicationThemeRes());
        setContentView(R.layout.call_details_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.call_details);
        toolbar.setNavigationOnClickListener(
                v -> {
                    PerformanceReport.recordClick(UiAction.Type.CLOSE_CALL_DETAIL_WITH_CANCEL_BUTTON);
                    finish();
                });
        checkRttTranscriptAvailabilityListener =
                DialerExecutorComponent.get(this)
                        .createUiListener(getFragmentManager(), "Query RTT transcript availability");
        handleIntent(getIntent());
        setupRecyclerViewForEntries();
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();

        // Some calls may not be recorded (eg. from quick contact),
        // so we should restart recording after these calls. (Recorded call is stopped)
        PostCall.restartPerformanceRecordingIfARecentCallExist(this);
        if (!PerformanceReport.isRecording()) {
            PerformanceReport.startRecording();
        }

        PostCall.promptUserForMessageIfNecessary(this, findViewById(R.id.recycler_view));

        EnrichedCallComponent.get(this)
                .getEnrichedCallManager()
                .registerHistoricalDataChangedListener(enrichedCallHistoricalDataChangedListener);
        EnrichedCallComponent.get(this)
                .getEnrichedCallManager()
                .requestAllHistoricalData(getNumber(), callDetailsEntries);
    }

    protected void loadRttTranscriptAvailability() {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (CallDetailsEntry entry : callDetailsEntries.getEntriesList()) {
            builder.add(entry.getCallMappingId());
        }
        checkRttTranscriptAvailabilityListener.listen(
                this,
                RttTranscriptUtil.getAvailableRttTranscriptIds(this, builder.build()),
                this::updateCallDetailsEntriesWithRttTranscriptAvailability,
                throwable -> {
                    throw new RuntimeException(throwable);
                });
    }

    private void updateCallDetailsEntriesWithRttTranscriptAvailability(
            ImmutableSet<String> availableTranscripIds) {
        CallDetailsEntries.Builder mutableCallDetailsEntries = CallDetailsEntries.newBuilder();
        for (CallDetailsEntry entry : callDetailsEntries.getEntriesList()) {
            CallDetailsEntry.Builder newEntry = CallDetailsEntry.newBuilder().mergeFrom(entry);
            newEntry.setHasRttTranscript(availableTranscripIds.contains(entry.getCallMappingId()));
            mutableCallDetailsEntries.addEntries(newEntry.build());
        }
        setCallDetailsEntries(mutableCallDetailsEntries.build());
    }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();

        EnrichedCallComponent.get(this)
                .getEnrichedCallManager()
                .unregisterHistoricalDataChangedListener(enrichedCallHistoricalDataChangedListener);
    }

    @Override
    @CallSuper
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
        setupRecyclerViewForEntries();
    }

    private void setupRecyclerViewForEntries() {
        adapter =
                createAdapter(
                        callDetailsEntryListener,
                        callDetailsHeaderListener,
                        reportCallIdListener,
                        deleteCallDetailsListener);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        PerformanceReport.logOnScrollStateChange(recyclerView);
    }

    final CallDetailsAdapterCommon getAdapter() {
        return adapter;
    }

    @Override
    @CallSuper
    public void onBackPressed() {
        PerformanceReport.recordClick(UiAction.Type.PRESS_ANDROID_BACK_BUTTON);
        super.onBackPressed();
    }

    protected final CallDetailsEntries getCallDetailsEntries() {
        return callDetailsEntries;
    }

    @MainThread
    protected final void setCallDetailsEntries(CallDetailsEntries entries) {
        Assert.isMainThread();
        this.callDetailsEntries = entries;
        if (adapter != null) {
            adapter.updateCallDetailsEntries(entries);
        }
    }

    /**
     * A {@link Worker} that deletes specified entries from the call log.
     */
    private static final class DeleteCallsWorker implements Worker<CallDetailsEntries, Void> {
        // Use a weak reference to hold the Activity so that there is no memory leak.
        private final WeakReference<Context> contextWeakReference;

        DeleteCallsWorker(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
        }

        private static List<String> getCallLogIdList(CallDetailsEntries callDetailsEntries) {
            Assert.checkArgument(callDetailsEntries.getEntriesCount() > 0);

            List<String> idStrings = new ArrayList<>(callDetailsEntries.getEntriesCount());

            for (CallDetailsEntry entry : callDetailsEntries.getEntriesList()) {
                idStrings.add(String.valueOf(entry.getCallId()));
            }

            return idStrings;
        }

        @Override
        // Suppress the lint check here as the user will not be able to see call log entries if
        // permission.WRITE_CALL_LOG is not granted.
        @SuppressLint("MissingPermission")
        @RequiresPermission(value = permission.WRITE_CALL_LOG)
        public Void doInBackground(CallDetailsEntries callDetailsEntries) {
            Context context = contextWeakReference.get();
            if (context == null) {
                return null;
            }

            Selection selection =
                    Selection.builder()
                            .and(Selection.column(CallLog.Calls._ID).in(getCallLogIdList(callDetailsEntries)))
                            .build();

            context
                    .getContentResolver()
                    .delete(Calls.CONTENT_URI, selection.getSelection(), selection.getSelectionArgs());
            return null;
        }
    }

    private static final class CallDetailsEntryListener
            implements CallDetailsEntryViewHolder.CallDetailsEntryListener {
        private final WeakReference<CallDetailsActivityCommon> activityWeakReference;

        CallDetailsEntryListener(CallDetailsActivityCommon activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void showRttTranscript(String transcriptId, String primaryText, PhotoInfo photoInfo) {
            getActivity()
                    .startActivity(
                            RttTranscriptActivity.getIntent(getActivity(), transcriptId, primaryText, photoInfo));
        }

        private CallDetailsActivityCommon getActivity() {
            return Preconditions.checkNotNull(activityWeakReference.get());
        }
    }

    private static final class CallDetailsHeaderListener
            implements CallDetailsHeaderViewHolder.CallDetailsHeaderListener {
        private final WeakReference<CallDetailsActivityCommon> activityWeakReference;

        CallDetailsHeaderListener(CallDetailsActivityCommon activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void placeImsVideoCall(String phoneNumber) {
            Logger.get(getActivity())
                    .logImpression(DialerImpression.Type.CALL_DETAILS_IMS_VIDEO_CALL_BACK);
            PreCall.start(
                    getActivity(),
                    new CallIntentBuilder(phoneNumber, CallInitiationType.Type.CALL_DETAILS)
                            .setIsVideoCall(true));
        }

        @Override
        public void placeDuoVideoCall(String phoneNumber) {
            Logger.get(getActivity())
                    .logImpression(DialerImpression.Type.CALL_DETAILS_LIGHTBRINGER_CALL_BACK);
            PreCall.start(
                    getActivity(),
                    new CallIntentBuilder(phoneNumber, CallInitiationType.Type.CALL_DETAILS)
                            .setIsDuoCall(true)
                            .setIsVideoCall(true));
        }

        @Override
        public void placeVoiceCall(String phoneNumber, String postDialDigits) {
            Logger.get(getActivity()).logImpression(DialerImpression.Type.CALL_DETAILS_VOICE_CALL_BACK);

            boolean canSupportedAssistedDialing =
                    getActivity()
                            .getIntent()
                            .getExtras()
                            .getBoolean(EXTRA_CAN_SUPPORT_ASSISTED_DIALING, false);
            CallIntentBuilder callIntentBuilder =
                    new CallIntentBuilder(phoneNumber + postDialDigits, CallInitiationType.Type.CALL_DETAILS);
            if (canSupportedAssistedDialing) {
                callIntentBuilder.setAllowAssistedDial(true);
            }

            PreCall.start(getActivity(), callIntentBuilder);
        }

        private CallDetailsActivityCommon getActivity() {
            return Preconditions.checkNotNull(activityWeakReference.get());
        }

        @Override
        public void openAssistedDialingSettings(View unused) {
            Intent intent = new Intent(getActivity(), AssistedDialingSettingActivity.class);
            getActivity().startActivity(intent);
        }

        @Override
        public void createAssistedDialerNumberParserTask(
                AssistedDialingNumberParseWorker worker,
                SuccessListener<Integer> successListener,
                FailureListener failureListener) {
            DialerExecutorComponent.get(getActivity().getApplicationContext())
                    .dialerExecutorFactory()
                    .createUiTaskBuilder(
                            getActivity().getFragmentManager(),
                            "CallDetailsActivityCommon.createAssistedDialerNumberParserTask",
                            new AssistedDialingNumberParseWorker())
                    .onSuccess(successListener)
                    .onFailure(failureListener)
                    .build()
                    .executeParallel(getActivity().getNumber());
        }
    }

    static final class AssistedDialingNumberParseWorker implements Worker<String, Integer> {

        @Override
        public Integer doInBackground(@NonNull String phoneNumber) {
            PhoneNumber parsedNumber;
            try {
                parsedNumber = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
            } catch (NumberParseException e) {
                LogUtil.w(
                        "AssistedDialingNumberParseWorker.doInBackground",
                        "couldn't parse phone number: " + LogUtil.sanitizePii(phoneNumber),
                        e);
                return 0;
            }
            return parsedNumber.getCountryCode();
        }
    }

    private static final class DeleteCallDetailsListener
            implements CallDetailsFooterViewHolder.DeleteCallDetailsListener {

        private final WeakReference<CallDetailsActivityCommon> activityWeakReference;

        DeleteCallDetailsListener(CallDetailsActivityCommon activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void delete() {
            CallDetailsActivityCommon activity = getActivity();
            Logger.get(activity).logImpression(DialerImpression.Type.USER_DELETED_CALL_LOG_ITEM);
            DialerExecutorComponent.get(activity)
                    .dialerExecutorFactory()
                    .createNonUiTaskBuilder(new DeleteCallsWorker(activity))
                    .onSuccess(
                            unused -> {
                                Intent data = new Intent();
                                data.putExtra(EXTRA_PHONE_NUMBER, activity.getNumber());
                                for (CallDetailsEntry entry : activity.getCallDetailsEntries().getEntriesList()) {
                                    if (entry.getHistoryResultsCount() > 0) {
                                        data.putExtra(EXTRA_HAS_ENRICHED_CALL_DATA, true);
                                        break;
                                    }
                                }

                                activity.setResult(RESULT_OK, data);
                                activity.finish();
                            })
                    .build()
                    .executeSerial(activity.getCallDetailsEntries());
        }

        private CallDetailsActivityCommon getActivity() {
            return Preconditions.checkNotNull(activityWeakReference.get());
        }
    }

    private static final class ReportCallIdListener
            implements CallDetailsFooterViewHolder.ReportCallIdListener {
        private final WeakReference<Activity> activityWeakReference;

        ReportCallIdListener(Activity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void reportCallId(String number) {
            ReportDialogFragment.newInstance(number)
                    .show(getActivity().getFragmentManager(), null /* tag */);
        }

        @Override
        public boolean canReportCallerId(String number) {
            return getActivity().getIntent().getExtras().getBoolean(EXTRA_CAN_REPORT_CALLER_ID, false);
        }

        private Activity getActivity() {
            return Preconditions.checkNotNull(activityWeakReference.get());
        }
    }

    private static final class EnrichedCallHistoricalDataChangedListener
            implements EnrichedCallManager.HistoricalDataChangedListener {
        private final WeakReference<CallDetailsActivityCommon> activityWeakReference;

        EnrichedCallHistoricalDataChangedListener(CallDetailsActivityCommon activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        private static CallDetailsEntries generateAndMapNewCallDetailsEntriesHistoryResults(
                @Nullable String number,
                @NonNull CallDetailsEntries callDetailsEntries,
                @NonNull Map<CallDetailsEntry, List<HistoryResult>> mappedResults) {
            if (number == null) {
                return callDetailsEntries;
            }
            CallDetailsEntries.Builder mutableCallDetailsEntries = CallDetailsEntries.newBuilder();
            for (CallDetailsEntry entry : callDetailsEntries.getEntriesList()) {
                CallDetailsEntry.Builder newEntry = CallDetailsEntry.newBuilder().mergeFrom(entry);
                List<HistoryResult> results = mappedResults.get(entry);
                if (results != null) {
                    newEntry.addAllHistoryResults(mappedResults.get(entry));
                    LogUtil.v(
                            "CallDetailsActivityCommon.generateAndMapNewCallDetailsEntriesHistoryResults",
                            "mapped %d results",
                            newEntry.getHistoryResultsList().size());
                }
                mutableCallDetailsEntries.addEntries(newEntry.build());
            }
            return mutableCallDetailsEntries.build();
        }

        @Override
        public void onHistoricalDataChanged() {
            CallDetailsActivityCommon activity = getActivity();
            Map<CallDetailsEntry, List<HistoryResult>> mappedResults =
                    getAllHistoricalData(activity.getNumber(), activity.callDetailsEntries);

            activity.setCallDetailsEntries(
                    generateAndMapNewCallDetailsEntriesHistoryResults(
                            activity.getNumber(), activity.callDetailsEntries, mappedResults));
        }

        private CallDetailsActivityCommon getActivity() {
            return Preconditions.checkNotNull(activityWeakReference.get());
        }

        @NonNull
        private Map<CallDetailsEntry, List<HistoryResult>> getAllHistoricalData(
                @Nullable String number, @NonNull CallDetailsEntries entries) {
            if (number == null) {
                return Collections.emptyMap();
            }

            Map<CallDetailsEntry, List<HistoryResult>> historicalData =
                    EnrichedCallComponent.get(getActivity())
                            .getEnrichedCallManager()
                            .getAllHistoricalData(number, entries);
            if (historicalData == null) {
                return Collections.emptyMap();
            }
            return historicalData;
        }
    }
}
