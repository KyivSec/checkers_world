package com.checkersworld.checkers.client;

import com.checkersworld.checkers.CheckersChunkGenerator;
import com.checkersworld.checkers.CheckersGeneratorSettings;
import com.checkersworld.checkers.CheckersWorldMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;

public final class CheckersPresetEditorClient {
    private CheckersPresetEditorClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(CheckersPresetEditorClient::registerEditors);
    }

    private static void registerEditors(net.minecraftforge.client.event.RegisterPresetEditorsEvent event) {
        event.register(CheckersWorldMod.CHECKERS_PRESET, CheckersPresetEditorClient::createScreen);
    }

    private static Screen createScreen(CreateWorldScreen lastScreen, WorldCreationContext context) {
        ChunkGenerator overworldGenerator = context.selectedDimensions().overworld();
        CheckersGeneratorSettings initialSettings = CheckersGeneratorSettings.DEFAULT;
        BiomeSource biomeSource = createDefaultBiomeSource(context);

        if (overworldGenerator instanceof CheckersChunkGenerator checkersChunkGenerator) {
            initialSettings = checkersChunkGenerator.settings();
            biomeSource = checkersChunkGenerator.getBiomeSource();
        }

        BiomeSource finalBiomeSource = biomeSource;
        return new CheckersWorldOptionsScreen(lastScreen, initialSettings, updatedSettings -> lastScreen.getUiState().updateDimensions(
            (registryAccess, dimensions) -> dimensions.replaceOverworldGenerator(
                registryAccess,
                new CheckersChunkGenerator(finalBiomeSource, updatedSettings)
            )
        ));
    }

    private static BiomeSource createDefaultBiomeSource(WorldCreationContext context) {
        HolderGetter<Biome> biomes = context.worldgenLoadContext().lookupOrThrow(Registries.BIOME);
        return new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS));
    }
}
