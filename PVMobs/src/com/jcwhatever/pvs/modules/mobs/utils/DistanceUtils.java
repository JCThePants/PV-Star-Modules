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


package com.jcwhatever.pvs.modules.mobs.utils;

import com.jcwhatever.nucleus.managed.astar.AStar;
import com.jcwhatever.nucleus.managed.astar.IAStarSettings;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.ThreadSingletons;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.modules.mobs.MobArenaExtension;
import com.jcwhatever.pvs.modules.mobs.paths.PathCache;
import com.jcwhatever.pvs.modules.mobs.paths.PathCacheEntry;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DistanceUtils {

    private DistanceUtils() {}

    public static final int MAX_PATH_DISTANCE = 15;
    public static final int SEARCH_RADIUS = 18;
    public static final byte MAX_DROP_HEIGHT = 6;
    public static final int MAX_ITERATIONS = 5000;

    private static ThreadSingletons<Location> PLAYER_LOCATIONS = LocationUtils.createThreadSingleton();
    private static ThreadSingletons<Location> BLOCK_LOCATIONS = LocationUtils.createThreadSingleton();

    /**
     * Determine if the specified destination is valid. Uses cached paths if available,
     * otherwise uses AStar path finding.
     *
     * @param destination      The destination location.
     * @param searchRadius     The max radius of valid destinations.
     * @param maxPathDistance  The max path distance to a destination.
     * @return
     */
    public static boolean isValidMobDestination(IArena arena, Spawnpoint source,
                                                Location destination, int searchRadius, int maxPathDistance) {
        PreCon.notNull(destination);

        // must be in same world
        if (!source.getWorld().equals(destination.getWorld()))
            return false;

        // must be within a certain radius
        if (source.distanceSquared(destination) > searchRadius * searchRadius)
            return false;

        // check for cached paths first
        ArenaExtension manager = arena.getExtensions().get(MobArenaExtension.NAME);
        if (manager instanceof MobArenaExtension) {

            PathCache pathCache = ((MobArenaExtension) manager).getGroupGenerator().getPathCache();

            PathCacheEntry entry = pathCache.getEntry(source);

            if (entry != null && entry.hasPathCache()) {

                // return cached result
                return entry.isValidDestination(destination);
            }
        }

        // Use real time path checking (slower)
        IAStarSettings settings = AStar.createSettings()
                .setRange(searchRadius)
                .setMaxDropHeight(MAX_DROP_HEIGHT)
                .setMaxIterations(MAX_ITERATIONS);

        int distance = AStar.search(source, destination, settings)
                .getPathDistance();

        return distance > -1 && distance <= maxPathDistance;
    }


    public static <T extends Spawnpoint> ArrayList<T> getClosestSpawns(
            IArena arena, Collection<IArenaPlayer> players, Collection<T> spawnpoints, int maxPathDistance) {

        PreCon.notNull(arena);
        PreCon.notNull(players);
        PreCon.notNull(spawnpoints);
        PreCon.greaterThanZero(maxPathDistance);

        ArrayList<T> result = new ArrayList<>(spawnpoints.size());
        if (spawnpoints.isEmpty())
            return result;

        List<T> spawns = new ArrayList<>(spawnpoints);

        Location playerLocation = PLAYER_LOCATIONS.get();
        Location blockLocation = BLOCK_LOCATIONS.get();

        for (IArenaPlayer player : players) {

            if (spawns.isEmpty())
                break;

            Location location  = LocationUtils.getBlockLocation(player.getLocation(playerLocation), blockLocation);

            Iterator<T> iterator = spawns.iterator();
            while (iterator.hasNext()) {
                T spawn = iterator.next();

                if (isValidMobDestination(arena, spawn, location, SEARCH_RADIUS, maxPathDistance)) {
                    result.add(spawn);
                    iterator.remove();
                }
            }
        }

        return result;
    }

    public static IArenaPlayer getClosestPlayer(
            Collection<IArenaPlayer> players, Location loc, int maxDistanceSquared) {

        PreCon.notNull(players);
        PreCon.notNull(loc);

        double current = maxDistanceSquared;
        IArenaPlayer result = null;

        for (IArenaPlayer player : players) {

            if (player.isImmobilized())
                continue;

            Location pLoc = player.getLocation(PLAYER_LOCATIONS.get());

            if (!pLoc.getWorld().equals(loc.getWorld()))
                continue;

            double dist = pLoc.distanceSquared(loc);
            if (dist >= current || dist >= maxDistanceSquared)
                continue;

            current = dist;
            result = player;
        }

        return result;
    }
}
