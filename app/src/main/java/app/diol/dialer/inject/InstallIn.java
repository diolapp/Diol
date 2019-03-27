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

package app.diol.dialer.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation for {@link dagger.Module dagger.Modules} which causes them to be installed in the
 * specified variants.
 *
 * <p>It has a parameter for users to enter on which variants annotated module will be installed and
 * also must be non-empty. Example:
 *
 * <pre>
 * <code>
 * @InstallIn(variants = {DialerVariant.DIALER_AOSP, DialerVariant.DIALER_TEST})
 * public class Module1 {}
 *
 * </code>
 * </pre>
 */
@Target(ElementType.TYPE)
public @interface InstallIn {
    DialerVariant[] variants();
}
