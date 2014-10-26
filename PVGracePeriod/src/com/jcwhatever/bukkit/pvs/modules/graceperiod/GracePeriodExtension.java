/* This file is part of PV-Star Modules: PVGracePeriod for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.modules.graceperiod;

import com.jcwhatever.bukkit.generic.events.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventListener;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.bukkit.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerDamagedEvent;
import com.jcwhatever.bukkit.pvs.api.utils.ArenaScheduler;

@ArenaExtensionInfo(
        name="PVGracePeriod",
        description = "Add grace period to the beginning of an arena which prevents PVP.")
public class GracePeriodExtension extends ArenaExtension implements GenericsEventListener {

    private int _gracePeriodSeconds = 10;

    public boolean _isGracePeriod = false;

    /**
     * Get grace period time in seconds.
     */
    public int getGracePeriodSeconds() {
        return _gracePeriodSeconds;
    }

    /**
     * Set grace period time.
     */
    public void setGracePeriodSeconds(int seconds) {
        _gracePeriodSeconds = seconds;

        getDataNode().set("seconds", seconds);
        getDataNode().saveAsync(null);
    }


    @Override
    protected void onEnable() {

        _gracePeriodSeconds = getDataNode().getInteger("seconds", _gracePeriodSeconds);

        getArena().getEventManager().register(this);
    }

    @Override
    protected void onDisable() {

        getArena().getEventManager().unregister(this);
    }

    @GenericsEventHandler
    private void onArenaStart(ArenaStartedEvent event) {

        GameManager gameManager = getArena().getGameManager();

        if (gameManager.getSettings().isPvpEnabled() ||
            gameManager.getSettings().isTeamPvpEnabled()) {

            gameManager.tell("Pvp grace period for the next {0} seconds.", _gracePeriodSeconds);

            ArenaScheduler.runTaskLater(getArena(), 20 * _gracePeriodSeconds,
                    new GracePeriod());
        }
    }

    @GenericsEventHandler(priority = GenericsEventPriority.FIRST)
    private void onPvp(PlayerDamagedEvent event) {

        if (!_isGracePeriod)
            return;

        if (event.getDamagerPlayer() == null)
            return;

        if (event.getPlayer().getArenaRelation() != ArenaPlayerRelation.GAME)
            return;

        event.setCancelled(true);
    }

    private class GracePeriod implements Runnable {

        @Override
        public void run() {
            _isGracePeriod = false;
            getArena().getGameManager().tell("Pvp grace period ended.");
        }
    }
}
