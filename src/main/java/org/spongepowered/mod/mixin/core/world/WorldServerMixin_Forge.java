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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.mod.world.gen.SpongeChunkGeneratorForge;

@Mixin(value = WorldServer.class, priority = 1001)
public abstract class WorldServerMixin_Forge extends WorldMixin_Forge implements WorldServerBridge {

    @Override
    public int bridge$getDimensionId() {
        return this.provider.getDimension();
    }

    @Override
    protected void forgeImpl$UseComparatorOutputLevel(final World world, final BlockPos pos, final Block blockIn, final BlockPos samePos) {
        if (!((WorldBridge) this).bridge$isFake()) {
            if (PhaseTracker.getInstance().getCurrentState().isRestoring()) {
                return;
            }
        }
        this.updateComparatorOutputLevel(pos, blockIn);
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/DimensionManager;setWorld(ILnet/minecraft/world/WorldServer;Lnet/minecraft/server/MinecraftServer;)V",
            remap = false
        )
    )
    private void redirectSetWorld(final int id, final WorldServer world, final MinecraftServer server) {
        // Handled by WorldManager
    }

    /**
     * @author gabizou - May 23rd, 2018
     * @reason - Even though Dedicated server does handle this change, I'm inlining the
     * block check for the player since
     * @param server
     * @param worldIn
     * @param pos
     * @param playerIn The player
     * @return True if the event is cancelled, meaning the block is protected
     */
    @Redirect(
        method = "canMineBlockBody",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;isBlockProtected(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;)Z"
        )
    )
    private boolean isSpongeBlockProtected(
        final MinecraftServer server, final net.minecraft.world.World worldIn, final BlockPos pos, final EntityPlayer playerIn) {
        if (server.isBlockProtected(worldIn, pos, playerIn)) {
            return true;
        }
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && !((WorldBridge) this).bridge$isFake() && SpongeImplHooks.isMainThread()) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                // Might as well provide the active item in use.
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(playerIn.getActiveItemStack()));
                return SpongeCommonEventFactory.callChangeBlockEventPre(this, pos, playerIn).isCancelled();
            }
        }
        return false;
    }

    /**
     * @author gabizou - May 23rd, 2018
     * @reason - Since Forge adds the override to check for world providers whether
     * a block is modifiable, we have to provide the same contract to call "super".
     *
     * <p>Note: The event thrown here MUST be inverted for the {@link Cancellable#isCancelled()}
     * check, because if the block is modifiable, then the event should not be cancelled; however,
     * if the event is cancelled, then the block is not modifiable.
     * </p>
     * @param player
     * @param pos
     * @return True if the block is modifiable, or if the event is not cancelled
     */
    @Overwrite
    @Override
    public boolean isBlockModifiable(final EntityPlayer player, final BlockPos pos) {
        if (super.isBlockModifiable(player, pos)) {
            return true;
        }
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && !((WorldBridge) this).bridge$isFake() && SpongeImplHooks.isMainThread()) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                // Might as well provide the active item in use.
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(player.getActiveItemStack()));
                return !SpongeCommonEventFactory.callChangeBlockEventPre(this, pos, player).isCancelled();
            }
        }
        return false;
    }

    @Override
    public void bridge$setProviderGenerator(final SpongeChunkGenerator newGenerator) {
        // We don't want to override the provider's generator.
    }

    @Override
    public SpongeChunkGenerator bridge$createChunkGenerator(final SpongeWorldGenerator newGenerator) {
        return new SpongeChunkGeneratorForge((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
    }

    @Override
    int vanillaImpl$updateRainTimeStart(final int newRainTime) {
        if (!((WorldBridge) this).bridge$isFake()) {
            if (this.worldInfo.getRainTime() - 1 != newRainTime) {
                this.bridge$setWeatherStartTime(this.getTotalWorldTime());
            }
        }
        return newRainTime;
    }

    @Override
    int vanillaImpl$updateThunderTimeStart(final int newThunderTime) {
        if (!((WorldBridge) this).bridge$isFake()) {
            if (this.worldInfo.getThunderTime() - 1 != newThunderTime) {
                this.bridge$setWeatherStartTime(this.getTotalWorldTime());
            }
        }
        return newThunderTime;
    }


}
