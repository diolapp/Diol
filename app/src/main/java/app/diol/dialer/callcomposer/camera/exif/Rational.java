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

package app.diol.dialer.callcomposer.camera.exif;

import java.util.Objects;

/**
 * The rational data type of EXIF tag. Contains a pair of longs representing the numerator and
 * denominator of a Rational number.
 */
public class Rational {

    private final long numerator;
    private final long denominator;

    /**
     * Create a Rational with a given numerator and denominator.
     */
    Rational(long nominator, long denominator) {
        numerator = nominator;
        this.denominator = denominator;
    }

    /**
     * Gets the numerator of the rational.
     */
    long getNumerator() {
        return numerator;
    }

    /**
     * Gets the denominator of the rational
     */
    long getDenominator() {
        return denominator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rational) {
            Rational data = (Rational) obj;
            return numerator == data.numerator && denominator == data.denominator;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
}
