/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent.reorder;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.operation.SetLocatedBlocks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Re-orders blocks into several stages.
 */
public class MultiStageReorder extends AbstractDelegateExtent implements ReorderingExtent {

    private static Map<BlockType, PlacementPriority> priorityMap = new HashMap<>();

    static {
        // Late
        priorityMap.put(BlockTypes.WATER, PlacementPriority.LATE);
        priorityMap.put(BlockTypes.LAVA, PlacementPriority.LATE);
        priorityMap.put(BlockTypes.SAND, PlacementPriority.LATE);
        priorityMap.put(BlockTypes.GRAVEL, PlacementPriority.LATE);

        // Late
        BlockCategories.SAPLINGS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.FLOWER_POTS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.BUTTONS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.ANVIL.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.WOODEN_PRESSURE_PLATES.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.CARPETS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        BlockCategories.RAILS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.LAST));
        priorityMap.put(BlockTypes.BLACK_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.BLUE_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.BROWN_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.CYAN_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.GRAY_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.GREEN_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LIGHT_BLUE_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LIGHT_GRAY_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LIME_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.MAGENTA_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.ORANGE_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.PINK_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.PURPLE_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.RED_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.WHITE_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.YELLOW_BED, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.GRASS, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.TALL_GRASS, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.ROSE_BUSH, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.DANDELION, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.BROWN_MUSHROOM, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.RED_MUSHROOM, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.FERN, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LARGE_FERN, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.OXEYE_DAISY, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.AZURE_BLUET, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.TORCH, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.WALL_TORCH, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.FIRE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.REDSTONE_WIRE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.CARROTS, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.POTATOES, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.WHEAT, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.BEETROOTS, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.COCOA, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LADDER, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LEVER, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.REDSTONE_TORCH, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.REDSTONE_WALL_TORCH, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.SNOW, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.NETHER_PORTAL, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.END_PORTAL, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.REPEATER, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.VINE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LILY_PAD, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.NETHER_WART, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.PISTON, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.STICKY_PISTON, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.TRIPWIRE_HOOK, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.TRIPWIRE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.STONE_PRESSURE_PLATE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.COMPARATOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.IRON_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.ACACIA_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.BIRCH_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.DARK_OAK_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.JUNGLE_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.OAK_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.SPRUCE_TRAPDOOR, PlacementPriority.LAST);
        priorityMap.put(BlockTypes.DAYLIGHT_DETECTOR, PlacementPriority.LAST);
        
        // Final
        BlockCategories.DOORS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.FINAL));
        BlockCategories.BANNERS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.FINAL));
        priorityMap.put(BlockTypes.SIGN, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.WALL_SIGN, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.CACTUS, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.SUGAR_CANE, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.CAKE, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.PISTON_HEAD, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.MOVING_PISTON, PlacementPriority.FINAL);
    }

    private Map<PlacementPriority, LocatedBlockList> stages = new HashMap<>();

    private boolean enabled;

    public enum PlacementPriority {
        CLEAR_FINAL,
        CLEAR_LAST,
        CLEAR_LATE,
        FIRST,
        LATE,
        LAST,
        FINAL
    }

    /**
     * Create a new instance when the re-ordering is enabled.
     *
     * @param extent the extent
     */
    public MultiStageReorder(Extent extent) {
        this(extent, true);
    }

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param enabled true to enable
     */
    public MultiStageReorder(Extent extent, boolean enabled) {
        super(extent);
        this.enabled = enabled;

        for (PlacementPriority priority : PlacementPriority.values()) {
            stages.put(priority, new LocatedBlockList());
        }
    }

    /**
     * Return whether re-ordering is enabled.
     *
     * @return true if re-ordering is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether re-ordering is enabled.
     *
     * @param enabled true if re-ordering is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean commitRequired() {
        return enabled && stages.values().stream().anyMatch(stage -> stage.size() > 0);
    }

    /**
     * Gets the stage priority of the block.
     *
     * @param block The block
     * @return The priority
     */
    private PlacementPriority getPlacementPriority(BlockStateHolder block) {
        return priorityMap.getOrDefault(block.getBlockType(), PlacementPriority.FIRST);
    }

    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException {
        if (!enabled) {
            return super.setBlock(location, block);
        }

        BlockState existing = getBlock(location);
        PlacementPriority priority = getPlacementPriority(block);
        PlacementPriority srcPriority = getPlacementPriority(existing);

        if (srcPriority != PlacementPriority.FIRST) {
            BlockStateHolder replacement = block.getBlockType().getMaterial().isAir() ? block : BlockTypes.AIR.getDefaultState();

            switch (srcPriority) {
                case FINAL:
                    stages.get(PlacementPriority.CLEAR_FINAL).add(location, replacement);
                    break;
                case LATE:
                    stages.get(PlacementPriority.CLEAR_LATE).add(location, replacement);
                    break;
                case LAST:
                    stages.get(PlacementPriority.CLEAR_LAST).add(location, replacement);
                    break;
            }

            if (block.getBlockType().getMaterial().isAir()) {
                return !existing.equalsFuzzy(block);
            }
        }

        stages.get(priority).add(location, block);
        return !existing.equalsFuzzy(block);
    }

    @Override
    public Operation commitBefore() {
        if (!enabled) {
            return null;
        }
        List<Operation> operations = new ArrayList<>();
        for (PlacementPriority priority : PlacementPriority.values()) {
            if (priority != PlacementPriority.FINAL) {
                operations.add(new SetLocatedBlocks(getExtent(), stages.get(priority)));
            }
        }

        operations.add(new FinalStageCommitter());
        return new OperationQueue(operations);
    }

    private class FinalStageCommitter implements Operation {
        private Extent extent = getExtent();

        private final Set<BlockVector3> blocks = new HashSet<>();
        private final Map<BlockVector3, BlockStateHolder> blockTypes = new HashMap<>();

        public FinalStageCommitter() {
            for (LocatedBlock entry : stages.get(PlacementPriority.FINAL)) {
                final BlockVector3 pt = entry.getLocation();
                blocks.add(pt);
                blockTypes.put(pt, entry.getBlock());
            }
        }

        @Override
        public Operation resume(RunContext run) throws WorldEditException {
            while (!blocks.isEmpty()) {
                BlockVector3 current = blocks.iterator().next();

                final Deque<BlockVector3> walked = new LinkedList<>();

                while (true) {
                    walked.addFirst(current);

                    assert (blockTypes.containsKey(current));

                    final BlockStateHolder blockStateHolder = blockTypes.get(current);

                    if (BlockCategories.DOORS.contains(blockStateHolder.getBlockType())) {
                        Property<Object> halfProperty = blockStateHolder.getBlockType().getProperty("half");
                        if (blockStateHolder.getState(halfProperty).equals("lower")) {
                            // Deal with lower door halves being attached to the floor AND the upper half
                            BlockVector3 upperBlock = current.add(0, 1, 0);
                            if (blocks.contains(upperBlock) && !walked.contains(upperBlock)) {
                                walked.addFirst(upperBlock);
                            }
                        }
                    } else if (BlockCategories.RAILS.contains(blockStateHolder.getBlockType())) {
                        BlockVector3 lowerBlock = current.add(0, -1, 0);
                        if (blocks.contains(lowerBlock) && !walked.contains(lowerBlock)) {
                            walked.addFirst(lowerBlock);
                        }
                    }

                    if (!blockStateHolder.getBlockType().getMaterial().isFragileWhenPushed()) {
                        // Block is not attached to anything => we can place it
                        break;
                    }

//                    current = current.add(attachment.vector()).toBlockVector();
//
//                    if (!blocks.contains(current)) {
//                        // We ran outside the remaining set => assume we can place blocks on this
//                        break;
//                    }
//
                    if (walked.contains(current)) {
                        // Cycle detected => This will most likely go wrong, but there's nothing we can do about it.
                        break;
                    }
                }

                for (BlockVector3 pt : walked) {
                    extent.setBlock(pt, blockTypes.get(pt));
                    blocks.remove(pt);
                }
            }

            for (LocatedBlockList stage : stages.values()) {
                stage.clear();
            }
            return null;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addStatusMessages(List<String> messages) {
        }

    }

}
