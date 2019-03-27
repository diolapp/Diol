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

package app.diol.dialer.enrichedcall.simulator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.enrichedcall.EnrichedCallManager;
import app.diol.dialer.enrichedcall.EnrichedCallManager.StateChangedListener;

/**
 * Activity used to display Enriched call sessions that are currently in memory, and create new
 * outgoing sessions with various bits of data.
 *
 * <p>This activity will dynamically refresh as new sessions are added or updated, but there's no
 * update when sessions are deleted from memory. Use the refresh button to update the view.
 */
public class EnrichedCallSimulatorActivity extends AppCompatActivity
        implements StateChangedListener, OnClickListener {

    private Button refreshButton;
    private SessionsAdapter sessionsAdapter;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(Assert.isNotNull(context), EnrichedCallSimulatorActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        LogUtil.enterBlock("EnrichedCallSimulatorActivity.onCreate");
        super.onCreate(bundle);
        setContentView(R.layout.enriched_call_simulator_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.enriched_call_simulator_activity);

        refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.sessions_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sessionsAdapter = new SessionsAdapter();
        sessionsAdapter.setSessionStrings(getEnrichedCallManager().getAllSessionsForDisplay());
        recyclerView.setAdapter(sessionsAdapter);
    }

    @Override
    protected void onResume() {
        LogUtil.enterBlock("EnrichedCallSimulatorActivity.onResume");
        super.onResume();
        getEnrichedCallManager().registerStateChangedListener(this);
    }

    @Override
    protected void onPause() {
        LogUtil.enterBlock("EnrichedCallSimulatorActivity.onPause");
        super.onPause();
        getEnrichedCallManager().unregisterStateChangedListener(this);
    }

    @Override
    public void onEnrichedCallStateChanged() {
        LogUtil.enterBlock("EnrichedCallSimulatorActivity.onEnrichedCallStateChanged");
        refreshSessions();
    }

    @Override
    public void onClick(View v) {
        if (v == refreshButton) {
            LogUtil.i("EnrichedCallSimulatorActivity.onClick", "refreshing sessions");
            refreshSessions();
        }
    }

    private void refreshSessions() {
        sessionsAdapter.setSessionStrings(getEnrichedCallManager().getAllSessionsForDisplay());
        sessionsAdapter.notifyDataSetChanged();
    }

    private EnrichedCallManager getEnrichedCallManager() {
        return EnrichedCallComponent.get(this).getEnrichedCallManager();
    }
}
