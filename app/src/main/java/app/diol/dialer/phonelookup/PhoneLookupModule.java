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

package app.diol.dialer.phonelookup;

import com.google.common.collect.ImmutableList;

import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import app.diol.dialer.phonelookup.blockednumber.SystemBlockedNumberPhoneLookup;
import app.diol.dialer.phonelookup.cequint.CequintPhoneLookup;
import app.diol.dialer.phonelookup.cnap.CnapPhoneLookup;
import app.diol.dialer.phonelookup.cp2.Cp2DefaultDirectoryPhoneLookup;
import app.diol.dialer.phonelookup.cp2.Cp2ExtendedDirectoryPhoneLookup;
import app.diol.dialer.phonelookup.emergency.EmergencyPhoneLookup;
import app.diol.dialer.phonelookup.spam.SpamPhoneLookup;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module which binds the PhoneLookup implementation.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class PhoneLookupModule {

    @Provides
    @SuppressWarnings({"unchecked", "rawtype"})
    static ImmutableList<PhoneLookup> providePhoneLookupList(
            CequintPhoneLookup cequintPhoneLookup,
            CnapPhoneLookup cnapPhoneLookup,
            Cp2DefaultDirectoryPhoneLookup cp2DefaultDirectoryPhoneLookup,
            Cp2ExtendedDirectoryPhoneLookup cp2ExtendedDirectoryPhoneLookup,
            EmergencyPhoneLookup emergencyPhoneLookup,
            SystemBlockedNumberPhoneLookup systemBlockedNumberPhoneLookup,
            SpamPhoneLookup spamPhoneLookup) {
        return ImmutableList.of(
                cequintPhoneLookup,
                cnapPhoneLookup,
                cp2DefaultDirectoryPhoneLookup,
                cp2ExtendedDirectoryPhoneLookup,
                emergencyPhoneLookup,
                systemBlockedNumberPhoneLookup,
                spamPhoneLookup);
    }
}
