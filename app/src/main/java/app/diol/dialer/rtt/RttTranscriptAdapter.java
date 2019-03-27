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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.glidephotomanager.PhotoInfo;

/**
 * Adapter class for holding RTT chat data.
 */
public class RttTranscriptAdapter extends RecyclerView.Adapter<RttTranscriptMessageViewHolder> {

    private final Context context;
    private PhotoInfo photoInfo;
    private RttTranscript rttTranscript;
    private int firstPositionToShowTimestamp;

    RttTranscriptAdapter(Context context) {
        this.context = context;
    }

    /**
     * Returns first position of message that should show time stamp. This is usually the last one of
     * first grouped messages.
     */
    protected static int findFirstPositionToShowTimestamp(RttTranscript rttTranscript) {
        int i = 0;
        while (i + 1 < rttTranscript.getMessagesCount()
                && rttTranscript.getMessages(i).getIsRemote()
                == rttTranscript.getMessages(i + 1).getIsRemote()) {
            i++;
        }
        return i;
    }

    @Override
    public RttTranscriptMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.rtt_transcript_list_item, parent, false);
        return new RttTranscriptMessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RttTranscriptMessageViewHolder rttChatMessageViewHolder, int i) {
        boolean isSameGroup = false;
        boolean hasMoreInSameGroup = false;
        RttTranscriptMessage rttTranscriptMessage = rttTranscript.getMessages(i);
        if (i > 0) {
            isSameGroup =
                    rttTranscriptMessage.getIsRemote() == rttTranscript.getMessages(i - 1).getIsRemote();
        }
        if (i + 1 < getItemCount()) {
            hasMoreInSameGroup =
                    rttTranscriptMessage.getIsRemote() == rttTranscript.getMessages(i + 1).getIsRemote();
        }
        rttChatMessageViewHolder.setMessage(rttTranscriptMessage, isSameGroup, photoInfo);
        if (hasMoreInSameGroup) {
            rttChatMessageViewHolder.hideTimestamp();
        } else {
            rttChatMessageViewHolder.showTimestamp(
                    rttTranscriptMessage.getTimestamp(),
                    rttTranscriptMessage.getIsRemote(),
                    i == firstPositionToShowTimestamp);
        }
    }

    @Override
    public int getItemCount() {
        if (rttTranscript == null) {
            return 0;
        }
        return rttTranscript.getMessagesCount();
    }

    void setRttTranscript(RttTranscript rttTranscript) {
        if (rttTranscript == null) {
            LogUtil.w("RttTranscriptAdapter.setRttTranscript", "null RttTranscript");
            return;
        }
        this.rttTranscript = rttTranscript;
        firstPositionToShowTimestamp = findFirstPositionToShowTimestamp(rttTranscript);

        notifyDataSetChanged();
    }

    void setPhotoInfo(PhotoInfo photoInfo) {
        this.photoInfo = photoInfo;
    }
}
