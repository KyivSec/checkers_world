package com.checkersworld.checkers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CheckersWorldMod.MOD_ID)
public final class CheckersWorldMod {
    public static final String MOD_ID = "checkersworld";
    public static final ResourceKey<WorldPreset> CHECKERS_PRESET = ResourceKey.create(
        Registries.WORLD_PRESET,
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "checkers")
    );

    public CheckersWorldMod(IEventBus modEventBus) {
        ModRegistries.CHUNK_GENERATORS.register(modEventBus);
    }
}

