package dev.alef.lazybuilder.bots;

import java.util.List;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import dev.alef.lazybuilder.structure.Undo;
import dev.alef.lazybuilder.structure.UndoList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;

public class UndoBot {
	
	public UndoBot() {
	}

	public static void undoAction(World worldIn, PlayerEntity player, UndoList undoList) {
		
		Undo actionList = undoList.get(worldIn, player);
		List<String> msg = null;
		
		if (actionList != null && actionList.getActionListSize() > 0) {
			Object lastAction = actionList.getLastAction();
	
			if (lastAction != null) {
				if (lastAction instanceof BreakEvent) {
					BlockPos pos = ((BreakEvent) lastAction).getPos();
					BlockState currentBlock = worldIn.getBlockState(pos);
					BlockState prevBlock = ((BreakEvent) lastAction).getState();
					if (searchBlockInInventory(prevBlock, player) || player.isCreative()) {
						if (!player.isCreative()) {
							removeBlockFromInventory(prevBlock, player);
						}
						Block.replaceBlock(currentBlock, prevBlock, worldIn, pos, 3);
						actionList.deleteLastAction(worldIn, player);
					}
					else if (worldIn.isRemote) {
						LazyBuilderClient.setTextActive(Refs.breakUndoError, 200);
					}
				}
				else if (lastAction instanceof EntityPlaceEvent) {
					BlockPos pos = ((EntityPlaceEvent) lastAction).getPos();
					worldIn.destroyBlock(pos, !player.isCreative());
					actionList.deleteLastAction(worldIn, player);
				}
			}
			else {
				msg = Refs.undoEmptyMsg;
			}
		}
		else {
			msg = Refs.undoEmptyMsg;
		}
		if (worldIn.isRemote && msg != null) {
			LazyBuilderClient.setTextActive(msg, 200);
		}
	}
	
	private static boolean searchBlockInInventory(BlockState state, PlayerEntity player) {

		if (state.getBlock().equals(Blocks.GRASS_BLOCK)) {
			state = Blocks.DIRT.getDefaultState();
		}
		
		for (ItemStack stack : player.inventory.mainInventory) {
			if (Block.getBlockFromItem(stack.getItem()).equals(state.getBlock())) {
				return true;
			}
		}
		return false;
	}
	
	private static void removeBlockFromInventory(BlockState state, PlayerEntity player) {
		
		if (state.getBlock().equals(Blocks.GRASS_BLOCK)) {
			state = Blocks.DIRT.getDefaultState();
		}
		
		for (ItemStack stack : player.inventory.mainInventory) {
			if (Block.getBlockFromItem(stack.getItem()).equals(state.getBlock())) {
				stack.setCount(stack.getCount()-1);
				break;
			}
		}
	}
}
