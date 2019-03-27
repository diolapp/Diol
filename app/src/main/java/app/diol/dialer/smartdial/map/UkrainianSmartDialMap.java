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

package app.diol.dialer.smartdial.map;

import android.support.v4.util.SimpleArrayMap;

import com.google.common.base.Optional;

import app.diol.dialer.dialpadview.DialpadCharMappings;

/**
 * A {@link SmartDialMap} for the Ukrainian alphabet.
 */
final class UkrainianSmartDialMap extends SmartDialMap {

    private static UkrainianSmartDialMap instance;

    private UkrainianSmartDialMap() {
    }

    static UkrainianSmartDialMap getInstance() {
        if (instance == null) {
            instance = new UkrainianSmartDialMap();
        }

        return instance;
    }

    @Override
    Optional<Character> normalizeCharacter(char ch) {
        ch = Character.toLowerCase(ch);
        return isValidDialpadAlphabeticChar(ch) ? Optional.of(ch) : Optional.absent();
    }

    @Override
    SimpleArrayMap<Character, Character> getCharToKeyMap() {
        return DialpadCharMappings.getCharToKeyMap("ukr");
    }
}
