package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
		
    	Structure structure = LazyBuilder.BUILD_LIST.get(worldIn, (PlayerEntity) placer);
    	
		int active = Refs.BUILDING;
    	if (!structure.isActive()) {
    		active = Refs.COPYPASTE;
        	structure = LazyBuilder.COPYPASTE_LIST.get(worldIn, (PlayerEntity) placer);
    	}

		int placed = -1;
    	int height = 0;
    	List<String> msg = null;
		BlockPos startBlock = structure.getStartBlockPos();
		BlockPos firstMidBlock = null;
		BlockPos endBlock = structure.getEndBlockPos();

		int midSize = structure.getMidBlockListSize();
		if (startBlock != null) {
			if (!structure.isLoaded() ) {
			   	if (midSize > 0) {
					firstMidBlock = structure.getMidBlockElement(0);
				}
			   	else {
			   		firstMidBlock = pos;
			   	}
				height = CalcVector.getHeight(startBlock, firstMidBlock);
				if (endBlock == null) {
					if (active == Refs.BUILDING) {
						if (height > 0 || midSize == 0) {
							placed = structure.setMidBlockPos(pos);
						}
						else {
							msg = Refs.midTooManyForHorizontalMsg;
						}
					}
					else if (midSize == 0) {
						if (CalcVector.getHeight(startBlock, firstMidBlock) == 0) {
							msg = Refs.copyHeightNotZeroMsg;
						}
						else {
							placed = structure.setMidBlockPos(pos);
						}
					}
					else if (midSize < 2) {
						placed = structure.setMidBlockPos(pos);
					}
					else {
						msg = Refs.midTooManyForCopyMsg;
					}
				}
				else {
					msg = Refs.midBeforeEndMsg;
				}
			}
			else {
    			msg = Refs.copyAlreadyDoneMsg;
			}
		}
		else {
			msg = Refs.midStartFirstMsg;
		}
		
		if (placed == -1) {
			worldIn.destroyBlock(pos, !((PlayerEntity) placer).isCreative());
		}
		else if (placed == 1) {
			Block.replaceBlock(worldIn.getBlockState(pos), BlockList.mid_block_marker.getDefaultState(), worldIn, pos, 3);
		}
		if (worldIn.isRemote && msg != null) {
			LazyBuilderClient.setTextActive(msg, 200);
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
	
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
    	Structure structure = LazyBuilder.BUILD_LIST.get(worldIn, player);
		int structType = Refs.BUILDING;

    	if (!structure.isActive()) {
        	structure = LazyBuilder.COPYPASTE_LIST.get(worldIn, player);
        	structType = Refs.COPYPASTE;
    	}
		
		for (int i = 0; i < structure.getMidBlockList().size(); ++i) {
			if (pos.equals(structure.getMidBlockElement(i))) {
				while (i < structure.getMidBlockListSize()) {
					worldIn.destroyBlock(structure.getMidBlockElement(i), !player.isCreative());
			    	if (!structure.isLoaded() || structType == Refs.BUILDING) {
			    		structure.deleteMidBlockElement(i);
			    	}
				}
			}
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
