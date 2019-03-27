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

package app.diol.dialer.voicemail.listui.error;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.diol.R;
import app.diol.dialer.app.alert.AlertManager;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.voicemail.listui.error.VoicemailErrorMessage.Action;

/**
 * UI for the voicemail error message, which will be inserted to the top of the voicemail tab if any
 * occurred.
 */
public class VoicemailErrorAlert {

    private final Context context;
    private final AlertManager alertManager;
    private final VoicemailErrorMessageCreator messageCreator;

    private final View view;
    private final TextView header;
    private final TextView details;
    private final TextView primaryAction;
    private final TextView secondaryAction;
    private final TextView primaryActionRaised;
    private final TextView secondaryActionRaised;
    private final AlertManager modalAlertManager;
    private View modalView;

    public VoicemailErrorAlert(
            Context context,
            AlertManager alertManager,
            AlertManager modalAlertManager,
            VoicemailErrorMessageCreator messageCreator) {
        this.context = context;
        this.alertManager = alertManager;
        this.modalAlertManager = modalAlertManager;
        this.messageCreator = messageCreator;

        view = alertManager.inflate(R.layout.voicemail_error_message_fragment);
        header = (TextView) view.findViewById(R.id.error_card_header);
        details = (TextView) view.findViewById(R.id.error_card_details);
        primaryAction = (TextView) view.findViewById(R.id.primary_action);
        secondaryAction = (TextView) view.findViewById(R.id.secondary_action);
        primaryActionRaised = (TextView) view.findViewById(R.id.primary_action_raised);
        secondaryActionRaised = (TextView) view.findViewById(R.id.secondary_action_raised);
    }

    public void updateStatus(List<VoicemailStatus> statuses, VoicemailStatusReader statusReader) {
        LogUtil.i("VoicemailErrorAlert.updateStatus", "%d status", statuses.size());
        VoicemailErrorMessage message = null;
        view.setVisibility(View.VISIBLE);
        for (VoicemailStatus status : statuses) {
            message = messageCreator.create(context, status, statusReader);
            if (message != null) {
                break;
            }
        }

        alertManager.clear();
        modalAlertManager.clear();
        if (message != null) {
            LogUtil.i(
                    "VoicemailErrorAlert.updateStatus",
                    "isModal: %b, %s",
                    message.isModal(),
                    message.getTitle());
            if (message.isModal()) {
                if (message instanceof VoicemailTosMessage) {
                    modalView = getTosView(modalAlertManager, (VoicemailTosMessage) message);
                } else {
                    throw new IllegalArgumentException("Modal message type is undefined!");
                }
                modalAlertManager.add(modalView);
            } else {
                loadMessage(message);
                alertManager.add(view);
            }
        }
    }

    @VisibleForTesting
    public View getView() {
        return view;
    }

    @VisibleForTesting
    public View getModalView() {
        return modalView;
    }

    void loadMessage(VoicemailErrorMessage message) {
        header.setText(message.getTitle());
        details.setText(message.getDescription());
        bindActions(message);
    }

    private View getTosView(AlertManager alertManager, VoicemailTosMessage message) {
        View view = alertManager.inflate(R.layout.voicemail_tos_fragment);
        TextView tosTitle = (TextView) view.findViewById(R.id.tos_message_title);
        tosTitle.setText(message.getTitle());
        TextView tosDetails = (TextView) view.findViewById(R.id.tos_message_details);
        tosDetails.setText(message.getDescription());
        tosDetails.setMovementMethod(LinkMovementMethod.getInstance());

        Assert.checkArgument(message.getActions().size() == 2);
        Action primaryAction = message.getActions().get(0);
        TextView primaryButton = (TextView) view.findViewById(R.id.voicemail_tos_button_decline);
        primaryButton.setText(primaryAction.getText());
        primaryButton.setOnClickListener(primaryAction.getListener());
        Action secondaryAction = message.getActions().get(1);
        TextView secondaryButton = (TextView) view.findViewById(R.id.voicemail_tos_button_accept);
        secondaryButton.setText(secondaryAction.getText());
        secondaryButton.setOnClickListener(secondaryAction.getListener());

        if (message.getImageResourceId() != null) {
            ImageView voicemailTosImage = (ImageView) view.findViewById(R.id.voicemail_image);
            voicemailTosImage.setImageResource(message.getImageResourceId());
            voicemailTosImage.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * Attach actions to buttons until all buttons are assigned. If there are not enough actions the
     * rest of the buttons will be removed. If there are more actions then buttons the extra actions
     * will be dropped. {@link VoicemailErrorMessage#getActions()} will specify what actions should be
     * shown and in what order.
     */
    private void bindActions(VoicemailErrorMessage message) {
        TextView[] buttons = new TextView[]{primaryAction, secondaryAction};
        TextView[] raisedButtons = new TextView[]{primaryActionRaised, secondaryActionRaised};
        for (int i = 0; i < buttons.length; i++) {
            if (message.getActions() != null && i < message.getActions().size()) {
                VoicemailErrorMessage.Action action = message.getActions().get(i);
                TextView button;
                if (action.isRaised()) {
                    button = raisedButtons[i];
                    buttons[i].setVisibility(View.GONE);
                } else {
                    button = buttons[i];
                    raisedButtons[i].setVisibility(View.GONE);
                }
                button.setText(action.getText());
                button.setOnClickListener(action.getListener());
                button.setVisibility(View.VISIBLE);
            } else {
                buttons[i].setVisibility(View.GONE);
                raisedButtons[i].setVisibility(View.GONE);
            }
        }
    }
}
