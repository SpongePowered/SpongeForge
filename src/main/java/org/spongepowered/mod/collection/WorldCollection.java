package org.spongepowered.mod.collection;

import org.spongepowered.api.world.World;

import java.util.ArrayList;

/**
 * Created by thomas on 25/09/14.
 */
public class WorldCollection extends ArrayList<World> {

    public WorldCollection(World[] worlds){
        for (World world : worlds)
            add(world);
    }

}
