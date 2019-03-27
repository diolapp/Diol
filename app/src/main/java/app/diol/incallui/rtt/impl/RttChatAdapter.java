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

package app.diol.incallui.rtt.impl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import app.diol.R;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.rtt.RttTranscript;
import app.diol.dialer.rtt.RttTranscriptMessage;
import app.diol.incallui.rtt.protocol.RttChatMessage;

/**
 * Adapter class for holding RTT chat data.
 */
public class RttChatAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final int POSITION_ADVISORY = 0;
    private final Context context;
    private final MessageListener messageListener;
    private Drawable avatarDrawable;
    private List<RttChatMessage> rttMessages = new ArrayList<>();
    private int lastIndexOfLocalMessage = -1;
    private boolean shouldShowAdvisory;
    RttChatAdapter(Context context, MessageListener listener) {
        this.context = context;
        this.messageListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, @RowType int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        switch (viewType) {
            case RowType.ADVISORY:
                View view = layoutInflater.inflate(R.layout.rtt_transcript_advisory, parent, false);
                return new AdvisoryViewHolder(view);
            case RowType.MESSAGE:
                view = layoutInflater.inflate(R.layout.rtt_chat_list_item, parent, false);
                return new RttChatMessageViewHolder(view);
            default:
                throw new RuntimeException("Unknown row type.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (shouldShowAdvisory && position == POSITION_ADVISORY) {
            return RowType.ADVISORY;
        } else {
            return RowType.MESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int itemPosition) {
        switch (getItemViewType(itemPosition)) {
            case RowType.ADVISORY:
                return;
            case RowType.MESSAGE:
                RttChatMessageViewHolder rttChatMessageViewHolder = (RttChatMessageViewHolder) viewHolder;
                int messagePosition = toMessagePosition(itemPosition);
                boolean isSameGroup = false;
                if (messagePosition > 0) {
                    isSameGroup =
                            rttMessages.get(messagePosition).isRemote
                                    == rttMessages.get(messagePosition - 1).isRemote;
                }
                rttChatMessageViewHolder.setMessage(
                        rttMessages.get(messagePosition), isSameGroup, avatarDrawable);
                return;
            default:
                throw new RuntimeException("Unknown row type.");
        }
    }

    @Override
    public int getItemCount() {
        return shouldShowAdvisory ? rttMessages.size() + 1 : rttMessages.size();
    }

    private void updateCurrentLocalMessage(String newMessage) {
        RttChatMessage rttChatMessage = null;
        if (lastIndexOfLocalMessage >= 0) {
            rttChatMessage = rttMessages.get(lastIndexOfLocalMessage);
        }
        if (rttChatMessage == null || rttChatMessage.isFinished()) {
            rttChatMessage = new RttChatMessage();
            rttChatMessage.append(newMessage);
            rttMessages.add(rttChatMessage);
            lastIndexOfLocalMessage = rttMessages.size() - 1;
            notifyItemInserted(toItemPosition(lastIndexOfLocalMessage));
        } else {
            rttChatMessage.append(newMessage);
            // Clear empty message bubble.
            if (TextUtils.isEmpty(rttChatMessage.getContent())) {
                rttMessages.remove(lastIndexOfLocalMessage);
                notifyItemRemoved(toItemPosition(lastIndexOfLocalMessage));
                lastIndexOfLocalMessage = -1;
            } else {
                notifyItemChanged(toItemPosition(lastIndexOfLocalMessage));
            }
        }
    }

    private int toMessagePosition(int itemPosition) {
        if (shouldShowAdvisory) {
            return itemPosition - 1;
        } else {
            return itemPosition;
        }
    }

    // Converts position in message list into item position in adapter.
    private int toItemPosition(int messagePosition) {
        if (messagePosition < 0) {
            return messagePosition;
        }
        if (shouldShowAdvisory) {
            return messagePosition + 1;
        } else {
            return messagePosition;
        }
    }

    private void updateCurrentRemoteMessage(String newMessage) {
        RttChatMessage.updateRemoteRttChatMessage(rttMessages, newMessage);
        lastIndexOfLocalMessage = RttChatMessage.getLastIndexLocalMessage(rttMessages);
        notifyDataSetChanged();
    }

    void addLocalMessage(String message) {
        updateCurrentLocalMessage(message);
        if (messageListener != null) {
            messageListener.onUpdateLocalMessage(toItemPosition(lastIndexOfLocalMessage));
        }
    }

    void submitLocalMessage() {
        LogUtil.enterBlock("RttChatAdapater.submitLocalMessage");
        rttMessages.get(lastIndexOfLocalMessage).finish();
        notifyItemChanged(toItemPosition(lastIndexOfLocalMessage));
        lastIndexOfLocalMessage = -1;
    }

    String computeChangeOfLocalMessage(String newMessage) {
        RttChatMessage rttChatMessage = null;
        if (lastIndexOfLocalMessage >= 0) {
            rttChatMessage = rttMessages.get(lastIndexOfLocalMessage);
        }
        if (rttChatMessage == null || rttChatMessage.isFinished()) {
            return newMessage;
        } else {
            return RttChatMessage.computeChangedString(rttChatMessage.getContent(), newMessage);
        }
    }

    void addRemoteMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        updateCurrentRemoteMessage(message);
        if (messageListener != null) {
            messageListener.onUpdateRemoteMessage(
                    toItemPosition(RttChatMessage.getLastIndexRemoteMessage(rttMessages)));
        }
    }

    void hideAdvisory() {
        shouldShowAdvisory = false;
        notifyItemRemoved(POSITION_ADVISORY);
    }

    void showAdvisory() {
        shouldShowAdvisory = true;
        notifyItemInserted(POSITION_ADVISORY);
    }

    /**
     * Retrieve last local message and update the index. This is used when deleting to previous
     * message bubble.
     */
    @Nullable
    String retrieveLastLocalMessage() {
        lastIndexOfLocalMessage = RttChatMessage.getLastIndexLocalMessage(rttMessages);
        if (lastIndexOfLocalMessage >= 0) {
            RttChatMessage rttChatMessage = rttMessages.get(lastIndexOfLocalMessage);
            rttChatMessage.unfinish();
            return rttChatMessage.getContent();
        } else {
            return null;
        }
    }

    void setAvatarDrawable(Drawable drawable) {
        avatarDrawable = drawable;
    }

    /**
     * Restores RTT chat history from {@code RttTranscript}.
     *
     * @param rttTranscript transcript saved previously.
     * @return last unfinished local message, return null if there is no current editing local
     * message.
     */
    @Nullable
    String onRestoreRttChat(RttTranscript rttTranscript) {
        LogUtil.enterBlock("RttChatAdapater.onRestoreRttChat");
        rttMessages = RttChatMessage.fromTranscript(rttTranscript);
        lastIndexOfLocalMessage = RttChatMessage.getLastIndexLocalMessage(rttMessages);
        notifyDataSetChanged();
        if (lastIndexOfLocalMessage < 0) {
            return null;
        }
        RttChatMessage message = rttMessages.get(lastIndexOfLocalMessage);
        if (!message.isFinished()) {
            return message.getContent();
        } else {
            return null;
        }
    }

    List<RttTranscriptMessage> getRttTranscriptMessageList() {
        return RttChatMessage.toTranscriptMessageList(rttMessages);
    }

    /**
     * IntDef for the different types of rows that can be shown in the call log.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RowType.ADVISORY,
            RowType.MESSAGE,
    })
    @interface RowType {
        /**
         * The transcript advisory message.
         */
        int ADVISORY = 1;

        /**
         * RTT chat message.
         */
        int MESSAGE = 2;
    }

    interface MessageListener {
        void onUpdateRemoteMessage(int position);

        void onUpdateLocalMessage(int position);
    }
}
