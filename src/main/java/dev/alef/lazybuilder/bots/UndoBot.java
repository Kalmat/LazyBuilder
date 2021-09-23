package dev.alef.lazybuilder.bots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.structure.Undo;
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
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	public UndoBot() {
	}

	public boolean undoAction(World worldIn, PlayerEntity player, Undo actionList) {
		
		Object lastAction = actionList.getLastAction();

		if (lastAction instanceof BreakEvent) {
			
			BlockPos pos = ((BreakEvent) lastAction).getPos();
			BlockState prevBlock = ((BreakEvent) lastAction).getState();
			
			if (this.searchBlockInInventory(prevBlock, player) || player.isCreative()) {
				if (!player.isCreative()) {
					this.removeBlockFromInventory(prevBlock, player);
				}
				worldIn.setBlockState(pos, prevBlock);
				actionList.deleteLastAction(worldIn, player);
			}
			else {
				return false;
			}
		}
		else if (lastAction instanceof EntityPlaceEvent) {
			
			BlockPos pos = ((EntityPlaceEvent) lastAction).getPos();
			
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			actionList.deleteLastAction(worldIn, player);
		}
		return true;
	}
 	
	public boolean redoAction(World worldIn, PlayerEntity player, Undo reActionList) {
		
	
		Object lastReAction = reActionList.getLastReAction();

		if (lastReAction instanceof BreakEvent) {
			
			BlockPos pos = ((BreakEvent) lastReAction).getPos();
			
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			reActionList.deleteLastReAction(worldIn, player);
		}
		else if (lastReAction instanceof EntityPlaceEvent) {
			
			BlockPos pos = ((EntityPlaceEvent) lastReAction).getPos();
			BlockState prevBlock = ((EntityPlaceEvent) lastReAction).getState();
			
			if (this.searchBlockInInventory(prevBlock, player) || player.isCreative()) {
				if (!player.isCreative()) {
					this.removeBlockFromInventory(prevBlock, player);
				}
				worldIn.setBlockState(pos, prevBlock);
				reActionList.deleteLastReAction(worldIn, player);
			}
			else {
				return false;
			}
		}
		return true;
	}
 	
	private boolean searchBlockInInventory(BlockState state, PlayerEntity player) {

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
	
	private void removeBlockFromInventory(BlockState state, PlayerEntity player) {
		
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
