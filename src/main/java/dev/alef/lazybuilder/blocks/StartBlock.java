package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.BuildBot;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.StartBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartBlock extends Block {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

    public StartBlock(Properties properties) {
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
	
			copypaste.destroy(worldIn, (PlayerEntity) placer, pos, true, !copypaste.isLoaded());
			copypaste.setActive(false);
			destruct.destroy(worldIn, (PlayerEntity) placer, pos, true, true);
	    	destruct.setActive(false);
			
			building.destroy(worldIn, (PlayerEntity) placer, pos, true, true);
			building.setStartBlockPos(pos);
			building.setActive(true);
			
			LazyBuilder.updateClientStructure(currentDimension, (PlayerEntity) placer, building);
    	}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
    
	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (!player.isSneaking()) {
			if (!worldIn.isRemote) {
				final TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof StartBlockTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (StartBlockTileEntity) tileEntity, pos);
				}
			}
		}
		else if (!worldIn.isRemote && new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ()).equals(pos.offset(Direction.UP, 1))) {
			
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
	    	Structure building = playerData.getStructure(currentDimension, Refs.BUILDING);
	    	
	    	if (building.isActive() && !building.isLoaded() && building.getMidBlockListSize() == 0 && building.getEndBlockPos() == null && building.getStartBlockPos().equals(pos)) {
		    	for (int i = 0; i < Math.max(Refs.magicColumnHeight, 10); ++i) {
		    		building.addToClipBoard(Blocks.DIRT.getDefaultState());
		    	}
		    	
		    	BlockPos startPos = pos;
	    		List<BlockPos> midPos = new ArrayList<BlockPos>();
				BlockPos endPos = pos.offset(Direction.UP, Refs.magicColumnHeight);
				BuildBot buildBot = new BuildBot();
				buildBot.build(worldIn, player, startPos, midPos, endPos, building.getClipBoard(), false);
				
				startPos = endPos.offset(Direction.DOWN, 1).offset(Direction.WEST, 2).offset(Direction.SOUTH, 2);
				midPos.add(startPos.offset(Direction.NORTH, 4));
				endPos = startPos.offset(Direction.EAST, 4);
				buildBot.build(worldIn, player, startPos, midPos, endPos, building.getClipBoard(), false);
				player.setPosition(pos.getX(), pos.getY() + Refs.magicColumnHeight + 1, pos.getZ());
				
		    	building.deleteClipBoard();
	    	}
		}
		return ActionResultType.SUCCESS;
    }
    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
		if (!worldIn.isRemote) {
			
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer((PlayerEntity) player);
	    	Structure building = playerData.getStructure(currentDimension, Refs.BUILDING);
	    	
	   	  	building.destroy(worldIn, player, pos, false, true);
			building.setActive(false);
			
			LazyBuilder.updateClientStructure(currentDimension, player, building);
		}
   	  	super.onBlockHarvested(worldIn, pos, state, player);
    }
	
	@Override
	public boolean hasTileEntity(final BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
		return TileEntityList.START_BLOCK.get().create();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		
		if (oldState.getBlock() != newState.getBlock()) {
			
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			
			if (tileEntity instanceof StartBlockTileEntity) {
				
				final ItemStackHandler inventory = ((StartBlockTileEntity) tileEntity).inventory;
				
				for (int slot = 0; slot < inventory.getSlots(); ++slot)
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
			}
		}
		super.onReplaced(oldState, worldIn, pos, newState, isMoving);
	}
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof StartBlock) {
				return true;
			}
		}
		return false;
	}
}