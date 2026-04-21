package com.checkersworld.checkers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
        Registries.CHUNK_GENERATOR,
        CheckersWorldMod.MOD_ID
    );

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<CheckersChunkGenerator>> CHECKERS_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("checkers", () -> CheckersChunkGenerator.CODEC);

    private ModRegistries() {
    }
}

