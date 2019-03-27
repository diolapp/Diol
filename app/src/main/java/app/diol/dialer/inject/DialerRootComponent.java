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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the place with this annotation when a RootComponent is needed.
 *
 * <p>Usually users put this annotation on application class that is root of dependencies (the last
 * thing to compile). The annotation processor will figure out what it needs to generate a variant
 * root through dependencies.
 *
 * <p>Example:
 *
 * <pre>
 * <code>
 * @DialerRootComponent(variant = DialerVariant.DIALER_AOSP)
 * public class RootDialerAosp {}
 * </code>
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DialerRootComponent {
    DialerVariant variant();

    Class<?> injectClass() default Object.class;
}
