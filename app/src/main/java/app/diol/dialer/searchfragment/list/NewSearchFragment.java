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

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.diol.R;
import app.diol.contacts.common.extensions.PhoneDirectoryExtenderAccessor;
import app.diol.dialer.animation.AnimUtils;
import app.diol.dialer.callcomposer.CallComposerActivity;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallIntentBuilder;
import app.diol.dialer.callintent.CallSpecificAppData;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.ThreadUtil;
import app.diol.dialer.dialercontact.DialerContact;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.enrichedcall.EnrichedCallManager.CapabilitiesListener;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.precall.PreCall;
import app.diol.dialer.searchfragment.common.RowClickListener;
import app.diol.dialer.searchfragment.common.SearchCursor;
import app.diol.dialer.searchfragment.cp2.SearchContactsCursorLoader;
import app.diol.dialer.searchfragment.directories.DirectoriesCursorLoader;
import app.diol.dialer.searchfragment.directories.DirectoriesCursorLoader.Directory;
import app.diol.dialer.searchfragment.directories.DirectoryContactsCursorLoader;
import app.diol.dialer.searchfragment.list.SearchActionViewHolder.Action;
import app.diol.dialer.searchfragment.nearbyplaces.NearbyPlacesCursorLoader;
import app.diol.dialer.util.CallUtil;
import app.diol.dialer.util.DialerUtils;
import app.diol.dialer.util.PermissionsUtil;
import app.diol.dialer.util.ViewUtil;
import app.diol.dialer.widget.EmptyContentView;
import app.diol.dialer.widget.EmptyContentView.OnEmptyViewActionButtonClickedListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Fragment used for searching contacts.
 */
public final class NewSearchFragment extends Fragment
        implements LoaderCallbacks<Cursor>,
        OnEmptyViewActionButtonClickedListener,
        CapabilitiesListener,
        OnTouchListener,
        RowClickListener {

    @VisibleForTesting
    public static final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1;
    // Since some of our queries can generate network requests, we should delay them until the user
    // stops typing to prevent generating too much network traffic.
    private static final int NETWORK_SEARCH_DELAY_MILLIS = 300;
    // To prevent constant capabilities updates refreshing the adapter, we want to add a delay between
    // updates so they are bundled together
    private static final int ENRICHED_CALLING_CAPABILITIES_UPDATED_DELAY = 400;
    private static final String KEY_LOCATION_PROMPT_DISMISSED = "search_location_prompt_dismissed";
    @VisibleForTesting
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private static final int CONTACTS_LOADER_ID = 0;
    private static final int NEARBY_PLACES_LOADER_ID = 1;

    // ID for the loader that loads info about all directories (local & remote).
    private static final int DIRECTORIES_LOADER_ID = 2;

    private static final int DIRECTORY_CONTACTS_LOADER_ID = 3;

    private static final String KEY_QUERY = "key_query";
    private static final String KEY_CALL_INITIATION_TYPE = "key_call_initiation_type";
    // Information about all local & remote directories (including ID, display name, etc, but not
    // the contacts in them).
    private final List<Directory> directories = new ArrayList<>();
    private final Runnable loaderCp2ContactsRunnable =
            () -> {
                if (getHost() != null) {
                    getLoaderManager().restartLoader(CONTACTS_LOADER_ID, null, this);
                }
            };
    private final Runnable loadNearbyPlacesRunnable =
            () -> {
                if (getHost() != null) {
                    getLoaderManager().restartLoader(NEARBY_PLACES_LOADER_ID, null, this);
                }
            };
    private final Runnable loadDirectoryContactsRunnable =
            () -> {
                if (getHost() != null) {
                    getLoaderManager().restartLoader(DIRECTORY_CONTACTS_LOADER_ID, null, this);
                }
            };
    private EmptyContentView emptyContentView;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private final Runnable capabilitiesUpdatedRunnable = () -> adapter.notifyDataSetChanged();
    private String query;
    // Raw query number from dialpad, which may contain special character such as "+". This is used
    // for actions to add contact or send sms.
    private String rawNumber;
    private CallInitiationType.Type callInitiationType = CallInitiationType.Type.UNKNOWN_INITIATION;
    private boolean directoriesDisabledForTesting;
    private Runnable updatePositionRunnable;

    public static NewSearchFragment newInstance() {
        return new NewSearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, parent, false);
        adapter = new SearchAdapter(getContext(), new SearchCursorManager(), this);
        adapter.setQuery(query, rawNumber);
        adapter.setSearchActions(getActions());
        showLocationPermission();
        emptyContentView = view.findViewById(R.id.empty_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setOnTouchListener(this);
        recyclerView.setAdapter(adapter);

        if (!PermissionsUtil.hasContactsReadPermissions(getContext())) {
            emptyContentView.setDescription(R.string.new_permission_no_search);
            emptyContentView.setActionLabel(R.string.permission_single_turn_on);
            emptyContentView.setActionClickedListener(this);
            emptyContentView.setImage(R.drawable.empty_contacts);
            emptyContentView.setVisibility(View.VISIBLE);
        } else {
            initLoaders();
        }

        if (savedInstanceState != null) {
            setQuery(
                    savedInstanceState.getString(KEY_QUERY),
                    CallInitiationType.Type.forNumber(savedInstanceState.getInt(KEY_CALL_INITIATION_TYPE)));
        }

        if (updatePositionRunnable != null) {
            ViewUtil.doOnPreDraw(view, false, updatePositionRunnable);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CALL_INITIATION_TYPE, callInitiationType.getNumber());
        outState.putString(KEY_QUERY, query);
    }

    private void initLoaders() {
        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
        loadDirectoriesCursor();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        LogUtil.i("NewSearchFragment.onCreateLoader", "loading cursor: " + id);
        if (id == CONTACTS_LOADER_ID) {
            return new SearchContactsCursorLoader(getContext(), query, isRegularSearch());
        } else if (id == NEARBY_PLACES_LOADER_ID) {
            // Directories represent contact data sources on the device, but since nearby places aren't
            // stored on the device, they don't have a directory ID. We pass the list of all existing IDs
            // so that we can find one that doesn't collide.
            List<Long> directoryIds = new ArrayList<>();
            for (Directory directory : directories) {
                directoryIds.add(directory.getId());
            }
            return new NearbyPlacesCursorLoader(getContext(), query, directoryIds);
        } else if (id == DIRECTORIES_LOADER_ID) {
            return new DirectoriesCursorLoader(getContext());
        } else if (id == DIRECTORY_CONTACTS_LOADER_ID) {
            return new DirectoryContactsCursorLoader(getContext(), query, directories);
        } else {
            throw new IllegalStateException("Invalid loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        LogUtil.i("NewSearchFragment.onLoadFinished", "Loader finished: " + loader);
        if (cursor != null
                && !(loader instanceof DirectoriesCursorLoader)
                && !(cursor instanceof SearchCursor)) {
            throw Assert.createIllegalStateFailException("Cursors must implement SearchCursor");
        }

        if (loader instanceof SearchContactsCursorLoader) {
            adapter.setContactsCursor((SearchCursor) cursor);

        } else if (loader instanceof NearbyPlacesCursorLoader) {
            adapter.setNearbyPlacesCursor((SearchCursor) cursor);

        } else if (loader instanceof DirectoryContactsCursorLoader) {
            adapter.setDirectoryContactsCursor((SearchCursor) cursor);

        } else if (loader instanceof DirectoriesCursorLoader) {
            directories.clear();
            directories.addAll(DirectoriesCursorLoader.toDirectories(cursor));
            loadNearbyPlacesCursor();
            loadDirectoryContactsCursors();

        } else {
            throw new IllegalStateException("Invalid loader: " + loader);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.i("NewSearchFragment.onLoaderReset", "Loader reset: " + loader);
        if (loader instanceof SearchContactsCursorLoader) {
            adapter.setContactsCursor(null);
        } else if (loader instanceof NearbyPlacesCursorLoader) {
            adapter.setNearbyPlacesCursor(null);
        } else if (loader instanceof DirectoryContactsCursorLoader) {
            adapter.setDirectoryContactsCursor(null);
        }
    }

    @VisibleForTesting
    public String getRawNumber() {
        return rawNumber;
    }

    public void setRawNumber(String rawNumber) {
        this.rawNumber = rawNumber;
    }

    public void setQuery(String query, CallInitiationType.Type callInitiationType) {
        this.query = query;
        this.callInitiationType = callInitiationType;
        if (adapter != null) {
            adapter.setQuery(query, rawNumber);
            adapter.setSearchActions(getActions());
            showLocationPermission();
            loadCp2ContactsCursor();
            loadNearbyPlacesCursor();
            loadDirectoryContactsCursors();
        }
    }

    /**
     * Returns true if the location permission was shown.
     */
    private boolean showLocationPermission() {
        if (adapter == null) {
            return false;
        }

        if (getContext() == null
                || PermissionsUtil.hasLocationPermissions(getContext())
                || hasBeenDismissed()
                || !isRegularSearch()) {
            adapter.hideLocationPermissionRequest();
            return false;
        }

        adapter.showLocationPermissionRequest(
                v -> requestLocationPermission(), v -> dismissLocationPermission());
        return true;
    }

    /**
     * Translate the search fragment and resize it to fit on the screen.
     */
    public void animatePosition(int start, int end, int duration) {
        // Called before the view is ready, prepare a runnable to run in onCreateView
        if (getView() == null) {
            updatePositionRunnable = () -> animatePosition(start, end, 0);
            return;
        }
        boolean slideUp = start > end;
        Interpolator interpolator = slideUp ? AnimUtils.EASE_IN : AnimUtils.EASE_OUT;
        int startHeight = getActivity().findViewById(android.R.id.content).getHeight();
        int endHeight = startHeight - (end - start);
        getView().setTranslationY(start);
        getView()
                .animate()
                .translationY(end)
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setUpdateListener(
                        animation -> setHeight(startHeight, endHeight, animation.getAnimatedFraction()));
        updatePositionRunnable = null;
    }

    private void setHeight(int start, int end, float percentage) {
        View view = getView();
        if (view == null) {
            return;
        }

        FrameLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.height = (int) (start + (end - start) * percentage);
        view.setLayoutParams(params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ThreadUtil.getUiThreadHandler().removeCallbacks(loaderCp2ContactsRunnable);
        ThreadUtil.getUiThreadHandler().removeCallbacks(loadNearbyPlacesRunnable);
        ThreadUtil.getUiThreadHandler().removeCallbacks(loadDirectoryContactsRunnable);
        ThreadUtil.getUiThreadHandler().removeCallbacks(capabilitiesUpdatedRunnable);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Force a refresh of the data since we were missing the permission before this.
                emptyContentView.setVisibility(View.GONE);
                initLoaders();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Force a refresh of the data since we were missing the permission before this.
                loadNearbyPlacesCursor();
                adapter.hideLocationPermissionRequest();
            }
        }
    }

    @Override
    public void onEmptyViewActionButtonClicked() {
        String[] deniedPermissions =
                PermissionsUtil.getPermissionsCurrentlyDenied(
                        getContext(), PermissionsUtil.allContactsGroupPermissionsUsedInDialer);
        if (deniedPermissions.length > 0) {
            LogUtil.i(
                    "NewSearchFragment.onEmptyViewActionButtonClicked",
                    "Requesting permissions: " + Arrays.toString(deniedPermissions));
            FragmentUtils.getParentUnsafe(this, SearchFragmentListener.class).requestingPermission();
            requestPermissions(deniedPermissions, READ_CONTACTS_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Loads info about all directories (local & remote).
     */
    private void loadDirectoriesCursor() {
        if (!directoriesDisabledForTesting) {
            getLoaderManager().initLoader(DIRECTORIES_LOADER_ID, null, this);
        }
    }

    /**
     * Loads contacts stored in directories.
     *
     * <p>Should not be called before finishing loading info about all directories (local & remote).
     */
    private void loadDirectoryContactsCursors() {
        if (directoriesDisabledForTesting) {
            return;
        }

        // Cancel existing load if one exists.
        ThreadUtil.getUiThreadHandler().removeCallbacks(loadDirectoryContactsRunnable);
        ThreadUtil.getUiThreadHandler()
                .postDelayed(loadDirectoryContactsRunnable, NETWORK_SEARCH_DELAY_MILLIS);
    }

    private void loadCp2ContactsCursor() {
        // Cancel existing load if one exists.
        ThreadUtil.getUiThreadHandler().removeCallbacks(loaderCp2ContactsRunnable);
        ThreadUtil.getUiThreadHandler()
                .postDelayed(loaderCp2ContactsRunnable, NETWORK_SEARCH_DELAY_MILLIS);
    }

    /**
     * Loads nearby places.
     *
     * <p>Should not be called before finishing loading info about all directories (local and remote).
     */
    private void loadNearbyPlacesCursor() {
        // If we're requesting the location permission, don't load nearby places cursor.
        if (showLocationPermission()) {
            return;
        }

        // If the user dismissed the prompt without granting us the permission, don't load the cursor.
        if (getContext() == null || !PermissionsUtil.hasLocationPermissions(getContext())) {
            return;
        }

        // Cancel existing load if one exists.
        ThreadUtil.getUiThreadHandler().removeCallbacks(loadNearbyPlacesRunnable);

        // If nearby places is not enabled, do not try to load them.
        if (!PhoneDirectoryExtenderAccessor.get(getContext()).isEnabled(getContext())) {
            return;
        }
        ThreadUtil.getUiThreadHandler()
                .postDelayed(loadNearbyPlacesRunnable, NETWORK_SEARCH_DELAY_MILLIS);
    }

    private void requestLocationPermission() {
        Assert.checkArgument(
                !PermissionsUtil.hasPermission(getContext(), ACCESS_FINE_LOCATION),
                "attempted to request already granted location permission");
        String[] deniedPermissions =
                PermissionsUtil.getPermissionsCurrentlyDenied(
                        getContext(), PermissionsUtil.allLocationGroupPermissionsUsedInDialer);
        FragmentUtils.getParentUnsafe(this, SearchFragmentListener.class).requestingPermission();
        requestPermissions(deniedPermissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @VisibleForTesting
    public void dismissLocationPermission() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(KEY_LOCATION_PROMPT_DISMISSED, true)
                .apply();
        adapter.hideLocationPermissionRequest();
    }

    private boolean hasBeenDismissed() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(KEY_LOCATION_PROMPT_DISMISSED, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        EnrichedCallComponent.get(getContext())
                .getEnrichedCallManager()
                .registerCapabilitiesListener(this);
        getLoaderManager().restartLoader(CONTACTS_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EnrichedCallComponent.get(getContext())
                .getEnrichedCallManager()
                .unregisterCapabilitiesListener(this);
    }

    @Override
    public void onCapabilitiesUpdated() {
        ThreadUtil.getUiThreadHandler().removeCallbacks(capabilitiesUpdatedRunnable);
        ThreadUtil.getUiThreadHandler()
                .postDelayed(capabilitiesUpdatedRunnable, ENRICHED_CALLING_CAPABILITIES_UPDATED_DELAY);
    }

    // Currently, setting up multiple FakeContentProviders doesn't work and results in this fragment
    // being untestable while it can query multiple datasources. This is a temporary fix.
    // TODO(a bug): Remove this method and test this fragment with multiple data sources
    @VisibleForTesting
    public void setDirectoriesDisabled(boolean disabled) {
        directoriesDisabledForTesting = disabled;
    }

    /**
     * Returns a list of search actions to be shown in the search results.
     *
     * <p>List will be empty if query is 1 or 0 characters or the query isn't from the Dialpad. For
     * the list of supported actions, see {@link SearchActionViewHolder.Action}.
     */
    private List<Integer> getActions() {
        boolean isDialableNumber = PhoneNumberUtils.isGlobalPhoneNumber(query);
        boolean nonDialableQueryInRegularSearch = isRegularSearch() && !isDialableNumber;
        if (TextUtils.isEmpty(query) || query.length() == 1 || nonDialableQueryInRegularSearch) {
            return Collections.emptyList();
        }

        List<Integer> actions = new ArrayList<>();
        if (!isRegularSearch()) {
            actions.add(Action.CREATE_NEW_CONTACT);
            actions.add(Action.ADD_TO_CONTACT);
        }

        if (isRegularSearch() && isDialableNumber) {
            actions.add(Action.MAKE_VOICE_CALL);
        }

        actions.add(Action.SEND_SMS);
        if (CallUtil.isVideoEnabled(getContext())) {
            actions.add(Action.MAKE_VILTE_CALL);
        }

        return actions;
    }

    // Returns true if currently in Regular Search (as opposed to Dialpad Search).
    private boolean isRegularSearch() {
        return callInitiationType == CallInitiationType.Type.REGULAR_SEARCH;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.performClick();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            FragmentUtils.getParentUnsafe(this, SearchFragmentListener.class).onSearchListTouch();
        }
        return false;
    }

    @Override
    public void placeVoiceCall(String phoneNumber, int ranking) {
        placeCall(phoneNumber, ranking, false);
    }

    @Override
    public void placeVideoCall(String phoneNumber, int ranking) {
        placeCall(phoneNumber, ranking, true);
    }

    private void placeCall(String phoneNumber, int position, boolean isVideoCall) {
        CallSpecificAppData callSpecificAppData =
                CallSpecificAppData.newBuilder()
                        .setCallInitiationType(callInitiationType)
                        .setPositionOfSelectedSearchResult(position)
                        .setCharactersInSearchString(query == null ? 0 : query.length())
                        .setAllowAssistedDialing(true)
                        .build();
        PreCall.start(
                getContext(),
                new CallIntentBuilder(phoneNumber, callSpecificAppData)
                        .setIsVideoCall(isVideoCall)
                        .setAllowAssistedDial(true));
        FragmentUtils.getParentUnsafe(this, SearchFragmentListener.class).onCallPlacedFromSearch();
    }

    @Override
    public void placeDuoCall(String phoneNumber) {
        Logger.get(getContext())
                .logImpression(DialerImpression.Type.LIGHTBRINGER_VIDEO_REQUESTED_FROM_SEARCH);
        PreCall.start(
                getContext(),
                new CallIntentBuilder(phoneNumber, CallInitiationType.Type.REGULAR_SEARCH)
                        .setIsVideoCall(true)
                        .setIsDuoCall(true));
        FragmentUtils.getParentUnsafe(this, SearchFragmentListener.class).onCallPlacedFromSearch();
    }

    @Override
    public void openCallAndShare(DialerContact contact) {
        Intent intent = CallComposerActivity.newIntent(getContext(), contact);
        DialerUtils.startActivityWithErrorToast(getContext(), intent);
    }

    /**
     * Callback to {@link NewSearchFragment}'s parent to be notified of important events.
     */
    public interface SearchFragmentListener {

        /**
         * Called when the list view in {@link NewSearchFragment} is clicked.
         */
        void onSearchListTouch();

        /**
         * Called when a call is placed from the search fragment.
         */
        void onCallPlacedFromSearch();

        /**
         * Called when a permission is about to be requested.
         */
        void requestingPermission();
    }
}
