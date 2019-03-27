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

package app.diol.dialer.calllog.ui;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.promotion.Promotion;

/**
 * ViewHolder for {@link NewCallLogAdapter} to display the Duo disclosure card.
 */
public class PromotionCardViewHolder extends ViewHolder {

    private final Button okButton;
    private final Promotion promotion;
    PromotionCardViewHolder(View itemView, Promotion promotion) {
        super(itemView);
        this.promotion = promotion;

        ImageView iconView = itemView.findViewById(R.id.new_call_log_promotion_card_icon);
        iconView.setImageResource(promotion.getIconRes());

        TextView cardTitleView = itemView.findViewById(R.id.new_call_log_promotion_card_title);
        cardTitleView.setText(promotion.getTitle());

        TextView cardDetailsView = itemView.findViewById(R.id.new_call_log_promotion_card_details);
        cardDetailsView.setText(promotion.getDetails());
        cardDetailsView.setMovementMethod(LinkMovementMethod.getInstance()); // make the link clickable

        // Obtain a reference to the "OK, got it" button.
        okButton = itemView.findViewById(R.id.new_call_log_promotion_card_ok);
    }

    void setDismissListener(DismissListener listener) {
        okButton.setOnClickListener(
                v -> {
                    promotion.dismiss();
                    listener.onDismiss();
                });
    }

    /**
     * Listener to be called when promotion card is dismissed.
     */
    interface DismissListener {
        void onDismiss();
    }
}
