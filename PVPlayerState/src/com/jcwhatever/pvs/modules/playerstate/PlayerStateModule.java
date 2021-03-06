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


package com.jcwhatever.pvs.modules.playerstate;

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.utils.player.PlayerState;
import com.jcwhatever.nucleus.utils.player.PlayerState.RestoreLocation;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IBukkitPlayer;
import com.jcwhatever.pvs.api.events.players.PlayerLeaveArenaEvent;
import com.jcwhatever.pvs.api.modules.PVStarModule;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

public class PlayerStateModule extends PVStarModule implements IEventListener {

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    protected void onRegisterTypes() {

        PVStarAPI.getExtensionManager().registerType(PlayerStateExtension.class);
    }

    @Override
    protected void onEnable() {

        PVStarAPI.getEventManager().register(this);
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), PVStarAPI.getPlugin());
    }

    @EventMethod
    private void onPlayerLeave(PlayerLeaveArenaEvent event) {

        if (!(event.getPlayer() instanceof IBukkitPlayer))
            return;

        if (!event.isRestoring() || event.getPlayer().isDead())
            return;

        PlayerState state = PlayerState.get(PVStarAPI.getPlugin(),
                ((IBukkitPlayer) event.getPlayer()).getPlayer());
        if (state == null || !state.isSaved())
            return;

        Location restoreLocation;

        try {

            restoreLocation = state.restore(RestoreLocation.FALSE);
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        if (!event.isRestoring()) {
            event.setRestoreLocation(restoreLocation);
        }
    }
}
