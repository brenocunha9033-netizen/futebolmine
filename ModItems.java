package com.exemplo.futebolmod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
	public static final Item SOCCER_BALL = Registry.register(Registries.ITEM,
			Identifier.of(FutebolMod.MOD_ID, "soccer_ball"),
			new BallItem(new Item.Settings().maxCount(16)));

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(SOCCER_BALL));
	}
}
