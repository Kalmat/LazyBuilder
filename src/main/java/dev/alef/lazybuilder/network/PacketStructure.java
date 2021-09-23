package dev.alef.lazybuilder.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.render.LazyBuilderRender;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketStructure {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private final CompoundNBT structure;

    public PacketStructure(PacketBuffer buf) {
    	this.structure = buf.readCompoundTag();
    }

    public PacketStructure(CompoundNBT structure) {
        this.structure = structure;
    }

    public void toBytes(PacketBuffer buf) {
    	buf.writeCompoundTag(this.structure);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
        	LazyBuilderRender.netSetClientStructure(this.structure);
        });
        return true;
    }
}
