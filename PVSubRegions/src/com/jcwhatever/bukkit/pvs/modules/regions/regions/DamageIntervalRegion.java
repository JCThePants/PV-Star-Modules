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


package com.jcwhatever.bukkit.pvs.modules.regions.regions;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.storage.settings.SettingDefinitions;
import com.jcwhatever.bukkit.generic.storage.settings.ValueType;
import com.jcwhatever.bukkit.generic.utils.Scheduler.ScheduledTask;
import com.jcwhatever.bukkit.generic.utils.Scheduler.TaskHandler;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.utils.ArenaScheduler;
import com.jcwhatever.bukkit.pvs.modules.regions.RegionTypeInfo;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RegionTypeInfo(
        name="damageinterval",
        description="Players are damaged at intervals while inside region.")
public class DamageIntervalRegion extends AbstractPVRegion {

    private static SettingDefinitions _possibleSettings = new SettingDefinitions();

    static {
        _possibleSettings
                .set("damage", 1.0D, ValueType.DOUBLE, "The amount of damage inflicted on a player at interval.")
                .set("interval", 1, ValueType.INTEGER, "The interval in seconds that damage is inflicted.")
        ;
    }

    private double _damage = 1.0D;
    private int _interval = 1;
    private Map<UUID, ScheduledTask> _tasks = new HashMap<>(25);

    public DamageIntervalRegion(String name) {
        super(name);
    }

    @Override
    protected void onPlayerEnter(final ArenaPlayer player, EnterRegionReason reason) {

        ScheduledTask task = ArenaScheduler.runTaskRepeat(getArena(), 1, _interval * 20, new TaskHandler() {

            @Override
            public void run() {

                Player p = player.getHandle();

                if (p.isDead() || !p.isOnline()) {
                    cancelTask();
                    return;
                }

                if (_damage < 0) { // give health
                    double health = p.getHealth() + Math.abs(_damage);
                    health = Math.min(p.getMaxHealth(), health);

                    p.setHealth(health);
                } else { // damage
                    p.damage(_damage);
                }
            }

            @Override
            protected void onCancel() {
                _tasks.remove(player.getUniqueId());
            }

        });

        _tasks.put(player.getUniqueId(), task);
    }

    @Override
    protected void onPlayerLeave(ArenaPlayer player, LeaveRegionReason reason) {

        ScheduledTask task = _tasks.remove(player.getUniqueId());

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    protected boolean onTrigger() {
        return false;
    }

    @Override
    protected boolean onUntrigger() {
        return false;
    }

    @Override
    protected void onEnable() {
        setIsPlayerWatcher(true);
    }

    @Override
    protected void onDisable() {
        setIsPlayerWatcher(false);
    }

    @Override
    protected void onLoadSettings(IDataNode dataNode) {
        _damage = dataNode.getDouble("damage", _damage);
        _interval = dataNode.getInteger("interval", _interval);
    }

    @Override
    protected SettingDefinitions getSettingDefinitions() {
        return _possibleSettings;
    }
}
