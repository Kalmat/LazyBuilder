package dev.alef.lazybuilder.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class Undo {
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private World currentWorld;
	private ResourceLocation currentDimension;
    private PlayerEntity currentPlayer;
    private UUID playerUUID;
    private List<Object> actionsList = new ArrayList<Object>();
    private List<Object> reActionsList = new ArrayList<Object>();
    
	public Undo(World worldIn, ResourceLocation dimension, PlayerEntity player) {
		this.currentWorld = worldIn;
		this.currentDimension = dimension;
		this.currentPlayer = player;
		this.playerUUID = player.getUniqueID();
	}

	public World getWorld() {
		return this.currentWorld;
	}

	public ResourceLocation getDimension() {
		return this.currentDimension;
	}

	public PlayerEntity getPlayer() {
		return this.currentPlayer;
	}

	public UUID getPlayerUUID() {
		return this.playerUUID;
	}

	public void addAction(Object action) {
		if (this.actionsList.size() >= Refs.maxUndoActions) {
			this.actionsList.remove(0);
		}
		this.actionsList.add(action);
	}
	
	public void addReAction(Object action) {
		if (this.reActionsList.size() >= Refs.maxUndoActions) {
			this.reActionsList.remove(0);
		}
		this.reActionsList.add(action);
	}
	
	public List<Object> getActionList() {
		return this.actionsList;
	}
	
	public List<Object> getReActionList() {
		return this.reActionsList;
	}
	
	public int getActionListSize() {
		return this.actionsList.size();
	}
	
	public int getReActionListSize() {
		return this.reActionsList.size();
	}
	
	public Object getLastAction() {
		
		if (this.actionsList.size() > 0) {
			Object lastAction = this.actionsList.get(this.actionsList.size()-1);
			return lastAction;
		}
		else {
			return null;
		}
	}
	
	public Object getLastReAction() {
		
		if (this.reActionsList.size() > 0) {
			Object lastReAction = this.reActionsList.get(this.reActionsList.size()-1);
			return lastReAction;
		}
		else {
			return null;
		}
	}
	
	public boolean deleteLastAction(World worldIn, PlayerEntity player) {
		
		if (this.actionsList.size() > 0) {
			this.addReAction(this.getLastAction());
			this.actionsList.remove(this.actionsList.size()-1);
			return true;
		}
		else return false;
	}
	
	public boolean deleteLastReAction(World worldIn, PlayerEntity player) {
		
		if (this.reActionsList.size() > 0) {
			this.addAction(this.getLastReAction());
			this.reActionsList.remove(this.reActionsList.size()-1);
			return true;
		}
		else return false;
	}
}
