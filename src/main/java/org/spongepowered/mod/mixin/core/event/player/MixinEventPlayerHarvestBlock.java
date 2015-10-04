/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.event.player;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.HarvestBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.mixin.core.event.block.MixinEventBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(value = HarvestDropsEvent.class, remap = false)
public abstract class MixinEventPlayerHarvestBlock extends MixinEventBlock implements HarvestBlockEvent {

    private ImmutableList<ItemStack> originalDrops;
    private Location<World> targetLocation;
    private int originalExperience;
    private float originalDropChance;
    private int experience;

    // TODO: add support for fortuneLevel
    @Shadow public int fortuneLevel;
    @Shadow public List<net.minecraft.item.ItemStack> drops;
    @Shadow public boolean isSilkTouching;
    @Shadow public float dropChance;
    @Shadow public EntityPlayer harvester;

    @SuppressWarnings({"unchecked"})
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, int fortuneLevel, float dropChance,
            List<net.minecraft.item.ItemStack> drops, EntityPlayer harvester, boolean isSilkTouching,
            CallbackInfo ci) {
        this.originalDrops = ImmutableList.copyOf((List<ItemStack>) (List<?>) drops);
        if (state == null || !(harvester instanceof EntityPlayer) || !canHarvestBlock(state, harvester)
                || (state.getBlock().canSilkHarvest(world, pos, state, harvester) && EnchantmentHelper
                        .getSilkTouchModifier(harvester))) {
            this.experience = 0;
        } else {
            int bonusLevel = EnchantmentHelper.getFortuneModifier(harvester);
            this.experience = state.getBlock().getExpDrop(world, pos, bonusLevel);
        }

        this.originalExperience = this.experience;
        this.originalDropChance = dropChance;
        this.targetLocation = new Location<World>((World) world, VecHelper.toVector(pos));
    }

    @Override
    public ImmutableList<ItemStack> getOriginalItemStacks() {
        return this.originalDrops;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ItemStack> getItemStacks() {
        return (List<ItemStack>) (List<?>) this.drops;
    }

    @Override
    public Collection<ItemStack> filterItemStacks(Predicate<ItemStack> predicate) {
        Iterator<net.minecraft.item.ItemStack> iterator = this.drops.iterator();
        while (iterator.hasNext()) {
            if (!predicate.apply((ItemStack) iterator.next())) {
                iterator.remove();
            }
        }
        return getItemStacks();
    }

    @Override
    public void setItems(Collection<ItemStack> items) {
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (ItemStack itemstack : items) {
            droppedItems.add((net.minecraft.item.ItemStack) itemstack);
        }
        this.drops = droppedItems;
    }

    @Override
    public float getDropChance() {
        return this.dropChance;
    }

    @Override
    public void setDropChance(float chance) {
        this.dropChance = chance;
    }

    @Override
    public Cause getCause() {
        if (this.harvester != null) {
            return Cause.of(this.harvester);
        } else {
            return Cause.of(this.world);
        }
    }

    @Override
    public BlockSnapshot getTargetBlock() {
        return this.blockOriginal;
    }

    @Override
    public float getOriginalDropChance() {
        return this.originalDropChance;
    }

    @Override
    public int getOriginalExperience() {
        return this.originalExperience;
    }

    @Override
    public int getExperience() {
        return this.experience;
    }

    @Override
    public void setExperience(int exp) {
        this.experience = exp;
    }

    @Override
    public void syncDataToSponge(net.minecraftforge.fml.common.eventhandler.Event forgeSyncEvent) {
        super.syncDataToSponge(forgeSyncEvent);

        HarvestDropsEvent forgeEvent = (HarvestDropsEvent) forgeSyncEvent;
        getItemStacks().clear();
        for (net.minecraft.item.ItemStack itemstack : forgeEvent.drops) {
            getItemStacks().add((ItemStack) itemstack);
        }
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);

        HarvestBlockEvent event = (HarvestBlockEvent) spongeEvent;
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (ItemStack itemstack : event.getItemStacks()) {
            droppedItems.add((net.minecraft.item.ItemStack) itemstack);
        }

        this.drops = droppedItems;
    }

    private boolean canHarvestBlock(IBlockState state, EntityPlayer player) {
        Block block = state.getBlock();
        if (block.getMaterial().isToolNotRequired()) {
            return true;
        }

        net.minecraft.item.ItemStack stack = player.inventory.getCurrentItem();
        String tool = block.getHarvestTool(state);
        if (stack == null || tool == null) {
            return player.canHarvestBlock(block);
        }

        int toolLevel = stack.getItem().getHarvestLevel(stack, tool);
        if (toolLevel < 0) {
            return player.canHarvestBlock(block);
        }

        return toolLevel >= block.getHarvestLevel(state);
    }
}
