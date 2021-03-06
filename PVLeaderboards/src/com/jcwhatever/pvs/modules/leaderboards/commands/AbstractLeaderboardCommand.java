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


package com.jcwhatever.pvs.modules.leaderboards.commands;

import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.pvs.modules.leaderboards.Lang;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.modules.leaderboards.LeaderboardsModule;
import com.jcwhatever.pvs.modules.leaderboards.leaderboards.Leaderboard;
import org.bukkit.command.CommandSender;

public class AbstractLeaderboardCommand extends AbstractPVCommand {

    @Localizable static final String _INVALID_LINE_NUMBER =
            "Invalid argument. <lineNumber> must be a number between 1 and 4.";

    @Localizable static final String _NOT_FOUND =
            "A leaderboard named '{0: leaderboard name}' was not found.";

    @Localizable static final String _ALREADY_EXISTS =
            "A leaderboard named '{0: leaderboard}' already exists.";

    protected Leaderboard getLeaderboard(CommandSender sender, String leaderboardName) throws CommandException {

        Leaderboard leaderboard = LeaderboardsModule.getModule().getLeaderboard(leaderboardName);
        if (leaderboard == null)
            throw new CommandException(Lang.get(_NOT_FOUND, leaderboardName));

        return leaderboard;
    }
}
