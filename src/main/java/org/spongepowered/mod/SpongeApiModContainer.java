package org.spongepowered.mod;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

public class SpongeApiModContainer extends DummyModContainer {

    public static PluginContainer instance;

    public SpongeApiModContainer() {
        super(new ModMetadata());

        ModMetadata md = getMetadata();
        md.modId = SpongeImpl.API_ID;
        md.name = SpongeImpl.API_NAME;
        md.version = SpongeImpl.API_VERSION.orElse("");

        instance = (PluginContainer) this;
    }

    // Implement methods in PluginContainer

    public Optional<String> getMinecraftVersion() {
        // Return the same Minecraft version as returned by the implementation
        return SpongeImpl.getPlugin().getMinecraftVersion();
    }

    public Logger getLogger() {
        return SpongeImpl.getSlf4jLogger();
    }

    @Override
    public Object getMod() {
        return SpongeImpl.getGame();
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

}
