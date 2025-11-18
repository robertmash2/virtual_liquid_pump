package com.rmash.liquidconduits;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LiquidConduitBlock extends BaseEntityBlock {
    public static final MapCodec<LiquidConduitBlock> CODEC = simpleCodec(LiquidConduitBlock::new);

    public LiquidConduitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LiquidConduitBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof LiquidConduitBlockEntity conduit)) {
            return InteractionResult.PASS;
        }

        // Show status when clicking with empty hand
        showStatus(player, conduit);
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof LiquidConduitBlockEntity conduit)) {
            return InteractionResult.PASS;
        }

        // Handle coal for fuel
        if (stack.is(Items.COAL)) {
            if (conduit.addFuel(1)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.displayClientMessage(
                    Component.literal("Fuel added. Current fuel: " + conduit.getFuelLevel()),
                    true
                );
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                    Component.literal("Fuel tank is full!"),
                    true
                );
                return InteractionResult.FAIL;
            }
        }

        // Handle fluid buckets
        var fluidStorage = FluidStorage.ITEM.find(stack, null);
        if (fluidStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                // Try to extract fluid from the bucket
                FluidVariant extractedVariant = null;
                long extractedAmount = 0;

                for (var view : fluidStorage.nonEmptyViews()) {
                    FluidVariant variant = view.getResource();
                    long extracted = fluidStorage.extract(variant, FluidConstants.BUCKET, transaction);
                    if (extracted > 0) {
                        extractedVariant = variant;
                        extractedAmount = extracted;
                        break;
                    }
                }

                // If we got fluid, try to transfer it
                if (extractedAmount > 0 && extractedVariant != null) {
                    if (conduit.isPaired()) {
                        // Transfer to paired conduit
                        long inserted = conduit.fluidStorage.insert(extractedVariant, extractedAmount, transaction);
                        if (inserted == extractedAmount) {
                            // Attempt instant transfer to paired conduit
                            if (conduit.transferToPaired(extractedAmount)) {
                                transaction.commit();
                                player.displayClientMessage(
                                    Component.literal("Fluid transferred to paired conduit!"),
                                    true
                                );
                                return InteractionResult.SUCCESS;
                            } else {
                                player.displayClientMessage(
                                    Component.literal("Transfer failed! Need fuel or paired conduit is full."),
                                    true
                                );
                            }
                        }
                    } else {
                        // Just store it if not paired
                        long inserted = conduit.fluidStorage.insert(extractedVariant, extractedAmount, transaction);
                        if (inserted == extractedAmount) {
                            transaction.commit();
                            player.displayClientMessage(
                                Component.literal("Fluid stored. Pair this conduit to enable transfer!"),
                                true
                            );
                            return InteractionResult.SUCCESS;
                        }
                    }
                }

                // Try to fill bucket from storage
                if (extractedAmount == 0 && conduit.fluidStorage.amount > 0) {
                    FluidVariant variant = conduit.fluidStorage.variant;
                    long toInsert = Math.min(FluidConstants.BUCKET, conduit.fluidStorage.amount);
                    long inserted = fluidStorage.insert(variant, toInsert, transaction);

                    if (inserted > 0) {
                        long extracted = conduit.fluidStorage.extract(variant, inserted, transaction);
                        if (extracted == inserted) {
                            transaction.commit();
                            player.displayClientMessage(
                                Component.literal("Filled bucket from conduit!"),
                                true
                            );
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    private void showStatus(Player player, LiquidConduitBlockEntity conduit) {
        StringBuilder status = new StringBuilder("Liquid Conduit Status:\n");
        status.append("Fuel: ").append(conduit.getFuelLevel()).append("\n");

        if (conduit.fluidStorage.amount > 0) {
            long buckets = conduit.fluidStorage.amount / FluidConstants.BUCKET;
            long droplets = conduit.fluidStorage.amount % FluidConstants.BUCKET;
            status.append("Stored: ").append(buckets).append(" buckets");
            if (droplets > 0) {
                status.append(" + ").append(droplets).append(" droplets");
            }
            status.append("\n");
        } else {
            status.append("Empty\n");
        }

        if (conduit.isPaired()) {
            BlockPos paired = conduit.getPairedPos();
            status.append("Paired to: ").append(paired.toShortString());
        } else {
            status.append("Not paired");
        }

        player.displayClientMessage(Component.literal(status.toString()), false);
    }
}
