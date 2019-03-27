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

import android.content.Context;
import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import app.diol.R;
import app.diol.dialer.common.Assert;

/**
 * A helper for extracting licenses.
 */
public final class Licenses {
    private static final String TAG = "Licenses";
    private static final String LICENSE_FILENAME = "third_party_licenses";
    private static final String LICENSE_METADATA_FILENAME = "third_party_license_metadata";

    /**
     * Return the licenses bundled into this app.
     */
    public static ArrayList<License> getLicenses(Context context) {
        return getLicenseListFromMetadata(
                getTextFromResource(context.getApplicationContext(), LICENSE_METADATA_FILENAME, 0, -1));
    }

    /**
     * Returns a list of {@link License}s parsed from a license metadata file.
     *
     * @param metadata a {@code String} containing the contents of a license metadata file.
     */
    private static ArrayList<License> getLicenseListFromMetadata(String metadata) {
        String[] entries = metadata.split("\n");
        ArrayList<License> licenses = new ArrayList<License>(entries.length);
        for (String entry : entries) {
            int delimiter = entry.indexOf(' ');
            String[] licenseLocation = entry.substring(0, delimiter).split(":");
            Assert.checkState(
                    delimiter > 0 && licenseLocation.length == 2,
                    "Invalid license meta-data line:\n" + entry);
            long licenseOffset = Long.parseLong(licenseLocation[0]);
            int licenseLength = Integer.parseInt(licenseLocation[1]);
            licenses.add(License.create(entry.substring(delimiter + 1), licenseOffset, licenseLength));
        }
        Collections.sort(licenses);
        return licenses;
    }

    /**
     * Return the text of a bundled license file.
     */
    public static String getLicenseText(Context context, License license) {
        long offset = license.getLicenseOffset();
        int length = license.getLicenseLength();
        return getTextFromResource(context, LICENSE_FILENAME, offset, length);
    }

    private static String getTextFromResource(
            Context context, String filename, long offset, int length) {
        Resources resources = context.getApplicationContext().getResources();
        // When aapt is called with --rename-manifest-package, the package name is changed for the
        // application, but not for the resources. This is to find the package name of a known
        // resource to know what package to lookup the license files in.
        String packageName = resources.getResourcePackageName(R.id.license);
        InputStream stream =
                resources.openRawResource(resources.getIdentifier(filename, "raw", packageName));
        return getTextFromInputStream(stream, offset, length);
    }

    private static String getTextFromInputStream(InputStream stream, long offset, int length) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream textArray = new ByteArrayOutputStream();

        try {
            stream.skip(offset);
            int bytesRemaining = length > 0 ? length : Integer.MAX_VALUE;
            int bytes = 0;

            while (bytesRemaining > 0
                    && (bytes = stream.read(buffer, 0, Math.min(bytesRemaining, buffer.length))) != -1) {
                textArray.write(buffer, 0, bytes);
                bytesRemaining -= bytes;
            }
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read license or metadata text.", e);
        }
        try {
            return textArray.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding UTF8. This should always be supported.", e);
        }
    }
}
