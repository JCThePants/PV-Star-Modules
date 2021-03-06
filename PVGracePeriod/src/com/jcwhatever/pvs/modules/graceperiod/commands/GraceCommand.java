/*
 * This file is part of PV-StarModules for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.pvs.modules.graceperiod.commands;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.modules.graceperiod.GracePeriodExtension;
import com.jcwhatever.pvs.modules.graceperiod.Lang;

import org.bukkit.command.CommandSender;

@CommandInfo(
        command={ "grace" },
        staticParams={"seconds=info"},
        description="Set or view the pvp grace period time in seconds for the " +
                "selected arena. [PVGracePeriod]",

        paramDescriptions = {
                "seconds= The number of seconds the grace period lasts. " +
                        "Leave blank to see current setting."})

public class GraceCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _GRACE_SECONDS_INFO =
            "Grace period seconds in arena '{0: arena name}' is set to {1: amount}.";

    @Localizable static final String _GRACE_SECONDS_SET =
            "Grace period seconds in arena '{0: arena name}' changed to {1: amount}.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "seconds"));
        if (arena == null)
            return; // finished

        GracePeriodExtension extension = getExtension(sender, arena, GracePeriodExtension.class);
        if (extension == null)
            return; // finished

        if (args.getString("seconds").equals("info")) {

            int seconds = extension.getGracePeriodSeconds();
            tell(sender, Lang.get(_GRACE_SECONDS_INFO, arena.getName(), seconds));
        } else {

            int seconds = args.getInteger("seconds");

            extension.setGracePeriodSeconds(seconds);

            tellSuccess(sender, Lang.get(_GRACE_SECONDS_SET, arena.getName(), seconds));
        }
    }
}
