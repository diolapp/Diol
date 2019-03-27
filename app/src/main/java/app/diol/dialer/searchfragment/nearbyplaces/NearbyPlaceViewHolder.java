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

package app.diol.dialer.searchfragment.nearbyplaces;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import app.diol.R;
import app.diol.contacts.common.util.Constants;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.searchfragment.common.Projections;
import app.diol.dialer.searchfragment.common.QueryBoldingUtil;
import app.diol.dialer.searchfragment.common.RowClickListener;
import app.diol.dialer.searchfragment.common.SearchCursor;

/**
 * ViewHolder for a nearby place row.
 */
public final class NearbyPlaceViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private final Context context;
    private final TextView placeName;
    private final TextView placeAddress;
    private final QuickContactBadge photo;
    private final RowClickListener listener;

    private String number;
    private int position;

    public NearbyPlaceViewHolder(View view, RowClickListener listener) {
        super(view);
        view.setOnClickListener(this);
        photo = view.findViewById(R.id.photo);
        placeName = view.findViewById(R.id.primary);
        placeAddress = view.findViewById(R.id.secondary);
        context = view.getContext();
        this.listener = listener;
    }

    private static Uri getContactUri(SearchCursor cursor) {
        // Since the lookup key for Nearby Places is actually a JSON representation of the information,
        // we need to pass it in as an encoded fragment in our contact uri.
        // It includes information like display name, photo uri, phone number, ect.
        String businessInfoJson = cursor.getString(Projections.LOOKUP_KEY);
        return Contacts.CONTENT_LOOKUP_URI
                .buildUpon()
                .appendPath(Constants.LOOKUP_URI_ENCODED)
                .appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(cursor.getDirectoryId()))
                .encodedFragment(businessInfoJson)
                .build();
    }

    /**
     * Binds the ViewHolder with a cursor from {@link NearbyPlacesCursorLoader} with the data found at
     * the cursors set position.
     */
    public void bind(SearchCursor cursor, String query) {
        number = cursor.getString(Projections.PHONE_NUMBER);
        position = cursor.getPosition();
        String name = cursor.getString(Projections.DISPLAY_NAME);
        String address = cursor.getString(Projections.PHONE_LABEL);

        placeName.setText(QueryBoldingUtil.getNameWithQueryBolded(query, name, context));
        placeAddress.setText(QueryBoldingUtil.getNameWithQueryBolded(query, address, context));
        String photoUri = cursor.getString(Projections.PHOTO_URI);
        ContactPhotoManager.getInstance(context)
                .loadDialerThumbnailOrPhoto(
                        photo,
                        getContactUri(cursor),
                        cursor.getLong(Projections.PHOTO_ID),
                        photoUri == null ? null : Uri.parse(photoUri),
                        name,
                        LetterTileDrawable.TYPE_BUSINESS);
    }

    @Override
    public void onClick(View v) {
        listener.placeVoiceCall(number, position);
    }
}
