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

package app.diol.dialer.feedback.stub;

import android.content.Context;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.feedback.FeedbackSender;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import app.diol.dialer.logging.LoggingBindings;
import app.diol.dialer.logging.LoggingBindingsFactory;
import app.diol.dialer.logging.LoggingBindingsStub;
import app.diol.incallui.call.CallList;
import dagger.Module;
import dagger.Provides;

/**
 * Module which bind {@link app.diol.dialer.feedback.stub.CallFeedbackListenerStub}.
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public class StubFeedbackModule {

    @Provides
    static LoggingBindings provideLoggingBindings(LoggingBindingsFactory factory) {
        return new LoggingBindingsStub();
    }

    @Provides
    static FeedbackSender provideCallFeedbackSender() {
        LogUtil.i("StubFeedbackModule.provideCallFeedbackSender", "return stub");
        return new FeedbackSenderStub();
    }

    @Provides
    static CallList.Listener provideCallFeedbackListener(@ApplicationContext Context context) {
        LogUtil.i("StubFeedbackModule.provideCallFeedbackListener", "returning stub");
        return new CallFeedbackListenerStub(context);
    }
}
