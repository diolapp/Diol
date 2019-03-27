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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.blocking.BlockedNumbersMigrator;
import app.diol.dialer.blocking.BlockedNumbersMigrator.Listener;
import app.diol.dialer.blocking.FilteredNumberCompat;
import app.diol.dialer.blocking.FilteredNumbersUtil;
import app.diol.dialer.blocking.FilteredNumbersUtil.CheckForSendToVoicemailContactListener;
import app.diol.dialer.blocking.FilteredNumbersUtil.ImportSendToVoicemailContactsListener;
import app.diol.dialer.database.FilteredNumberContract;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.dialer.voicemailstatus.VisualVoicemailEnabledChecker;

/**
 * TODO(calderwoodra): documentation
 */
public class BlockedNumbersFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener,
        VisualVoicemailEnabledChecker.Callback {

    private static final char ADD_BLOCKED_NUMBER_ICON_LETTER = '+';
    protected View migratePromoView;
    private BlockedNumbersMigrator blockedNumbersMigratorForTest;
    private TextView blockedNumbersText;
    private TextView footerText;
    private BlockedNumbersAdapter adapter;
    private VisualVoicemailEnabledChecker voicemailEnabledChecker;
    private View importSettings;
    private View blockedNumbersDisabledForEmergency;
    private View blockedNumberListDivider;

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getListView().addHeaderView(inflater.inflate(R.layout.blocked_number_header, null));
        getListView().addFooterView(inflater.inflate(R.layout.blocked_number_footer, null));
        //replace the icon for add number with LetterTileDrawable(), so it will have identical style
        LetterTileDrawable drawable = new LetterTileDrawable(getResources());
        drawable.setLetter(ADD_BLOCKED_NUMBER_ICON_LETTER);
        drawable.setColor(ThemeComponent.get(getContext()).theme().getColorIcon());
        drawable.setIsCircular(true);

        if (adapter == null) {
            adapter =
                    BlockedNumbersAdapter.newBlockedNumbersAdapter(
                            getContext(), getActivity().getFragmentManager());
        }
        setListAdapter(adapter);

        blockedNumbersText = (TextView) getListView().findViewById(R.id.blocked_number_text_view);
        migratePromoView = getListView().findViewById(R.id.migrate_promo);
        getListView().findViewById(R.id.migrate_promo_allow_button).setOnClickListener(this);
        importSettings = getListView().findViewById(R.id.import_settings);
        blockedNumbersDisabledForEmergency =
                getListView().findViewById(R.id.blocked_numbers_disabled_for_emergency);
        blockedNumberListDivider = getActivity().findViewById(R.id.blocked_number_list_divider);
        getListView().findViewById(R.id.import_button).setOnClickListener(this);
        getListView().findViewById(R.id.view_numbers_button).setOnClickListener(this);

        footerText = (TextView) getActivity().findViewById(R.id.blocked_number_footer_textview);
        voicemailEnabledChecker = new VisualVoicemailEnabledChecker(getContext(), this);
        voicemailEnabledChecker.asyncUpdate();
        updateActiveVoicemailProvider();
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
        ColorDrawable backgroundDrawable =
                new ColorDrawable(ThemeComponent.get(getContext()).theme().getColorPrimary());
        actionBar.setBackgroundDrawable(backgroundDrawable);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.manage_blocked_numbers_label);

        // If the device can use the framework blocking solution, users should not be able to add
        // new blocked numbers from the Blocked Management UI. They will be shown a promo card
        // asking them to migrate to new blocking instead.
        if (FilteredNumberCompat.canUseNewFiltering()) {
            migratePromoView.setVisibility(View.VISIBLE);
            blockedNumbersText.setVisibility(View.GONE);
            blockedNumberListDivider.setVisibility(View.GONE);
            importSettings.setVisibility(View.GONE);
            getListView().findViewById(R.id.import_button).setOnClickListener(null);
            getListView().findViewById(R.id.view_numbers_button).setOnClickListener(null);
            blockedNumbersDisabledForEmergency.setVisibility(View.GONE);
            footerText.setVisibility(View.GONE);
        } else {
            FilteredNumbersUtil.checkForSendToVoicemailContact(
                    getActivity(),
                    new CheckForSendToVoicemailContactListener() {
                        @Override
                        public void onComplete(boolean hasSendToVoicemailContact) {
                            final int visibility = hasSendToVoicemailContact ? View.VISIBLE : View.GONE;
                            importSettings.setVisibility(visibility);
                        }
                    });
        }

        // All views except migrate and the block list are hidden when new filtering is available
        if (!FilteredNumberCompat.canUseNewFiltering()
                && FilteredNumbersUtil.hasRecentEmergencyCall(getContext())) {
            blockedNumbersDisabledForEmergency.setVisibility(View.VISIBLE);
        } else {
            blockedNumbersDisabledForEmergency.setVisibility(View.GONE);
        }

        voicemailEnabledChecker.asyncUpdate();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.blocked_number_fragment, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                FilteredNumberContract.FilteredNumberColumns._ID,
                FilteredNumberContract.FilteredNumberColumns.COUNTRY_ISO,
                FilteredNumberContract.FilteredNumberColumns.NUMBER,
                FilteredNumberContract.FilteredNumberColumns.NORMALIZED_NUMBER
        };
        final String selection =
                FilteredNumberContract.FilteredNumberColumns.TYPE
                        + "="
                        + FilteredNumberContract.FilteredNumberTypes.BLOCKED_NUMBER;
        return new CursorLoader(
                getContext(),
                FilteredNumberContract.FilteredNumber.CONTENT_URI,
                projection,
                selection,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (FilteredNumberCompat.canUseNewFiltering() || data.getCount() == 0) {
            blockedNumberListDivider.setVisibility(View.INVISIBLE);
        } else {
            blockedNumberListDivider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onClick(final View view) {
        final BlockedNumbersSettingsActivity activity = (BlockedNumbersSettingsActivity) getActivity();
        if (activity == null) {
            return;
        }

        int resId = view.getId();
        if (resId == R.id.view_numbers_button) {
            activity.showNumbersToImportPreviewUi();
        } else if (resId == R.id.import_button) {
            FilteredNumbersUtil.importSendToVoicemailContacts(
                    activity,
                    new ImportSendToVoicemailContactsListener() {
                        @Override
                        public void onImportComplete() {
                            importSettings.setVisibility(View.GONE);
                        }
                    });
        } else if (resId == R.id.migrate_promo_allow_button) {
            view.setEnabled(false);
            (blockedNumbersMigratorForTest != null
                    ? blockedNumbersMigratorForTest
                    : new BlockedNumbersMigrator(getContext()))
                    .migrate(
                            new Listener() {
                                @Override
                                public void onComplete() {
                                    getContext()
                                            .startActivity(
                                                    FilteredNumberCompat.createManageBlockedNumbersIntent(getContext()));
                                    // Remove this activity from the backstack
                                    activity.finish();
                                }
                            });
        }
    }

    @Override
    public void onVisualVoicemailEnabledStatusChanged(boolean newStatus) {
        updateActiveVoicemailProvider();
    }

    private void updateActiveVoicemailProvider() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (voicemailEnabledChecker.isVisualVoicemailEnabled()) {
            footerText.setText(R.string.block_number_footer_message_vvm);
        } else {
            footerText.setText(R.string.block_number_footer_message_no_vvm);
        }
    }

    void setBlockedNumbersMigratorForTest(BlockedNumbersMigrator blockedNumbersMigrator) {
        blockedNumbersMigratorForTest = blockedNumbersMigrator;
    }
}
