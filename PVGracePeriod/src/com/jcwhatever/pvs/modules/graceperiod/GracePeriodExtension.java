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


package com.jcwhatever.pvs.modules.graceperiod;

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.observer.event.EventSubscriberPriority;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.pvs.api.arena.context.IGameContext;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.pvs.api.utils.ArenaScheduler;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

@ArenaExtensionInfo(
        name="PVGracePeriod",
        description = "Add grace period to the beginning of an arena which prevents PVP.")
public class GracePeriodExtension extends ArenaExtension implements IEventListener {

    @Localizable static final String _GRACE_STARTED =
            "{GOLD}Pvp grace period for the next {0: amount} seconds.";

    @Localizable static final String _GRACE_ENDED =
            "{GOLD}Pvp grace period ended.";

    private int _gracePeriodSeconds = 10;

    public boolean _isGracePeriod = false;

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

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
        getDataNode().save();
    }

    @Override
    protected void onAttach() {
        _gracePeriodSeconds = getDataNode().getInteger("seconds", _gracePeriodSeconds);
    }

    @Override
    protected void onEnable() {
        getArena().getEventManager().register(this);
    }

    @Override
    protected void onDisable() {
        getArena().getEventManager().unregister(this);
    }

    @EventMethod
    private void onArenaStart(@SuppressWarnings("unused") ArenaStartedEvent event) {

        IGameContext gameManager = getArena().getGame();

        if (gameManager.getSettings().isPvpEnabled() ||
            gameManager.getSettings().isTeamPvpEnabled()) {

            gameManager.tell(Lang.get(_GRACE_STARTED, _gracePeriodSeconds));

            _isGracePeriod = true;

            ArenaScheduler.runTaskLater(getArena(), 20 * _gracePeriodSeconds,
                    new GracePeriod());
        }
    }

    @EventMethod(priority = EventSubscriberPriority.FIRST)
    private void onPvp(EntityDamageByEntityEvent event) {

        if (!_isGracePeriod)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getDamager() instanceof Player))
            return;

        IArenaPlayer player = PVStarAPI.getArenaPlayer(event.getEntity());

        if (player.getContext() != ArenaContext.GAME)
            return;

        event.setCancelled(true);
    }

    private class GracePeriod implements Runnable {

        @Override
        public void run() {
            _isGracePeriod = false;
            getArena().getGame().tell(Lang.get(_GRACE_ENDED));
        }
    }
}
