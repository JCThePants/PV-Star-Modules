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


package com.jcwhatever.pvs.modules.regions.regions;

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.regions.options.EnterRegionReason;
import com.jcwhatever.nucleus.regions.options.LeaveRegionReason;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.storage.settings.PropertyDefinition;
import com.jcwhatever.nucleus.storage.settings.PropertyValueType;
import com.jcwhatever.nucleus.storage.settings.SettingsBuilder;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.events.players.PlayerAddToContextEvent;
import com.jcwhatever.pvs.api.utils.ArenaConverters;
import com.jcwhatever.pvs.api.utils.ArenaScheduler;
import com.jcwhatever.pvs.modules.regions.RegionTypeInfo;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RegionTypeInfo(
        name="forwarding",
        description="Forward players to another arena.")
public class ForwardingRegion extends AbstractPVRegion implements IEventListener {

    private static Map<String, PropertyDefinition> _possibleSettings;

    static {
        _possibleSettings = new SettingsBuilder()
                .set("forward-to-arena", PropertyValueType.UNIQUE_ID,
                        "Set the arena to forward to.")

                .set("do-teleport", PropertyValueType.BOOLEAN, true,
                        "Set flag for teleporting to the arena.")

                .set("teleport-region", PropertyValueType.STRING,
                        "Set destination special region to teleport player to. Overrides arenas " +
                                "teleport if set. Region must be in the arena specified in " +
                                "'forward-to-arena' setting.")

                .set("yaw-adjust", PropertyValueType.DOUBLE, 0.0D,
                        "Used by 'teleport-region' setting. Adjust the players yaw position when teleported.")

                .setConverters("forward-to-arena", ArenaConverters.ARENA_ID, ArenaConverters.ARENA_NAME)

                .build()
        ;
    }

    private IArena _forwardArena;
    private boolean _doTeleport = true;
    private AbstractPVRegion _destinationRegion;
    private float _yawAdjust = 0.0F;

    private Map<UUID, Location> _forwardLocMap = new HashMap<>(10);
    private Map<UUID, Vector> _vectorMap = new HashMap<>(10);

    public ForwardingRegion(String name) {
        super(name);
    }

    @EventMethod
    private void onPlayerAdded(final PlayerAddToContextEvent event) {

        Location location = _forwardLocMap.remove(event.getPlayer().getUniqueId());
        if (location == null)
            return;

        event.setSpawnLocation(location);

        final Vector vector = _vectorMap.remove(event.getPlayer().getUniqueId());
        if (vector == null)
            return;

        ArenaScheduler.runTaskLater(getArena(), 1, new Runnable() {

            @Override
            public void run() {

                Entity entity = event.getPlayer().getEntity();
                if (entity != null)
                    entity.setVelocity(vector);
            }
        });
    }

    @Override
    protected boolean canDoPlayerEnter(Player p, EnterRegionReason reason) {
        return _forwardArena != null && super.canDoPlayerEnter(p, reason);
    }

    @Override
    protected void onPlayerEnter(final IArenaPlayer player, EnterRegionReason reason) {

        boolean doRegionTeleport = _doTeleport &&
                                   _destinationRegion != null &&
                                   _destinationRegion.isDefined();

        if (doRegionTeleport) {
            Location destination = getRegionDestination(player);
            _forwardLocMap.put(player.getUniqueId(), destination);

            Entity entity = player.getEntity();
            assert entity != null;

            Vector vector = entity.getVelocity();
            _vectorMap.put(player.getUniqueId(), vector);
        }

        getArena().getGame().forwardPlayer(player, _forwardArena);
    }

    @Override
    protected boolean canDoPlayerLeave(Player p, LeaveRegionReason reason) {
        return false;
    }

    @Override
    protected void onPlayerLeave(IArenaPlayer player, LeaveRegionReason reason) {
        // do nothing
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
        getArena().getEventManager().register(this);
        setEventListener(true);
    }

    @Override
    protected void onDisable() {
        getArena().getEventManager().unregister(this);
        setEventListener(false);
    }

    @Override
    protected void onLoadSettings(IDataNode dataNode) {

        // get forwarding arena
        UUID arenaId = dataNode.getUUID("forward-to-arena");
        if (arenaId != null)
            _forwardArena = PVStarAPI.getArenaManager().getArena(arenaId);

        // teleport settings
        _doTeleport = dataNode.getBoolean("do-teleport", _doTeleport);

        // player yaw adjustment
        _yawAdjust = (float)dataNode.getDouble("yaw-adjust", _yawAdjust);

        // get destination region
        if (_forwardArena != null) {
            String regionName = dataNode.getString("teleport-region");
            if (regionName != null) {

                // search own region for destination region
                _destinationRegion = getModule().getManager(getArena()).getRegion(regionName);

                // search destination arena for destination region
                if (_destinationRegion == null) {
                    _destinationRegion = getModule().getManager(_forwardArena).getRegion(regionName);
                }
            }
        }
    }

    @Override
    protected Map<String, PropertyDefinition> getDefinitions() {
        return _possibleSettings;
    }

    private Location getRegionDestination(IArenaPlayer p) {
        Location pLoc = p.getLocation();

        double x = pLoc.getX() - getXStart();
        double y = pLoc.getY() - getYStart();
        double z = pLoc.getZ() - getZStart();

        double dx = _destinationRegion.getXStart() + x;
        double dy = _destinationRegion.getYStart() + y;
        double dz = _destinationRegion.getZStart() + z;

        float dyaw = (pLoc.getYaw() + _yawAdjust) % 360;

        return new Location(getWorld(), dx, dy, dz, dyaw, pLoc.getPitch());
    }
}
