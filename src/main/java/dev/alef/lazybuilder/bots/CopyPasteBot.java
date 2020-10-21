package dev.alef.lazybuilder.bots;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;

public class CopyPasteBot {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static List<BlockState> NEW_CLIPBOARD = new ArrayList<BlockState>();
	private static int STATE_INDEX = 0;
	private static int BLOCKS_INDEX = 0;
	
	public CopyPasteBot() {
	}
	
	public static List<Object> Copy(World worldIn, PlayerEntity player, BlockPos startBlockPos, List<BlockPos> midBlockPos, BlockPos endBlockPos, List<BlockState> clipBoard) {
		
		NEW_CLIPBOARD = new ArrayList<BlockState>();
		STATE_INDEX = 0;
		BLOCKS_INDEX = 0;
		
		copypasteComplexStruct(worldIn, startBlockPos, midBlockPos, endBlockPos, clipBoard, player, Refs.COPY);
		
		List<Object> copyResult = new ArrayList<Object>();
		copyResult.add(STATE_INDEX);
		copyResult.add(NEW_CLIPBOARD);
		return copyResult;
	}
	
	public static void Paste(World worldIn, PlayerEntity player, BlockPos startBlockPos, List<BlockPos> midBlockPos, BlockPos endBlockPos, List<BlockState> clipBoard) {
		
		STATE_INDEX = 0;
		
		copypasteComplexStruct(worldIn, startBlockPos, midBlockPos, endBlockPos, clipBoard, player, Refs.PASTE);
	}

	public static BlockPos copypasteOneLine(World worldIn, BlockPos startBlockPos, BlockPos endBlockPos, List<BlockState> clipBoard, PlayerEntity player, int mode) {
		
		Direction dir = CalcVector.getHDirection(startBlockPos, endBlockPos);
		if (dir == null) {
			dir = CalcVector.getVDirection(startBlockPos, endBlockPos);
		}
		int len = CalcVector.getLength(startBlockPos, endBlockPos);
		BlockPos newPos = startBlockPos;
		BlockState block;
		
		for (int i = 0; i < len - 1; ++i) {
			newPos = newPos.offset(dir, 1);
			if (mode == Refs.COPY) {
				if (STATE_INDEX < clipBoard.size() && STATE_INDEX < Integer.MAX_VALUE) {
					block = worldIn.getBlockState(newPos);
					customCopyBlock(block, newPos, player);
					if (!block.equals(Blocks.AIR.getDefaultState()) && !block.equals(Blocks.WATER.getDefaultState())) {
						BLOCKS_INDEX += 1;
						if (STATE_INDEX == 0 || BLOCKS_INDEX % Refs.blocksPerItem == 0) {
							STATE_INDEX += 1;
						}
					}
				}
				else {
					STATE_INDEX = 0;
					NEW_CLIPBOARD.clear();
					break;
				}
			}
			else {
				if (STATE_INDEX < clipBoard.size()) {
					customPasteBlock(worldIn.getBlockState(newPos), clipBoard.get(STATE_INDEX), worldIn, newPos, 3, player, dir);
					STATE_INDEX += 1;
				}
				else {
					break;
				}
			}
		}
		return newPos;
	}
	
	private static void customCopyBlock(BlockState state, BlockPos pos, PlayerEntity player) {

		if (!state.getBlock().getRegistryName().getNamespace().equals("minecraft") || // --> Avoid copying non-vanilla blocks (blocks from other mods)state.equals(Blocks.DIAMOND_BLOCK.getDefaultState()) || 
				state.equals(Blocks.DIAMOND_ORE.getDefaultState()) ||
				state.equals(Blocks.REDSTONE_BLOCK.getDefaultState()) ||
				state.equals(Blocks.EMERALD_ORE.getDefaultState()) ||
				state.equals(Blocks.EMERALD_BLOCK.getDefaultState()) ||
				state.equals(Blocks.GOLD_ORE.getDefaultState()) ||
				state.equals(Blocks.GOLD_BLOCK.getDefaultState()) ||
				state.equals(Blocks.ENCHANTING_TABLE.getDefaultState()) ||
				state.equals(Blocks.OBSIDIAN.getDefaultState()) ||
				state.equals(Blocks.TNT.getDefaultState()) ||
				state.equals(Blocks.BOOKSHELF.getDefaultState()) ||
				state.equals(Blocks.WATER.getDefaultState())
			) {
			state = Blocks.AIR.getDefaultState();
		}
		NEW_CLIPBOARD.add(state);
	}
	
	private static void customPasteBlock(BlockState oldBlock, BlockState newBlock, World worldIn, BlockPos pos, int flags, PlayerEntity player, Direction dir) {
		
		BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
		BlockPos newPos = playerPos.offset(dir, 1);

		if (playerPos.equals(pos)) {
			player.setPosition(newPos.getX(), newPos.getY(), newPos.getZ());
		}
		Block.replaceBlock(oldBlock, newBlock, worldIn, pos, flags);
	}


	public static BlockPos copypasteMultiLine(World worldIn, BlockPos startBlockPos, BlockPos midBlockPos, BlockPos endBlockPos, List<BlockState> clipBoard, PlayerEntity player, int mode) {
		
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

		for (int i = 0; i < length; ++i) {
			startPos = startBlockPos.offset(lDir, i);
			endPos = endPos.offset(lDir, 1);
			pos = copypasteOneLine(worldIn, startPos, endPos, clipBoard, player, mode);
			if (STATE_INDEX < 0) {
				NEW_CLIPBOARD = new ArrayList<BlockState>();
				break;
			}
		}
		return pos;
	}
	
	public static void copypasteComplexStruct(World worldIn, BlockPos startBlockPos, List<BlockPos> midBlockPosList, BlockPos endBlockPos, List<BlockState> clipBoard, PlayerEntity player, int mode) {
		
		BlockPos newStart;
		BlockPos newMid;
		BlockPos newEnd;
		int height = CalcVector.getHeight(startBlockPos, midBlockPosList.get(0));
		
		for (int i = 0; i < height; ++i) {
			newStart = CalcVector.setY(startBlockPos, startBlockPos.getY()+i);
			newMid = CalcVector.setY(midBlockPosList.get(1), startBlockPos.getY()+i);
			newEnd = CalcVector.setY(endBlockPos, startBlockPos.getY()+i);;
			copypasteMultiLine(worldIn, newStart, newMid, newEnd, clipBoard, player, mode);
			if (STATE_INDEX < 0) {
				STATE_INDEX = 0;
				break;
			}
		}
	}
}

