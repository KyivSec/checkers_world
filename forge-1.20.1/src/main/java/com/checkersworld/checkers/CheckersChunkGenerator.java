package com.checkersworld.checkers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class CheckersChunkGenerator extends ChunkGenerator {
    public static final Codec<CheckersChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter(CheckersChunkGenerator::getBiomeSource),
        CheckersGeneratorSettings.CODEC.fieldOf("settings").forGetter(CheckersChunkGenerator::settings)
    ).apply(instance, CheckersChunkGenerator::new));

    private final CheckersGeneratorSettings settings;

    public CheckersChunkGenerator(BiomeSource biomeSource, CheckersGeneratorSettings settings) {
        super(biomeSource);
        this.settings = settings;
    }

    public CheckersGeneratorSettings settings() {
        return this.settings;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed) {
        Stream<Holder<StructureSet>> structureSets = structureSetLookup.listElements().map(holder -> (Holder<StructureSet>) holder);
        return ChunkGeneratorStructureState.createForFlat(randomState, seed, this.biomeSource, structureSets);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return ModRegistries.CHECKERS_CHUNK_GENERATOR.get();
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void applyCarvers(
        WorldGenRegion level,
        long seed,
        RandomState random,
        BiomeManager biomeManager,
        StructureManager structureManager,
        ChunkAccess chunk,
        GenerationStep.Carving step
    ) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public void createStructures(
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager,
        ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager
    ) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        int segmentSize = this.settings.segmentSize();
        int requestedBottom = this.settings.startY() - this.settings.depthLevel() * segmentSize + 1;
        int bottomY = Math.max(chunk.getMinBuildHeight(), requestedBottom);
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, this.settings.startY());
        if (bottomY > topY) {
            return CompletableFuture.completedFuture(chunk);
        }

        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkPos.getMinBlockX() + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = chunkPos.getMinBlockZ() + localZ;
                for (int y = bottomY; y <= topY; y++) {
                    BlockState state = this.resolveBlock(worldX, y, worldZ, bottomY);
                    chunk.setBlockState(cursor.set(localX, y, localZ), state, false);
                    oceanFloor.update(localX, y, localZ, state);
                    worldSurface.update(localX, y, localZ, state);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int bottomY = Math.max(level.getMinBuildHeight(), this.settings.startY() - this.settings.depthLevel() * this.settings.segmentSize() + 1);
        int topY = Math.min(level.getMaxBuildHeight() - 1, this.settings.startY());

        for (int y = topY; y >= bottomY; y--) {
            BlockState state = this.resolveBlock(x, y, z, bottomY);
            if (!state.isAir() && type.isOpaque().test(state)) {
                return y + 1;
            }
        }

        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        int worldMinY = height.getMinBuildHeight();
        BlockState[] states = new BlockState[height.getHeight()];
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }

        int bottomY = Math.max(height.getMinBuildHeight(), this.settings.startY() - this.settings.depthLevel() * this.settings.segmentSize() + 1);
        int topY = Math.min(height.getMaxBuildHeight() - 1, this.settings.startY());
        for (int y = bottomY; y <= topY; y++) {
            states[y - worldMinY] = this.resolveBlock(x, y, z, bottomY);
        }

        return new NoiseColumn(worldMinY, states);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return Math.min(level.getMaxBuildHeight() - 1, this.settings.startY() + 1);
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }

    private BlockState resolveBlock(int worldX, int y, int worldZ, int generatedBottomY) {
        if (this.settings.generateBedrock() && y == generatedBottomY) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        int segmentSize = this.settings.segmentSize();
        int checkerIndex = Math.floorDiv(worldX, segmentSize) + Math.floorDiv(y, segmentSize) + Math.floorDiv(worldZ, segmentSize);
        return (checkerIndex & 1) == 0 ? this.settings.firstBlock() : this.settings.secondBlock();
    }
}
