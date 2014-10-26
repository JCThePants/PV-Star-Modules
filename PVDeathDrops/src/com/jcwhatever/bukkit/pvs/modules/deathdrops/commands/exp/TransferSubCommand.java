/* This file is part of PV-Star Modules: PVDeathDrops for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.modules.deathdrops.commands.exp;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.modules.deathdrops.DeathDropsExtension;
import com.jcwhatever.bukkit.pvs.modules.deathdrops.DropSettings;
import com.jcwhatever.bukkit.pvs.modules.deathdrops.commands.AbstractDropsCommand;
import com.jcwhatever.bukkit.pvs.modules.deathdrops.commands.TransferType;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="exp",
        command="transfer",
        staticParams = { "specificity", "transferType=info"},
        usage="/{plugin-command} {command} exp transfer <specificity> [transferType|clear]",
        description="View or set how dropped exp is transferred to a player in the selected arena at the specified specificity.")

public class TransferSubCommand extends AbstractDropsCommand {

    @Localizable static final String _INFO_DIRECT = "Exp is transferred directly to the player in arena '{0}'.";
    @Localizable static final String _INFO_DROP = "Exp is dropped in arena '{0}'.";
    @Localizable static final String _SET_DIRECT = "Exp transfer type changed to DIRECT in arena '{0}'.";
    @Localizable static final String _SET_DROP = "Exp transfer type changed to DROP in arena '{0}'.";
    @Localizable static final String _CLEAR = "Value cleared for specificity '{0}' in arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "transferType"));
        if (arena == null)
            return; // finished

        DeathDropsExtension extension = getExtension(sender, arena, DeathDropsExtension.class);
        if (extension == null)
            return; // finished

        String specificity = args.getString("specificity");

        DropSettings settings = getDropSettings(specificity, extension);

        if (args.getString("transferType").equals("info")) {

            boolean isDirect = settings.isDirectExpTransfer();

            if (isDirect)
                tell(sender, Lang.get(_INFO_DIRECT, arena.getName()));
            else
                tell(sender, Lang.get(_INFO_DROP, arena.getName()));
        }
        else if (args.getString("transferType").equalsIgnoreCase("clear")) {

            settings.clearDirectExpTransfer();
            tellSuccess(sender, Lang.get(_CLEAR, specificity, arena.getName()));
        }
        else {

            TransferType type = args.getEnum("transferType", TransferType.class);

            settings.setDirectExpTransfer(type == TransferType.DIRECT);

            if (type == TransferType.DIRECT)
                tellSuccess(sender, Lang.get(_SET_DIRECT, arena.getName()));
            else
                tellSuccess(sender, Lang.get(_SET_DROP, arena.getName()));
        }
    }
}
