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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class to store the name of a library and the filename of its associated license file.
 */
public final class License implements Comparable<License>, Parcelable {
    public static final Parcelable.Creator<License> CREATOR =
            new Parcelable.Creator<License>() {
                @Override
                public License createFromParcel(Parcel in) {
                    return new License(in);
                }

                @Override
                public License[] newArray(int size) {
                    return new License[size];
                }
            };
    // Name of the third-party library.
    private final String libraryName;
    // Byte offset in the file to the start of the license text.
    private final long licenseOffset;
    // Byte length of the license text.
    private final int licenseLength;

    private License(String libraryName, long licenseOffset, int licenseLength) {
        this.libraryName = libraryName;
        this.licenseOffset = licenseOffset;
        this.licenseLength = licenseLength;
    }

    private License(Parcel in) {
        libraryName = in.readString();
        licenseOffset = in.readLong();
        licenseLength = in.readInt();
    }

    /**
     * Create an object representing a stored license. The text for all licenses is stored in a single
     * file, so the offset and length describe this license's position within the file.
     */
    static License create(String libraryName, long licenseOffset, int licenseLength) {
        return new License(libraryName, licenseOffset, licenseLength);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(libraryName);
        dest.writeLong(licenseOffset);
        dest.writeInt(licenseLength);
    }

    @Override
    public int compareTo(License o) {
        return libraryName.compareToIgnoreCase(o.getLibraryName());
    }

    @Override
    public String toString() {
        return getLibraryName();
    }

    String getLibraryName() {
        return libraryName;
    }

    long getLicenseOffset() {
        return licenseOffset;
    }

    int getLicenseLength() {
        return licenseLength;
    }
}
