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

package app.diol.dialer.speeddial;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.speeddial.database.SpeedDialEntry.Channel;
import app.diol.dialer.speeddial.loader.SpeedDialUiItem;

/**
 * {@link PopupMenu} which presents contact options for starred contacts.
 */
public class ContextMenu extends PopupMenu implements OnMenuItemClickListener {

    private final ContextMenuItemListener listener;

    private final SpeedDialUiItem speedDialUiItem;
    private final Channel voiceChannel;
    private final Channel videoChannel;

    private boolean visible;

    private ContextMenu(
            @NonNull Context context,
            @NonNull View anchor,
            ContextMenuItemListener listener,
            SpeedDialUiItem speedDialUiItem) {
        super(context, anchor, Gravity.CENTER);
        this.listener = listener;
        this.speedDialUiItem = speedDialUiItem;
        voiceChannel = speedDialUiItem.getDefaultVoiceChannel();
        videoChannel = speedDialUiItem.getDefaultVideoChannel();

        setOnMenuItemClickListener(this);
        getMenuInflater().inflate(R.menu.starred_contact_context_menu, getMenu());
        getMenu().findItem(R.id.voice_call_container).setVisible(voiceChannel != null);
        getMenu().findItem(R.id.video_call_container).setVisible(videoChannel != null);
        getMenu().findItem(R.id.send_message_container).setVisible(voiceChannel != null);
        if (voiceChannel != null) {
            String secondaryInfo =
                    TextUtils.isEmpty(voiceChannel.label())
                            ? voiceChannel.number()
                            : context.getString(
                            R.string.call_subject_type_and_number,
                            voiceChannel.label(),
                            voiceChannel.number());
            getMenu().findItem(R.id.starred_contact_context_menu_title).setTitle(secondaryInfo);
            getMenu().findItem(R.id.starred_contact_context_menu_title).setVisible(true);
        } else {
            getMenu().findItem(R.id.starred_contact_context_menu_title).setVisible(false);
        }
    }

    /**
     * Creates a new context menu and displays it.
     *
     * @see #show()
     */
    public static ContextMenu show(
            Context context,
            View anchor,
            ContextMenuItemListener contextMenuListener,
            SpeedDialUiItem speedDialUiItem) {
        ContextMenu menu = new ContextMenu(context, anchor, contextMenuListener, speedDialUiItem);
        menu.show();
        menu.visible = true;
        return menu;
    }

    /**
     * Hides the context menu.
     *
     * @see #dismiss()
     */
    public void hide() {
        dismiss();
        visible = false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.voice_call_container) {
            listener.placeCall(Assert.isNotNull(voiceChannel));
        } else if (menuItem.getItemId() == R.id.video_call_container) {
            listener.placeCall(Assert.isNotNull(videoChannel));
        } else if (menuItem.getItemId() == R.id.send_message_container) {
            listener.openSmsConversation(voiceChannel.number());
        } else if (menuItem.getItemId() == R.id.remove_container) {
            listener.removeFavoriteContact(speedDialUiItem);
        } else if (menuItem.getItemId() == R.id.contact_info_container) {
            listener.openContactInfo(speedDialUiItem);
        } else {
            throw Assert.createIllegalStateFailException("Menu option click not handled");
        }
        return true;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public boolean isVisible() {
        return visible;
    }

    /**
     * Listener to report user clicks on menu items.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public interface ContextMenuItemListener {

        /**
         * Called when the user selects "voice call" or "video call" option from the context menu.
         */
        void placeCall(Channel channel);

        /**
         * Called when the user selects "send message" from the context menu.
         */
        void openSmsConversation(String number);

        /**
         * Called when the user selects "remove" from the context menu.
         */
        void removeFavoriteContact(SpeedDialUiItem speedDialUiItem);

        /**
         * Called when the user selects "contact info" from the context menu.
         */
        void openContactInfo(SpeedDialUiItem speedDialUiItem);
    }
}
