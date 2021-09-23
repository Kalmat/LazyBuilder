package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MidBlock extends Block {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
    public MidBlock(Properties properties) {
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
			
			int placed = -1;
	    	int height = 0;
			boolean drop = !((PlayerEntity) placer).isCreative();
			BlockPos startBlock = structure.getStartBlockPos();
			boolean startPlaced = (structType != Refs.EMPTY && (structType == Refs.BUILDING && StartBlock.isPlaced(worldIn, startBlock)) || (structType == Refs.COPYPASTE && CopyPasteBlock.isPlaced(worldIn, startBlock)) || (structType == Refs.DESTRUCT && DestructBlock.isPlaced(worldIn, startBlock)));
			BlockPos firstMidBlock = null;
			BlockPos endBlock = structure.getEndBlockPos();
	
			int midSize = structure.getMidBlockListSize();
			if (startPlaced && startBlock != null) {
				if (structType == Refs.COPYPASTE) {
					msg = Refs.midTooManyMsg;
					if (structure.isLoaded()) {
		    			msg = Refs.copyAlreadyDoneMsg;
					}
				}
				else if (structType == Refs.DESTRUCT) {
					msg = Refs.midTooManyMsg;
				}
				else {
				   	if (midSize > 0) {
						firstMidBlock = structure.getMidBlockElement(0);
					}
				   	else {
				   		firstMidBlock = pos;
				   	}
					height = CalcVector.getHeight(startBlock, firstMidBlock);
					if (endBlock == null) {
						if (height > 0 || midSize == 0) {
							placed = structure.addMidBlockPos(pos);
						}
						else {
							msg = Refs.midTooManyForHorizontalMsg;
						}
					}
					else {
						msg = Refs.midBeforeEndMsg;
					}
				}
			} 
			else {
				msg = Refs.midStartFirstMsg;
			}
			
			if (placed == -1) {
				worldIn.destroyBlock(pos, drop);
			}
			else if (placed == 1) {
				Block.replaceBlock(worldIn.getBlockState(pos), BlockList.mid_block_marker.getDefaultState(), worldIn, pos, 3);
			}
			LazyBuilder.updateClientStructure(currentDimension, (PlayerEntity) placer, structure);
			if (msg != null) {
				LazyBuilder.showClientText(msg, 200, (PlayerEntity) placer);
			}
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
	
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		
		if (!worldIn.isRemote) {
			
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
			int structType = playerData.findActiveStructType(currentDimension);
			if (structType < 0) {
				return;
			}
	    	Structure structure = playerData.getStructure(currentDimension, structType);
	   	
			for (int i = 0; i < structure.getMidBlockListSize(); ++i) {
				if (pos.equals(structure.getMidBlockElement(i))) {
					while (i < structure.getMidBlockListSize()) {
						worldIn.destroyBlock(structure.getMidBlockElement(i), !player.isCreative());
						if ((structType == Refs.COPYPASTE && !structure.isLoaded()) || structType == Refs.BUILDING || structType == Refs.DESTRUCT) {
				    		structure.deleteMidBlockElement(i);
					    }
						else {
							++i;
						}
					}
					break;
				}
			}
			
			if (structure.getEndBlockPos() != null ) {
				worldIn.destroyBlock(structure.getEndBlockPos(), !player.isCreative());
		    	if ((structType == Refs.COPYPASTE && !structure.isLoaded()) || structType == Refs.BUILDING || structType == Refs.DESTRUCT) {
		    		structure.deleteEndBlockPos();
		    	}
			}
			LazyBuilder.updateClientStructure(currentDimension, player, structure);
		}
		super.onBlockHarvested(worldIn, pos, state, player);
    }

	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof MidBlock || worldIn.getBlockState(pos).getBlock() instanceof MidBlockMarker) {
				return true;
			}
		}
		return false;
	}
}
