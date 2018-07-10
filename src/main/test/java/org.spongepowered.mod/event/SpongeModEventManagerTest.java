package org.spongepowered.mod.event;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeEventManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Loader.class)
public class SpongeModEventManagerTest {

    private EventManager eventManager;
    private Object plugin;
    private PluginContainer container;

    @Before
    public void init() throws Exception {
        PluginManager manager = Mockito.mock(PluginManager.class);
        this.eventManager = new SpongeEventManager(manager);

        this.plugin = new Object();
        this.container = Mockito.mock(PluginContainer.class);
        Mockito.when(manager.fromInstance(plugin)).thenReturn(Optional.of(this.container));

        this.resetStatics();
    }

    private void resetStatics() throws IllegalAccessException {
        for (Field field: ShouldFire.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                field.set(null, false);
            }
        }
    }

    @Test
    public void testSpawn() throws ClassNotFoundException {
        SpawnListener listener = new SpawnListener();

        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNKLOAD);
        this.eventManager.registerListeners(this.plugin, listener);
        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNKLOAD);

        this.eventManager.unregisterListeners(listener);
        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNKLOAD);

        ForgeSpawnEventListener forgeListener = new ForgeSpawnEventListener();
        EventBus eventBus = new EventBus();

        eventBus.register(forgeListener);

        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
    }

    private static class SpawnListener {

        @Listener
        public void onSpawn(SpawnEntityEvent event) {}
    }

    private static class SubListener {

        @Listener
        public void onCustom(SpawnEntityEvent.Custom event) {}
    }

    private static class ForgeSpawnEventListener {

        @SubscribeEvent
        public void onSpawn(EntityJoinWorldEvent event) {

        }

    }

}
