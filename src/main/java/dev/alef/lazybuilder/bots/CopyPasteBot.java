package dev.alef.lazybuilder.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;

public class CopyPasteBot {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<BlockState> newClipBoard = new ArrayList<BlockState>();
	private int stateIndex = 0;
	private int blocksIndex = 0;

	public CopyPasteBot() {
	}
	
	public List<Object> copy(World worldIn, BlockPos startBlockPos, BlockPos endBlockPos) {
		
		this.newClipBoard = new ArrayList<BlockState>();
		this.blocksIndex = 0;
		
		startBlockPos = CalcVector.fixStartPos(startBlockPos, endBlockPos);
		endBlockPos = CalcVector.fixAltEndPos(startBlockPos, endBlockPos);
		
		Stream<BlockPos> blocks = BlockPos.getAllInBox(startBlockPos, endBlockPos);
		blocks.forEach(pos -> this.customCopyBlock(worldIn, pos));

		List<Object> copyResult = new ArrayList<Object>();
		if (this.blocksIndex == 0) {
			this.newClipBoard = new ArrayList<BlockState>();
			copyResult.add(0);
		}
		else {
			copyResult.add(((int) this.blocksIndex / Refs.copyBlocksPerItem) + 1);
		}
		copyResult.add(this.newClipBoard);
		return copyResult;
	}
	
	private void customCopyBlock(World worldIn, BlockPos pos) {
		
		BlockState state = worldIn.getBlockState(pos);

		if (!state.getBlock().getRegistryName().getNamespace().equals("minecraft") || // --> Avoid copying non-vanilla blocks (blocks from other mods)
				Refs.copyNotStateList.contains(state.getBlock())) {
			state = Blocks.AIR.getDefaultState();
		}
		this.newClipBoard.add(state);
		
		if (!state.getBlock().isAir(state, worldIn, pos)) {
			++this.blocksIndex;
		}
	}
	
	public int paste(World worldIn, BlockPos startBlockPos, BlockPos endBlockPos, List<BlockState> clipBoard, int rotations) {
		
		this.stateIndex = 0;
		
		BlockPos startPos = CalcVector.fixStartPos(startBlockPos, endBlockPos);
		BlockPos endPos = CalcVector.fixAltEndPos(startBlockPos, endBlockPos);
		
		Stream<BlockPos> blocks = BlockPos.getAllInBox(startPos, endPos);
		blocks.limit(clipBoard.size()).forEach(pos -> this.customPasteBlock(worldIn, startBlockPos, pos, clipBoard.get(this.stateIndex), rotations));
		return this.stateIndex;
	}
	
	private void customPasteBlock(World worldIn, BlockPos startBlockPos, BlockPos pos, BlockState newState, int rotations) {
		
		pos = CalcVector.rotateBlock(startBlockPos, pos, rotations);
		
		if (!Refs.modBlockList.contains(worldIn.getBlockState(pos).getBlock())) {
			worldIn.setBlockState(pos, newState);
		}
		++this.stateIndex;
	}
}
