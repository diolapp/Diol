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

package app.diol.dialer.metrics.jank;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import app.diol.dialer.metrics.Metrics;

/**
 * Logs jank for {@link RecyclerView} scrolling events.
 */
public final class RecyclerViewJankLogger extends OnScrollListener {

    private final Metrics metrics;
    private final String eventName;

    private boolean isScrolling;

    public RecyclerViewJankLogger(Metrics metrics, String eventName) {
        this.metrics = metrics;
        this.eventName = eventName;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (!isScrolling && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            isScrolling = true;
            metrics.startJankRecorder(eventName);
        } else if (isScrolling && newState == RecyclerView.SCROLL_STATE_IDLE) {
            isScrolling = false;
            metrics.stopJankRecorder(eventName);
        }
    }
}
