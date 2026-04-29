package com.example.examplemod.client.gui;

import com.example.examplemod.client.ClientSkillData;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.WriteScrollPacket;
import com.example.examplemod.skill.SpellScrollRecipes;
import com.example.examplemod.skill.SkillTierManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WritingTableScreen extends Screen {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;
    private final BlockPos tablePos;
    private final List<SpellScrollRecipes.Recipe> allRecipes = new ArrayList<>(SpellScrollRecipes.all());
    private SpellScrollRecipes.Recipe selected;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button writeButton;
    private int leftPos;
    private int topPos;
    private int pageTier;

    public WritingTableScreen(BlockPos tablePos) {
        super(Component.translatable("screen.sevenstars.writing_table"));
        this.tablePos = tablePos;
        List<SpellScrollRecipes.Recipe> recipes = pageRecipes();
        if (!recipes.isEmpty()) {
            selected = recipes.get(0);
        }
    }

    @Override
    protected void init() {
        clearWidgets();
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            if (pageTier > 0) {
                pageTier--;
                selectFirstOnPage();
                init();
            }
        }).bounds(leftPos + 14, topPos + 130, 20, 20).build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            if (canTurnToNextPage()) {
                pageTier++;
                selectFirstOnPage();
                init();
            }
        }).bounds(leftPos + 56, topPos + 130, 20, 20).build());
        writeButton = addRenderableWidget(Button.builder(Component.translatable("button.sevenstars.write_scroll"), button -> {
            if (selected != null) {
                ModNetwork.CHANNEL.sendToServer(new WriteScrollPacket(selected.skillId(), tablePos));
            }
        }).bounds(leftPos + 112, topPos + 132, 50, 20).build());
        updateButtonState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderPanel(graphics);
        graphics.drawString(font, title, leftPos + 8, topPos + 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("screen.sevenstars.tier_page", pageTier), leftPos + 12, topPos + 22, 0x404040, false);
        updateButtonState();
        renderRecipeSlots(graphics, mouseX, mouseY);
        renderOutputAndMaterials(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<SpellScrollRecipes.Recipe> recipes = pageRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            int slotX = leftPos + 16 + (i % 2) * 34;
            int slotY = topPos + 40 + (i / 2) * 28;
            if (mouseX >= slotX && mouseX < slotX + 22 && mouseY >= slotY && mouseY < slotY + 22) {
                selected = recipes.get(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderPanel(GuiGraphics graphics) {
        graphics.fill(leftPos, topPos, leftPos + IMAGE_WIDTH, topPos + IMAGE_HEIGHT, 0xFFC6C6C6);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + IMAGE_WIDTH - 3, topPos + IMAGE_HEIGHT - 3, 0xFFE7E7E7);
        graphics.fill(leftPos + 7, topPos + 18, leftPos + 82, topPos + 122, 0xFFD0D0D0);
        graphics.fill(leftPos + 88, topPos + 18, leftPos + 168, topPos + 122, 0xFFD0D0D0);
    }

    private void renderRecipeSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        List<SpellScrollRecipes.Recipe> recipes = pageRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            SpellScrollRecipes.Recipe recipe = recipes.get(i);
            int slotX = leftPos + 16 + (i % 2) * 34;
            int slotY = topPos + 40 + (i / 2) * 28;
            boolean isSelected = recipe == selected;
            boolean locked = isLocked(recipe);
            boolean mastered = recipe.unlocksSeries() ? ClientSkillData.isSeriesUnlocked(recipe.series()) : ClientSkillData.isUnlocked(recipe.skillId());
            int border = isSelected ? 0xFFFFE080 : locked || mastered ? 0xFF777777 : 0xFF555555;
            graphics.fill(slotX - 2, slotY - 2, slotX + 24, slotY + 24, border);
            graphics.fill(slotX, slotY, slotX + 22, slotY + 22, locked || mastered ? 0xFF777777 : 0xFF8B8B8B);
            graphics.fill(slotX + 2, slotY + 2, slotX + 20, slotY + 20, locked || mastered ? 0xFFB0B0B0 : 0xFFEFEFEF);
            graphics.renderItem(recipe.output(), slotX + 3, slotY + 3);
            if (locked) {
                graphics.drawCenteredString(font, Component.literal("X"), slotX + 11, slotY + 7, 0xCC3030);
            } else if (mastered) {
                graphics.drawCenteredString(font, Component.translatable("screen.sevenstars.mastered_mark"), slotX + 11, slotY + 7, 0x309030);
            }
            if (mouseX >= slotX && mouseX < slotX + 22 && mouseY >= slotY && mouseY < slotY + 22) {
                graphics.renderTooltip(font, recipe.displayName(), mouseX, mouseY);
            }
        }
    }

    private void renderOutputAndMaterials(GuiGraphics graphics) {
        if (selected != null) {
            boolean locked = isLocked(selected);
            boolean mastered = selected.unlocksSeries() ? ClientSkillData.isSeriesUnlocked(selected.series()) : ClientSkillData.isUnlocked(selected.skillId());
            int outputX = leftPos + 121;
            int outputY = topPos + 35;
            graphics.drawString(font, Component.translatable("screen.sevenstars.required_materials"), leftPos + 94, topPos + 22, 0x404040, false);
            graphics.fill(outputX - 3, outputY - 3, outputX + 21, outputY + 21, 0xFF555555);
            graphics.fill(outputX - 1, outputY - 1, outputX + 19, outputY + 19, 0xFFEFEFEF);
            graphics.renderItem(selected.output(), outputX, outputY);
            graphics.drawCenteredString(font, selected.displayName(), outputX + 8, outputY + 24, 0x404040);
            if (locked) {
                graphics.drawString(font, selected.unlockRequirementText(), leftPos + 94, topPos + 63, 0xB03030, false);
                return;
            }
            if (mastered) {
                graphics.drawString(font, Component.translatable("screen.sevenstars.mastered"), leftPos + 94, topPos + 63, 0x309030, false);
                return;
            }
            int y = topPos + 75;
            for (ItemStack cost : selected.cost()) {
                int owned = count(cost);
                int x = leftPos + 96;
                graphics.renderItem(cost, x, y - 5);
                graphics.drawString(font, cost.getHoverName(), x + 18, y - 4, 0x404040, false);
                graphics.drawString(font, Component.literal(owned + " / " + cost.getCount()),
                        x + 18, y + 6, owned >= cost.getCount() ? 0x305030 : 0xB03030, false);
                y += 22;
            }
        }
        if (!canTurnToNextPage() && pageTier < maxTier()) {
            graphics.drawString(font, Component.translatable("screen.sevenstars.need_master_page"), leftPos + 13, topPos + 153, 0x8A3030, false);
        }
    }

    private int count(ItemStack target) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        return minecraft.player.getInventory().countItem(target.getItem());
    }

    private boolean isLocked(SpellScrollRecipes.Recipe recipe) {
        return !SkillTierManager.canAccessTier(ClientSkillData.getUnlockedSkills(), recipe.requiredTier());
    }

    private List<SpellScrollRecipes.Recipe> pageRecipes() {
        return allRecipes.stream().filter(recipe -> recipe.tier() == pageTier).toList();
    }

    private void selectFirstOnPage() {
        List<SpellScrollRecipes.Recipe> recipes = pageRecipes();
        selected = recipes.isEmpty() ? null : recipes.get(0);
    }

    private boolean canTurnToNextPage() {
        return pageTier < maxTier() && isPageMastered(pageTier);
    }

    private boolean isPageMastered(int tier) {
        List<SpellScrollRecipes.Recipe> recipes = allRecipes.stream().filter(recipe -> recipe.tier() == tier).toList();
        if (recipes.isEmpty()) {
            return true;
        }
        for (SpellScrollRecipes.Recipe recipe : recipes) {
            boolean mastered = recipe.unlocksSeries() ? ClientSkillData.isSeriesUnlocked(recipe.series()) : ClientSkillData.isUnlocked(recipe.skillId());
            if (!mastered) {
                return false;
            }
        }
        return true;
    }

    private int maxTier() {
        return allRecipes.stream().mapToInt(SpellScrollRecipes.Recipe::tier).max().orElse(0);
    }

    private void updateButtonState() {
        if (previousPageButton != null) {
            previousPageButton.active = pageTier > 0;
        }
        if (nextPageButton != null) {
            nextPageButton.active = canTurnToNextPage();
        }
        if (writeButton != null) {
            writeButton.active = selected != null && !isLocked(selected)
                    && !(selected.unlocksSeries() ? ClientSkillData.isSeriesUnlocked(selected.series()) : ClientSkillData.isUnlocked(selected.skillId()));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
