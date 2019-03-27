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

/**
 * Definition for utilities that supports smart dial in different languages.
 */
@SuppressWarnings("Guava")
abstract class SmartDialMap {

    /**
     * Returns true if the provided character can be mapped to a key on the dialpad.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     */
    protected boolean isValidDialpadCharacter(char ch) {
        return isValidDialpadAlphabeticChar(ch) || isValidDialpadNumericChar(ch);
    }

    /**
     * Returns true if the provided character is a letter and can be mapped to a key on the dialpad.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     */
    protected boolean isValidDialpadAlphabeticChar(char ch) {
        return getCharToKeyMap().containsKey(ch);
    }

    /**
     * Returns true if the provided character is a digit, and can be mapped to a key on the dialpad.
     */
    protected boolean isValidDialpadNumericChar(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * Get the index of the key on the dialpad which the character corresponds to.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     *
     * <p>An {@link Optional#absent()} is returned if the provided character can't be mapped to a key
     * on the dialpad.
     */
    protected Optional<Byte> getDialpadIndex(char ch) {
        if (isValidDialpadNumericChar(ch)) {
            return Optional.of((byte) (ch - '0'));
        }

        if (isValidDialpadAlphabeticChar(ch)) {
            return Optional.of((byte) (getCharToKeyMap().get(ch) - '0'));
        }

        return Optional.absent();
    }

    /**
     * Get the actual numeric character on the dialpad which the character corresponds to.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     *
     * <p>An {@link Optional#absent()} is returned if the provided character can't be mapped to a key
     * on the dialpad.
     */
    protected Optional<Character> getDialpadNumericCharacter(char ch) {
        return isValidDialpadAlphabeticChar(ch)
                ? Optional.of(getCharToKeyMap().get(ch))
                : Optional.absent();
    }

    /**
     * Converts uppercase characters to lower case ones, and on a best effort basis, strips accents
     * from accented characters.
     *
     * <p>An {@link Optional#absent()} is returned if the provided character can't be mapped to a key
     * on the dialpad.
     */
    abstract Optional<Character> normalizeCharacter(char ch);

    /**
     * Returns a map in which each key is a normalized character and the corresponding value is a
     * dialpad key.
     */
    abstract SimpleArrayMap<Character, Character> getCharToKeyMap();
}
