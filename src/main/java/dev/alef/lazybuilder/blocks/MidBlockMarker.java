package dev.alef.lazybuilder.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MidBlockMarker extends Block {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
    public MidBlockMarker(Properties properties) {
		super(properties);
    }
    
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		worldIn.destroyBlock(pos, !((PlayerEntity) placer).isCreative());
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
}

