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


package com.jcwhatever.bukkit.pvs.modules.showspawns;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.events.manager.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.manager.IGenericsEventListener;
import com.jcwhatever.bukkit.generic.utils.ItemStackUtils;
import com.jcwhatever.bukkit.generic.utils.SignUtils;
import com.jcwhatever.bukkit.generic.utils.LocationUtils;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreAddEvent;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.modules.showspawns.commands.HideCommand;
import com.jcwhatever.bukkit.pvs.modules.showspawns.commands.ShowCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShowSpawnsModule extends PVStarModule implements IGenericsEventListener {

    private static ShowSpawnsModule _module;

    public static ShowSpawnsModule getModule() {
        return _module;
    }

    private Map<Arena, LinkedList<BlockState>> _blockStates = new HashMap<>(20);

    public ShowSpawnsModule() {
        _module = this;
    }

    @Override
    protected void onRegisterTypes() {
        // do nothing
    }

    @Override
    protected void onEnable() {

        PVStarAPI.getEventManager().register(this);

        AbstractCommand spawnsCommand = PVStarAPI.getCommandHandler().getCommand("spawns");
        if (spawnsCommand != null) {
            spawnsCommand.registerSubCommand(ShowCommand.class);
            spawnsCommand.registerSubCommand(HideCommand.class);
        }
    }

    public void showSigns(Arena arena) {

        hideSigns(arena);

        List<Spawnpoint> spawnpoints = arena.getSpawnManager().getSpawns();
        LinkedList<BlockState> states = new LinkedList<>();

        for (Spawnpoint spawn : spawnpoints) {

            states.addLast(spawn.getBlock().getState());

            MaterialData materialData = new MaterialData(Material.BEDROCK);
            ItemStackUtils.setBlock(spawn.getBlock(), materialData);

            Block above = spawn.getBlock().getRelative(0, 1, 0);

            states.addFirst(above.getState());

            ItemStack signStack = new ItemStack(Material.SIGN_POST);

            ItemStackUtils.setBlock(above, signStack);

            above = spawn.getBlock().getRelative(0, 1, 0);

            Sign sign = SignUtils.getSign(above);
            if (sign == null)
                continue;

            sign.setLine(1, spawn.getName());
            sign.setLine(2, spawn.getSpawnType().getName());

            BlockFace facing = LocationUtils.getBlockFacingYaw(spawn);

            org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.SIGN_POST);
            matSign.setFacingDirection(facing);

            sign.setData(matSign);
            sign.update();
        }

        if (!states.isEmpty())
            _blockStates.put(arena, states);
    }

    public void hideSigns(Arena arena) {
        LinkedList<BlockState> signList = _blockStates.remove(arena);
        if (signList == null)
            return;

        while (!signList.isEmpty()) {
            BlockState state = signList.remove();
            state.update(true);
        }
    }

    @GenericsEventHandler
    private void onArenaPlayerAdd(PlayerPreAddEvent event) {
        hideSigns(event.getArena());
    }


}
