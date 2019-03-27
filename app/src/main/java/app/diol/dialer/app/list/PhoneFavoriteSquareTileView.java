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

package app.diol.dialer.app.list;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.QuickContact;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import app.diol.R;
import app.diol.contacts.common.list.ContactEntry;
import app.diol.dialer.logging.InteractionEvent;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.widget.BidiTextView;

/**
 * Displays the contact's picture overlaid with their name and number type in a tile.
 */
public class PhoneFavoriteSquareTileView extends PhoneFavoriteTileView {

    private final float heightToWidthRatio;

    private ImageButton secondaryButton;

    private ContactEntry contactEntry;

    public PhoneFavoriteSquareTileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        heightToWidthRatio =
                getResources().getFraction(R.dimen.contact_tile_height_to_width_ratio, 1, 1);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        BidiTextView nameView = findViewById(R.id.contact_tile_name);
        nameView.setElegantTextHeight(false);

        TextView phoneTypeView = findViewById(R.id.contact_tile_phone_type);
        phoneTypeView.setElegantTextHeight(false);
        secondaryButton = findViewById(R.id.contact_tile_secondary_button);
    }

    @Override
    protected int getApproximateImageSize() {
        // The picture is the full size of the tile (minus some padding, but we can be generous)
        return getWidth();
    }

    private void launchQuickContact() {
        QuickContact.showQuickContact(
                getContext(),
                PhoneFavoriteSquareTileView.this,
                getLookupUri(),
                null,
                Phone.CONTENT_ITEM_TYPE);
    }

    @Override
    public void loadFromContact(ContactEntry entry) {
        super.loadFromContact(entry);
        if (entry != null) {
            secondaryButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Logger.get(getContext())
                                    .logInteraction(InteractionEvent.Type.SPEED_DIAL_OPEN_CONTACT_CARD);
                            launchQuickContact();
                        }
                    });
        }
        contactEntry = entry;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = (int) (heightToWidthRatio * width);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i)
                    .measure(
                            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected String getNameForView(ContactEntry contactEntry) {
        return contactEntry.getPreferredDisplayName(getContext());
    }

    public ContactEntry getContactEntry() {
        return contactEntry;
    }
}
