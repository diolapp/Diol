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

package app.diol.incallui.rtt.protocol;

import android.support.annotation.NonNull;

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.diol.dialer.common.Assert;
import app.diol.dialer.rtt.RttTranscript;
import app.diol.dialer.rtt.RttTranscriptMessage;

/**
 * Message class that holds one RTT chat content.
 */
public final class RttChatMessage {

    private static final Splitter SPLITTER = Splitter.on(Constants.BUBBLE_BREAKER);
    private final StringBuilder content = new StringBuilder();
    public boolean isRemote;
    private long timstamp;
    private boolean isFinished;

    public RttChatMessage() {
        timstamp = System.currentTimeMillis();
    }

    /**
     * Computes delta change of two string.
     *
     * <p>e.g. "hello world" -> "hello" : "\b\b\b\b\b\b"
     *
     * <p>"hello world" -> "hello mom!" : "\b\b\b\b\bmom!"
     *
     * <p>"hello world" -> "hello d" : "\b\b\b\b\bd"
     *
     * <p>"hello world" -> "hello new world" : "\b\b\b\b\bnew world"
     */
    public static String computeChangedString(String oldMessage, String newMesssage) {
        StringBuilder modify = new StringBuilder();
        int indexChangeStart = 0;
        while (indexChangeStart < oldMessage.length()
                && indexChangeStart < newMesssage.length()
                && oldMessage.charAt(indexChangeStart) == newMesssage.charAt(indexChangeStart)) {
            indexChangeStart++;
        }
        for (int i = indexChangeStart; i < oldMessage.length(); i++) {
            modify.append('\b');
        }
        for (int i = indexChangeStart; i < newMesssage.length(); i++) {
            modify.append(newMesssage.charAt(i));
        }
        return modify.toString();
    }

    public static RttTranscript getRttTranscriptWithNewRemoteMessage(
            RttTranscript rttTranscript, @NonNull String text) {
        List<RttChatMessage> messageList = fromTranscript(rttTranscript);
        updateRemoteRttChatMessage(messageList, text);
        return RttTranscript.newBuilder()
                .setId(rttTranscript.getId())
                .setNumber(rttTranscript.getNumber())
                .setTimestamp(rttTranscript.getTimestamp())
                .addAllMessages(toTranscriptMessageList(messageList))
                .build();
    }

    /**
     * Update list of {@code RttChatMessage} based on given remote text.
     */
    public static void updateRemoteRttChatMessage(
            List<RttChatMessage> messageList, @NonNull String text) {
        Assert.isNotNull(messageList);
        Iterator<String> splitText = SPLITTER.split(text).iterator();

        while (splitText.hasNext()) {
            String singleMessageContent = splitText.next();
            RttChatMessage message;
            int index = getLastIndexUnfinishedRemoteMessage(messageList);
            if (index < 0) {
                message = new RttChatMessage();
                message.append(singleMessageContent);
                message.isRemote = true;
                if (splitText.hasNext()) {
                    message.finish();
                }
                if (message.content.length() != 0) {
                    messageList.add(message);
                }
            } else {
                message = messageList.get(index);
                message.append(singleMessageContent);
                if (splitText.hasNext()) {
                    message.finish();
                }
                if (message.content.length() == 0) {
                    messageList.remove(index);
                }
            }
            StringBuilder content = message.content;
            // Delete previous messages.
            while (content.length() > 0 && content.charAt(0) == '\b') {
                messageList.remove(message);
                content.delete(0, 1);
                int previous = getLastIndexRemoteMessage(messageList);
                // There are more backspaces than existing characters.
                if (previous < 0) {
                    while (content.length() > 0 && content.charAt(0) == '\b') {
                        content.deleteCharAt(0);
                    }
                    // Add message if there are still characters after backspaces.
                    if (content.length() > 0) {
                        message = new RttChatMessage();
                        message.append(content.toString());
                        message.isRemote = true;
                        if (splitText.hasNext()) {
                            message.finish();
                        }
                        messageList.add(message);
                    }
                    break;
                }
                message = messageList.get(previous);
                message.unfinish();
                message.append(content.toString());
                content = message.content;
            }
        }
        if (text.endsWith(Constants.BUBBLE_BREAKER)) {
            int lastIndexRemoteMessage = getLastIndexRemoteMessage(messageList);
            messageList.get(lastIndexRemoteMessage).finish();
        }
    }

    private static int getLastIndexUnfinishedRemoteMessage(List<RttChatMessage> messageList) {
        int i = messageList.size() - 1;
        while (i >= 0 && (!messageList.get(i).isRemote || messageList.get(i).isFinished)) {
            i--;
        }
        return i;
    }

    public static int getLastIndexRemoteMessage(List<RttChatMessage> messageList) {
        int i = messageList.size() - 1;
        while (i >= 0 && !messageList.get(i).isRemote) {
            i--;
        }
        return i;
    }

    public static int getLastIndexLocalMessage(List<RttChatMessage> messageList) {
        int i = messageList.size() - 1;
        while (i >= 0 && messageList.get(i).isRemote) {
            i--;
        }
        return i;
    }

    public static List<RttTranscriptMessage> toTranscriptMessageList(
            List<RttChatMessage> messageList) {
        List<RttTranscriptMessage> transcriptMessageList = new ArrayList<>();
        for (RttChatMessage message : messageList) {
            transcriptMessageList.add(
                    RttTranscriptMessage.newBuilder()
                            .setContent(message.getContent())
                            .setTimestamp(message.timstamp)
                            .setIsRemote(message.isRemote)
                            .setIsFinished(message.isFinished)
                            .build());
        }
        return transcriptMessageList;
    }

    public static List<RttChatMessage> fromTranscript(RttTranscript rttTranscript) {
        List<RttChatMessage> messageList = new ArrayList<>();
        if (rttTranscript == null) {
            return messageList;
        }
        for (RttTranscriptMessage message : rttTranscript.getMessagesList()) {
            RttChatMessage chatMessage = new RttChatMessage();
            chatMessage.append(message.getContent());
            chatMessage.timstamp = message.getTimestamp();
            chatMessage.isRemote = message.getIsRemote();
            if (message.getIsFinished()) {
                chatMessage.finish();
            }
            messageList.add(chatMessage);
        }
        return messageList;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void finish() {
        isFinished = true;
    }

    public void unfinish() {
        isFinished = false;
    }

    public void append(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\b' && content.length() > 0 && content.charAt(content.length() - 1) != '\b') {
                content.deleteCharAt(content.length() - 1);
            } else {
                content.append(c);
            }
        }
    }

    public String getContent() {
        return content.toString();
    }
}