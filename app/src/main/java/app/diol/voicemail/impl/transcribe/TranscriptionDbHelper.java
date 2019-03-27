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
package app.diol.voicemail.impl.transcribe;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.VoicemailContract.Voicemails;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.compat.android.provider.VoicemailCompat;

/**
 * Helper class for reading and writing transcription data in the database
 */
@TargetApi(Build.VERSION_CODES.O)
public class TranscriptionDbHelper {
    @VisibleForTesting
    static final String[] PROJECTION = new String[]{Voicemails._ID, // 0
            Voicemails.TRANSCRIPTION, // 1
            VoicemailCompat.TRANSCRIPTION_STATE // 2
    };

    static final int ID = 0;
    static final int TRANSCRIPTION = 1;
    static final int TRANSCRIPTION_STATE = 2;

    private final ContentResolver contentResolver;
    private final Uri uri;

    TranscriptionDbHelper(Context context, Uri uri) {
        Assert.isNotNull(uri);
        this.contentResolver = context.getContentResolver();
        this.uri = uri;
    }

    TranscriptionDbHelper(Context context) {
        this(context, Voicemails.buildSourceUri(context.getPackageName()));
    }

    @WorkerThread
    Pair<String, Integer> getTranscriptionAndState() {
        Assert.checkState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        Assert.isWorkerThread();
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, null, null, null)) {
            if (cursor == null) {
                LogUtil.e("TranscriptionDbHelper.getTranscriptionAndState", "query failed.");
                return null;
            }

            if (cursor.moveToFirst()) {
                String transcription = cursor.getString(TRANSCRIPTION);
                int transcriptionState = cursor.getInt(TRANSCRIPTION_STATE);
                return new Pair<>(transcription, transcriptionState);
            }
        }
        LogUtil.i("TranscriptionDbHelper.getTranscriptionAndState", "query returned no results");
        return null;
    }

    @WorkerThread
    List<Uri> getUntranscribedVoicemails() {
        Assert.checkState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        Assert.isWorkerThread();
        List<Uri> untranscribed = new ArrayList<>();
        String whereClause = "(" + Voicemails.TRANSCRIPTION + " is NULL OR " + Voicemails.TRANSCRIPTION + " = '')" + " AND "
                + VoicemailCompat.TRANSCRIPTION_STATE + "=?";
        String[] whereArgs = {String.valueOf(VoicemailCompat.TRANSCRIPTION_NOT_STARTED)};
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, whereClause, whereArgs, null)) {
            if (cursor == null) {
                LogUtil.e("TranscriptionDbHelper.getUntranscribedVoicemails", "query failed.");
            } else {
                while (cursor.moveToNext()) {
                    untranscribed.add(ContentUris.withAppendedId(uri, cursor.getLong(ID)));
                }
            }
        }
        return untranscribed;
    }

    @WorkerThread
    List<Uri> getTranscribingVoicemails() {
        Assert.checkState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        Assert.isWorkerThread();
        List<Uri> inProgress = new ArrayList<>();
        String whereClause = VoicemailCompat.TRANSCRIPTION_STATE + "=?";
        String[] whereArgs = {String.valueOf(VoicemailCompat.TRANSCRIPTION_IN_PROGRESS)};
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, whereClause, whereArgs, null)) {
            if (cursor == null) {
                LogUtil.e("TranscriptionDbHelper.getTranscribingVoicemails", "query failed.");
            } else {
                while (cursor.moveToNext()) {
                    inProgress.add(ContentUris.withAppendedId(uri, cursor.getLong(ID)));
                }
            }
        }
        return inProgress;
    }

    @WorkerThread
    void setTranscriptionState(int transcriptionState) {
        Assert.isWorkerThread();
        LogUtil.i("TranscriptionDbHelper.setTranscriptionState", "uri: " + uri + ", state: " + transcriptionState);
        ContentValues values = new ContentValues();
        values.put(VoicemailCompat.TRANSCRIPTION_STATE, transcriptionState);
        updateDatabase(values);
    }

    @WorkerThread
    void setTranscriptionAndState(String transcription, int transcriptionState) {
        Assert.isWorkerThread();
        LogUtil.i("TranscriptionDbHelper.setTranscriptionAndState", "uri: " + uri + ", state: " + transcriptionState);
        ContentValues values = new ContentValues();
        values.put(Voicemails.TRANSCRIPTION, transcription);
        values.put(VoicemailCompat.TRANSCRIPTION_STATE, transcriptionState);
        updateDatabase(values);
    }

    private void updateDatabase(ContentValues values) {
        int updatedCount = contentResolver.update(uri, values, null, null);
        if (updatedCount != 1) {
            LogUtil.e("TranscriptionDbHelper.updateDatabase",
                    "Wrong row count, should have updated 1 row, was: " + updatedCount);
        }
    }
}
