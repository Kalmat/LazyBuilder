package dev.alef.lazybuilder.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class StructureList {
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private List<Structure> STRUCTURE_LIST = new ArrayList<Structure>();

	public StructureList() {
	}
	
	public boolean add(World worldIn, PlayerEntity player) {
		STRUCTURE_LIST.add(new Structure(worldIn, player));
		
		return true;
	}
	
	public Structure get(World worldIn, PlayerEntity player) {
		
		Structure structure = null;

		for (int i = 0; i < STRUCTURE_LIST.size(); ++i) {
			structure = STRUCTURE_LIST.get(i);
			if (structure.getWorld().equals(worldIn) && structure.getPlayer().equals(player)) {
				return STRUCTURE_LIST.get(i);
			}
		}
		return null;
	}
	
	public List<Structure> getList() {
		
		return STRUCTURE_LIST;
	}
	
	public void delete(World worldIn, PlayerEntity player) {
		
		Structure structure = null;

		for (int i = 0; i < STRUCTURE_LIST.size(); ++i) {
			structure = STRUCTURE_LIST.get(i);
			if (structure.getWorld().equals(worldIn) && structure.getPlayer().equals(player)) {
				STRUCTURE_LIST.get(i).deleteStructure();
				STRUCTURE_LIST.get(i).deleteClipBoard();
				STRUCTURE_LIST.remove(i);
				break;
			}
		}
	}
}

