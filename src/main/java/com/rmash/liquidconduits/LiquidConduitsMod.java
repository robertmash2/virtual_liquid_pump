package com.rmash.liquidconduits;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquidConduitsMod implements ModInitializer {
	public static final String MOD_ID = "liquidconduits";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Define the custom Liquid Conduit Block (placeholder for now)
	public static final Block LIQUID_CONDUIT_BLOCK = new Block(BlockBehaviour.Properties.of()
			.strength(1.5f, 6.0f)
			.sound(SoundType.METAL)
			.lightLevel(state -> 5));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Liquid Conduits Mod - Instant liquid transport!");

		// Register the block
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "liquid_conduit"), LIQUID_CONDUIT_BLOCK);

		// Register the block item (so it can be held/placed)
		Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "liquid_conduit"),
				new BlockItem(LIQUID_CONDUIT_BLOCK, new Item.Properties()));

		// Add to creative inventory
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(LIQUID_CONDUIT_BLOCK);
		});

		LOGGER.info("Liquid Conduit block registered successfully!");
	}
}
