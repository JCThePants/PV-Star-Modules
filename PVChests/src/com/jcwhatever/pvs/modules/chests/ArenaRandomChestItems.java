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


package com.jcwhatever.pvs.modules.chests;

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.mixins.IArenaOwned;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Stores {@link ItemStack}'s that can be randomly placed in
 * chests in an arena.
 */
public class ArenaRandomChestItems implements IArenaOwned {

    private final IArena _arena;
    private final IDataNode _dataNode;
    private final IDataNode _itemsNode;
    private WeightedItems _chestItems;
    private boolean _isPresetContentsRandomized = false; // randomize contents of chests with preset contents
    private int _maxRandomItems = 4;

    /**
     * Constructor.
     *
     * @param arena     The owning arena.
     * @param dataNode  The data node where items are stored.
     */
    public ArenaRandomChestItems(IArena arena, IDataNode dataNode) {
        _arena = arena;
        _dataNode = dataNode;
        _itemsNode = dataNode.getNode("chest-items");

        load();
    }

    @Override
    public IArena getArena() {
        return _arena;
    }

    /**
     * Get the total number of available items.
     */
    public int getTotalItems() {
        return _chestItems.size();
    }

    /**
     * Get the maximum number of items that can be randomly placed into
     * a chest.
     */
    public int getMaxRandomItems() {
        return _maxRandomItems;
    }

    /**
     * Set the maximum number of items that can be randomly placed into
     * a chest.
     *
     * @param max  The max number of items.
     */
    public void setMaxRandomItems(int max) {
        PreCon.positiveNumber(max);

        _maxRandomItems = max;
        _dataNode.set("max-random-items", max);
        _dataNode.save();
    }

    /**
     * Determine if chest that have preset contents should have the contents
     * randomized.
     *
     * <p>Preset contents are contents that have been set specific to the chest.</p>
     */
    public boolean isPresetContentsRandomized() {
        return _isPresetContentsRandomized;
    }

    /**
     * Change the setting that determines if chests that have preset contents
     * should have the contents randomized.
     *
     * @param isRandomContents  True to randomize preset contents, false to leave contents as is.
     */
    public void setPresetContentsRandomized(boolean isRandomContents) {
        _isPresetContentsRandomized = isRandomContents;
        _dataNode.set("random-contents", isRandomContents);
        _dataNode.save();
    }

    /**
     * Get the collection of items.
     */
    public WeightedItems getItems() {
        return _chestItems;
    }

    /**
     * Clear all saved items.
     */
    public void clearItems() {
        _chestItems.clear();
        _itemsNode.remove("items");
        _itemsNode.save();
    }

    /**
     * Add default items.
     */
    public void addDefaultItems() {

        new DefaultItemHelper()
                .add(Material.APPLE)       		    .add(Material.ARROW)
                .add(Material.BAKED_POTATO)		    .add(Material.BLAZE_POWDER)
                .add(Material.BOAT)        		    .add(Material.BONE)
                .add(Material.BOWL)        		    .add(Material.BREAD)
                .add(Material.BROWN_MUSHROOM)		.add(Material.CAKE)
                .add(Material.CARROT)      		    .add(Material.CHAINMAIL_BOOTS)
                .add(Material.CHAINMAIL_CHESTPLATE) .add(Material.CHAINMAIL_HELMET)
                .add(Material.CHAINMAIL_LEGGINGS)	.add(Material.COAL)
                .add(Material.COOKED_BEEF)			.add(Material.COOKED_CHICKEN)
                .add(Material.COOKED_FISH)			.add(Material.COOKIE)
                .add(Material.DIAMOND)				.add(Material.DIAMOND_AXE)
                .add(Material.DIAMOND_BOOTS)		.add(Material.DIAMOND_CHESTPLATE)
                .add(Material.DIAMOND_HELMET)		.add(Material.DIAMOND_LEGGINGS)
                .add(Material.DIAMOND_SWORD)		.add(Material.EGG)
                .add(Material.ENDER_PEARL)			.add(Material.FEATHER)
                .add(Material.ARROW)				.add(Material.FLINT)
                .add(Material.FLINT_AND_STEEL)		.add(Material.GOLD_AXE)
                .add(Material.GOLD_BOOTS)			.add(Material.GOLD_CHESTPLATE)
                .add(Material.GOLD_HELMET)			.add(Material.GOLD_INGOT)
                .add(Material.GOLD_LEGGINGS)		.add(Material.GOLD_SWORD)
                .add(Material.IRON_AXE)			    .add(Material.IRON_BOOTS)
                .add(Material.IRON_CHESTPLATE)		.add(Material.IRON_HELMET)
                .add(Material.IRON_INGOT)			.add(Material.IRON_LEGGINGS)
                .add(Material.LEATHER_BOOTS)		.add(Material.LEATHER_CHESTPLATE)
                .add(Material.LEATHER_HELMET)		.add(Material.LEATHER_LEGGINGS)
                .add(Material.MELON)				.add(Material.ARROW)
                .add(Material.NETHER_WARTS)		    .add(Material.ARROW)
                .add(Material.POISONOUS_POTATO)	    .add(Material.PORK)
                .add(Material.POTATO_ITEM)			.add(Material.PUMPKIN_PIE)
                .add(Material.RAW_BEEF)			    .add(Material.RAW_CHICKEN)
                .add(Material.RAW_FISH)			    .add(Material.ROTTEN_FLESH)
                .add(Material.SNOW_BALL)			.add(Material.STICK)
                .add(Material.STONE_AXE)			.add(Material.STONE_SWORD)
                .add(Material.STRING)				.add(Material.STICK)
                .add(Material.WOOD_AXE)			    .add(Material.WOOD_SWORD)
        ;

        save();
    }

    /*
     * Load items and settings from the data node.
     */
    private void load() {

        _isPresetContentsRandomized = _dataNode.getBoolean("is-random-contents", _isPresetContentsRandomized);
        _maxRandomItems = _dataNode.getInteger("max-random-items", _maxRandomItems);

        ItemStack[] items = _itemsNode.getItemStacks("items");
        _chestItems = items == null
                ? new WeightedItems()
                : new WeightedItems(items);
    }

    /*
     * Save items to the data node.
     */
    private void save() {
        ItemStack[] items = _chestItems.toArray(new ItemStack[_chestItems.size()]);
        _itemsNode.set("items", items);
        _itemsNode.save();
    }

    private class DefaultItemHelper {
        public DefaultItemHelper add(Material material) {
            _chestItems.add(new ItemStack(material));
            return this;
        }
    }
}
