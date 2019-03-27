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

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;

import com.google.common.base.Optional;

import app.diol.dialer.i18n.LocaleUtils;

/**
 * A utility class that combines the functionality of two implementations of {@link SmartDialMap} so
 * that we support smart dial for dual alphabets.
 *
 * <p>Of the two implementations of {@link SmartDialMap}, the default one always takes precedence.
 * The second one is consulted only when the default one is unable to provide a valid result.
 *
 * <p>Note that the second implementation can be absent if it is not defined for the system's 1st
 * language preference.
 */
@SuppressWarnings("Guava")
public class CompositeSmartDialMap {

    private static final SmartDialMap DEFAULT_MAP = LatinSmartDialMap.getInstance();

    // A map in which each key is an ISO 639-2 language code and the corresponding value is a
    // SmartDialMap
    private static final SimpleArrayMap<String, SmartDialMap> EXTRA_MAPS = new SimpleArrayMap<>();

    static {
        EXTRA_MAPS.put("bul", BulgarianSmartDialMap.getInstance());
        EXTRA_MAPS.put("rus", RussianSmartDialMap.getInstance());
        EXTRA_MAPS.put("ukr", UkrainianSmartDialMap.getInstance());
    }

    private CompositeSmartDialMap() {
    }

    /**
     * Returns true if the provided character can be mapped to a key on the dialpad.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     */
    public static boolean isValidDialpadCharacter(Context context, char ch) {
        if (DEFAULT_MAP.isValidDialpadCharacter(ch)) {
            return true;
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        return extraMap.isPresent() && extraMap.get().isValidDialpadCharacter(ch);
    }

    /**
     * Returns true if the provided character is a letter, and can be mapped to a key on the dialpad.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     */
    public static boolean isValidDialpadAlphabeticChar(Context context, char ch) {
        if (DEFAULT_MAP.isValidDialpadAlphabeticChar(ch)) {
            return true;
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        return extraMap.isPresent() && extraMap.get().isValidDialpadAlphabeticChar(ch);
    }

    /**
     * Returns true if the provided character is a digit, and can be mapped to a key on the dialpad.
     */
    public static boolean isValidDialpadNumericChar(Context context, char ch) {
        if (DEFAULT_MAP.isValidDialpadNumericChar(ch)) {
            return true;
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        return extraMap.isPresent() && extraMap.get().isValidDialpadNumericChar(ch);
    }

    /**
     * Get the index of the key on the dialpad which the character corresponds to.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     *
     * <p>If the provided character can't be mapped to a key on the dialpad, return -1.
     */
    public static byte getDialpadIndex(Context context, char ch) {
        Optional<Byte> dialpadIndex = DEFAULT_MAP.getDialpadIndex(ch);
        if (dialpadIndex.isPresent()) {
            return dialpadIndex.get();
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        if (extraMap.isPresent()) {
            dialpadIndex = extraMap.get().getDialpadIndex(ch);
        }

        return dialpadIndex.isPresent() ? dialpadIndex.get() : -1;
    }

    /**
     * Get the actual numeric character on the dialpad which the character corresponds to.
     *
     * <p>The provided character is expected to be a normalized character. See {@link
     * SmartDialMap#normalizeCharacter(char)} for details.
     *
     * <p>If the provided character can't be mapped to a key on the dialpad, return the character.
     */
    public static char getDialpadNumericCharacter(Context context, char ch) {
        Optional<Character> dialpadNumericChar = DEFAULT_MAP.getDialpadNumericCharacter(ch);
        if (dialpadNumericChar.isPresent()) {
            return dialpadNumericChar.get();
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        if (extraMap.isPresent()) {
            dialpadNumericChar = extraMap.get().getDialpadNumericCharacter(ch);
        }

        return dialpadNumericChar.isPresent() ? dialpadNumericChar.get() : ch;
    }

    /**
     * Converts uppercase characters to lower case ones, and on a best effort basis, strips accents
     * from accented characters.
     *
     * <p>If the provided character can't be mapped to a key on the dialpad, return the character.
     */
    public static char normalizeCharacter(Context context, char ch) {
        Optional<Character> normalizedChar = DEFAULT_MAP.normalizeCharacter(ch);
        if (normalizedChar.isPresent()) {
            return normalizedChar.get();
        }

        Optional<SmartDialMap> extraMap = getExtraMap(context);
        if (extraMap.isPresent()) {
            normalizedChar = extraMap.get().normalizeCharacter(ch);
        }

        return normalizedChar.isPresent() ? normalizedChar.get() : ch;
    }

    @VisibleForTesting
    static Optional<SmartDialMap> getExtraMap(Context context) {
        String languageCode = LocaleUtils.getLocale(context).getISO3Language();
        return EXTRA_MAPS.containsKey(languageCode)
                ? Optional.of(EXTRA_MAPS.get(languageCode))
                : Optional.absent();
    }
}
