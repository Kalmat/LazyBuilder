package dev.alef.lazybuilder.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.render.LazyBuilderRender;


public class PacketText {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private final int lines;
    private final List<String> msg;
    private final int time;

    public PacketText(PacketBuffer buf) {
        this.time = buf.readInt();
    	this.lines = buf.readInt();
        this.msg = new ArrayList<String>();
    	for (int i = 0; i < this.lines; ++i) {
            msg.add(buf.readString());
    	}
    }

    public PacketText(int time, int lines, List<String> msg) {
        this.time = time;
        this.lines = lines;
        this.msg = new ArrayList<String>(msg);
    }

    public void toBytes(PacketBuffer buf) {
    	buf.writeInt(this.time);
    	buf.writeInt(this.lines);
    	for (int i = 0; i < this.lines; ++i) {
    		buf.writeString(this.msg.get(i));
    	}
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
        	LazyBuilderRender.setTextActive(this.msg, this.time);
        });
        return true;
    }
}
