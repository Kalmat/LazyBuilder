package dev.alef.lazybuilder.playerdata;

import java.util.ArrayList;
import java.util.List;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class PlayerData implements IPlayerData {
	
	private List<Structure> overworldStructureList;
	private List<Structure> netherStructureList;
	private List<Structure> endStructureList;

	public PlayerData() {
		this.setOverworldStructureList(createStructureList(Refs.overworld));
		this.setNetherStructureList(createStructureList(Refs.the_nether));
		this.setEndStructureList(createStructureList(Refs.the_end));
	}
	
	private List<Structure> createStructureList(ResourceLocation dimension) {
		List<Structure> list = new ArrayList<Structure>();
		list.add(new Structure(dimension, Refs.BUILDING));
		list.add(new Structure(dimension, Refs.COPYPASTE));
		list.add(new Structure(dimension, Refs.DESTRUCT));
		list.add(new Structure(dimension, Refs.PROTECT));
		return list;
	}
	
	public List<Structure> getOverworldStructureList() {
		return this.overworldStructureList;
	}
	
	public void setOverworldStructureList(List<Structure> list) {
		this.overworldStructureList = list;
	}
	
	public Structure getOverworldStructure(int structType) {
		return overworldStructureList.get(structType);
	}

	public void setOverworldStructure(int structType, Structure overworldStructure) {
		this.overworldStructureList.set(structType, overworldStructure);
	}
	
	public List<Structure> getNetherStructureList() {
		return this.netherStructureList;
	}
	
	public void setNetherStructureList(List<Structure> list) {
		this.netherStructureList = list;
	}
	
    public Structure getNetherStructure(int structType) {
		return netherStructureList.get(structType);
	}

	public void setNetherStructure(int structType, Structure netherStructure) {
		this.netherStructureList.set(structType, netherStructure);
	}

	public List<Structure> getEndStructureList() {
		return this.endStructureList;
	}
	
	public void setEndStructureList(List<Structure> list) {
		this.endStructureList = list;
	}
	
	public Structure getEndStructure(int structType) {
		return endStructureList.get(structType);
	}

	public void setEndStructure(int structType, Structure endStructure) {
		this.endStructureList.set(structType, endStructure);
	}

    public Structure getStructure(ResourceLocation dimension, int structType) {

    	if (structType != Refs.EMPTY) {
	    	if (dimension == Refs.overworld) {
	    		return this.getOverworldStructure(structType);
	    	}
	    	else if (dimension == Refs.the_nether) {
	    		return this.getNetherStructure(structType);
	    	}
	    	else if (dimension == Refs.the_end) {
	    		return this.getEndStructure(structType);
	    	}
    	}
    	return Refs.EMPTY_STRUCT;
    }
    
    public void setStructure(ResourceLocation dimension, int structType, Structure struct) {

    	if (structType != Refs.EMPTY && !struct.equals(Refs.EMPTY_STRUCT)) {
	    	if (dimension == Refs.overworld) {
	    		this.setOverworldStructure(structType, struct);
	    	}
	    	else if (dimension == Refs.the_nether) {
	    		this.setNetherStructure(structType, struct);
	    	}
	    	else if (dimension == Refs.the_end) {
	    		this.setEndStructure(structType, struct);
	    	}
    	}
    }
    
    public Structure getActiveStructure(ResourceLocation dimension) {
    	
    	int structType = this.findActiveStructType(dimension);
    	
    	if (structType != Refs.EMPTY) {
			if (dimension == Refs.overworld) {
				return this.getOverworldStructure(structType);
			}
			else if (dimension == Refs.the_nether) {
				return this.getNetherStructure(structType);
			}
			else if (dimension == Refs.the_end) {
				return this.getEndStructure(structType);
			}
    	}
    	return Refs.EMPTY_STRUCT;
    }

	public int findActiveStructType(ResourceLocation dimension) {
		
		int structType = Refs.EMPTY;
		List<Structure> structList = null;
		
    	if (dimension == Refs.overworld) {
    		structList = this.getOverworldStructureList();
    	}
    	else if (dimension == Refs.the_nether) {
    		structList = this.getNetherStructureList();
    	}
    	else if (dimension == Refs.the_end) {
    		structList = this.getEndStructureList();
    	}
    	for (Structure struct : structList) {
 			if (struct.getStructType() != Refs.PROTECT && struct.isActive()) {
 				structType = struct.getStructType();
				break;
			}
		}
		return structType;
	}

	@Override
    public void copyForRespawn(IPlayerData deadPlayer) {
        this.setOverworldStructureList(deadPlayer.getOverworldStructureList());
        this.setNetherStructureList(deadPlayer.getNetherStructureList());
        this.setEndStructureList(deadPlayer.getEndStructureList());
    }
	
    public static IPlayerData getFromPlayer(PlayerEntity player) {
        return player
                .getCapability(PlayerDataProvider.LazyBuilderStateCap, null)
                .orElseThrow(()->new IllegalArgumentException("LazyOptional must be not empty!"));
    }
}
