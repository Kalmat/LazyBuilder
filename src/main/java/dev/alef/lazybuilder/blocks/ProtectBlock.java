package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.ProtectBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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
		
    	Structure protectBlock = LazyBuilder.PROTECT_LIST.get(worldIn, (PlayerEntity) placer);
    	
    	protectBlock.setStartBlockPos(pos, (PlayerEntity) placer);
    	protectBlock.setActive(true);
    	
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
		return ActionResultType.SUCCESS;
    }
    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
    	Structure protectBlock = LazyBuilder.PROTECT_LIST.get(worldIn, player);
    	
    	protectBlock.deleteStartBlockPos();
    	protectBlock.setActive(false);

   	  	super.onBlockHarvested(worldIn, pos, state, player);
    }
	
	public static boolean isBlockProtected(World worldIn, PlayerEntity playerProtect, PlayerEntity playerBreaking, BlockPos protectBlockPos, BlockPos blockPos) {
		
    	int numItems = 0;
		int distance = 0;
		boolean ret = false;
		
		if (protectBlockPos != null) {
			
			numItems = ProtectBlockTileEntity.countItems(worldIn, playerProtect, protectBlockPos);
			distance = Math.min(Refs.protectPerItem * numItems, Refs.maxProtectArea);
			
			if (numItems > 0 && isPlaced(worldIn, protectBlockPos) && !playerProtect.equals(playerBreaking) &&
					(CalcVector.getLength(protectBlockPos, blockPos) < distance || CalcVector.getHeight(protectBlockPos, blockPos) < distance)) {
				ret = true;
			}
		}
		return ret;
	}
	
	public static void checkProtectionCost(World worldIn, PlayerEntity playerProtect, BlockPos protectBlockPos) {
		
    	Structure protectBlock = LazyBuilder.PROTECT_LIST.get(worldIn, playerProtect);

    	protectBlock.setProtectCount(protectBlock.getProtectCount() + 1);
    	
    	if (protectBlock.getProtectCount() >= Refs.protectionCost) {
    		protectBlock.setProtectCount(0);
        	ProtectBlockTileEntity.extractItem(worldIn, playerProtect, protectBlock.getStartBlockPos(), 1);
    	}
	}
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof ProtectBlock) {
				return true;
			}
		}
		return false;
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
}