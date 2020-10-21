package dev.alef.lazybuilder.bots;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuildBot {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static int STATE_INDEX = 0;

	public BuildBot() {
		
	}
	
	public static int startBuilding(World worldIn, PlayerEntity player, BlockPos startBlockPos, List<BlockPos> midBlockPos, BlockPos endBlockPos, List<BlockState> buildStack) {
		
		STATE_INDEX = 0;
		
		if (midBlockPos.size() == 0) {
			buildOneLine(worldIn, startBlockPos, endBlockPos, buildStack, player);
		}
		else {
			int height = CalcVector.getHeight(startBlockPos, midBlockPos.get(0));
			if (midBlockPos.size() == 1 && height <= 1 && (midBlockPos.get(0).getX() != startBlockPos.getX() || midBlockPos.get(0).getZ() != startBlockPos.getZ())) {
				buildHMultiLine(worldIn, startBlockPos, midBlockPos.get(0), endBlockPos, buildStack, player);
			} else {
				if (midBlockPos.size() == 1) {
					buildVMultiLine(worldIn, startBlockPos, midBlockPos.get(0), endBlockPos, buildStack, player);
				}
				else {
					buildComplexStruct(worldIn, startBlockPos, midBlockPos, endBlockPos, buildStack, player);
				}
			}
		}
		return STATE_INDEX;
	}
	
	public static BlockPos buildOneLine(World worldIn, BlockPos startBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player) {
		
		Direction dir = CalcVector.getHDirection(startBlockPos, endBlockPos);
		if (dir == null) {
			dir = CalcVector.getVDirection(startBlockPos, endBlockPos);
		}
		int len = CalcVector.getLength(startBlockPos, endBlockPos);
		BlockPos newPos = startBlockPos;
		
		for (int i = 0; i < len - 1; ++i) {
			newPos = startBlockPos.offset(dir, i+1);
			if (STATE_INDEX < buildStack.size()) {
				customReplaceBlock(worldIn.getBlockState(newPos), buildStack.get(STATE_INDEX), worldIn, newPos, 3, player, dir);
				STATE_INDEX += 1;
			}
			else {
				break;
			}
		}
		return newPos;
	}
	
	private static void customReplaceBlock(BlockState oldBlock, BlockState newBlock, World worldIn, BlockPos pos, int flags, PlayerEntity player, Direction dir) {
		
		BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
		BlockPos newPos = playerPos.offset(dir, 1);

		if (playerPos.equals(pos)) {
			player.setPosition(newPos.getX(), newPos.getY(), newPos.getZ());
		}
		Block.replaceBlock(oldBlock, newBlock, worldIn, pos, flags);
	}

	public static BlockPos buildVMultiLine(World worldIn, BlockPos startBlockPos, BlockPos midBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player) {
		
		Direction vDir = CalcVector.getVDirection(startBlockPos, midBlockPos);
		int height = CalcVector.getHeight(startBlockPos, midBlockPos);
		BlockPos startPos = startBlockPos;
		BlockPos endPos = endBlockPos;
		BlockPos pos = endBlockPos;
		
		for (int i = 0; i < height; ++i) {
			startPos = startBlockPos.offset(vDir, i);
			endPos = CalcVector.setY(endBlockPos, startPos.getY());
			pos = buildOneLine(worldIn, startPos, endPos, buildStack, player);
		}
		return pos;
	}
	
	public static void buildComplexStruct(World worldIn, BlockPos startBlockPos, List<BlockPos> midBlockPosList, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player) {
		
		BlockPos newStart;
		BlockPos newMid;
		BlockPos newEnd;
		BlockPos pos;
		int initHeight = startBlockPos.getY();
		int finalHeight = midBlockPosList.get(0).getY();

		for (int i = 0; i < midBlockPosList.size(); ++i) {
			newStart = startBlockPos;
			newMid = CalcVector.setY(midBlockPosList.get(i), finalHeight);
			if (i+1 >= midBlockPosList.size()) {
				newEnd = CalcVector.setY(endBlockPos, initHeight);
			}
			else {
				newEnd = CalcVector.setY(midBlockPosList.get(i+1), initHeight);
			}
			pos = buildVMultiLine(worldIn, newStart, newMid, newEnd, buildStack, player);
			startBlockPos = CalcVector.setY(pos, initHeight);
		}
	}
	
	public static BlockPos buildHMultiLine(World worldIn, BlockPos startBlockPos, BlockPos midBlockPos, BlockPos endBlockPos, List<BlockState> buildStack, PlayerEntity player) {
		
		Direction lDir = CalcVector.getHDirection(startBlockPos, midBlockPos);
		Direction wDir = CalcVector.getHDirection(startBlockPos, endBlockPos);
		int length = CalcVector.getLength(startBlockPos, midBlockPos);
		int width = CalcVector.getLength(startBlockPos, endBlockPos);
		if (wDir == lDir) {
			wDir = CalcVector.getHDirection(midBlockPos, endBlockPos);
			width = CalcVector.getLength(midBlockPos, endBlockPos);
		}
		BlockPos startPos;
		BlockPos endPos = CalcVector.setY(startBlockPos.offset(wDir, width), startBlockPos.getY());
		BlockPos pos = endBlockPos.offset(lDir, -1);

		for (int i = 1; i < length; ++i) {
			startPos = startBlockPos.offset(lDir, i);
			endPos = endPos.offset(lDir, 1);
			pos = buildOneLine(worldIn, startPos, endPos, buildStack, player);
		}
		return pos;
	}
}
