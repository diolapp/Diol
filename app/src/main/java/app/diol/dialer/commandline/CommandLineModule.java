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

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;

import app.diol.dialer.commandline.impl.ActiveCallsCommand;
import app.diol.dialer.commandline.impl.BlockingCommand;
import app.diol.dialer.commandline.impl.CallCommand;
import app.diol.dialer.commandline.impl.Echo;
import app.diol.dialer.commandline.impl.Help;
import app.diol.dialer.commandline.impl.Version;
import app.diol.dialer.function.Supplier;
import app.diol.dialer.inject.DialerVariant;
import app.diol.dialer.inject.InstallIn;
import dagger.Module;
import dagger.Provides;

/**
 * Provides {@link Command}
 */
@InstallIn(variants = {DialerVariant.DIALER_TEST})
@Module
public abstract class CommandLineModule {

    @Provides
    static Supplier<ImmutableMap<String, Command>> provideCommandSupplier(
            AospCommandInjector aospCommandInjector) {

        return aospCommandInjector.inject(CommandSupplier.builder()).build();
    }

    /**
     * Injects standard commands to the builder
     */
    public static class AospCommandInjector {
        private final Help help;
        private final Version version;
        private final Echo echo;
        private final BlockingCommand blockingCommand;
        private final CallCommand callCommand;
        private final ActiveCallsCommand activeCallsCommand;

        @Inject
        AospCommandInjector(
                Help help,
                Version version,
                Echo echo,
                BlockingCommand blockingCommand,
                CallCommand callCommand,
                ActiveCallsCommand activeCallsCommand) {
            this.help = help;
            this.version = version;
            this.echo = echo;
            this.blockingCommand = blockingCommand;
            this.callCommand = callCommand;
            this.activeCallsCommand = activeCallsCommand;
        }

        public CommandSupplier.Builder inject(CommandSupplier.Builder builder) {
            builder.addCommand("help", help);
            builder.addCommand("version", version);
            builder.addCommand("echo", echo);
            builder.addCommand("blocking", blockingCommand);
            builder.addCommand("call", callCommand);
            builder.addCommand("activecalls", activeCallsCommand);
            return builder;
        }
    }
}
