package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
import dev.alef.lazybuilder.render.LazyBuilderRender;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.ProtectBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtectBlock extends Block {

	@SuppressWarnings("unused")
	private final static Logger LOGGER = LogManager.getLogger();
	
    public ProtectBlock(Properties properties) {
		super(properties);
    }
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

    	if (!worldIn.isRemote) {
    		
    		ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer((PlayerEntity) placer);
	    	Structure protectBlock = playerData.getStructure(currentDimension, Refs.PROTECT);
	    	
	    	protectBlock.addMidBlockPos(pos);
	    	protectBlock.setActive(true);
    	}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
    
	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (!player.isSneaking()) {
			if (!worldIn.isRemote) {
				final TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof ProtectBlockTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (ProtectBlockTileEntity) tileEntity, pos);
				}
			}
		}
		else {
			if (worldIn.isRemote) {
				LazyBuilderRender.switchProtectArea((ClientWorld) worldIn, (ClientPlayerEntity) player, pos);
			}
		}
		return ActionResultType.SUCCESS;
    }
    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
    	if (!worldIn.isRemote) {
		
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
	    	Structure protectBlock = playerData.getStructure(currentDimension, Refs.PROTECT);
	    	
	    	if (protectBlock.isActive() && protectBlock.getMidBlockListSize() > 0) {
		    	for (int i = 0; i < protectBlock.getMidBlockListSize(); ++i) {
		    		if (pos.equals(protectBlock.getMidBlockElement(i))) {
		    			protectBlock.deleteMidBlockElement(i);
		    			break;
		    		}
		    	}
		    	if (protectBlock.getMidBlockListSize() == 0) {
		    		protectBlock.setActive(false);
		    	}
	    	}
    	}
    	else if (worldIn.isRemote) {
			LazyBuilderRender.deleteProtectArea(pos);
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
		return TileEntityList.PROTECT_BLOCK.get().create();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		
		if (oldState.getBlock() != newState.getBlock()) {
			
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			
			if (tileEntity instanceof ProtectBlockTileEntity) {
				
				final ItemStackHandler inventory = ((ProtectBlockTileEntity) tileEntity).inventory;
				
				for (int slot = 0; slot < inventory.getSlots(); ++slot)
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
			}
		}
		super.onReplaced(oldState, worldIn, pos, newState, isMoving);
	}
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof ProtectBlock) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBlockProtected(World worldIn, BlockPos blockPos, @Nullable PlayerEntity playerBreaking, int explosionBonus) {
		
		for (PlayerEntity player : worldIn.getPlayers()) {
			
			ResourceLocation currentDimension = LazyBuilder.getDimension(worldIn);
			IPlayerData playerData = PlayerData.getFromPlayer(player);
	    	Structure protectBlocks = playerData.getStructure(currentDimension, Refs.PROTECT);
			
    		int numItems = 0;
    		int distance = 0;
			for (BlockPos block : protectBlocks.getMidBlockList()) {
				
				if (block != null && block.withinDistance(blockPos, Refs.protectMaxArea + explosionBonus + 1) && 
						ProtectBlock.isPlaced(worldIn, block) && (playerBreaking == null || (playerBreaking != null && !player.getUniqueID().equals(playerBreaking.getUniqueID())))) {
					
					numItems = ProtectBlockTileEntity.countItems(worldIn, block);
					distance = Math.min(Refs.protectPerItem * numItems, Refs.protectMaxArea) + explosionBonus;
					if (block.withinDistance(blockPos, distance + 1)) {
						return true;
					}
				}
			}
    	}
		return false;
	}
}
