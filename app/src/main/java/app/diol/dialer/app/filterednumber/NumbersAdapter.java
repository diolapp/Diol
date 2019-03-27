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
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.View;
import android.widget.QuickContactBadge;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.contactphoto.ContactPhotoManager.DefaultImageRequest;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.phonenumbercache.ContactInfo;
import app.diol.dialer.phonenumbercache.ContactInfoHelper;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;
import app.diol.dialer.util.UriUtils;

/**
 * TODO(calderwoodra): documentation
 */
public class NumbersAdapter extends SimpleCursorAdapter {

    private final Context context;
    private final FragmentManager fragmentManager;
    private final ContactInfoHelper contactInfoHelper;
    private final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
    private final ContactPhotoManager contactPhotoManager;

    public NumbersAdapter(
            Context context,
            FragmentManager fragmentManager,
            ContactInfoHelper contactInfoHelper,
            ContactPhotoManager contactPhotoManager) {
        super(context, R.layout.blocked_number_item, null, new String[]{}, new int[]{}, 0);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.contactInfoHelper = contactInfoHelper;
        this.contactPhotoManager = contactPhotoManager;
    }

    public void updateView(View view, String number, String countryIso) {
        final TextView callerName = (TextView) view.findViewById(R.id.caller_name);
        final TextView callerNumber = (TextView) view.findViewById(R.id.caller_number);
        final QuickContactBadge quickContactBadge =
                (QuickContactBadge) view.findViewById(R.id.quick_contact_photo);
        quickContactBadge.setOverlay(null);
        quickContactBadge.setPrioritizedMimeType(Phone.CONTENT_ITEM_TYPE);

        ContactInfo info = contactInfoHelper.lookupNumber(number, countryIso);
        if (info == null) {
            info = new ContactInfo();
            info.number = number;
        }
        final CharSequence locationOrType = getNumberTypeOrLocation(info, countryIso);
        final String displayNumber = getDisplayNumber(info);
        final String displayNumberStr =
                bidiFormatter.unicodeWrap(displayNumber, TextDirectionHeuristics.LTR);

        String nameForDefaultImage;
        if (!TextUtils.isEmpty(info.name)) {
            nameForDefaultImage = info.name;
            callerName.setText(info.name);
            callerNumber.setText(locationOrType + " " + displayNumberStr);
        } else {
            nameForDefaultImage = displayNumber;
            callerName.setText(displayNumberStr);
            if (!TextUtils.isEmpty(locationOrType)) {
                callerNumber.setText(locationOrType);
                callerNumber.setVisibility(View.VISIBLE);
            } else {
                callerNumber.setVisibility(View.GONE);
            }
        }
        loadContactPhoto(info, nameForDefaultImage, quickContactBadge);
    }

    private void loadContactPhoto(ContactInfo info, String displayName, QuickContactBadge badge) {
        final String lookupKey =
                info.lookupUri == null ? null : UriUtils.getLookupKeyFromUri(info.lookupUri);
        final int contactType =
                contactInfoHelper.isBusiness(info.sourceType)
                        ? LetterTileDrawable.TYPE_BUSINESS
                        : LetterTileDrawable.TYPE_DEFAULT;
        final DefaultImageRequest request =
                new DefaultImageRequest(displayName, lookupKey, contactType, true /* isCircular */);
        badge.assignContactUri(info.lookupUri);
        badge.setContentDescription(
                context.getResources().getString(R.string.description_contact_details, displayName));
        contactPhotoManager.loadDirectoryPhoto(
                badge, info.photoUri, false /* darkTheme */, true /* isCircular */, request);
    }

    private String getDisplayNumber(ContactInfo info) {
        if (!TextUtils.isEmpty(info.formattedNumber)) {
            return info.formattedNumber;
        } else if (!TextUtils.isEmpty(info.number)) {
            return info.number;
        } else {
            return "";
        }
    }

    private CharSequence getNumberTypeOrLocation(ContactInfo info, String countryIso) {
        if (!TextUtils.isEmpty(info.name)) {
            return ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.getResources(), info.type, info.label);
        } else {
            return PhoneNumberHelper.getGeoDescription(context, info.number, countryIso);
        }
    }

    protected Context getContext() {
        return context;
    }

    protected FragmentManager getFragmentManager() {
        return fragmentManager;
    }
}
