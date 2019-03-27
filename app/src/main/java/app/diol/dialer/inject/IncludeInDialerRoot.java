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
 * Annotates a type that should be included in Dialer Root Component. Typically, annotated types are
 * HasComponent interfaces.
 *
 * <p>An example:
 *
 * <pre>
 * <code>
 * {@literal @}dagger.Subcomponent
 * public abstract class SimulatorComponent {
 *   public static SimulatorComponent get(Context context) {
 *      return ((HasComponent)((HasRootComponent) context.getApplicationContext()).component())
 *         .simulatorComponent();
 *   }
 *   {@literal @}IncludeInDialerRoot
 *   public interface HasComponent {
 *      SimulatorComponent simulatorComponent();
 *  }
 * }
 * </code>
 * </pre>
 */
@Target(ElementType.TYPE)
public @interface IncludeInDialerRoot {
    Class<?>[] modules() default {};
}
