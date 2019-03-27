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

package app.diol.dialer.inject;

/**
 * Represents all dialer variants.
 */
public enum DialerVariant {
    // AOSP Dialer variants
    DIALER_AOSP("DialerAosp"),
    DIALER_AOSP_ESPRESSO("DialerAospEspresso"),
    DIALER_ROBOLECTRIC("DialerRobolectric"),


    // TEST variant will be used in situations where we need create in-test application class which
    // doesn't belong to any variants listed above
    DIALER_TEST("DialerTest"),
    // Just for sample code in inject/demo.
    DIALER_DEMO("DialerDemo");

    private final String variant;

    DialerVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String toString() {
        return variant;
    }
}
