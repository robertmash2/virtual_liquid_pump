package com.rmash.sydblock;

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

public class SydBlockMod implements ModInitializer {
	public static final String MOD_ID = "sydblock";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Define the custom Syd Block
	public static final Block SYD_BLOCK = new Block(BlockBehaviour.Properties.of()
			.strength(1.5f, 6.0f)
			.sound(SoundType.AMETHYST)
			.lightLevel(state -> 7));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Syd Block Mod - Hello from Syd!");

		// Register the block
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "syd_block"), SYD_BLOCK);

		// Register the block item (so it can be held/placed)
		Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "syd_block"),
				new BlockItem(SYD_BLOCK, new Item.Properties()));

		// Add to creative inventory
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(SYD_BLOCK);
		});

		LOGGER.info("Syd Block registered successfully!");
	}
}