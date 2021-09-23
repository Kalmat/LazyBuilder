package dev.alef.lazybuilder.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.BuildBot;
import dev.alef.lazybuilder.bots.CopyPasteBot;
import dev.alef.lazybuilder.bots.DestructBot;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
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
		
		if (!worldIn.isRemote) {
		
	    	List<String> msg = null;
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer((PlayerEntity) placer);
			int structType = playerData.findActiveStructType(currentDimension);
	    	Structure structure = playerData.getStructure(currentDimension, structType);
	
	    	BlockPos endBlock = structure.getEndBlockPos();
			boolean placed = false;
			boolean drop = !((PlayerEntity) placer).isCreative();
			BlockPos startBlock = structure.getStartBlockPos();
			boolean startPlaced = (structType != Refs.EMPTY && (structType == Refs.BUILDING && StartBlock.isPlaced(worldIn, startBlock)) || (structType == Refs.COPYPASTE && CopyPasteBlock.isPlaced(worldIn, startBlock)) || (structType == Refs.DESTRUCT && DestructBlock.isPlaced(worldIn, startBlock)));
			int blocksRequired = 0;
			
	    	if (startPlaced && startBlock != null) {
	    		if (structType == Refs.BUILDING) {
	    			placed = structure.setEndBlockPos(pos);
	    			BuildBot buildBot = new BuildBot();
	    			blocksRequired = buildBot.build(worldIn, (PlayerEntity) placer, structure.getStartBlockPos(), structure.getMidBlockList(), structure.getEndBlockPos(), structure.getClipBoard(), true);
	    			msg = Arrays.asList(Refs.buildBlocksReqMsg+Integer.toString(blocksRequired));
	    		}
	    		else {
	    			if (!structure.isLoaded() || structType == Refs.DESTRUCT) {
						placed = structure.setEndBlockPos(pos);
						if (structType == Refs.COPYPASTE) {
							List<Object> copyResult = new ArrayList<Object>();
							CopyPasteBot copypasteBot = new CopyPasteBot();
							copyResult = copypasteBot.copy(worldIn, structure.getStartBlockPos(), structure.getEndBlockPos());
							blocksRequired = (int) copyResult.get(0);
							msg = Arrays.asList(Refs.copyBlocksReqMsg+Integer.toString(blocksRequired));
						}
					}
			    	else {
			    		msg = Refs.copyAlreadyDoneMsg;
			    	}
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
				worldIn.destroyBlock(pos, drop);
			}
			LazyBuilder.updateClientStructure(currentDimension, (PlayerEntity) placer, structure);
			if (msg != null) {
				LazyBuilder.showClientText(msg, 200, (ServerPlayerEntity) placer);
			}
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
	
	@SuppressWarnings("unchecked")
	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
    	if (!worldIn.isRemote) {

        	List<String> msg = null;
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
			int structType = playerData.findActiveStructType(currentDimension);
			if (structType < 0) {
				return ActionResultType.SUCCESS;
			}
	    	Structure structure = playerData.getStructure(currentDimension, structType);
	   	
			if (structType == Refs.BUILDING && structure.getStartBlockPos() != null && structure.getEndBlockPos() != null && structure.getEndBlockPos().equals(pos)) {
				structure.setClipBoard(StartBlockTileEntity.fillClipBoardFromContainer(worldIn, player, structure.getStartBlockPos()));
				int clipboardSize = structure.getClipBoardSize();
				BuildBot buildBot = new BuildBot();
				int blocksRequired = buildBot.build(worldIn, player, structure.getStartBlockPos(), structure.getMidBlockList(), structure.getEndBlockPos(), structure.getClipBoard(), true);
				if (clipboardSize >= blocksRequired) {
					int start = buildBot.build(worldIn, player, structure.getStartBlockPos(), structure.getMidBlockList(), structure.getEndBlockPos(), structure.getClipBoard(), false);
					StartBlockTileEntity.fillContainerFromClipBoard(worldIn, player, structure.getStartBlockPos(), structure.getClipBoard(), start, clipboardSize);
					structure.deleteClipBoard();
				}
				else {
					if (clipboardSize > 0) {
						StartBlockTileEntity.fillContainerFromClipBoard(worldIn, player, structure.getStartBlockPos(), structure.getClipBoard(), 0, clipboardSize);
						msg = Arrays.asList(Refs.buildNotEnoughMsg+blocksRequired+Refs.buildItemsInStartMsg+clipboardSize);
					}
					else {
						msg = Arrays.asList(Refs.buildNotEnoughMsg+blocksRequired+Refs.buildEmptyMsg);
					}
				}
			}
			else if(structType == Refs.DESTRUCT && structure.getStartBlockPos() != null && structure.getEndBlockPos() != null && structure.getEndBlockPos().equals(pos)) {
				DestructBot destructBot = new DestructBot();
				destructBot.destruct(worldIn, player, structure.getStartBlockPos(), structure.getEndBlockPos());
			}
			else if (!structure.isLoaded()) {
				if (structType == Refs.COPYPASTE && structure.getStartBlockPos() != null && structure.getEndBlockPos() != null  && structure.getEndBlockPos().equals(pos)) {
					List<Object> copyResult = new ArrayList<Object>();
					List<BlockState> newStateList = new ArrayList<BlockState>();
					int cost = 0;
					
					structure.setClipBoard(CopyPasteBlockTileEntity.fillClipBoardFromContainer(worldIn, player, structure.getStartBlockPos()));
					int clipboardSize = structure.getClipBoardSize();
					CopyPasteBot copypasteBot = new CopyPasteBot();
					copyResult = copypasteBot.copy(worldIn, structure.getStartBlockPos(), structure.getEndBlockPos());
					cost = (int) copyResult.get(0);
					newStateList = (List<BlockState>) (Object) copyResult.get(1);
					if (cost > 0) {
						if ((clipboardSize >= cost || player.isCreative()) && newStateList.size() > 0) {
							msg = Refs.copyMsg;
						}
						else {
							if (clipboardSize > 0) {
								msg = Arrays.asList(Refs.copyNotEnoughMsg+cost+Refs.copyItemsInStartMsg+clipboardSize);
							}
							else {
								msg = Arrays.asList(Refs.copyNotEnoughMsg+cost+Refs.copyEmptyMsg);
							}
							newStateList = new ArrayList<BlockState>();
							cost = 0;
						}
					}
					else {
						newStateList = new ArrayList<BlockState>();
						msg = Refs.copyOnlyAirMsg;
					}
					CopyPasteBlockTileEntity.fillContainerFromClipBoard(worldIn, player, structure.getStartBlockPos(), structure.getClipBoard(), cost, clipboardSize);
					structure.setClipBoard(newStateList);
				}
			}
			else {
				msg = Refs.copyAlreadyDoneMsg;
			}
			LazyBuilder.updateClientStructure(currentDimension, player, structure);
			if (msg != null) {
				LazyBuilder.showClientText(msg, 200, player);
			}
    	}
		return ActionResultType.SUCCESS;
    }

	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {

		if (!worldIn.isRemote) {
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
			int structType = playerData.findActiveStructType(currentDimension);
			if (structType < 0) {
				return;
			}
	    	Structure structure = playerData.getStructure(currentDimension, structType);

	    	if (!(structType == Refs.COPYPASTE && structure.isLoaded()) || structType == Refs.BUILDING || structType == Refs.DESTRUCT) {
				structure.deleteEndBlockPos();
	    	}
			LazyBuilder.updateClientStructure(currentDimension, player, structure);
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
