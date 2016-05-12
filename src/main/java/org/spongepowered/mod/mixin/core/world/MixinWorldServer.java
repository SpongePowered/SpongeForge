package org.spongepowered.mod.mixin.core.world;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.mod.world.gen.SpongeChunkGeneratorForge;

@Mixin(value = WorldServer.class, priority = 1001)
public abstract class MixinWorldServer implements World, IMixinWorldServer {
    @Shadow @Final public WorldProvider worldProvider;

    @Override
    public void setDimensionId(int dimensionId) {
        throw new IllegalStateException("Sponge implementation should never set the dimension id, only Forge should!");
    }

    @Override
    public Integer getDimensionId() {
        return this.worldProvider.getDimension();
    }

    @Override
    public SpongeChunkGenerator createChunkGenerator(SpongeWorldGenerator newGenerator) {
        return new SpongeChunkGeneratorForge((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
    }
}
