package com.example.examplemod.compat.jei;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SpellScrollRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class WritingTableJeiCategory implements IRecipeCategory<SpellScrollRecipes.Recipe> {
    public static final RecipeType<SpellScrollRecipes.Recipe> RECIPE_TYPE = RecipeType.create(
            ExampleMod.MODID, "writing_table", SpellScrollRecipes.Recipe.class);

    private final IDrawableStatic background;
    private final IDrawable icon;

    public WritingTableJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 76);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.WRITING_TABLE.get()));
    }

    @Override
    public RecipeType<SpellScrollRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.sevenstars.writing_table");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpellScrollRecipes.Recipe recipe, IFocusGroup focuses) {
        int x = 4;
        int y = 20;
        for (ItemStack cost : recipe.cost()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack(cost);
            x += 20;
            if (x > 84) {
                x = 4;
                y += 20;
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 30).addItemStack(recipe.output());
    }

    @Override
    public void draw(SpellScrollRecipes.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                Component.translatable("jei.sevenstars.tier", recipe.tier()), 4, 4, 0x404040, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                Component.literal("->"), 104, 35, 0x404040, false);
    }

    @Override
    public ResourceLocation getRegistryName(SpellScrollRecipes.Recipe recipe) {
        return new ResourceLocation(ExampleMod.MODID, "writing_table/" + recipe.skillId());
    }
}
