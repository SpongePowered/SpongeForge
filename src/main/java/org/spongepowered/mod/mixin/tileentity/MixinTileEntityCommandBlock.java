package org.spongepowered.mod.mixin.tileentity;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntityCommandBlock.class)
public class MixinTileEntityCommandBlock extends TileEntity {
	
	private CommandBlockLogic cbl;
	
	@Inject(method="<init>", at = { @At("RETURN") })
	public void onConstructed(CallbackInfo ci){
		cbl = new CommandBlockLogic()
	    {
	        private static final String __OBFID = "CL_00000348";
	        public BlockPos getPosition()
	        {
	            return TileEntityCommandBlock.this.pos;
	        }
	        public Vec3 getPositionVector()
	        {
	            return new Vec3((double)TileEntityCommandBlock.this.pos.getX() + 0.5D, (double)TileEntityCommandBlock.this.pos.getY() + 0.5D, (double)TileEntityCommandBlock.this.pos.getZ() + 0.5D);
	        }
	        public World getEntityWorld()
	        {
	            return TileEntityCommandBlock.this.getWorld();
	        }
	        /**
	         * Sets the command.
	         */
	        public void setCommand(String p_145752_1_)
	        {
	            super.setCommand(p_145752_1_);
	            TileEntityCommandBlock.this.markDirty();
	        }
	        public void func_145756_e()
	        {
	            TileEntityCommandBlock.this.getWorld().markBlockForUpdate(TileEntityCommandBlock.this.pos);
	        }
	        @SideOnly(Side.CLIENT)
	        public int func_145751_f()
	        {
	            return 0;
	        }
	        @SideOnly(Side.CLIENT)
	        public void func_145757_a(ByteBuf p_145757_1_)
	        {
	            p_145757_1_.writeInt(TileEntityCommandBlock.this.pos.getX());
	            p_145757_1_.writeInt(TileEntityCommandBlock.this.pos.getY());
	            p_145757_1_.writeInt(TileEntityCommandBlock.this.pos.getZ());
	        }
	        public Entity getCommandSenderEntity()
	        {
	            return null;
	        }
	        /*public void sendMessage(String... strings){
	        	//Do nothing
	        }
	        public void sendMessage(Message... messages){
	        	//Do nothing
	        }
	        public void sendMessage(Iterable<Message> messages){
	        	//Do nothing
	        }*/
	    };
	}
    
    @Overwrite
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        this.cbl.writeDataToNBT(compound);
    }
    
    @Overwrite
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.cbl.readDataFromNBT(compound);
    }
    
    @Overwrite
    public CommandBlockLogic getCommandBlockLogic()
    {
        return this.cbl;
    }
    
    @Overwrite
    public CommandResultStats func_175124_c()
    {
        return this.cbl.func_175572_n();
    }
}
