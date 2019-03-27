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

package app.diol.dialer.searchfragment.list;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.searchfragment.common.RowClickListener;
import app.diol.dialer.util.DialerUtils;
import app.diol.dialer.util.IntentUtil;

/**
 * {@link RecyclerView.ViewHolder} for showing an {@link SearchActionViewHolder.Action} in a list.
 */
final class SearchActionViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final Context context;
    private final ImageView actionImage;
    private final TextView actionText;
    private final RowClickListener listener;
    private @Action
    int action;
    private int position;
    private String query;
    SearchActionViewHolder(View view, RowClickListener listener) {
        super(view);
        context = view.getContext();
        actionImage = view.findViewById(R.id.search_action_image);
        actionText = view.findViewById(R.id.search_action_text);
        this.listener = listener;
        view.setOnClickListener(this);
    }

    void setAction(@Action int action, int position, String query) {
        this.action = action;
        this.position = position;
        this.query = query;
        switch (action) {
            case Action.ADD_TO_CONTACT:
                actionText.setText(R.string.search_shortcut_add_to_contact);
                actionImage.setImageResource(R.drawable.quantum_ic_person_add_vd_theme_24);
                break;
            case Action.CREATE_NEW_CONTACT:
                actionText.setText(R.string.search_shortcut_create_new_contact);
                actionImage.setImageResource(R.drawable.quantum_ic_person_add_vd_theme_24);
                break;
            case Action.MAKE_VILTE_CALL:
                actionText.setText(R.string.search_shortcut_make_video_call);
                actionImage.setImageResource(R.drawable.quantum_ic_videocam_vd_theme_24);
                break;
            case Action.SEND_SMS:
                actionText.setText(R.string.search_shortcut_send_sms_message);
                actionImage.setImageResource(R.drawable.quantum_ic_message_vd_theme_24);
                break;
            case Action.MAKE_VOICE_CALL:
                actionText.setText(context.getString(R.string.search_shortcut_make_voice_call, query));
                actionImage.setImageResource(R.drawable.quantum_ic_phone_vd_theme_24);
                break;
            case Action.INVALID:
            default:
                throw Assert.createIllegalStateFailException("Invalid action: " + action);
        }
    }

    @VisibleForTesting
    @Action
    int getAction() {
        return action;
    }

    @Override
    public void onClick(View v) {
        switch (action) {
            case Action.ADD_TO_CONTACT:
                Logger.get(context).logImpression(DialerImpression.Type.ADD_TO_A_CONTACT_FROM_DIALPAD);
                Intent intent = IntentUtil.getAddToExistingContactIntent(query);
                @StringRes int errorString = R.string.add_contact_not_available;
                DialerUtils.startActivityWithErrorToast(context, intent, errorString);
                break;

            case Action.CREATE_NEW_CONTACT:
                Logger.get(context).logImpression(DialerImpression.Type.CREATE_NEW_CONTACT_FROM_DIALPAD);
                intent = IntentUtil.getNewContactIntent(query);
                DialerUtils.startActivityWithErrorToast(context, intent);
                break;

            case Action.MAKE_VILTE_CALL:
                listener.placeVideoCall(query, position);
                break;

            case Action.SEND_SMS:
                intent = IntentUtil.getSendSmsIntent(query);
                DialerUtils.startActivityWithErrorToast(context, intent);
                break;

            case Action.MAKE_VOICE_CALL:
                listener.placeVoiceCall(query, position);
                break;

            case Action.INVALID:
            default:
                throw Assert.createIllegalStateFailException("Invalid action: " + action);
        }
    }

    /**
     * IntDef for the different types of actions that can be used.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            Action.INVALID,
            Action.CREATE_NEW_CONTACT,
            Action.ADD_TO_CONTACT,
            Action.SEND_SMS,
            Action.MAKE_VILTE_CALL,
            Action.MAKE_VOICE_CALL
    })
    @interface Action {
        int INVALID = 0;
        /**
         * Opens the prompt to create a new contact.
         */
        int CREATE_NEW_CONTACT = 1;
        /**
         * Opens a prompt to add to an existing contact.
         */
        int ADD_TO_CONTACT = 2;
        /**
         * Opens the SMS conversation with the default SMS app.
         */
        int SEND_SMS = 3;
        /**
         * Attempts to make a VILTE call to the number.
         */
        int MAKE_VILTE_CALL = 4;
        /**
         * Places a voice call to the number.
         */
        int MAKE_VOICE_CALL = 5;
    }
}
