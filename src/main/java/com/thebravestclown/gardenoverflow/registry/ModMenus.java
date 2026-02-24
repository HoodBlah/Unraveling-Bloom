package com.thebravestclown.gardenoverflow.registry;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class ModMenus {
    
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, GardenOverflowMod.MODID);

    // No menus currently registered

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
