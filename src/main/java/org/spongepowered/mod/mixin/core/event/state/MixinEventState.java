package org.spongepowered.mod.mixin.core.event.state;

import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;

@NonnullByDefault
@Mixin(FMLStateEvent.class)
public abstract class MixinEventState extends FMLEvent {

    public Game getGame() {
        return SpongeMod.instance.getGame();
    }
}
