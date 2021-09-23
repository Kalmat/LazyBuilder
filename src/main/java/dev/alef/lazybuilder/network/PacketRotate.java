package dev.alef.lazybuilder.network;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.LazyBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRotate {
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private final UUID player;
    private final BlockPos type;

    public PacketRotate(PacketBuffer buf) {
        type = buf.readBlockPos();
    	player = buf.readUniqueId();
    }

    public PacketRotate(BlockPos type, UUID player) {
        this.type = type;
        this.player = player;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(type);
    	buf.writeUniqueId(player);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            		PlayerEntity player = ctx.get().getSender();
            		LazyBuilder.rotateClipboard(player);
        });
        return true;
    }

}
