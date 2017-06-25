package org.spongepowered.mod.mixin.core.item.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.mod.item.inventory.fabric.InvWrapperFabric;

import java.util.ArrayList;
import java.util.List;

@Mixin(InvWrapper.class)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class MixinInvWrapper implements MinecraftInventoryAdapter, IMixinInventory {

    protected EmptyInventory empty;
    protected Inventory parent;
    protected Inventory next;
    protected SlotCollection slots;
    protected List<Inventory> children = new ArrayList<Inventory>();
    protected Iterable<Slot> slotIterator;
    private Fabric<InvWrapper> fabric;
    protected Lens<IInventory, ItemStack> lens = null;

    private List<SlotTransaction> capturedTransactions = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        System.out.println("Constructed InvWrapper");
        this.fabric = new InvWrapperFabric(((InvWrapper)(Object) this));
        this.slots = new SlotCollection.Builder().add(this.fabric.getSize()).build();
        this.lens = new OrderedInventoryLensImpl(0, this.fabric.getSize(), 1, slots);
    }

    @Override
    public Inventory parent() {
        return this.parent == null ? this : this.parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T first() {
        return (T) this.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T next() {
        return (T) this.emptyInventory(); // TODO implement me
    }

    protected final EmptyInventory emptyInventory() {
        if (this.empty == null) {
            this.empty = new EmptyInventoryImpl(this);
        }
        return this.empty;
    }

    @Override
    public SlotProvider<IInventory, ItemStack> getSlotProvider() {
        return this.slots;
    }

    @Override
    public Inventory getChild(int index) {
        if (index < 0 || index >= this.getRootLens().getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.children.size()) {
            this.children.add(null);
        }
        Inventory child = this.children.get(index);
        if (child == null) {
            child = this.getRootLens().getChildren().get(index).getAdapter(this.getInventory(), this);
            this.children.set(index, child);
        }
        return child;
    }

    // TODO getChild with lens not implemented

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.slotIterator == null) {
            this.slotIterator = this.slots.getIterator(this);
        }
        return (Iterable<T>) this.slotIterator;
    }

    @Intrinsic
    public void inventory$clear() {
        this.getInventory().clear();
    }

    public Lens<IInventory, ItemStack> getRootLens() {
        return this.lens;
    }

    public Fabric<IInventory> getInventory() {
        return ((Fabric) this.fabric);
    }

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedTransactions;
    }


}
