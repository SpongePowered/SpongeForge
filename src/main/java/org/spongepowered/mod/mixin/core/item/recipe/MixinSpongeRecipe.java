package org.spongepowered.mod.mixin.core.item.recipe;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.recipe.crafting.DelegateSpongeCraftingRecipe;

import javax.annotation.Nullable;

@Mixin(value = DelegateSpongeCraftingRecipe.class, remap = false)
public abstract class MixinSpongeRecipe implements IForgeRegistryEntry<IRecipe> {

    private ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name) {
        if (getRegistryName() != null)
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + getRegistryName());
        this.registryName = name;
        return ((IRecipe) this);
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }
}
