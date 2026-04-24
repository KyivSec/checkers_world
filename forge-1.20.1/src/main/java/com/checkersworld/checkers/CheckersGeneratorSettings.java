package com.checkersworld.checkers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record CheckersGeneratorSettings(
    BlockState firstBlock,
    BlockState secondBlock,
    int depthLevel,
    boolean generateBedrock,
    int startY,
    int segmentSize
) {
    private static final int MIN_DEPTH = 1;
    private static final int MAX_DEPTH = 128;
    private static final int MIN_SEGMENT_SIZE = 1;
    private static final int MAX_SEGMENT_SIZE = 32;
    private static final BlockState DEFAULT_FIRST_BLOCK = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState DEFAULT_SECOND_BLOCK = Blocks.GRAY_CONCRETE.defaultBlockState();
    private static final int DEFAULT_DEPTH = 1;
    private static final boolean DEFAULT_BEDROCK = true;
    private static final int DEFAULT_START_Y = 64;
    private static final int DEFAULT_SEGMENT_SIZE = 5;

    public static final CheckersGeneratorSettings DEFAULT = new CheckersGeneratorSettings(
        DEFAULT_FIRST_BLOCK,
        DEFAULT_SECOND_BLOCK,
        DEFAULT_DEPTH,
        DEFAULT_BEDROCK,
        DEFAULT_START_Y,
        DEFAULT_SEGMENT_SIZE
    );

    public static final Codec<CheckersGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.optionalFieldOf("first_block", DEFAULT.firstBlock).forGetter(CheckersGeneratorSettings::firstBlock),
        BlockState.CODEC.optionalFieldOf("second_block", DEFAULT.secondBlock).forGetter(CheckersGeneratorSettings::secondBlock),
        Codec.INT.optionalFieldOf("depth_level", DEFAULT.depthLevel).forGetter(CheckersGeneratorSettings::depthLevel),
        Codec.BOOL.optionalFieldOf("generate_bedrock", DEFAULT.generateBedrock).forGetter(CheckersGeneratorSettings::generateBedrock),
        Codec.INT.optionalFieldOf("start_y", DEFAULT.startY).forGetter(CheckersGeneratorSettings::startY),
        Codec.INT.optionalFieldOf("segment_size", DEFAULT.segmentSize).forGetter(CheckersGeneratorSettings::segmentSize)
    ).apply(instance, CheckersGeneratorSettings::new));

    public CheckersGeneratorSettings {
        firstBlock = sanitizeBlock(firstBlock, DEFAULT_FIRST_BLOCK);
        secondBlock = sanitizeBlock(secondBlock, DEFAULT_SECOND_BLOCK);
        depthLevel = Mth.clamp(depthLevel, MIN_DEPTH, MAX_DEPTH);
        segmentSize = Mth.clamp(segmentSize, MIN_SEGMENT_SIZE, MAX_SEGMENT_SIZE);
    }

    public static int minDepth() {
        return MIN_DEPTH;
    }

    public static int maxDepth() {
        return MAX_DEPTH;
    }

    public static int minSegmentSize() {
        return MIN_SEGMENT_SIZE;
    }

    public static int maxSegmentSize() {
        return MAX_SEGMENT_SIZE;
    }

    private static BlockState sanitizeBlock(BlockState value, BlockState fallback) {
        if (value == null || value.isAir()) {
            return fallback;
        }
        return value;
    }
}
