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

package com.jcwhatever.pvs.modules.mobs.commands.limit;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.entity.EntityTypeProperty;
import com.jcwhatever.nucleus.utils.entity.EntityTypes;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.modules.mobs.Lang;
import com.jcwhatever.pvs.modules.mobs.MobArenaExtension;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.Set;

@CommandInfo(
        parent="limit",
        command="info",
        staticParams = { "page=1" },
        description="Get entity spawn limit info for the selected arena.",

        paramDescriptions = {
                "page= {PAGE}"})

public class InfoSubCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _EXTENSION_NOT_INSTALLED =
            "PVMobs extension is not installed in arena '{0: arena name}'.";

    @Localizable static final String _PAGINATOR_TITLE =
            "Mob Limits in arena '{0: arena name}'";

    @Localizable static final String _LABEL_NONE = "none";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNING);
        if (arena == null)
            return; // finish

        MobArenaExtension extension = arena.getExtensions().get(MobArenaExtension.class);
        if (extension == null)
            throw new CommandException(Lang.get(_EXTENSION_NOT_INSTALLED, arena.getName()));

        int page = args.getInteger("page");

        ChatPaginator pagin = createPagin(args, 7, Lang.get(_PAGINATOR_TITLE, arena.getName()));

        Set<EntityType> mobTypes = EntityTypes.get(EntityTypeProperty.ALIVE);

        String noneLabel = Lang.get(_LABEL_NONE).toString();

        for (EntityType type : mobTypes) {

            int limit = extension.getTypeLimits().get(type);

            pagin.add(type.name(), limit >= 0 ? limit : noneLabel);
        }

        pagin.show(sender, page, FormatTemplate.CONSTANT_DEFINITION);
    }
}

