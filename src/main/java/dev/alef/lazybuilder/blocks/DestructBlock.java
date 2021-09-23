package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DestructBlock extends Block {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
    public DestructBlock(Properties properties) {
		super(properties);
    }
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    	
    	if (!worldIn.isRemote) {
		
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer((PlayerEntity) placer);
	    	Structure building = playerData.getStructure(currentDimension, Refs.BUILDING);
	    	Structure copypaste = playerData.getStructure(currentDimension, Refs.COPYPASTE);
	    	Structure destruct = playerData.getStructure(currentDimension, Refs.DESTRUCT);
	    	
			building.destroy(worldIn, (PlayerEntity) placer, pos, true, true);
			building.setActive(false);
			copypaste.destroy(worldIn, (PlayerEntity) placer, pos, true, !copypaste.isLoaded());
			copypaste.setActive(false);
			
			destruct.destroy(worldIn, (PlayerEntity) placer, pos, true, true);
			destruct.setStartBlockPos(pos);
			destruct.setActive(true);
			
			LazyBuilder.updateClientStructure(currentDimension, (PlayerEntity) placer, destruct);
    	}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
		if (!worldIn.isRemote) {
		
			IPlayerData playerData = PlayerData.getFromPlayer(player);
	    	Structure destruct = playerData.getStructure(LazyBuilder.getDimension(worldIn), Refs.DESTRUCT);
	    	
	   	  	destruct.destroy(worldIn, player, pos, false, true);
			destruct.setActive(false);
			
			LazyBuilder.updateClientStructure(LazyBuilder.getDimension(worldIn), player, destruct);
		}
   	  	super.onBlockHarvested(worldIn, pos, state, player);
    }
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof DestructBlock) {
				return true;
			}
		}
		return false;
	}
}