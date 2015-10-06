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
package org.spongepowered.mod.mixin.core.event.world;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinBlockSnapshot;
import org.spongepowered.mod.mixin.core.fml.common.eventhandler.MixinEvent;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = net.minecraftforge.event.world.ExplosionEvent.class, remap = false)
public abstract class MixinEventWorldExplosion extends MixinEvent implements ExplosionEvent {

    @Shadow public net.minecraft.world.World world;
    @Shadow public net.minecraft.world.Explosion explosion;

    @Override
    public Explosion getExplosion() {
        return (Explosion) this.explosion;
    }

    @Override
    public Cause getCause() {
        if (this.explosion.exploder != null) {
            return Cause.of(this.explosion.exploder);
        } else {
            IBlockState state = this.world.getBlockState(new BlockPos(this.explosion.getPosition()));
            return Cause.of(state.getBlock());
        }
    }

    @Override
    public World getTargetWorld() {
        return (World) this.world;
    }

    @Mixin(value = net.minecraftforge.event.world.ExplosionEvent.Start.class, remap = false)
    static abstract class Pre extends MixinEventWorldExplosion implements ExplosionEvent.Pre {

    }

    @Mixin(value = net.minecraftforge.event.world.ExplosionEvent.Detonate.class, remap = false)
    static abstract class Detonate extends MixinEventWorldExplosion implements ExplosionEvent.Detonate {

        private ImmutableList<EntitySnapshot> entitySnapshots;
        private ImmutableList<BlockTransaction> blockTransactions;

        @Shadow private List<net.minecraft.entity.Entity> entityList;

        @Inject(method = "<init>", at = @At("RETURN"))
        public void onConstructed(net.minecraft.world.World world, net.minecraft.world.Explosion explosion, List<Entity> entityList, CallbackInfo ci) {
            createSpongeData();
        }

        @SuppressWarnings("unchecked")
        public void createSpongeData() {
            List<BlockPos> affectedPositions = this.explosion.func_180343_e();
            ImmutableList.Builder<BlockTransaction> builder = new ImmutableList.Builder<BlockTransaction>();
            for (BlockPos pos : affectedPositions) {
                Location<World> location = new Location<World>((World) this.world, VecHelper.toVector(pos));
                BlockSnapshot originalSnapshot = ((IMixinBlockSnapshot) net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(this.world, pos)).createSpongeBlockSnapshot();
                final SpongeBlockSnapshotBuilder replacementBuilder = new SpongeBlockSnapshotBuilder()
                    .blockState(BlockTypes.AIR.getDefaultState())
                    .position(location.getBlockPosition())
                    .worldId(location.getExtent().getUniqueId());
                BlockSnapshot replacementSnapshot = replacementBuilder.build();
                builder.add(new BlockTransaction(originalSnapshot, replacementSnapshot)).build();
            }
            this.blockTransactions = builder.build();
        }

        @Override
        public ImmutableList<BlockTransaction> getTransactions() {
            return this.blockTransactions;
        }

        @Override
        public List<BlockTransaction> filter(Predicate<Location<World>> predicate) {
            Iterator<BlockTransaction> iterator = getTransactions().iterator();
            while (iterator.hasNext()) {
                BlockTransaction transaction = iterator.next();
                Location<World> location = transaction.getOriginal().getLocation().get();
                if (!predicate.test(location)) {
                    transaction.setIsValid(false);
                }
            }
            return this.blockTransactions;
        }

        @Override
        public ImmutableList<EntitySnapshot> getEntitySnapshots() {
            return this.entitySnapshots;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Entity> getEntities() {
            return (List<Entity>) (List<?>) this.entityList;
        }

        @Override
        public List<? extends Entity> filterEntityLocations(Predicate<Location<World>> predicate) {
            if (((net.minecraftforge.event.world.ExplosionEvent.Detonate) (Object) this).isCancelable()) {
                Iterator<? extends Entity> iterator = this.getEntities().iterator();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    Location<World> location = entity.getLocation();
                    if (!predicate.test(location)) {
                        iterator.remove();
                    }
                }
            }
            return this.getEntities();
        }

        @Override
        public List<Entity> filterEntities(Predicate<Entity> predicate) {
            if (((net.minecraftforge.event.world.ExplosionEvent.Detonate) (Object) this).isCancelable()) {
                Iterator<? extends Entity> iterator = this.getEntities().iterator();
                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();
                    if (!predicate.test(entity)) {
                        iterator.remove();
                    }
                }
            }
            return this.getEntities();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void syncDataToForge(org.spongepowered.api.event.Event spongeEvent) {
            super.syncDataToForge(spongeEvent);

            ExplosionEvent.Detonate event = (ExplosionEvent.Detonate) spongeEvent;
            List<BlockPos> affectedBlocks = this.explosion.func_180343_e();
            affectedBlocks.clear();

            for (BlockTransaction blockTransaction : event.getTransactions()) {
                if (blockTransaction.isValid()) {
                    affectedBlocks.add(VecHelper.toBlockPos(blockTransaction.getFinalReplacement().getPosition()));
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void syncDataToSponge(Event forgeEvent) {
            super.syncDataToSponge(forgeEvent);

            net.minecraftforge.event.world.ExplosionEvent event = (net.minecraftforge.event.world.ExplosionEvent) forgeEvent;
            // TODO - handle this better
            List<BlockPos> affectedBlocks = event.explosion.func_180343_e();
            for (BlockTransaction transaction : this.blockTransactions) {
                BlockPos pos = VecHelper.toBlockPos(transaction.getFinalReplacement().getPosition());
                boolean match = false;
                for (BlockPos forgePos : affectedBlocks) {
                    if (forgePos.getX() == pos.getX() && forgePos.getY() == pos.getY() && forgePos.getZ() == pos.getZ()) {
                        match = true;
                    }
                }
                if (!match) {
                    transaction.setIsValid(false);
                }
            }
        }
    }

}
