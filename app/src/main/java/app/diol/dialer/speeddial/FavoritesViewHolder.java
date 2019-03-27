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
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.glidephotomanager.GlidePhotoManagerComponent;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.speeddial.database.SpeedDialEntry.Channel;
import app.diol.dialer.speeddial.draghelper.SpeedDialFavoritesViewHolderOnTouchListener;
import app.diol.dialer.speeddial.draghelper.SpeedDialFavoritesViewHolderOnTouchListener.OnTouchFinishCallback;
import app.diol.dialer.speeddial.loader.SpeedDialUiItem;

/**
 * ViewHolder for starred/favorite contacts in {@link SpeedDialFragment}.
 */
public class FavoritesViewHolder extends RecyclerView.ViewHolder
        implements OnClickListener, OnLongClickListener, OnTouchFinishCallback {

    private final FavoriteContactsListener listener;

    private final QuickContactBadge photoView;
    private final TextView nameView;
    private final TextView phoneType;
    private final FrameLayout videoCallIcon;

    private final FrameLayout avatarContainer;

    private SpeedDialUiItem speedDialUiItem;

    public FavoritesViewHolder(View view, ItemTouchHelper helper, FavoriteContactsListener listener) {
        super(view);
        photoView = view.findViewById(R.id.avatar);
        nameView = view.findViewById(R.id.name);
        phoneType = view.findViewById(R.id.phone_type);
        videoCallIcon = view.findViewById(R.id.video_call_container);
        avatarContainer = view.findViewById(R.id.avatar_container);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        view.setOnTouchListener(
                new SpeedDialFavoritesViewHolderOnTouchListener(
                        ViewConfiguration.get(view.getContext()), helper, this, this));
        photoView.setClickable(false);
        this.listener = listener;
    }

    public void bind(Context context, SpeedDialUiItem speedDialUiItem) {
        this.speedDialUiItem = Assert.isNotNull(speedDialUiItem);
        Assert.checkArgument(speedDialUiItem.isStarred());

        nameView.setText(speedDialUiItem.name());

        Channel channel = speedDialUiItem.defaultChannel();
        if (channel == null) {
            channel = speedDialUiItem.getDefaultVoiceChannel();
        }

        if (channel != null) {
            phoneType.setText(channel.label());
            videoCallIcon.setVisibility(channel.isVideoTechnology() ? View.VISIBLE : View.GONE);
        } else {
            phoneType.setText("");
            videoCallIcon.setVisibility(View.GONE);
        }

        GlidePhotoManagerComponent.get(context)
                .glidePhotoManager()
                .loadQuickContactBadge(
                        photoView,
                        PhotoInfo.newBuilder()
                                .setPhotoId(speedDialUiItem.photoId())
                                .setPhotoUri(speedDialUiItem.photoUri())
                                .setName(speedDialUiItem.name())
                                .setLookupUri(
                                        Contacts.getLookupUri(speedDialUiItem.contactId(), speedDialUiItem.lookupKey())
                                                .toString())
                                .build());
    }

    @Override
    public void onClick(View v) {
        if (speedDialUiItem.defaultChannel() != null) {
            listener.onClick(speedDialUiItem.defaultChannel());
        } else {
            listener.onAmbiguousContactClicked(speedDialUiItem);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        // TODO(calderwoodra): add bounce/sin wave scale animation
        listener.showContextMenu(photoView, speedDialUiItem);
        return true;
    }

    @Override
    public void onTouchFinished(boolean closeContextMenu) {
        listener.onTouchFinished(closeContextMenu);
    }

    FrameLayout getAvatarContainer() {
        return avatarContainer;
    }

    void onSelectedChanged(boolean selected) {
        nameView.setVisibility(selected ? View.GONE : View.VISIBLE);
        phoneType.setVisibility(selected ? View.GONE : View.VISIBLE);
    }

    /**
     * Listener/callback for {@link FavoritesViewHolder} actions.
     */
    public interface FavoriteContactsListener {

        /**
         * Called when the user clicks on a favorite contact that doesn't have a default number.
         */
        void onAmbiguousContactClicked(SpeedDialUiItem speedDialUiItem);

        /**
         * Called when the user clicks on a favorite contact.
         */
        void onClick(Channel channel);

        /**
         * Called when the user long clicks on a favorite contact.
         */
        void showContextMenu(View view, SpeedDialUiItem speedDialUiItem);

        /**
         * Called when the user is no longer touching the favorite contact.
         */
        void onTouchFinished(boolean closeContextMenu);

        /**
         * Called when the user drag the favorite to remove.
         */
        void onRequestRemove(SpeedDialUiItem speedDialUiItem);
    }
}
