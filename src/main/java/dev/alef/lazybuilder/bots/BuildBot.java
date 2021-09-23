package dev.alef.lazybuilder.bots;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;

public class BuildBot {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	private int stateIndex = 0;

	public BuildBot() {
	}
	
	public int build(World worldIn, PlayerEntity player, BlockPos startBlockPos, List<BlockPos> midBlockPosList, BlockPos endBlockPos, List<BlockState> buildStack, boolean simulate) {
		
		this.stateIndex = 0;
		
		if (midBlockPosList.size() == 0) {
			this.buildOneLine(worldIn, startBlockPos, endBlockPos, buildStack, player, simulate);
		}
		else {
			if (midBlockPosList.size() == 1) {
				if (CalcVector.getHeight(startBlockPos, midBlockPosList.get(0)) <= 1) {
					this.buildHMultiLine(worldIn, startBlockPos, midBlockPosList.get(0), endBlockPos, buildStack, player, simulate);
				}
				else {
					this.buildVMultiLine(worldIn, startBlockPos, midBlockPosList.get(0), endBlockPos, buildStack, player, simulate);
				}
			}
			else {
				this.buildComplexStruct(worldIn, startBlockPos, midBlockPosList, endBlockPos, buildStack, player, simulate);
			}
		}
		return this.stateIndex;
	}
	
	public BlockPos buildOneLine(World worldIn, BlockPos startBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player, boolean simulate) {
		
		Direction dir = CalcVector.getDirection(startBlockPos, endBlockPos);
		if (dir == null) {
			dir = CalcVector.getVDirection(startBlockPos, endBlockPos);
		}
		int len = CalcVector.getDistance(startBlockPos, endBlockPos);
		BlockPos newPos = startBlockPos;
		
		for (int i = 0; i < len - 1 && (this.stateIndex < buildStack.size() || simulate); ++i) {
			newPos = startBlockPos.offset(dir, i+1);
			if (!simulate) {
				this.customBuildBlock(worldIn.getBlockState(newPos), buildStack.get(this.stateIndex), worldIn, newPos, 3, player, dir);
			}
			this.stateIndex += 1;
		}
		return newPos;
	}
	
	private void customBuildBlock(BlockState oldBlock, BlockState newBlock, World worldIn, BlockPos pos, int flags, PlayerEntity player, Direction dir) {
		
		BlockPos playerPos = new BlockPos(player.getPositionVec());
		BlockPos newPos = playerPos.offset(dir, 1);

		if (playerPos.equals(pos)) {
			player.setPosition(newPos.getX(), newPos.getY(), newPos.getZ());
		}
		if (!Refs.modBlockList.contains(oldBlock.getBlock())) {
			worldIn.setBlockState(pos, newBlock);
		}
	}

	public BlockPos buildVMultiLine(World worldIn, BlockPos startBlockPos, BlockPos midBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player, boolean simulate) {
		
		Direction vDir = CalcVector.getVDirection(startBlockPos, midBlockPos);
		int height = CalcVector.getHeight(startBlockPos, midBlockPos);
		BlockPos startPos = startBlockPos;
		BlockPos endPos = endBlockPos;
		BlockPos pos = endBlockPos;
		
		for (int i = 0; i < height && (this.stateIndex < buildStack.size() || simulate); ++i) {
			startPos = startBlockPos.offset(vDir, i);
			endPos = CalcVector.setY(endBlockPos, startPos.getY());
			pos = this.buildOneLine(worldIn, startPos, endPos, buildStack, player, simulate);
		}
		return pos;
	}
	
	public void buildComplexStruct(World worldIn, BlockPos startBlockPos, List<BlockPos> midBlockPosList, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player, boolean simulate) {
		
		BlockPos newStart;
		BlockPos newMid;
		BlockPos newEnd;
		BlockPos pos;
		int initHeight = startBlockPos.getY();
		int finalHeight = midBlockPosList.get(0).getY();

		for (int i = 0; i < midBlockPosList.size() && (this.stateIndex < buildStack.size() || simulate); ++i) {
			newStart = startBlockPos;
			newMid = CalcVector.setY(midBlockPosList.get(i), finalHeight);
			if (i+1 >= midBlockPosList.size()) {
				newEnd = CalcVector.setY(endBlockPos, initHeight);
			}
			else {
				newEnd = CalcVector.setY(midBlockPosList.get(i+1), initHeight);
			}
			pos = this.buildVMultiLine(worldIn, newStart, newMid, newEnd, buildStack, player, simulate);
			startBlockPos = CalcVector.setY(pos, initHeight);
		}
	}
	
	public BlockPos buildHMultiLine(World worldIn, BlockPos startBlockPos, BlockPos midBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player, boolean simulate) {
		
		Direction lDir = CalcVector.getDirection(startBlockPos, midBlockPos);
		Direction wDir = CalcVector.getDirection(startBlockPos, endBlockPos);
		int length = CalcVector.getDistance(startBlockPos, midBlockPos);
		int width = CalcVector.getDistance(startBlockPos, endBlockPos);
		if (wDir == lDir) {
			wDir = CalcVector.getDirection(midBlockPos, endBlockPos);
			width = CalcVector.getDistance(midBlockPos, endBlockPos);
		}
		BlockPos startPos;
		BlockPos endPos = CalcVector.setY(startBlockPos.offset(wDir, width), startBlockPos.getY());
		BlockPos pos = endBlockPos.offset(lDir, -1);

		for (int i = 1; i < length && (this.stateIndex < buildStack.size() || simulate); ++i) {
			startPos = startBlockPos.offset(lDir, i);
			endPos = endPos.offset(lDir, 1);
			pos = this.buildOneLine(worldIn, startPos, endPos, buildStack, player, simulate);
		}
		return pos;
	}
}
