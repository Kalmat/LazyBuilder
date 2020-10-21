package dev.alef.lazybuilder.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.BuildBot;
import dev.alef.lazybuilder.bots.CopyPasteBot;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.CopyPasteBlockTileEntity;
import dev.alef.lazybuilder.tileentity.StartBlockTileEntity;

public class EndBlock extends Block {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
    public EndBlock(Properties properties) {
		super(properties);
    }
    
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		
		int active = Refs.BUILDING;
    	Structure structure = LazyBuilder.BUILD_LIST.get(worldIn, (PlayerEntity) placer);
    	if (!structure.isActive()) {
    		active = Refs.COPYPASTE;
        	structure = LazyBuilder.COPYPASTE_LIST.get(worldIn, (PlayerEntity) placer);
    	}
		
    	BlockPos endBlock = structure.getEndBlockPos();
		boolean placed = false;
		boolean drop = !((PlayerEntity) placer).isCreative();
    	List<String> msg = null;
		
    	if (structure.getStartBlockPos() != null) {
    		if (!structure.isLoaded()) {
				if (active == Refs.BUILDING || (active == Refs.COPYPASTE && structure.getMidBlockListSize() == 2)) {
					placed = structure.setEndBlockPos(pos, (PlayerEntity) placer);
				}
				else if (active == Refs.COPYPASTE && structure.getMidBlockListSize() < 2) {
					msg = Refs.midNotEnoughMidMsg;
				}
    		}
    		else {
    			msg = Refs.copyAlreadyDoneMsg;
    		}
    	}
		else {
			msg = Refs.endStartFirstMsg;
		}
		if (placed) {
			if (endBlock != null) {
				worldIn.destroyBlock(endBlock, drop);
			}
		}
		else {
			if (msg != null) {
				if (worldIn.isRemote) {
					LazyBuilderClient.setTextActive(msg, 200);
				}
			}
			worldIn.destroyBlock(pos, drop);
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
	
	@SuppressWarnings("unchecked")
	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
    	Structure structure = LazyBuilder.BUILD_LIST.get(worldIn, player);
    	Structure clipBoard = LazyBuilder.COPYPASTE_LIST.get(worldIn, player);
    	List<String> msg = null;
    	
		if (structure.isActive() && structure.getStartBlockPos() != null && structure.getEndBlockPos() != null) {
			structure.setClipBoard(StartBlockTileEntity.fillClipBoardFromContainer(worldIn, player, structure.getStartBlockPos()));
			if (structure.getClipBoardSize() > 0) {
				int start = BuildBot.startBuilding(worldIn, player, structure.getStartBlockPos(), structure.getMidBlockList(), structure.getEndBlockPos(), structure.getClipBoard());
				StartBlockTileEntity.fillContainerFromClipBoard(worldIn, player, structure.getStartBlockPos(), structure.getClipBoard(), start, structure.getClipBoardSize());
			}
			else {
				msg = Refs.buildEmptyMsg;
			}
		}
		else if (!clipBoard.isLoaded()) {
			if (clipBoard.isActive() && clipBoard.getStartBlockPos() != null && clipBoard.getMidBlockListSize() == 2 && clipBoard.getEndBlockPos() != null) {
				List<Object> copyResult = new ArrayList<Object>();
				List<BlockState> newStateList = new ArrayList<BlockState>();
				int cost = 0;
				
				clipBoard.setClipBoard(CopyPasteBlockTileEntity.fillClipBoardFromContainer(worldIn, player, clipBoard.getStartBlockPos()));
				if (clipBoard.getClipBoardSize() > 0) {
					copyResult = CopyPasteBot.Copy(worldIn, player, clipBoard.getStartBlockPos(), clipBoard.getMidBlockList(), clipBoard.getEndBlockPos(), clipBoard.getClipBoard());
					cost = (int) copyResult.get(0);
					newStateList = (List<BlockState>) (Object) copyResult.get(1);
					if (newStateList.size() > 0) {
						if (cost > 0) {
							msg = Refs.copyMsg;
						}
						else {
							newStateList.clear();
							msg = Refs.copyOnlyAirMsg;
						}
					}
					else {
						msg = Refs.copyNotEnoughMsg;
					}
					CopyPasteBlockTileEntity.fillContainerFromClipBoard(worldIn, player, clipBoard.getStartBlockPos(), clipBoard.getClipBoard(), cost, clipBoard.getClipBoardSize());
					clipBoard.setClipBoard(newStateList);
				}
				else {
					msg = Refs.copyEmptyMsg;
				}
			}
		}
		else {
			msg = Refs.copyAlreadyDoneMsg;
		}
		if (worldIn.isRemote && msg != null) {
			LazyBuilderClient.setTextActive(msg, 200);
		}
		return ActionResultType.SUCCESS;
    }

	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {

    	Structure structure = LazyBuilder.BUILD_LIST.get(worldIn, player);
    	int structType = Refs.BUILDING;
    	
    	if (!structure.isActive()) {
        	structure = LazyBuilder.COPYPASTE_LIST.get(worldIn, player);
        	structType = Refs.COPYPASTE;
    	}
		
    	if (!structure.isLoaded() || structType == Refs.BUILDING) {
			structure.deleteEndBlockPos();
    	}
		super.onBlockHarvested(worldIn, pos, state, player);
    }
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof EndBlock) {
				return true;
			}
		}
		return false;
	}
}
