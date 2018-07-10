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
package org.spongepowered.mod.mixin.core.forge.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import javax.annotation.Nonnull;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class MixinForgeHooks {

    /**
     * @author blood - February 18th, 2017
     * @reason Force Forge's RightClickItem onto event bus to bypass our event handling.
     *   We handle InteractItemEvent.Secondary for plugins in PacketUtil which occurs before this event.
     * @param player The player
     * @param hand The hand used
     */
    @Overwrite
    public static EnumActionResult onItemRightClick(EntityPlayer player, EnumHand hand) {
        // Force Forge's RightClickItem onto event bus to bypass our event handling.
        // We handle InteractItemEvent.Secondary in PacketUtil which occurs before this event.
        PlayerInteractEvent.RightClickItem evt = new PlayerInteractEvent.RightClickItem(player, hand);
        final boolean post = ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(evt, true);
        return post ? evt.getCancellationResult() : null;
    }

    /**
     * @author gabizou - May 24th, 2018
     * @reason Add procussive {@link ChangeBlockEvent.Pre} events to be thrown here
     * as mods can use this hook to check if a block can be broken. Specifically
     * however, this does not interact with {@link ForgeEventFactory#doPlayerHarvestCheck(EntityPlayer, IBlockState, boolean)}
     * since that can potentially be a byproduct of {@link EntityPlayer#canHarvestBlock(IBlockState)}.
     * This assures that Sponge will be able to at the very least interact with the event pre-emptively
     * if some hook elsewhere fails.
     *
     * @param block
     * @param player
     * @param world
     * @param pos
     * @return
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static boolean canHarvestBlock(@Nonnull Block block, @Nonnull EntityPlayer player, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        if (state.getMaterial().isToolNotRequired())
        {
            return true;
        }

        ItemStack stack = player.getHeldItemMainhand();
        String tool = block.getHarvestTool(state);
        if (stack.isEmpty() || tool == null)
        {
            return player.canHarvestBlock(state);
        }

        // Sponge Start - Add the changeblockevent.pre check here before we bother with item stacks.
        if (world instanceof IMixinWorldServer && !((IMixinWorld) world).isFake() && SpongeImplHooks.isMainThread()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                // Might as well provide the active item in use.
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(player.getActiveItemStack()));
                if (SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) world, pos, player).isCancelled()) {
                    // Since a plugin cancelled it, go ahead and cancel it.
                    return false;
                }
            }
        }
        // Sponge End

        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0)
        {
            return player.canHarvestBlock(state);
        }

        return toolLevel >= block.getHarvestLevel(state);
    }
}
