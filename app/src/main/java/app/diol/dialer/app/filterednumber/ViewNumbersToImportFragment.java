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

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.diol.R;
import app.diol.dialer.blocking.FilteredNumbersUtil;
import app.diol.dialer.blocking.FilteredNumbersUtil.ImportSendToVoicemailContactsListener;

/**
 * TODO(calderwoodra): documentation
 */
public class ViewNumbersToImportFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private ViewNumbersToImportAdapter adapter;

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (adapter == null) {
            adapter =
                    ViewNumbersToImportAdapter.newViewNumbersToImportAdapter(
                            getContext(), getActivity().getFragmentManager());
        }
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        setListAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.import_send_to_voicemail_numbers_label);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        getActivity().findViewById(R.id.cancel_button).setOnClickListener(this);
        getActivity().findViewById(R.id.import_button).setOnClickListener(this);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_numbers_to_import_fragment, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final CursorLoader cursorLoader =
                new CursorLoader(
                        getContext(),
                        Phone.CONTENT_URI,
                        FilteredNumbersUtil.PhoneQuery.PROJECTION,
                        FilteredNumbersUtil.PhoneQuery.SELECT_SEND_TO_VOICEMAIL_TRUE,
                        null,
                        null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.import_button) {
            FilteredNumbersUtil.importSendToVoicemailContacts(
                    getContext(),
                    new ImportSendToVoicemailContactsListener() {
                        @Override
                        public void onImportComplete() {
                            if (getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        }
                    });
        } else if (view.getId() == R.id.cancel_button) {
            getActivity().onBackPressed();
        }
    }
}
