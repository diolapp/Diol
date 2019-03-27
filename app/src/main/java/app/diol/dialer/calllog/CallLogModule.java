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

package app.diol.dialer.calllog;

import com.google.common.collect.ImmutableList;

import app.diol.dialer.calllog.database.CallLogDatabaseModule;
import app.diol.dialer.calllog.datasources.CallLogDataSource;
import app.diol.dialer.calllog.datasources.DataSources;
import app.diol.dialer.calllog.datasources.phonelookup.PhoneLookupDataSource;
import app.diol.dialer.calllog.datasources.systemcalllog.SystemCallLogDataSource;
import app.diol.dialer.calllog.datasources.voicemail.VoicemailDataSource;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module which satisfies call log dependencies.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module(includes = CallLogDatabaseModule.class)
public abstract class CallLogModule {

    @Provides
    static DataSources provideCallLogDataSources(
            SystemCallLogDataSource systemCallLogDataSource,
            PhoneLookupDataSource phoneLookupDataSource,
            VoicemailDataSource voicemailDataSource) {
        // System call log must be first, see getDataSourcesExcludingSystemCallLog below.
        ImmutableList<CallLogDataSource> allDataSources =
                ImmutableList.of(systemCallLogDataSource, phoneLookupDataSource, voicemailDataSource);
        return new DataSources() {
            @Override
            public SystemCallLogDataSource getSystemCallLogDataSource() {
                return systemCallLogDataSource;
            }

            @Override
            public ImmutableList<CallLogDataSource> getDataSourcesIncludingSystemCallLog() {
                return allDataSources;
            }

            @Override
            public ImmutableList<CallLogDataSource> getDataSourcesExcludingSystemCallLog() {
                return allDataSources.subList(1, allDataSources.size());
            }
        };
    }
}
