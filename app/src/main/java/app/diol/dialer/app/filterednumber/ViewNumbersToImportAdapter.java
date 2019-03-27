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

package app.diol.dialer.app.filterednumber;

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.view.View;

import app.diol.R;
import app.diol.dialer.blocking.FilteredNumbersUtil;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.phonenumbercache.ContactInfoHelper;

/**
 * TODO(calderwoodra): documentation
 */
public class ViewNumbersToImportAdapter extends NumbersAdapter {

    private ViewNumbersToImportAdapter(
            Context context,
            FragmentManager fragmentManager,
            ContactInfoHelper contactInfoHelper,
            ContactPhotoManager contactPhotoManager) {
        super(context, fragmentManager, contactInfoHelper, contactPhotoManager);
    }

    public static ViewNumbersToImportAdapter newViewNumbersToImportAdapter(
            Context context, FragmentManager fragmentManager) {
        return new ViewNumbersToImportAdapter(
                context,
                fragmentManager,
                new ContactInfoHelper(context, GeoUtil.getCurrentCountryIso(context)),
                ContactPhotoManager.getInstance(context));
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        final String number = cursor.getString(FilteredNumbersUtil.PhoneQuery.NUMBER_COLUMN_INDEX);

        view.findViewById(R.id.delete_button).setVisibility(View.GONE);
        updateView(view, number, null /* countryIso */);
    }
}
