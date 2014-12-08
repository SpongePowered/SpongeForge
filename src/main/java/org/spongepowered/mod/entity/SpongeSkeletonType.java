package org.spongepowered.mod.entity;

import org.spongepowered.api.entity.living.meta.SkeletonType;

public class SpongeSkeletonType implements SkeletonType {

    public final int type;
    public final String name;

    public SpongeSkeletonType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
