package com.checkersworld.checkers.client;

import com.checkersworld.checkers.CheckersGeneratorSettings;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CheckersWorldOptionsScreen extends Screen {
    private static final ResourceLocation SLOT_SPRITE = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final int START_Y_MIN = -64;
    private static final int START_Y_MAX = 320;

    private final Screen parent;
    private final Consumer<CheckersGeneratorSettings> onApply;

    private CheckersGeneratorSettings current;
    private EditBox firstBlockInput;
    private EditBox secondBlockInput;
    private IntSlider depthSlider;
    private IntSlider segmentSizeSlider;
    private IntSlider startYSlider;
    private CycleButton<Boolean> bedrockToggle;

    private BlockState parsedFirstBlock;
    private BlockState parsedSecondBlock;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int inputLeft;
    private int inputWidth;
    private int previewX;
    private int firstLabelY;
    private int firstInputY;
    private int secondLabelY;
    private int secondInputY;
    private int depthY;
    private int segmentY;
    private int startYY;
    private int bedrockY;
    private int buttonY;

    public CheckersWorldOptionsScreen(Screen parent, CheckersGeneratorSettings initial, Consumer<CheckersGeneratorSettings> onApply) {
        super(Component.translatable("checkersworld.screen.title"));
        this.parent = parent;
        this.onApply = onApply;
        this.current = initial;
        this.parsedFirstBlock = initial.firstBlock();
        this.parsedSecondBlock = initial.secondBlock();
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(360, this.width - 12);
        this.panelHeight = Math.min(280, this.height - 12);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;
        this.previewX = this.panelLeft + this.panelWidth - 30;
        this.inputLeft = this.panelLeft + 12;
        this.inputWidth = Math.max(120, this.previewX - this.inputLeft - 8);

        this.firstLabelY = this.panelTop + 20;
        this.firstInputY = this.panelTop + 32;
        this.secondLabelY = this.panelTop + 62;
        this.secondInputY = this.panelTop + 74;
        this.depthY = this.panelTop + 106;
        this.segmentY = this.panelTop + 134;
        this.startYY = this.panelTop + 162;
        this.bedrockY = this.panelTop + 190;
        this.buttonY = this.panelTop + this.panelHeight - 24;

        this.firstBlockInput = new EditBox(this.font, this.inputLeft, this.firstInputY, this.inputWidth, 20, Component.translatable("checkersworld.first_block"));
        this.firstBlockInput.setValue(formatBlock(this.current.firstBlock()));
        this.firstBlockInput.setResponder(this::onFirstBlockChanged);
        this.addRenderableWidget(this.firstBlockInput);

        this.secondBlockInput = new EditBox(this.font, this.inputLeft, this.secondInputY, this.inputWidth, 20, Component.translatable("checkersworld.second_block"));
        this.secondBlockInput.setValue(formatBlock(this.current.secondBlock()));
        this.secondBlockInput.setResponder(this::onSecondBlockChanged);
        this.addRenderableWidget(this.secondBlockInput);

        this.depthSlider = this.addRenderableWidget(new IntSlider(
            this.inputLeft,
            this.depthY,
            this.inputWidth + 26,
            20,
            Component.translatable("checkersworld.depth"),
            CheckersGeneratorSettings.minDepth(),
            CheckersGeneratorSettings.maxDepth(),
            this.current.depthLevel(),
            value -> this.current = new CheckersGeneratorSettings(
                this.parsedFirstBlock,
                this.parsedSecondBlock,
                value,
                this.current.generateBedrock(),
                this.current.startY(),
                this.current.segmentSize()
            )
        ));

        this.segmentSizeSlider = this.addRenderableWidget(new IntSlider(
            this.inputLeft,
            this.segmentY,
            this.inputWidth + 26,
            20,
            Component.translatable("checkersworld.segment_size"),
            CheckersGeneratorSettings.minSegmentSize(),
            CheckersGeneratorSettings.maxSegmentSize(),
            this.current.segmentSize(),
            value -> this.current = new CheckersGeneratorSettings(
                this.parsedFirstBlock,
                this.parsedSecondBlock,
                this.current.depthLevel(),
                this.current.generateBedrock(),
                this.current.startY(),
                value
            )
        ));

        this.startYSlider = this.addRenderableWidget(new IntSlider(
            this.inputLeft,
            this.startYY,
            this.inputWidth + 26,
            20,
            Component.translatable("checkersworld.start_y"),
            START_Y_MIN,
            START_Y_MAX,
            Mth.clamp(this.current.startY(), START_Y_MIN, START_Y_MAX),
            value -> this.current = new CheckersGeneratorSettings(
                this.parsedFirstBlock,
                this.parsedSecondBlock,
                this.current.depthLevel(),
                this.current.generateBedrock(),
                value,
                this.current.segmentSize()
            )
        ));

        this.bedrockToggle = this.addRenderableWidget(CycleButton.onOffBuilder(this.current.generateBedrock())
            .create(this.inputLeft, this.bedrockY, this.inputWidth + 26, 20, Component.translatable("checkersworld.bedrock"), (button, value) -> {
                this.current = new CheckersGeneratorSettings(
                    this.parsedFirstBlock,
                    this.parsedSecondBlock,
                    this.current.depthLevel(),
                    value,
                    this.current.startY(),
                    this.current.segmentSize()
                );
            }));

        int actionWidth = (this.panelWidth - 28) / 2;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone())
            .bounds(this.panelLeft + 12, this.buttonY, actionWidth, 20)
            .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
            .bounds(this.panelLeft + 16 + actionWidth, this.buttonY, actionWidth, 20)
            .build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.panelLeft + this.panelWidth / 2;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, this.panelTop + 6, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("checkersworld.first_block"), this.inputLeft, this.firstLabelY, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("checkersworld.second_block"), this.inputLeft, this.secondLabelY, 0xFFFFFF);

        drawBlockPreview(guiGraphics, this.previewX, this.firstInputY + 1, this.parsedFirstBlock);
        drawBlockPreview(guiGraphics, this.previewX, this.secondInputY + 1, this.parsedSecondBlock);

    }

    private void onDone() {
        BlockState first = this.parsedFirstBlock != null ? this.parsedFirstBlock : CheckersGeneratorSettings.DEFAULT.firstBlock();
        BlockState second = this.parsedSecondBlock != null ? this.parsedSecondBlock : CheckersGeneratorSettings.DEFAULT.secondBlock();
        CheckersGeneratorSettings result = new CheckersGeneratorSettings(
            first,
            second,
            this.depthSlider.getValueInt(),
            this.bedrockToggle.getValue(),
            this.startYSlider.getValueInt(),
            this.segmentSizeSlider.getValueInt()
        );
        this.onApply.accept(result);
        this.minecraft.setScreen(this.parent);
    }

    private void onFirstBlockChanged(String text) {
        this.parsedFirstBlock = parseBlock(text);
        this.current = new CheckersGeneratorSettings(
            this.parsedFirstBlock != null ? this.parsedFirstBlock : this.current.firstBlock(),
            this.parsedSecondBlock,
            this.current.depthLevel(),
            this.current.generateBedrock(),
            this.current.startY(),
            this.current.segmentSize()
        );
    }

    private void onSecondBlockChanged(String text) {
        this.parsedSecondBlock = parseBlock(text);
        this.current = new CheckersGeneratorSettings(
            this.parsedFirstBlock,
            this.parsedSecondBlock != null ? this.parsedSecondBlock : this.current.secondBlock(),
            this.current.depthLevel(),
            this.current.generateBedrock(),
            this.current.startY(),
            this.current.segmentSize()
        );
    }

    private static BlockState parseBlock(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw.trim());
        if (id == null) {
            return null;
        }

        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (block == null || block == Blocks.AIR) {
            return null;
        }

        return block.defaultBlockState();
    }

    private static String formatBlock(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private void drawBlockPreview(GuiGraphics guiGraphics, int x, int y, BlockState state) {
        guiGraphics.blit(SLOT_SPRITE, x, y, 176, 0, 18, 18, 256, 256);
        ItemStack stack = itemStackForState(state);
        if (!stack.isEmpty()) {
            guiGraphics.renderFakeItem(stack, x + 1, y + 1);
        }
    }

    private static ItemStack itemStackForState(BlockState state) {
        if (state == null) {
            return ItemStack.EMPTY;
        }

        Item item = state.getBlock().asItem();
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final Component label;
        private final int min;
        private final int max;
        private final Consumer<Integer> onChange;
        private int valueInt;

        private IntSlider(int x, int y, int width, int height, Component label, int min, int max, int initial, Consumer<Integer> onChange) {
            super(x, y, width, height, CommonComponents.EMPTY, 0.0D);
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.valueInt = Mth.clamp(initial, min, max);
            this.value = (double) (this.valueInt - min) / (double) (max - min);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("checkersworld.slider", this.label, this.valueInt));
        }

        @Override
        protected void applyValue() {
            int nextValue = Mth.floor(Mth.lerp(this.value, this.min, this.max));
            nextValue = Mth.clamp(nextValue, this.min, this.max);
            if (nextValue != this.valueInt) {
                this.valueInt = nextValue;
                this.onChange.accept(this.valueInt);
            }
            this.updateMessage();
        }

        public int getValueInt() {
            return this.valueInt;
        }
    }
}

