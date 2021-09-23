package dev.alef.lazybuilder.playerdata;

import java.util.List;

import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.util.ResourceLocation;

public interface IPlayerData {
	
	public List<Structure> getOverworldStructureList();
	public void setOverworldStructureList(List<Structure> list);
	public Structure getOverworldStructure(int structType);
	public void setOverworldStructure(int structType, Structure overworldStructure);
	
	public List<Structure> getNetherStructureList();
	public void setNetherStructureList(List<Structure> list);
	public Structure getNetherStructure(int structType);
	public void setNetherStructure(int structType, Structure netherStructure);
	
	public List<Structure> getEndStructureList();
	public void setEndStructureList(List<Structure> list);
	public Structure getEndStructure(int structType);
	public void setEndStructure(int structType, Structure endStructure);
	
    public Structure getStructure(ResourceLocation dimension, int structType);
    public void setStructure(ResourceLocation dimension, int structType, Structure struct);
	public Structure getActiveStructure(ResourceLocation dimension);
	public int findActiveStructType(ResourceLocation dimension);
	
    public void copyForRespawn(IPlayerData deadPlayer);
}