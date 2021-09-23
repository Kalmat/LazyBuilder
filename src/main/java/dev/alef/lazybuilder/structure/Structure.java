package dev.alef.lazybuilder.structure;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.lists.BlockList;

public class Structure {
	
    @SuppressWarnings("unused")
	private final static Logger LOGGER = LogManager.getLogger();
    
    private ResourceLocation currentDimension;
	private int structType;
	private int rotations = 0;
    private boolean structActive = false;
	private BlockPos startBlockPos = null;
	private List<BlockPos> midBlockPosList = new ArrayList<BlockPos>();
	private BlockPos endBlockPos = null;
	private BlockPos notRotatedEndBlockPos = null;
	private List<BlockState> stateList = new ArrayList<BlockState>();
	
	public Structure(ResourceLocation dimension, int structType) {
		this.currentDimension = dimension;
		this.structType = structType;
	}
	
	public ResourceLocation getDimension() {
		return this.currentDimension;
	}
	
	public int getStructType() {
		return structType;
	}

	public boolean setRotations(int rotateNum) {
		this.rotations = rotateNum;
		return true;
	}

	public int getRotations() {
		return this.rotations;
	}

	public boolean setStartBlockPos(BlockPos pos) {
		this.startBlockPos = pos.toImmutable();
		this.setActive(true);
		return true;
	}
	
	public BlockPos getStartBlockPos() {
		return this.startBlockPos;
	}
	
	public boolean deleteStartBlockPos() {
		if (this.startBlockPos != null) {
			this.startBlockPos = null;
			this.setActive(false);
			this.setRotations(0);
			return true;
		}
		else {
			return false;
		}
	}

	public int addMidBlockPos(BlockPos pos) {
		
		if (this.midBlockPosList.add(pos.toImmutable())) {
			return this.midBlockPosList.size();
		}
		return -1;
	}
	
	public int getMidBlockListSize() {
		return this.midBlockPosList.size();
	}
	
	public BlockPos getMidBlockElement(int index) {
		if (this.getMidBlockListSize() > index) {
			return this.midBlockPosList.get(index);
		}
		else {
			return null;
		}
	}
	
	public boolean deleteMidBlockElement(int index) {
		if (this.getMidBlockListSize() > index) {
			this.midBlockPosList.remove(index);
			return true;
		}
		else {
			return false;
		}
	}

	public List<BlockPos> getMidBlockList() {
		return this.midBlockPosList;
	}
	
	public boolean deleteMidBlockList() {
		if (this.getMidBlockListSize() > 0) {
			this.midBlockPosList = new ArrayList<BlockPos>();
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean replaceMidBlockList(List<BlockPos> newList) {
		if (newList.size() > 0) {
			this.midBlockPosList = newList;
			return true;
		}
		else {
			return false;
		}
	}

	public boolean setEndBlockPos(BlockPos pos) {
		this.endBlockPos = pos.toImmutable();
		this.notRotatedEndBlockPos = pos.toImmutable();
		return true;
	}
		
	public BlockPos getEndBlockPos() {
		return this.endBlockPos;
	}
	
	public boolean setNotRotatedEndBlockPos(BlockPos pos) {
		this.notRotatedEndBlockPos = pos;
		return true;
	}
	
	public BlockPos getNotRotatedEndBlockPos() {
		return this.notRotatedEndBlockPos;
	}
	
	public boolean deleteEndBlockPos() {
		if (this.endBlockPos != null) {
			this.endBlockPos = null;
			this.notRotatedEndBlockPos = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	public int addToClipBoard(BlockState state) {
		this.stateList.add(state);
		return this.stateList.size();
	}
	
	public boolean setInClipBoard(int index, BlockState state) {
		if (index < this.getClipBoardSize()) {
			this.stateList.set(index, state);
			return true;
		}
		return false;
	}
	
	public BlockState getFromClipBoard(int index) {
		return this.stateList.get(index);
	}
	
	public int getClipBoardSize() {
		return this.stateList.size();
	}
	
	public void setClipBoard(List<BlockState> newStateList) {
		this.deleteClipBoard();
		this.stateList = newStateList;
	}
	
	public List<BlockState> getClipBoard() {
		return this.stateList;
	}
	
	public void deleteClipBoard() {
		this.stateList = new ArrayList<BlockState>();
		this.setRotations(0);
	}
	
	public void deleteStructure() {
		this.deleteStartBlockPos();
		this.deleteMidBlockList();
		this.deleteEndBlockPos();
		this.deleteClipBoard();
	}
	
	public void destroy(World worldIn, PlayerEntity player, BlockPos pos, boolean destroyStart, boolean destroyStruct) {
		
		boolean drop = !player.isCreative();

		if (destroyStart) {
			if (this.startBlockPos != null && !this.startBlockPos.equals(pos)) {
				Block startState = worldIn.getBlockState(this.startBlockPos).getBlock();
				if (startState.equals(BlockList.start_block) || 
					startState.equals(BlockList.copy_paste_block) ||
					startState.equals(BlockList.destruct_block)) {
					worldIn.destroyBlock(this.startBlockPos, drop);
				}
			}
			if (destroyStruct) {
				this.deleteStartBlockPos();
			}
		}
		Block midState;
		for (int i = 0; i < this.midBlockPosList.size(); ++i) {
			midState = worldIn.getBlockState(this.midBlockPosList.get(i)).getBlock();
			if (midState != null) {
				if (midState.equals(BlockList.mid_block) || 
					midState.equals(BlockList.mid_block_marker)) {
					worldIn.destroyBlock(this.midBlockPosList.get(i), drop);
				}
			}
		}
		if (this.endBlockPos != null) {
			if (worldIn.getBlockState(this.endBlockPos).getBlock().equals(BlockList.end_block)) {
				worldIn.destroyBlock(this.endBlockPos, drop);
			}
		}
		if (destroyStruct) {
			this.deleteMidBlockList();
			this.deleteEndBlockPos();
			this.deleteClipBoard();
		}
	}
	
	public void shift(BlockPos pos) {
		
		if (this.getStartBlockPos() == null || this.getEndBlockPos() == null || this.getNotRotatedEndBlockPos() == null) {
			return;
		}
		BlockPos notRotatedEnd = this.getNotRotatedEndBlockPos();
		this.setEndBlockPos(this.getEndBlockPos().add(pos.subtract(this.getStartBlockPos())));
		this.setNotRotatedEndBlockPos(notRotatedEnd.add(pos.subtract(this.getStartBlockPos())));
	}
	
	@SuppressWarnings("deprecation")
	public void rotate() {
		
		this.setRotations((this.getRotations() + 1) % 4);
		
		BlockPos notRotatedEnd = this.getNotRotatedEndBlockPos();
		this.setEndBlockPos(CalcVector.rotateBlock(this.getStartBlockPos(), this.getEndBlockPos(), 1));
		this.setNotRotatedEndBlockPos(notRotatedEnd);

		for (int i = 0; i < this.getClipBoardSize(); ++i) {
			this.setInClipBoard(i, this.getFromClipBoard(i).rotate(Rotation.COUNTERCLOCKWISE_90));
		}
	}
	
	public void setActive(boolean active) {
    	this.structActive = active;
    }
    
    public boolean isActive() {
    	return this.structActive;
    }
    
    public boolean isLoaded() {
    	return (this.getClipBoardSize() > 0);
    }

    // DEBUG purposes ONLY
	public void place(World worldIn) {
		
		BlockPos bp = this.getStartBlockPos();
		
		//Block.replaceBlock(worldIn.getBlockState(bp), BlockList.start_block.getDefaultState(), worldIn, bp, 3);

		for (int i = 0; i < this.getMidBlockListSize(); ++i) {
			bp = this.getMidBlockElement(i);
			Block.replaceBlock(worldIn.getBlockState(bp), BlockList.mid_block.getDefaultState(), worldIn, bp, 3);
		}

		bp = this.getEndBlockPos();
		Block.replaceBlock(worldIn.getBlockState(bp), BlockList.end_block.getDefaultState(), worldIn, bp, 3);
	}
	
    public static CompoundNBT createNBT(Structure struct, boolean putClipboard) {
    	
        CompoundNBT tag = new CompoundNBT();

        tag.putString("DM", struct.getDimension().toString());
        tag.putInt("ST", struct.getStructType());
        tag.putBoolean("AC", struct.isActive());
        if (struct.getStartBlockPos() != null) {
        	tag.putLong("SB", struct.getStartBlockPos().toLong());
        }
		tag.putInt("RT", struct.getRotations());
		long[] list = new long[struct.getMidBlockListSize()];
		for (int i = 0; i < list.length; ++i) list[i] = struct.getMidBlockElement(i).toLong();
		tag.putLongArray("MB", list);
		if (struct.getEndBlockPos() != null) {
			tag.putLong("EB", struct.getEndBlockPos().toLong());
		}
		if (struct.getNotRotatedEndBlockPos() != null) {
			tag.putLong("OE", struct.getNotRotatedEndBlockPos().toLong());
		}
		if (putClipboard) {
			for (int j = 0; j < struct.getClipBoardSize(); ++j) tag.put("CB"+j, (INBT) NBTUtil.writeBlockState(struct.getFromClipBoard(j)));
		}
		tag.putBoolean("IL", struct.isLoaded());
        return tag;
    }

    public static Structure retrieveNBT(CompoundNBT tag, boolean retriveClipboard) {
    	
    	Structure struct = new Structure(new ResourceLocation(tag.getString("DM")), tag.getInt("ST"));
    	
    	struct.setActive(tag.getBoolean("AC"));
    	if (tag.getLong("SB") != 0L) {
    		struct.setStartBlockPos(BlockPos.fromLong(tag.getLong("SB")));
    	}
		struct.setRotations(tag.getInt("RT"));
		long[] list = tag.getLongArray("MB");
		for (long pos : list) {
			struct.addMidBlockPos(BlockPos.fromLong(pos));
		}
    	if (tag.getLong("EB") != 0L) {
    		struct.setEndBlockPos(BlockPos.fromLong(tag.getLong("EB")));
    	}
    	if (tag.getLong("OE") != 0L) {
    		struct.setNotRotatedEndBlockPos(BlockPos.fromLong(tag.getLong("OE")));
    	}
		if (retriveClipboard) {
			int j = 0;
			while (tag.contains("CB"+j)) {
				struct.addToClipBoard(NBTUtil.readBlockState(tag.getCompound("CB"+j)));
				++j;
			}
		}
		else if (tag.getBoolean("IL")) {
			struct.addToClipBoard(Blocks.AIR.getDefaultState());
		}
    	return struct;
    }
}
