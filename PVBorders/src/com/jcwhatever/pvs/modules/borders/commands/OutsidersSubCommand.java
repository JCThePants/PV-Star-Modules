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


package com.jcwhatever.pvs.modules.borders.commands;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.mixins.IVisibleCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.modules.borders.BordersExtension;
import com.jcwhatever.pvs.modules.borders.Lang;
import com.jcwhatever.pvs.modules.borders.OutsidersAction;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="arena",
        command="outsiders",
        staticParams = {"none|join|kick|prevent"},
        description="Set or view the action taken when a non-arena player " +
                "enters the selected arenas region. [PVBorders]")

public class OutsidersSubCommand extends AbstractPVCommand
        implements IExecutableCommand, IVisibleCommand {

    @Localizable static final String _EXTENSION_NOT_FOUND =
            "PVBorders extension is not installed in arena '{0: arena name}'.";

    @Localizable static final String _VIEW_OUTSIDERS =
            "Action taken when outsiders enter region for arena '{0: arena name}' is {1: action}.";

    @Localizable static final String _SET_OUTSIDERS =
            "Action taken when outsiders enter region for arena '{0: arena name}' changed to {1: action}.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "none|join|kick|prevent"));
        if (arena == null)
            return; // finished

        BordersExtension extension = arena.getExtensions().get(BordersExtension.class);
        if (extension == null)
            throw new CommandException(Lang.get(_EXTENSION_NOT_FOUND, arena.getName()));

        if (args.isDefaultValue("none|join|kick|prevent")) {
            tell(sender, Lang.get(_VIEW_OUTSIDERS, arena.getName(), extension.getOutsidersAction().name()));
        }
        else {
            OutsidersAction action = args.getEnum("none|join|kick|prevent", OutsidersAction.class);
            extension.setOutsidersAction(action);

            tellSuccess(sender, Lang.get(_SET_OUTSIDERS, arena.getName(), action.name()));
        }
    }

    @Override
    public boolean isVisible(CommandSender sender) {

        IArena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        return arena != null && arena.getExtensions().has(BordersExtension.class);
    }
}
