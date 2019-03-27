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

import app.diol.bubble.BubbleComponent;
import app.diol.dialer.activecalls.ActiveCallsComponent;
import app.diol.dialer.calllog.CallLogComponent;
import app.diol.dialer.calllog.config.CallLogConfigComponent;
import app.diol.dialer.calllog.database.CallLogDatabaseComponent;
import app.diol.dialer.calllog.ui.CallLogUiComponent;
import app.diol.dialer.commandline.CommandLineComponent;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.contacts.ContactsComponent;
import app.diol.dialer.duo.DuoComponent;
import app.diol.dialer.enrichedcall.EnrichedCallComponent;
import app.diol.dialer.feedback.FeedbackComponent;
import app.diol.dialer.glidephotomanager.GlidePhotoManagerComponent;
import app.diol.dialer.metrics.MetricsComponent;
import app.diol.dialer.phonelookup.PhoneLookupComponent;
import app.diol.dialer.phonelookup.database.PhoneLookupDatabaseComponent;
import app.diol.dialer.phonenumbergeoutil.PhoneNumberGeoUtilComponent;
import app.diol.dialer.precall.PreCallComponent;
import app.diol.dialer.preferredsim.PreferredSimComponent;
import app.diol.dialer.preferredsim.suggestion.SimSuggestionComponent;
import app.diol.dialer.promotion.PromotionComponent;
import app.diol.dialer.spam.SpamComponent;
import app.diol.dialer.speeddial.loader.UiItemLoaderComponent;
import app.diol.dialer.storage.StorageComponent;
import app.diol.dialer.strictmode.StrictModeComponent;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.incallui.calllocation.CallLocationComponent;
import app.diol.incallui.maps.MapsComponent;
import app.diol.incallui.speakeasy.SpeakEasyComponent;
import app.diol.voicemail.VoicemailComponent;

/**
 * Base class for the core application-wide component. All variants of the Dialer app should extend
 * from this component.
 */
public interface BaseDialerRootComponent
        extends ActiveCallsComponent.HasComponent,
        BubbleComponent.HasComponent,
        CallLocationComponent.HasComponent,
        CallLogComponent.HasComponent,
        CallLogConfigComponent.HasComponent,
        CallLogDatabaseComponent.HasComponent,
        CallLogUiComponent.HasComponent,
        ConfigProviderComponent.HasComponent,
        CommandLineComponent.HasComponent,
        ContactsComponent.HasComponent,
        DialerExecutorComponent.HasComponent,
        DuoComponent.HasComponent,
        EnrichedCallComponent.HasComponent,
        FeedbackComponent.HasComponent,
        GlidePhotoManagerComponent.HasComponent,
        MapsComponent.HasComponent,
        MetricsComponent.HasComponent,
        PhoneLookupComponent.HasComponent,
        PhoneLookupDatabaseComponent.HasComponent,
        PhoneNumberGeoUtilComponent.HasComponent,
        PreCallComponent.HasComponent,
        PreferredSimComponent.HasComponent,
        PromotionComponent.HasComponent,
        UiItemLoaderComponent.HasComponent,
        SimSuggestionComponent.HasComponent,
        SpamComponent.HasComponent,
        SpeakEasyComponent.HasComponent,
        StorageComponent.HasComponent,
        StrictModeComponent.HasComponent,
        ThemeComponent.HasComponent,
        VoicemailComponent.HasComponent {
}
