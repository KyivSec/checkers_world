package com.checkersworld.checkers;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistries {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
        Registries.CHUNK_GENERATOR,
        CheckersWorldMod.MOD_ID
    );

    public static final RegistryObject<Codec<CheckersChunkGenerator>> CHECKERS_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("checkers", () -> CheckersChunkGenerator.CODEC);

    private ModRegistries() {
    }
}
