package com.fuzs.easymagic.client.gui.screen;

import com.fuzs.easymagic.client.element.EasyEnchantingExtension;
import com.fuzs.easymagic.client.gui.widget.LineTabWidget;
import com.fuzs.easymagic.client.gui.widget.TabWidget;
import com.fuzs.easymagic.client.gui.widget.TextTabWidget;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnchantmentInventoryScreen extends EnchantmentScreen {

    private final List<List<ITextComponent>> slotTooltips = IntStream.range(0, 3)
            .mapToObj(i -> Lists.<ITextComponent>newArrayList())
            .collect(Collectors.toList());

    private TextTabWidget tabWidget1;
    private LineTabWidget tabWidget2;

    public EnchantmentInventoryScreen(EnchantmentContainer container, PlayerInventory playerInventory, ITextComponent textComponent) {

        super(container, playerInventory, textComponent);
    }

    @Override
    protected void init() {

        super.init();
        String tabContent = "This is a test. Hopefully nothing is too long. Let's just see what happens. Although we can never be really certain, it's still worth it to try something new now and then. This is what we are made for, so let's give it a shot.";
        this.tabWidget1 = new TextTabWidget(this, TabWidget.Side.left(0), 0x79B637, new StringTextComponent("Tips & Hints"));
        this.tabWidget2 = new LineTabWidget(this, TabWidget.Side.right(0), 0x47B5FE, new StringTextComponent("Info"));
        this.tabWidget1.setAtlasIcon(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EasyEnchantingExtension.QUESTION_MARK_LOCATION);
        this.tabWidget2.setItemIcon(Items.BOOKSHELF);
        this.tabWidget1.setTextContent(new StringTextComponent(tabContent));
        this.tabWidget2.setLineContent(new ITextComponent[][]{{new StringTextComponent("Enchanting Power:thisisa nrn fn"), new StringTextComponent("    10 / 15")}, {new StringTextComponent("Enchanting Power:"), new StringTextComponent("10 / 15")}});
        this.addButton(this.tabWidget1);
//        this.addButton(this.tabWidget2);
    }

    @Override
    public void tick() {

        super.tick();
        this.tabWidget1.tick();
        this.tabWidget2.tick();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // rendering of vanilla tooltip is canceled in #isPointInRegion when this is true
        int slot = this.getEnchantingSlot(mouseX, mouseY);
        if (slot != -1 && !this.slotTooltips.get(slot).isEmpty()) {

            this.func_243308_b(matrixStack, this.slotTooltips.get(slot), mouseX, mouseY);
        }
    }

    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {

        // small hack to prevent EnchantmentScreen::render from rendering vanilla enchanting tooltips
        if (this.getSelectedSlot(mouseX, mouseY) != null) {

            return super.isPointInRegion(x, y, width, height, mouseX, mouseY);
        }

        return false;
    }

    @Nullable
    private Slot getSelectedSlot(double mouseX, double mouseY) {

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {

            Slot slot = this.container.inventorySlots.get(i);
            if (this.isSlotSelected(slot, mouseX, mouseY) && slot.isEnabled()) {

                return slot;
            }
        }

        return null;
    }

    private boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {

        return super.isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
    }

    private int getEnchantingSlot(int mouseX, int mouseY) {

        for (int j = 0; j < 3; ++j) {

            int levels = this.container.enchantLevels[j];
            // coordinates are off, but the rest of the tooltip is computed using the same coordinates
            if (super.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && levels > 0) {

                return j;
            }
        }

        return -1;
    }

    public void setSlotTooltip(int slot, List<EnchantmentData> slotData) {

        List<ITextComponent> slotTooltip = this.slotTooltips.get(slot);
        slotTooltip.clear();
        this.addSlotEnchantments(slot, slotData, slotTooltip);
    }

    private void addSlotEnchantments(int slot, List<EnchantmentData> slotData, List<ITextComponent> slotTooltip) {

        // don't add forge text later when tooltip is purposefully empty
        boolean hasValidEnchantment = slotData.isEmpty();
        for (EnchantmentData data : slotData) {

            if (data.enchantment != null) {

                slotTooltip.add((new TranslationTextComponent("container.enchant.clue", data.enchantment.getDisplayName(data.enchantmentLevel))).mergeStyle(TextFormatting.WHITE));
                hasValidEnchantment = true;
            }
        }

        this.addSlotInfo(slot, slotTooltip, hasValidEnchantment);
    }

    @SuppressWarnings("ConstantConditions")
    private void addSlotInfo(int slot, List<ITextComponent> slotTooltip, boolean hasValidEnchantment) {

        List<ITextComponent> additionalTooltip = Lists.newArrayList();
        if (!hasValidEnchantment) {

            additionalTooltip.add(new TranslationTextComponent("forge.container.enchant.limitedEnchantability").mergeStyle(TextFormatting.RED));
        } else if (!this.minecraft.player.abilities.isCreativeMode) {

            int enchantLevels = this.container.enchantLevels[slot];
            if (this.minecraft.player.experienceLevel < enchantLevels) {

                additionalTooltip.add((new TranslationTextComponent("container.enchant.level.requirement", enchantLevels)).mergeStyle(TextFormatting.RED));
            } else {

                slot++;
                this.addLapisComponent(slot, additionalTooltip);
                this.addLevelComponent(slot, additionalTooltip);
            }
        }

        if (!additionalTooltip.isEmpty()) {

            if (!slotTooltip.isEmpty()) {

                slotTooltip.add(StringTextComponent.EMPTY);
            }

            slotTooltip.addAll(additionalTooltip);
        }
    }

    private void addLapisComponent(int slot, List<ITextComponent> slotTooltip) {

        IFormattableTextComponent lapisComponent;
        if (slot == 1) {

            lapisComponent = new TranslationTextComponent("container.enchant.lapis.one");
        } else {

            lapisComponent = new TranslationTextComponent("container.enchant.lapis.many", slot);
        }

        slotTooltip.add(lapisComponent.mergeStyle(this.container.getLapisAmount() >= slot ? TextFormatting.GRAY : TextFormatting.RED));
    }

    private void addLevelComponent(int slot, List<ITextComponent> slotTooltip) {

        IFormattableTextComponent levelComponent;
        if (slot == 1) {

            levelComponent = new TranslationTextComponent("container.enchant.level.one");
        } else {

            levelComponent = new TranslationTextComponent("container.enchant.level.many", slot);
        }

        slotTooltip.add(levelComponent.mergeStyle(TextFormatting.GRAY));
    }

}
