package com.rmash.liquidconduits;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LiquidConduitBlockEntity extends BlockEntity {
    // Fluid storage - holds 10 buckets worth of fluid
    private static final long CAPACITY = FluidConstants.BUCKET * 10;

    // Paired conduit position (null if unpaired)
    private BlockPos pairedPos = null;

    // Fuel level (in coal items)
    private int fuelLevel = 0;
    private static final int MAX_FUEL = 64;

    // Fluid storage implementation
    public final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<FluidVariant>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return CAPACITY;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public LiquidConduitBlockEntity(BlockPos pos, BlockState state) {
        super(LiquidConduitsMod.LIQUID_CONDUIT_BLOCK_ENTITY, pos, state);
    }

    // Pairing methods
    public void setPairedPos(BlockPos pos) {
        this.pairedPos = pos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getPairedPos() {
        return pairedPos;
    }

    public boolean isPaired() {
        return pairedPos != null;
    }

    public void unpair() {
        this.pairedPos = null;
        setChanged();
    }

    // Fuel methods
    public boolean addFuel(int amount) {
        if (fuelLevel < MAX_FUEL) {
            fuelLevel = Math.min(fuelLevel + amount, MAX_FUEL);
            setChanged();
            return true;
        }
        return false;
    }

    public boolean consumeFuel(int amount) {
        if (fuelLevel >= amount) {
            fuelLevel -= amount;
            setChanged();
            return true;
        }
        return false;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    // Transfer fluid to paired conduit
    public boolean transferToPaired(long amount) {
        if (!isPaired() || level == null) {
            return false;
        }

        // Check if we have enough fuel (1 coal per bucket transferred)
        long bucketsToTransfer = (amount + FluidConstants.BUCKET - 1) / FluidConstants.BUCKET;
        if (!consumeFuel((int) bucketsToTransfer)) {
            return false;
        }

        BlockEntity targetEntity = level.getBlockEntity(pairedPos);
        if (!(targetEntity instanceof LiquidConduitBlockEntity targetConduit)) {
            return false;
        }

        // Perform the transfer
        try (Transaction transaction = Transaction.openOuter()) {
            FluidVariant variant = fluidStorage.variant;
            long extracted = fluidStorage.extract(variant, amount, transaction);

            if (extracted > 0) {
                long inserted = targetConduit.fluidStorage.insert(variant, extracted, transaction);

                if (inserted == extracted) {
                    transaction.commit();
                    return true;
                }
            }
            // Transaction will auto-abort if not committed
        }

        return false;
    }

    // TODO: Re-implement NBT persistence once we understand 1.21 APIs better
    // For now, data won't persist across world reloads (will be added back)

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
