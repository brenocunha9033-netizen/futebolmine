package com.exemplo.futebolmod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
	/** Bloco sem colisão: a bola entra e conta como gol. */
	public static final Block GOAL_NET = register("goal_net",
			new Block(AbstractBlock.Settings.create().noCollision().nonOpaque().strength(0.3f)));

	private static Block register(String name, Block block) {
		Identifier id = Identifier.of(FutebolMod.MOD_ID, name);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, id, block);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(e -> e.add(GOAL_NET));
	}
}
