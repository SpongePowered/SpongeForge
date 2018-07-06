package org.spongepowered.mod.test;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Random;

@Mod(modid = GrassCreator.MOD_ID, name = "Grass Swapper Test", acceptableRemoteVersions = "*")
public class GrassCreator {

    public static final String MOD_ID = "grassswapper";
    private static GrassCreator INSTANCE;
    private boolean areListenersEnabled = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, this);
        Sponge.getCommandManager().register(this, getCommand(), "grassToSponge");
    }

    private static CommandCallable getCommand() {
        return CommandSpec.builder()
            .description(Text.of(TextColors.BLUE, "Toggles turning tall grass population to sponges"))
            .executor((src, args) -> {
                INSTANCE.areListenersEnabled = !INSTANCE.areListenersEnabled;
                return CommandResult.success();
            })
            .build();
    }

    @SubscribeEvent
    public void onGrassPlacement(DecorateBiomeEvent.Decorate event) {
        if (this.areListenersEnabled && event.getType() == DecorateBiomeEvent.Decorate.EventType.GRASS) {
            final World world = event.getWorld();
            final Random random = event.getRand();
            final int x = random.nextInt(16) + 8;
            final int z = random.nextInt(16) + 8;
            final BlockPos pos = world.getHeight(event.getPos().add(x, 0, z));
            event.setResult(Event.Result.DENY);
            event.setResult(Event.Result.DENY);
            BlockPos origin = pos;
            final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(origin);

            // Find starting point
            for (IBlockState state = world.getBlockState(mutPos);
                 (state.getBlock().isAir(state, world, mutPos) || state.getBlock().isLeaves(state, world, mutPos)) && mutPos.getY() > 0;
                 state = world.getBlockState(mutPos)) {
                mutPos.setPos(mutPos.getX(), mutPos.getY() - 1, mutPos.getZ());
            }

            // Place randomly around origin
            origin = new BlockPos(mutPos);

            for (int i = 0; i < 128; i++) {
                final BlockPos targetPos =
                    origin.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

                final IBlockState existingState = world.getBlockState(targetPos);

                if (existingState.getBlock().isAir(existingState, world, targetPos)) {
                    world.setBlockState(targetPos, Blocks.SPONGE.getDefaultState(), 3);

                }
            }

        }
    }

}
