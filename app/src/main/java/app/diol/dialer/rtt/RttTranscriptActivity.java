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

package app.diol.dialer.rtt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.common.concurrent.SupportUiListener;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.protos.ProtoParsers;
import app.diol.dialer.widget.DialerToolbar;

/**
 * Activity holds RTT transcript.
 */
public class RttTranscriptActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSCRIPT_ID = "extra_transcript_id";
    public static final String EXTRA_PRIMARY_TEXT = "extra_primary_text";
    public static final String EXTRA_PHOTO_INFO = "extra_photo_info";

    private RttTranscriptAdapter adapter;
    private SupportUiListener<RttTranscript> rttTranscriptUiListener;
    private DialerToolbar toolbar;

    public static Intent getIntent(
            Context context, String transcriptId, String primaryText, PhotoInfo photoInfo) {
        Intent intent = new Intent(context, RttTranscriptActivity.class);
        intent.putExtra(RttTranscriptActivity.EXTRA_TRANSCRIPT_ID, transcriptId);
        intent.putExtra(RttTranscriptActivity.EXTRA_PRIMARY_TEXT, primaryText);
        ProtoParsers.put(intent, RttTranscriptActivity.EXTRA_PHOTO_INFO, Assert.isNotNull(photoInfo));
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_rtt_transcript);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getColor(R.color.rtt_transcript_primary_color));
        getWindow().setStatusBarColor(getColor(R.color.rtt_transcript_primary_color_dark));

        RecyclerView recyclerView = findViewById(R.id.rtt_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RttTranscriptAdapter(this);
        recyclerView.setAdapter(adapter);

        rttTranscriptUiListener =
                DialerExecutorComponent.get(this)
                        .createUiListener(getSupportFragmentManager(), "Load RTT transcript");
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Assert.checkArgument(intent.hasExtra(EXTRA_TRANSCRIPT_ID));
        Assert.checkArgument(intent.hasExtra(EXTRA_PRIMARY_TEXT));
        Assert.checkArgument(intent.hasExtra(EXTRA_PHOTO_INFO));

        String id = intent.getStringExtra(EXTRA_TRANSCRIPT_ID);
        rttTranscriptUiListener.listen(
                this,
                RttTranscriptUtil.loadRttTranscript(this, id),
                adapter::setRttTranscript,
                throwable -> {
                    throw new RuntimeException(throwable);
                });

        String primaryText = intent.getStringExtra(EXTRA_PRIMARY_TEXT);
        toolbar.setTitle(primaryText);

        PhotoInfo photoInfo =
                ProtoParsers.getTrusted(intent, EXTRA_PHOTO_INFO, PhotoInfo.getDefaultInstance());
        // Photo shown here shouldn't have video or RTT badge.
        PhotoInfo sanitizedPhotoInfo =
                PhotoInfo.newBuilder().mergeFrom(photoInfo).setIsRtt(false).setIsVideo(false).build();
        adapter.setPhotoInfo(sanitizedPhotoInfo);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
