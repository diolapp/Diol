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
package app.diol.voicemail.impl.mail;

import android.util.ArrayMap;

import java.util.Map;

/**
 * A utility class for creating and modifying Strings that are tagged and packed together.
 *
 * <p>Uses non-printable (control chars) for internal delimiters; Intended for regular displayable
 * strings only, so please use base64 or other encoding if you need to hide any binary data here.
 *
 * <p>Binary compatible with Address.pack() format, which should migrate to use this code.
 */
public class PackedString {

    /**
     * Packing format is: element : [ value ] or [ value TAG-DELIMITER tag ] packed-string : [ element
     * ] [ ELEMENT-DELIMITER [ element ] ]*
     */
    private static final char DELIMITER_ELEMENT = '\1';

    private static final char DELIMITER_TAG = '\2';
    private static final ArrayMap<String, String> EMPTY_MAP = new ArrayMap<String, String>();
    private String string;
    private ArrayMap<String, String> exploded;

    /**
     * Create a packed string using an already-packed string (e.g. from database)
     *
     * @param string packed string
     */
    public PackedString(String string) {
        this.string = string;
        exploded = null;
    }

    /**
     * Read out all values into a map.
     */
    private static ArrayMap<String, String> explode(String packed) {
        if (packed == null || packed.length() == 0) {
            return EMPTY_MAP;
        }
        ArrayMap<String, String> map = new ArrayMap<String, String>();

        int length = packed.length();
        int elementStartIndex = 0;
        int elementEndIndex = 0;
        int tagEndIndex = packed.indexOf(DELIMITER_TAG);

        while (elementStartIndex < length) {
            elementEndIndex = packed.indexOf(DELIMITER_ELEMENT, elementStartIndex);
            if (elementEndIndex == -1) {
                elementEndIndex = length;
            }
            String tag;
            String value;
            if (tagEndIndex == -1 || elementEndIndex <= tagEndIndex) {
                // in this case the DELIMITER_PERSONAL is in a future pair (or not found)
                // so synthesize a positional tag for the value, and don't update tagEndIndex
                value = packed.substring(elementStartIndex, elementEndIndex);
                tag = Integer.toString(map.size());
            } else {
                value = packed.substring(elementStartIndex, tagEndIndex);
                tag = packed.substring(tagEndIndex + 1, elementEndIndex);
                // scan forward for next tag, if any
                tagEndIndex = packed.indexOf(DELIMITER_TAG, elementEndIndex + 1);
            }
            map.put(tag, value);
            elementStartIndex = elementEndIndex + 1;
        }

        return map;
    }

    /**
     * Get the value referred to by a given tag. If the tag does not exist, return null.
     *
     * @param tag identifier of string of interest
     * @return returns value, or null if no string is found
     */
    public String get(String tag) {
        if (exploded == null) {
            exploded = explode(string);
        }
        return exploded.get(tag);
    }

    /**
     * Return a map of all of the values referred to by a given tag. This is a shallow copy, don't
     * edit the values.
     *
     * @return a map of the values in the packed string
     */
    public Map<String, String> unpack() {
        if (exploded == null) {
            exploded = explode(string);
        }
        return new ArrayMap<String, String>(exploded);
    }

    /**
     * Builder class for creating PackedString values. Can also be used for editing existing
     * PackedString representations.
     */
    public static class Builder {
        ArrayMap<String, String> map;

        /**
         * Create a builder that's empty (for filling)
         */
        public Builder() {
            map = new ArrayMap<String, String>();
        }

        /**
         * Create a builder using the values of an existing PackedString (for editing).
         */
        public Builder(String packed) {
            map = explode(packed);
        }

        /**
         * Add a tagged value
         *
         * @param tag   identifier of string of interest
         * @param value the value to record in this position. null to delete entry.
         */
        public void put(String tag, String value) {
            if (value == null) {
                map.remove(tag);
            } else {
                map.put(tag, value);
            }
        }

        /**
         * Get the value referred to by a given tag. If the tag does not exist, return null.
         *
         * @param tag identifier of string of interest
         * @return returns value, or null if no string is found
         */
        public String get(String tag) {
            return map.get(tag);
        }

        /**
         * Pack the values and return a single, encoded string
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(DELIMITER_ELEMENT);
                }
                sb.append(entry.getValue());
                sb.append(DELIMITER_TAG);
                sb.append(entry.getKey());
            }
            return sb.toString();
        }
    }
}
