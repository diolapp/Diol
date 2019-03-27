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

package app.diol.incallui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import app.diol.R;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.logging.ScreenEvent;
import app.diol.incallui.ConferenceManagerPresenter.ConferenceManagerUi;
import app.diol.incallui.baseui.BaseFragment;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;

/**
 * Fragment that allows the user to manage a conference call.
 */
public class ConferenceManagerFragment
        extends BaseFragment<ConferenceManagerPresenter, ConferenceManagerUi>
        implements ConferenceManagerPresenter.ConferenceManagerUi {

    private ListView conferenceParticipantList;
    private ContactPhotoManager contactPhotoManager;
    private ConferenceParticipantListAdapter conferenceParticipantListAdapter;

    @Override
    public ConferenceManagerPresenter createPresenter() {
        return new ConferenceManagerPresenter();
    }

    @Override
    public ConferenceManagerPresenter.ConferenceManagerUi getUi() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Logger.get(getContext()).logScreenView(ScreenEvent.Type.CONFERENCE_MANAGEMENT, getActivity());
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.conference_manager_fragment, container, false);

        conferenceParticipantList = (ListView) parent.findViewById(R.id.participantList);
        contactPhotoManager = ContactPhotoManager.getInstance(getActivity().getApplicationContext());

        return parent;
    }

    @Override
    public void onResume() {
        super.onResume();
        final CallList calls = CallList.getInstance();
        getPresenter().init(calls);
        // Request focus on the list of participants for accessibility purposes.  This ensures
        // that once the list of participants is shown, the first participant is announced.
        conferenceParticipantList.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean isFragmentVisible() {
        return isVisible();
    }

    @Override
    public void update(List<DialerCall> participants, boolean parentCanSeparate) {
        if (conferenceParticipantListAdapter == null) {
            conferenceParticipantListAdapter =
                    new ConferenceParticipantListAdapter(conferenceParticipantList, contactPhotoManager);

            conferenceParticipantList.setAdapter(conferenceParticipantListAdapter);
        }
        conferenceParticipantListAdapter.updateParticipants(participants, parentCanSeparate);
    }

    @Override
    public void refreshCall(DialerCall call) {
        conferenceParticipantListAdapter.refreshCall(call);
    }
}
