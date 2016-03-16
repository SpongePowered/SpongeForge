package org.spongepowered.example;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = ExamplePlugin.ID, name = ExamplePlugin.NAME, version = ExamplePlugin.VERSION)
public class ExamplePlugin {

    public static final String ID = "ExamplePlugin";
    public static final String NAME = "Example Sponge Plugin";
    public static final String VERSION = "1.0";

    @Inject private Logger logger;

    @Listener
    public void onInit(GameInitializationEvent event) {
        this.logger.info("Doing initialization!");
        // Do one-time initialization here
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.logger.debug("So you want to start a server?");
        CommandSpec exampleCommand = CommandSpec.builder()
                .executor((src, args) -> {
                    src.sendMessage(Text.of("Hello World"));
                    return CommandResult.success();
                })
                .build();
        Sponge.getCommandManager().register(this, exampleCommand, "example");
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        this.logger.debug("Bye for now...");
    }

    @Listener
    public void onShutdown(GameStoppingEvent event) {
        this.logger.debug("OK I quit!");
    }

}
