package dev.alef.lazybuilder.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class Undo {
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private World WORLD;
    private PlayerEntity PLAYER;
    private List<Object> ACTIONS = new ArrayList<Object>();
    
    public boolean UNDO_ACTION_SERVER = false;
    public boolean UNDO_ACTION_CLIENT = false;

	public Undo(World worldIn, PlayerEntity player) {
		WORLD = worldIn;
		PLAYER = player;
	}

	public World getWorld() {
		return WORLD;
	}

	public PlayerEntity getPlayer() {
		return PLAYER;
	}

	public void addAction(Object action) {
		if (ACTIONS.size() >= Refs.maxUndoActions) {
			ACTIONS.remove(0);
		}
		ACTIONS.add(action);
	}
	
	public Object getActionByIndex(int index) {
		if (index < ACTIONS.size()) {
			return ACTIONS.get(index);
		}
		return null;
	}
	
	public Object getLastAction() {
		
		if (ACTIONS.size() > 0) {
			Object lastAction = ACTIONS.get(ACTIONS.size()-1);
			return lastAction;
		}
		else {
			return null;
		}
	}
	
	public List<Object> getActionList() {
		return ACTIONS;
	}
	
	public int getActionListSize() {
		return ACTIONS.size();
	}
	
	public boolean deleteAction(int index) {
		if (index < ACTIONS.size()) {
			ACTIONS.remove(index);
			return true;
		}
		return false;
	}
	
	public boolean deleteLastAction(World worldIn, PlayerEntity player) {
		
		if (ACTIONS.size() > 0) {
			ACTIONS.remove(ACTIONS.size()-1);
			return true;
		}
		else return false;
	}
	
	public void deleteActionList() {
		ACTIONS.clear();
	}
}
