package dev.alef.lazybuilder.playerdata;

import javax.annotation.Nullable;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerDataStorage implements Capability.IStorage<IPlayerData> {
	
    @Nullable
    @Override
    public INBT writeNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side) {
    	
        CompoundNBT tag = new CompoundNBT();
        ResourceLocation dimension;
        int structType;

        for (int i = 0; i < Refs.dimensionList.size(); ++i) {
        	dimension = Refs.dimensionList.get(i);
        	for (int j = 0; j < Refs.structTypeList.size(); ++j) {
        		structType = Refs.structTypeList.get(j);
        		if (structType != Refs.EMPTY) {
        			tag.put(dimension.toString()+structType, (INBT) Structure.createNBT(instance.getStructure(dimension, structType), true));
        		}
        	}
        }
        return tag;
    }
    
    @Override
    public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side, INBT nbt) {

    	CompoundNBT tag = (CompoundNBT) nbt;
        ResourceLocation dimension;
        int structType;

        for (int i = 0; i < Refs.dimensionList.size(); ++i) {
        	dimension = Refs.dimensionList.get(i);
        	for (int j = 0; j < Refs.structTypeList.size(); ++j) {
        		structType = Refs.structTypeList.get(j);
        		if (structType != Refs.EMPTY) {
        			instance.setStructure(dimension, structType, Structure.retrieveNBT(tag.getCompound(dimension.toString()+structType), true));
        		}
        	}
        }
     }
}