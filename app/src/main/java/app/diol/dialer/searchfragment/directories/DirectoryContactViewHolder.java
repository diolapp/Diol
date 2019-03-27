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

package app.diol.dialer.searchfragment.directories;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.cp2.DirectoryUtils;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.searchfragment.common.Projections;
import app.diol.dialer.searchfragment.common.QueryBoldingUtil;
import app.diol.dialer.searchfragment.common.RowClickListener;
import app.diol.dialer.searchfragment.common.SearchCursor;

/**
 * ViewHolder for a directory contact row.
 */
public final class DirectoryContactViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private final Context context;
    private final TextView nameView;
    private final TextView numberView;
    private final QuickContactBadge photo;
    private final ImageView workBadge;
    private final RowClickListener listener;

    private String number;
    private int position;

    public DirectoryContactViewHolder(View view, RowClickListener listener) {
        super(view);
        view.setOnClickListener(this);
        photo = view.findViewById(R.id.photo);
        nameView = view.findViewById(R.id.primary);
        numberView = view.findViewById(R.id.secondary);
        workBadge = view.findViewById(R.id.work_icon);
        context = view.getContext();
        this.listener = listener;
    }

    // TODO(calderwoodra): unify this into a utility method with CallLogAdapter#getNumberType
    private static String getLabel(Resources resources, Cursor cursor) {
        int numberType = cursor.getInt(Projections.PHONE_TYPE);
        String numberLabel = cursor.getString(Projections.PHONE_LABEL);

        // Returns empty label instead of "custom" if the custom label is empty.
        if (numberType == Phone.TYPE_CUSTOM && TextUtils.isEmpty(numberLabel)) {
            return "";
        }
        return (String) Phone.getTypeLabel(resources, numberType, numberLabel);
    }

    private static Uri getContactUri(SearchCursor cursor) {
        String lookupKey = cursor.getString(Projections.LOOKUP_KEY);
        return Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey)
                .buildUpon()
                .appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(cursor.getDirectoryId()))
                .build();
    }

    /**
     * Binds the ViewHolder with a cursor from {@link DirectoryContactsCursorLoader} with the data
     * found at the cursors current position.
     */
    public void bind(SearchCursor cursor, String query) {
        number = cursor.getString(Projections.PHONE_NUMBER);
        position = cursor.getPosition();
        String name = cursor.getString(Projections.DISPLAY_NAME);
        String label = getLabel(context.getResources(), cursor);
        String secondaryInfo =
                TextUtils.isEmpty(label)
                        ? number
                        : context.getString(R.string.call_subject_type_and_number, label, number);

        nameView.setText(QueryBoldingUtil.getNameWithQueryBolded(query, name, context));
        numberView.setText(QueryBoldingUtil.getNameWithQueryBolded(query, secondaryInfo, context));
        workBadge.setVisibility(
                DirectoryUtils.isLocalEnterpriseDirectoryId(cursor.getDirectoryId())
                        ? View.VISIBLE
                        : View.GONE);

        if (shouldShowPhoto(cursor)) {
            nameView.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
            String photoUri = cursor.getString(Projections.PHOTO_URI);
            ContactPhotoManager.getInstance(context)
                    .loadDialerThumbnailOrPhoto(
                            photo,
                            getContactUri(cursor),
                            cursor.getLong(Projections.PHOTO_ID),
                            photoUri == null ? null : Uri.parse(photoUri),
                            name,
                            LetterTileDrawable.TYPE_DEFAULT);
        } else {
            nameView.setVisibility(View.GONE);
            photo.setVisibility(View.INVISIBLE);
        }
    }

    // Show the contact photo next to only the first number if a contact has multiple numbers
    private boolean shouldShowPhoto(SearchCursor cursor) {
        int currentPosition = cursor.getPosition();
        String currentLookupKey = cursor.getString(Projections.LOOKUP_KEY);
        cursor.moveToPosition(currentPosition - 1);

        if (!cursor.isHeader() && !cursor.isBeforeFirst()) {
            String previousLookupKey = cursor.getString(Projections.LOOKUP_KEY);
            cursor.moveToPosition(currentPosition);
            return !currentLookupKey.equals(previousLookupKey);
        }
        cursor.moveToPosition(currentPosition);
        return true;
    }

    @Override
    public void onClick(View v) {
        listener.placeVoiceCall(number, position);
    }
}
