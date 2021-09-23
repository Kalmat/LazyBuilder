package dev.alef.lazybuilder.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class UndoList {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private List<Undo> eventsList = new ArrayList<Undo>();

	public UndoList() {
	}
	
	public void create(World worldIn, PlayerEntity player) {
		
		for (ResourceLocation dimension : Refs.dimensionList) {
			this.add(worldIn, dimension, player);
		}
	}
	
	public boolean add(World worldIn, ResourceLocation dimension, PlayerEntity player) {
		if (this.get(worldIn, dimension, player) == null) {
			return this.eventsList.add(new Undo(worldIn, dimension, player));
		}
		return false;
	}
	
	public Undo get(World worldIn, ResourceLocation dimension, PlayerEntity player) {
		
		for (int i = 0; i < this.eventsList.size(); ++i) {

			Undo undo = this.eventsList.get(i);

			if (worldIn.toString().equals(undo.getWorld().toString()) && dimension.equals(undo.getDimension()) && player.getUniqueID().equals(undo.getPlayerUUID())) {
				return undo;
			}
		}
		return null;
	}
	
	public int getSize() {
		return this.eventsList.size();
	}
	
	public boolean delete(World worldIn, PlayerEntity player) {
		
		boolean deleted = false;
		
		for (int i = 0; i < this.eventsList.size(); ++i) {

			Undo undo = this.eventsList.get(i);

			if (worldIn.toString().equals(undo.getWorld().toString()) && player.getUniqueID().equals(undo.getPlayerUUID())) {
				this.eventsList.remove(i);
				deleted = true;
			}
		}
		return deleted;
	}
	
	public boolean deleteAll(PlayerEntity player) {
		
		boolean deleted = false;
		
		for (int i = 0; i < this.eventsList.size(); ++i) {

			Undo undo = this.eventsList.get(i);

			if (player.getUniqueID().equals(undo.getPlayerUUID())) {
				this.eventsList.remove(i);
				deleted = true;
			}
		}
		return deleted;
	}
}

