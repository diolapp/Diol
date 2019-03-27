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

package app.diol.dialer.commandline;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import app.diol.dialer.function.Supplier;

/**
 * Supplies commands
 */
@AutoValue
public abstract class CommandSupplier implements Supplier<ImmutableMap<String, Command>> {

    public static Builder builder() {
        return new AutoValue_CommandSupplier.Builder();
    }

    public abstract ImmutableMap<String, Command> commands();

    @Override
    public ImmutableMap<String, Command> get() {
        return commands();
    }

    /**
     * builder for the supplier
     */
    @AutoValue.Builder
    public abstract static class Builder {

        abstract ImmutableMap.Builder<String, Command> commandsBuilder();

        public Builder addCommand(String key, Command command) {
            commandsBuilder().put(key, command);
            return this;
        }

        public abstract CommandSupplier build();
    }
}
