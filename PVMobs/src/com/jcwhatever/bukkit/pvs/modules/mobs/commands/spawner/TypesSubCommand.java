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


package com.jcwhatever.bukkit.pvs.modules.mobs.commands.spawner;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.modules.mobs.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.modules.mobs.spawners.ISpawner;
import com.jcwhatever.bukkit.pvs.modules.mobs.spawners.SpawnerInfo;
import com.jcwhatever.bukkit.pvs.modules.mobs.spawners.SpawnerManager;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="spawner",
        command="types",
        staticParams = { "page=1" },
        usage="/{plugin-command} spawner types [page]",
        description="List available mob spawners.")

public class TypesSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Available Mob Spawners";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidCommandSenderException, InvalidValueException {

        int page = args.getInteger("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<Class<? extends ISpawner>> spawnerTypes = SpawnerManager.getSpawnerClasses();
        for (Class<? extends ISpawner> type : spawnerTypes) {
            SpawnerInfo info = type.getAnnotation(SpawnerInfo.class);
            if (info == null)
                continue;

            pagin.add(info.name(), info.description());
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}

