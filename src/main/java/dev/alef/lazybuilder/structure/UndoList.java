package dev.alef.lazybuilder.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class UndoList {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private List<Undo> EVENTS_LIST = new ArrayList<Undo>();

	public UndoList() {
	}
	
	public boolean add(World worldIn, PlayerEntity player) {
		EVENTS_LIST.add(new Undo(worldIn, player));
		return true;
	}
	
	public Undo get(World worldIn, PlayerEntity player) {
		
		for (int i = 0; i < EVENTS_LIST.size(); ++i) {
			if (EVENTS_LIST.get(i).getWorld().equals(worldIn) && EVENTS_LIST.get(i).getPlayer().equals(player)) {
				return EVENTS_LIST.get(i);
			}
		}
		return null;
	}
	
	public int getSize() {
		return EVENTS_LIST.size();
	}
	
	public boolean delete(World worldIn, PlayerEntity player) {
		
		for (int i = 0; i < EVENTS_LIST.size(); ++i) {
			if (EVENTS_LIST.get(i).getWorld().equals(worldIn) && EVENTS_LIST.get(i).getPlayer().equals(player)) {
				EVENTS_LIST.remove(i);
				return true;
			}
		}
		return false;
	}
}

