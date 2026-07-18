package com.example.examplemod.client.gui;

import com.example.examplemod.client.ClientCodexData;
import com.example.examplemod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class SevenStarsCodexScreen extends Screen {
    private static final int BOOK_WIDTH = 406;
    private static final int BOOK_HEIGHT = 236;
    private static final int LIST_WIDTH = 142;
    private static final int ROW_HEIGHT = 22;

    private static final List<Chapter> CHAPTERS = List.of(
            chapter("stage0.introduction", "stage0", 2, new ItemStack(Items.BOOK)),
            chapter("stage0.qi", "stage0", 3, new ItemStack(ModItems.QI_CRYSTAL_SHARD.get())),
            chapter("stage0.spellcasting", "stage0", 2, new ItemStack(ModItems.SEVEN_STAR_SCROLL.get())),
            chapter("stage0.basic_spells", "stage0", 4, new ItemStack(ModItems.LIGHT_WAVE_SCROLL.get())),
            chapter("stage1.journey", "stage1", 2, new ItemStack(Items.COMPASS)),
            chapter("stage1.pegasus", "stage1", 2, new ItemStack(ModItems.STAR_REIN_BELL.get())),
            chapter("stage1.ice_dipper", "stage1", 2, new ItemStack(ModItems.FROST_MARROW_WAND.get())),
            chapter("stage1.hound_claw", "stage1", 2, new ItemStack(ModItems.CRACKED_CLAW_DAGGER.get())),
            chapter("stage1.completion", "stage1", 1, new ItemStack(Items.NETHER_STAR)),
            chapter("stage2.overview", "stage2", 1, new ItemStack(ModItems.COMPLETE_GOAT_HORN.get())),
            chapter("stage2.goat_horn", "stage2", 2, new ItemStack(ModItems.GOAT_HORN_ARMOR.get())),
            chapter("stage2.goat_temple", "stage2", 2, new ItemStack(ModItems.GOAT_HORN_ALTAR.get())),
            chapter("stage2.chop", "stage2", 2, new ItemStack(ModItems.BLOODY_CLEAVER.get())),
            chapter("stage2.triangle_town", "stage2", 2, new ItemStack(ModItems.SOUL_CALMING_LAMP.get())),
            chapter("stage2.triangle", "stage2", 2, new ItemStack(ModItems.TRIANGLE_ARMOR.get())),
            chapter("stage3.azure_dragon", "stage3", 9, new ItemStack(ModItems.AZURE_DRAGON_SCALE.get()))
    );

    private int bookLeft;
    private int bookTop;
    private int chapterScroll;
    private int selectedIndex;
    private int page;
    private Button previousPage;
    private Button nextPage;

    public SevenStarsCodexScreen() {
        super(Component.translatable("screen.sevenstars.codex"));
    }

    @Override
    protected void init() {
        bookLeft = (width - BOOK_WIDTH) / 2;
        bookTop = (height - BOOK_HEIGHT) / 2;
        int buttonY = bookTop + BOOK_HEIGHT - 25;
        previousPage = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(bookLeft + LIST_WIDTH + 15, buttonY, 26, 18).build());
        nextPage = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(bookLeft + BOOK_WIDTH - 41, buttonY, 26, 18).build());
        updatePageButtons();
    }

    public void refreshUnlocks() {
        selectedIndex = Mth.clamp(selectedIndex, 0, CHAPTERS.size() - 1);
        page = Math.min(page, currentChapter().pageCount() - 1);
        updatePageButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(bookLeft, bookTop, bookLeft + BOOK_WIDTH, bookTop + BOOK_HEIGHT, 0xFFE6D5A9);
        graphics.fill(bookLeft + 3, bookTop + 3, bookLeft + BOOK_WIDTH - 3, bookTop + BOOK_HEIGHT - 3, 0xFFF4E8C5);
        graphics.fill(bookLeft + LIST_WIDTH, bookTop + 4, bookLeft + LIST_WIDTH + 2, bookTop + BOOK_HEIGHT - 4, 0xFF8B6A3E);

        graphics.drawCenteredString(font, title, bookLeft + BOOK_WIDTH / 2, bookTop + 9, 0x4D2D18);
        renderChapterList(graphics, mouseX, mouseY);
        renderChapter(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderChapterList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listTop = bookTop + 27;
        int visibleRows = visibleRows();
        for (int row = 0; row < visibleRows; row++) {
            int index = chapterScroll + row;
            if (index >= CHAPTERS.size()) {
                break;
            }
            Chapter chapter = CHAPTERS.get(index);
            int y = listTop + row * ROW_HEIGHT;
            boolean hovered = mouseX >= bookLeft + 6 && mouseX < bookLeft + LIST_WIDTH - 4
                    && mouseY >= y && mouseY < y + ROW_HEIGHT - 1;
            int background = index == selectedIndex ? 0x806F4E2A : hovered ? 0x407B5A34 : 0x00000000;
            graphics.fill(bookLeft + 6, y, bookLeft + LIST_WIDTH - 5, y + ROW_HEIGHT - 1, background);

            boolean unlocked = ClientCodexData.isUnlocked(chapter.id());
            ItemStack icon = unlocked ? chapter.icon() : new ItemStack(Items.IRON_BARS);
            graphics.renderItem(icon, bookLeft + 9, y + 2);
            Component stage = Component.translatable("codex.sevenstars." + chapter.stageKey())
                    .withStyle(ChatFormatting.DARK_GRAY);
            graphics.drawString(font, stage, bookLeft + 28, y + 2, 0x6B5841, false);
            Component chapterTitle = unlocked ? chapter.title() : chapter.lockedTitle();
            graphics.drawString(font, font.plainSubstrByWidth(chapterTitle.getString(), LIST_WIDTH - 39),
                    bookLeft + 28, y + 11, unlocked ? 0x2C2118 : 0x777067, false);
        }
    }

    private void renderChapter(GuiGraphics graphics) {
        Chapter chapter = currentChapter();
        int contentLeft = bookLeft + LIST_WIDTH + 15;
        int contentRight = bookLeft + BOOK_WIDTH - 15;
        int contentWidth = contentRight - contentLeft;
        boolean unlocked = ClientCodexData.isUnlocked(chapter.id());

        Component heading = unlocked ? chapter.title() : chapter.lockedTitle();
        graphics.drawCenteredString(font, heading, contentLeft + contentWidth / 2, bookTop + 30,
                unlocked ? 0x4D2D18 : 0x6F6A63);
        graphics.fill(contentLeft + 12, bookTop + 43, contentRight - 12, bookTop + 44, 0x806B4A28);

        Component body = unlocked ? Component.translatable(chapter.pageKey(page)) : chapter.lockedBody();
        int y = bookTop + 53;
        for (FormattedCharSequence line : font.split(body, contentWidth)) {
            if (y > bookTop + BOOK_HEIGHT - 39) {
                break;
            }
            graphics.drawString(font, line, contentLeft, y, unlocked ? 0x34271D : 0x706A63, false);
            y += 11;
        }

        if (unlocked && chapter.pageCount() > 1) {
            Component pageLabel = Component.translatable("codex.sevenstars.page", page + 1, chapter.pageCount());
            graphics.drawCenteredString(font, pageLabel, contentLeft + contentWidth / 2,
                    bookTop + BOOK_HEIGHT - 20, 0x6B5841);
        }
        graphics.drawString(font, Component.translatable("codex.sevenstars.hint"),
                bookLeft + 8, bookTop + BOOK_HEIGHT - 13, 0x7D6B52, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= bookLeft + 6 && mouseX < bookLeft + LIST_WIDTH - 4) {
            int row = (int) ((mouseY - (bookTop + 27)) / ROW_HEIGHT);
            if (mouseY >= bookTop + 27 && row >= 0 && row < visibleRows()) {
                int index = chapterScroll + row;
                if (index < CHAPTERS.size()) {
                    selectedIndex = index;
                    page = 0;
                    updatePageButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= bookLeft && mouseX <= bookLeft + LIST_WIDTH) {
            int maxScroll = Math.max(0, CHAPTERS.size() - visibleRows());
            chapterScroll = Mth.clamp(chapterScroll - (int) Math.signum(delta), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void changePage(int direction) {
        page = Mth.clamp(page + direction, 0, currentChapter().pageCount() - 1);
        updatePageButtons();
    }

    private void updatePageButtons() {
        if (previousPage == null || nextPage == null) {
            return;
        }
        boolean unlocked = ClientCodexData.isUnlocked(currentChapter().id());
        previousPage.visible = unlocked && currentChapter().pageCount() > 1;
        nextPage.visible = previousPage.visible;
        previousPage.active = page > 0;
        nextPage.active = page + 1 < currentChapter().pageCount();
    }

    private int visibleRows() {
        return (BOOK_HEIGHT - 48) / ROW_HEIGHT;
    }

    private Chapter currentChapter() {
        return CHAPTERS.get(selectedIndex);
    }

    private static Chapter chapter(String id, String stageKey, int pageCount, ItemStack icon) {
        return new Chapter(id, stageKey, pageCount, icon);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Chapter(String id, String stageKey, int pageCount, ItemStack icon) {
        private Component title() {
            return Component.translatable("codex.sevenstars.chapter." + id + ".title");
        }

        private Component lockedTitle() {
            if ("stage3.azure_dragon".equals(id)) {
                return Component.translatable("codex.sevenstars.chapter.stage3.azure_dragon.locked_title");
            }
            return Component.translatable("codex.sevenstars.locked");
        }

        private Component lockedBody() {
            if ("stage3.azure_dragon".equals(id)) {
                return Component.translatable("codex.sevenstars.chapter.stage3.azure_dragon.unlock_hint");
            }
            return Component.translatable("codex.sevenstars.locked.body");
        }

        private String pageKey(int page) {
            return "codex.sevenstars.chapter." + id + ".page" + (page + 1);
        }
    }
}
