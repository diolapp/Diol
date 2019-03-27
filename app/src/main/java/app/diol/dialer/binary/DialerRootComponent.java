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

package app.diol.dialer.binary;

import javax.inject.Singleton;

import app.diol.bubble.stub.StubBubbleModule;
import app.diol.dialer.activecalls.ActiveCallsModule;
import app.diol.dialer.calllog.CallLogModule;
import app.diol.dialer.calllog.config.CallLogConfigModule;
import app.diol.dialer.commandline.CommandLineModule;
import app.diol.dialer.common.concurrent.DialerExecutorModule;
import app.diol.dialer.configprovider.SharedPrefConfigProviderModule;
import app.diol.dialer.contacts.ContactsModule;
import app.diol.dialer.duo.stub.StubDuoModule;
import app.diol.dialer.enrichedcall.stub.StubEnrichedCallModule;
import app.diol.dialer.feedback.stub.StubFeedbackModule;
import app.diol.dialer.glidephotomanager.GlidePhotoManagerModule;
import app.diol.dialer.inject.ContextModule;
import app.diol.dialer.metrics.StubMetricsModule;
import app.diol.dialer.phonelookup.PhoneLookupModule;
import app.diol.dialer.phonenumbergeoutil.impl.PhoneNumberGeoUtilModule;
import app.diol.dialer.precall.impl.PreCallModule;
import app.diol.dialer.preferredsim.PreferredSimModule;
import app.diol.dialer.preferredsim.suggestion.stub.StubSimSuggestionModule;
import app.diol.dialer.promotion.impl.PromotionModule;
import app.diol.dialer.spam.stub.StubSpamModule;
import app.diol.dialer.storage.StorageModule;
import app.diol.dialer.strictmode.impl.SystemStrictModeModule;
import app.diol.dialer.theme.base.impl.AospThemeModule;
import app.diol.incallui.calllocation.stub.StubCallLocationModule;
import app.diol.incallui.maps.stub.StubMapsModule;
import app.diol.incallui.speakeasy.StubSpeakEasyModule;
import app.diol.voicemail.impl.VoicemailModule;
import dagger.Component;

/**
 * Root component for the AOSP Dialer application.
 */
@Singleton
@Component(
        modules = {
                ActiveCallsModule.class,
                CallLogModule.class,
                CallLogConfigModule.class,
                CommandLineModule.class,
                ContactsModule.class,
                ContextModule.class,
                DialerExecutorModule.class,
                GlidePhotoManagerModule.class,
                PhoneLookupModule.class,
                PhoneNumberGeoUtilModule.class,
                PreCallModule.class,
                PreferredSimModule.class,
                PromotionModule.class,
                SharedPrefConfigProviderModule.class,
                StorageModule.class,
                StubCallLocationModule.class,
                StubDuoModule.class,
                StubEnrichedCallModule.class,
                StubBubbleModule.class,
                StubMetricsModule.class,
                StubFeedbackModule.class,
                StubMapsModule.class,
                StubSimSuggestionModule.class,
                StubSpamModule.class,
                StubSpeakEasyModule.class,
                SystemStrictModeModule.class,
                AospThemeModule.class,
                VoicemailModule.class,
        })
public interface DialerRootComponent extends BaseDialerRootComponent {
}
