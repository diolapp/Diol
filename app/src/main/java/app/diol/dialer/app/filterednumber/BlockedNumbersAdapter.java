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
import app.diol.dialer.blocking.BlockNumberDialogFragment;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.database.FilteredNumberContract.FilteredNumberColumns;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.logging.InteractionEvent;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.phonenumbercache.ContactInfoHelper;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;

/**
 * TODO(calderwoodra): documentation
 */
public class BlockedNumbersAdapter extends NumbersAdapter {

    private BlockedNumbersAdapter(
            Context context,
            FragmentManager fragmentManager,
            ContactInfoHelper contactInfoHelper,
            ContactPhotoManager contactPhotoManager) {
        super(context, fragmentManager, contactInfoHelper, contactPhotoManager);
    }

    public static BlockedNumbersAdapter newBlockedNumbersAdapter(
            Context context, FragmentManager fragmentManager) {
        return new BlockedNumbersAdapter(
                context,
                fragmentManager,
                new ContactInfoHelper(context, GeoUtil.getCurrentCountryIso(context)),
                ContactPhotoManager.getInstance(context));
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        final Integer id = cursor.getInt(cursor.getColumnIndex(FilteredNumberColumns._ID));
        final String countryIso =
                cursor.getString(cursor.getColumnIndex(FilteredNumberColumns.COUNTRY_ISO));
        final String number = cursor.getString(cursor.getColumnIndex(FilteredNumberColumns.NUMBER));

        final View deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BlockNumberDialogFragment.show(
                                id,
                                number,
                                countryIso,
                                PhoneNumberHelper.formatNumber(getContext(), number, countryIso),
                                R.id.blocked_numbers_activity_container,
                                getFragmentManager(),
                                new BlockNumberDialogFragment.Callback() {
                                    @Override
                                    public void onFilterNumberSuccess() {
                                    }

                                    @Override
                                    public void onUnfilterNumberSuccess() {
                                        Logger.get(context)
                                                .logInteraction(InteractionEvent.Type.UNBLOCK_NUMBER_MANAGEMENT_SCREEN);
                                    }

                                    @Override
                                    public void onChangeFilteredNumberUndo() {
                                    }
                                });
                    }
                });

        updateView(view, number, countryIso);
    }

    @Override
    public boolean isEmpty() {
        // Always return false, so that the header with blocking-related options always shows.
        return false;
    }
}
