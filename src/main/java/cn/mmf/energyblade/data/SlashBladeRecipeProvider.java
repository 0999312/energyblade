package cn.mmf.energyblade.data;

import java.util.concurrent.CompletableFuture;

import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

public class SlashBladeRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SlashBladeRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        SlashBladeShapedRecipeBuilder.shaped(BuiltInSlashBladeRegistry.HF_BLADE.location())
        .pattern("SLJ")
        .pattern("LBL")
        .pattern("JLS")
        .define('B',
                SlashBladeIngredient
                        .of(RequestDefinition.Builder.newInstance().refineCount(10).build()))
        .define('S', Ingredient.of(SBItems.proudsoul_sphere))
        .define('J', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
        .define('L', Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON))
        .unlockedBy(getHasName(SBItems.slashblade), has(SBItems.slashblade)).save(output);

    }

}
