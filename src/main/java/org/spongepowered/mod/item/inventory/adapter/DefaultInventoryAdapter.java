package org.spongepowered.mod.item.inventory.adapter;

import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;

public interface DefaultInventoryAdapter extends InventoryAdapter {
    @Override
    default SlotProvider bridge$getSlotProvider() {
        return ReusableLens.defaultReusableLens(this).getSlots();
    }

    @Override
    default Lens bridge$getRootLens() {
        return ReusableLens.defaultReusableLens(this).getLens();
    }

    @Override
    default Fabric bridge$getFabric() {
        return (Fabric) this;
    }


}
