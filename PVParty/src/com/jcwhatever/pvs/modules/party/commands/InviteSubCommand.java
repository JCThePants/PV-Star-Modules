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


package com.jcwhatever.pvs.modules.party.commands;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.pvs.modules.party.Party;
import com.jcwhatever.pvs.modules.party.PartyManager;
import com.jcwhatever.pvs.modules.party.PartyModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@CommandInfo(
		parent="party",
		command="invite",
		staticParams={"playerName"},
		description="Invite a player to your party.",
		permissionDefault=PermissionDefault.TRUE,

		paramDescriptions = {
				"playerName= The name of the player to invite."})

public class InviteSubCommand extends AbstractCommand implements IExecutableCommand {
	
	@Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

		CommandException.checkNotConsole(getPlugin(), this, sender);
		
		Player p = (Player)sender;

        PartyManager manager = PartyModule.getModule().getManager();
		if (!manager.isInParty(p))
			throw new CommandException("You don't have a party to invite anyone to.");

		Party party = manager.getParty(p);
		assert party != null && party.getLeader() != null;
		
		if (!party.getLeader().equals(p))
			throw new CommandException("You can't invite players because you're not the party leader.");

		String playerName = args.getName("playerName");
		
		Player invitee = PlayerUtils.getPlayer(playerName);
		
		if (invitee == null)
			throw new CommandException("Could not find player '{0}'", playerName);

		if (!manager.invitePlayer(invitee, party))
			throw new CommandException("Failed to invite player.");

		if (manager.getInvitedParties(invitee).size() > 1)
			tell(invitee, "You've been invited to {0}. Type '/pv party join {1}' to join.", party.getPartyName(), party.getLeader().getName());
		else
			tell(invitee, "You've been invited to {0}. Type '/pv party join' to join.", party.getPartyName());
				
		tell(invitee, "Invitation expires after {0} seconds.", manager.getInvitationTimeout());
		
		tellSuccess(p, "{0} has been invited to your party.", invitee.getName());
    }
}

