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

package app.diol.dialer.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import app.diol.R;

/**
 * An Activity listing third party libraries with notice licenses.
 */
public final class LicenseMenuActivity extends AppCompatActivity
        implements LoaderCallbacks<List<License>> {

    static final String ARGS_LICENSE = "license";

    private static final int LOADER_ID = 54321;

    private ArrayAdapter<License> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license_menu_activity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listAdapter = new ArrayAdapter<>(this, R.layout.license, R.id.license, new ArrayList<>());
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        ListView listView = (ListView) findViewById(R.id.license_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        License license = (License) parent.getItemAtPosition(position);
                        Intent licenseIntent = new Intent(LicenseMenuActivity.this, LicenseActivity.class);
                        licenseIntent.putExtra(ARGS_LICENSE, license);
                        startActivity(licenseIntent);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Go back one place in the history stack, if the app icon is clicked.
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public Loader<List<License>> onCreateLoader(int id, Bundle args) {
        return new LicenseLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<License>> loader, List<License> licenses) {
        listAdapter.clear();
        listAdapter.addAll(licenses);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<License>> loader) {
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
    }
}
