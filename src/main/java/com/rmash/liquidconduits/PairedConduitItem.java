package com.rmash.liquidconduits;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PairedConduitItem extends BlockItem {
    public PairedConduitItem(Block block, Properties properties) {
        super(block, properties);
    }

    // TODO: Implement pairing system via NBT components (1.21+ uses DataComponents instead of getTag/setTag)
    // For now, pairing will need to be done manually via a special item or command
}
