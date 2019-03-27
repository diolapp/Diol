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

package app.diol.incallui.speakeasy;

import android.content.Context;
import android.preference.PreferenceActivity;

import com.google.common.base.Optional;

import app.diol.dialer.inject.HasRootComponent;
import app.diol.incallui.speakeasy.Annotations.SpeakEasyChipResourceId;
import app.diol.incallui.speakeasy.Annotations.SpeakEasySettingsActivity;
import app.diol.incallui.speakeasy.Annotations.SpeakEasySettingsObject;
import app.diol.incallui.speakeasy.Annotations.SpeakEasyTextResourceId;
import dagger.Subcomponent;

/**
 * Dagger component to get SpeakEasyCallManager.
 */
@Subcomponent
public abstract class SpeakEasyComponent {

    public static SpeakEasyComponent get(Context context) {
        return ((SpeakEasyComponent.HasComponent)
                ((HasRootComponent) context.getApplicationContext()).component())
                .speakEasyComponent();
    }

    public abstract SpeakEasyCallManager speakEasyCallManager();

    public abstract @SpeakEasySettingsActivity
    Optional<PreferenceActivity>
    speakEasySettingsActivity();

    public abstract @SpeakEasySettingsObject
    Optional<Object> speakEasySettingsObject();

    public abstract @SpeakEasyChipResourceId
    Optional<Integer> speakEasyChip();

    public abstract @SpeakEasyTextResourceId
    Optional<Integer> speakEasyTextResource();

    /**
     * Used to refer to the root application component.
     */
    public interface HasComponent {
        SpeakEasyComponent speakEasyComponent();
    }
}
