package com.rmash.liquidconduits;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquidConduitsMod implements ModInitializer {
	public static final String MOD_ID = "liquidconduits";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Define the Liquid Conduit Block with BlockEntity support
	public static final Block LIQUID_CONDUIT_BLOCK = new LiquidConduitBlock(BlockBehaviour.Properties.of()
			.strength(1.5f, 6.0f)
			.sound(SoundType.METAL)
			.lightLevel(state -> 5));

	// Block Entity Type
	public static BlockEntityType<LiquidConduitBlockEntity> LIQUID_CONDUIT_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Liquid Conduits Mod - Instant liquid transport!");

		// Register the block
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "liquid_conduit"), LIQUID_CONDUIT_BLOCK);

		// Register the block entity type
		LIQUID_CONDUIT_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "liquid_conduit"),
			FabricBlockEntityTypeBuilder.create(LiquidConduitBlockEntity::new, LIQUID_CONDUIT_BLOCK).build()
		);

		// Register the paired conduit item
		Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "liquid_conduit"),
				new PairedConduitItem(LIQUID_CONDUIT_BLOCK, new Item.Properties()));

		// Register fluid storage for the block entity
		FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.fluidStorage, LIQUID_CONDUIT_BLOCK_ENTITY);

		// Add to creative inventory
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(LIQUID_CONDUIT_BLOCK);
		});

		LOGGER.info("Liquid Conduit block and block entity registered successfully!");
	}
}
