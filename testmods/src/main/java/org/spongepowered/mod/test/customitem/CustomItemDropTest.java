package org.spongepowered.mod.test.customitem;

import com.google.common.eventbus.Subscribe;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "customitemtest", name = "Custom Item Test", version = "0.0.1")
public class CustomItemDropTest {


    @Subscribe
    public void onEntityDrop(LivingDropsEvent)

}
