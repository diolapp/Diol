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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import app.diol.R;

/**
 * Simple Activity that renders locally stored open source legal info in a text view.
 */
public final class LicenseActivity extends AppCompatActivity {
    private static final String TAG = "LicenseActivity";
    private static final String STATE_SCROLL_POS = "scroll_pos";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.license_scrollview);

        License license = getIntent().getParcelableExtra(LicenseMenuActivity.ARGS_LICENSE);
        getSupportActionBar().setTitle(license.getLibraryName());

        // Show 'up' button with no logo.
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(null);

        TextView textView = (TextView) findViewById(R.id.license_activity_textview);
        String licenseText = Licenses.getLicenseText(this, license);
        if (licenseText == null) {
            finish();
            return;
        }
        textView.setText(licenseText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ScrollView scrollView = (ScrollView) findViewById(R.id.license_activity_scrollview);
        TextView textView = (TextView) findViewById(R.id.license_activity_textview);
        int firstVisibleLine = textView.getLayout().getLineForVertical(scrollView.getScrollY());
        int firstVisibleChar = textView.getLayout().getLineStart(firstVisibleLine);
        outState.putInt(STATE_SCROLL_POS, firstVisibleChar);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.license_activity_scrollview);
        final int firstVisibleChar = savedInstanceState.getInt(STATE_SCROLL_POS);
        scrollView.post(
                new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.license_activity_textview);
                        int firstVisibleLine = textView.getLayout().getLineForOffset(firstVisibleChar);
                        int offset = textView.getLayout().getLineTop(firstVisibleLine);
                        scrollView.scrollTo(0, offset);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
