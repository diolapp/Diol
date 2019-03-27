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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.historyitemactions.HistoryItemBottomSheetHeaderInfo;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.phonenumberutil.PhoneNumberHelper;
import app.diol.dialer.speeddial.database.SpeedDialEntry.Channel;
import app.diol.dialer.speeddial.loader.SpeedDialUiItem;
import app.diol.dialer.widget.ContactPhotoView;

/**
 * ViewHolder for displaying suggested contacts in {@link SpeedDialFragment}.
 */
public class SuggestionViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final SuggestedContactsListener listener;

    private final ContactPhotoView photoView;
    private final TextView nameOrNumberView;
    private final TextView numberView;

    private SpeedDialUiItem speedDialUiItem;

    SuggestionViewHolder(View view, SuggestedContactsListener listener) {
        super(view);
        photoView = view.findViewById(R.id.avatar);
        nameOrNumberView = view.findViewById(R.id.name);
        numberView = view.findViewById(R.id.number);
        itemView.setOnClickListener(this);
        view.findViewById(R.id.overflow).setOnClickListener(this);
        this.listener = listener;
    }

    public void bind(Context context, SpeedDialUiItem speedDialUiItem) {
        Assert.isNotNull(speedDialUiItem.defaultChannel());
        this.speedDialUiItem = speedDialUiItem;
        String number =
                PhoneNumberHelper.formatNumber(
                        context,
                        speedDialUiItem.defaultChannel().number(),
                        GeoUtil.getCurrentCountryIso(context));

        String label = speedDialUiItem.defaultChannel().label();
        String secondaryInfo =
                TextUtils.isEmpty(label)
                        ? number
                        : context.getString(R.string.call_subject_type_and_number, label, number);

        nameOrNumberView.setText(speedDialUiItem.name());
        numberView.setText(secondaryInfo);

        photoView.setPhoto(speedDialUiItem.getPhotoInfo());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.overflow) {
            listener.onOverFlowMenuClicked(speedDialUiItem, getHeaderInfo());
        } else {
            listener.onRowClicked(speedDialUiItem.defaultChannel());
        }
    }

    private HistoryItemBottomSheetHeaderInfo getHeaderInfo() {
        return HistoryItemBottomSheetHeaderInfo.newBuilder()
                .setPhotoInfo(speedDialUiItem.getPhotoInfo())
                .setPrimaryText(nameOrNumberView.getText().toString())
                .setSecondaryText(numberView.getText().toString())
                .build();
    }

    /**
     * Listener/Callback for {@link SuggestionViewHolder} parents.
     */
    public interface SuggestedContactsListener {

        void onOverFlowMenuClicked(
                SpeedDialUiItem speedDialUiItem, HistoryItemBottomSheetHeaderInfo headerInfo);

        /**
         * Called when a suggested contact is clicked.
         */
        void onRowClicked(Channel channel);
    }
}
