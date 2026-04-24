package com.checkersworld.checkers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CheckersWorldMod.MOD_ID)
public final class CheckersWorldMod {
    public static final String MOD_ID = "checkersworld";
    public static final ResourceKey<WorldPreset> CHECKERS_PRESET = ResourceKey.create(
        Registries.WORLD_PRESET,
        new ResourceLocation(MOD_ID, "checkers")
    );

    public CheckersWorldMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistries.CHUNK_GENERATORS.register(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.checkersworld.checkers.client.CheckersPresetEditorClient.register(modBus));
    }
}
