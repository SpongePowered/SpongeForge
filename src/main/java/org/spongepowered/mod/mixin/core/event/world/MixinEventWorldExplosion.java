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
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final private net.minecraft.world.Explosion explosion;

    @Shadow public abstract net.minecraft.world.World getWorld();
    @Shadow public abstract net.minecraft.world.Explosion shadow$getExplosion();

    private Cause cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo callbackInfo) {
        if (this.world.isRemote) {
            return;
        }

        if (this.explosion.exploder == null) {
            this.cause = Cause.of(NamedCause.source(this.world.getBlockState(new BlockPos(this.explosion.getPosition()))));
        } else {
            final net.minecraft.entity.Entity entity = this.explosion.exploder;
            if (entity instanceof Projectile) {
                try {
                    final ProjectileSource source = ((Projectile) entity).getShooter();
                    this.cause = Cause.of(NamedCause.source(entity), NamedCause.of("ProjectileSource", source));
                } catch (Exception e) {
                    this.cause = Cause.of(NamedCause.source(entity));
                }
            } else {
                this.cause = Cause.of(NamedCause.source(entity));
            }
        }
    }

    @Override
    public Explosion getExplosion() {
        return (Explosion) this.explosion;
    }

    @Override
    public Cause getCause() {
        return this.cause;
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
        private ImmutableList<Transaction<BlockSnapshot>> blockTransactions;

        @Shadow @Final private List<net.minecraft.entity.Entity> entityList;

        @Inject(method = "<init>", at = @At("RETURN"))
        public void onConstructed(net.minecraft.world.World world, net.minecraft.world.Explosion explosion, List<Entity> entityList,
                CallbackInfo ci) {
            if (this.getWorld().isRemote) {
                return;
            }

            createSpongeData();
        }

        public void createSpongeData() {
            List<BlockPos> affectedPositions = this.shadow$getExplosion().getAffectedBlockPositions();
            ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<>();
            for (BlockPos pos : affectedPositions) {
                Location<World> location = new Location<>((World) this.getWorld(), VecHelper.toVector3d(pos));
                BlockSnapshot originalSnapshot =
                        ((IMixinBlockSnapshot) net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(this.getWorld(), pos))
                                .createSpongeBlockSnapshot();
                final SpongeBlockSnapshotBuilder replacementBuilder = new SpongeBlockSnapshotBuilder()
                        .blockState(BlockTypes.AIR.getDefaultState())
                        .position(location.getBlockPosition())
                    .worldId(location.getExtent().getUniqueId());
                BlockSnapshot replacementSnapshot = replacementBuilder.build();
                builder.add(new Transaction<>(originalSnapshot, replacementSnapshot)).build();
            }
            this.blockTransactions = builder.build();

            ImmutableList.Builder<EntitySnapshot> entityListBuilder = ImmutableList.builder();
            for (net.minecraft.entity.Entity entity : this.entityList) {
                entityListBuilder.add(((Entity) entity).createSnapshot());
            }
            this.entitySnapshots = entityListBuilder.build();
        }

        @Override
        public ImmutableList<Transaction<BlockSnapshot>> getTransactions() {
            return this.blockTransactions;
        }

        @Override
        public List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
            Iterator<Transaction<BlockSnapshot>> iterator = getTransactions().iterator();
            while (iterator.hasNext()) {
                Transaction<BlockSnapshot> transaction = iterator.next();
                Location<World> location = transaction.getOriginal().getLocation().get();
                if (!predicate.test(location)) {
                    transaction.setValid(false);
                }
            }
            return this.blockTransactions;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Entity> getEntities() {
            return (List<Entity>) (List<?>) this.entityList;
        }

        @Override
        public void syncDataToForge(org.spongepowered.api.event.Event spongeEvent) {
            super.syncDataToForge(spongeEvent);

            ExplosionEvent.Detonate event = (ExplosionEvent.Detonate) spongeEvent;
            List<BlockPos> affectedBlocks = this.shadow$getExplosion().getAffectedBlockPositions();
            affectedBlocks.clear();

            for (Transaction<BlockSnapshot> blockTransaction : event.getTransactions()) {
                if (blockTransaction.isValid()) {
                    affectedBlocks.add(VecHelper.toBlockPos(blockTransaction.getFinal().getPosition()));
                }
            }
        }

        @Override
        public void syncDataToSponge(org.spongepowered.api.event.Event spongeEvent) {
            super.syncDataToSponge(spongeEvent);

            final ExplosionEvent.Detonate detonate = (ExplosionEvent.Detonate) spongeEvent;
            detonate.getTransactions().forEach(transaction -> {
                BlockPos pos = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                boolean match = false;
                for (BlockPos forgePos : this.shadow$getExplosion().getAffectedBlockPositions()) {
                    if (forgePos.getX() == pos.getX() && forgePos.getY() == pos.getY() && forgePos.getZ() == pos.getZ()) {
                        match = true;
                    }
                }
                if (!match) {
                    transaction.setValid(false);
                }
            });
        }

        private boolean cancelled = false;

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void postProcess() {
            if (this.cancelled) {
                this.entityList.clear();
                this.shadow$getExplosion().clearAffectedBlockPositions();
            }
        }
    }

}
