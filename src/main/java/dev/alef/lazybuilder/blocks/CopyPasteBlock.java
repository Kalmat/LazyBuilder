package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.bots.CopyPasteBot;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.CopyPasteBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Random;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyPasteBlock extends Block {
	
	@SuppressWarnings("unused")
	private final Logger LOGGER = LogManager.getLogger();
	
    public CopyPasteBlock(Properties properties) {
		super(properties);
    }
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		
    	Structure building = LazyBuilder.BUILD_LIST.get(worldIn, (PlayerEntity) placer);
    	Structure copypaste = LazyBuilder.COPYPASTE_LIST.get(worldIn, (PlayerEntity) placer);

		building.destroy(worldIn, pos, state, (PlayerEntity) placer, true, true);
    	building.setActive(false);

    	if (copypaste.isLoaded()) {
    		copypaste.shift(pos);
    	}

    	copypaste.destroy(worldIn, pos, state, (PlayerEntity) placer, true, !copypaste.isLoaded());
    	copypaste.setStartBlockPos(pos, (PlayerEntity) placer);
    	copypaste.setActive(true);

    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

	
	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (!player.isSneaking()) {	
			if (!worldIn.isRemote) {
				final TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof CopyPasteBlockTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (CopyPasteBlockTileEntity) tileEntity, pos);
				}
			}
		}
		else {
	    	Structure copypaste = LazyBuilder.COPYPASTE_LIST.get(worldIn, player);
	    	
			if (copypaste.isActive() && copypaste.isLoaded()) {
				copypaste.destroy(worldIn, pos, state, player, false, false);
				CopyPasteBot.Paste(worldIn, player, copypaste.getStartBlockPos(), copypaste.getMidBlockList(), copypaste.getEndBlockPos(), copypaste.getClipBoard());
				copypaste.destroy(worldIn, pos, state, player, false, true);
				if (worldIn.isRemote) {
					LazyBuilderClient.setTextActive(Refs.pasteMsg, 200);
				}
			}
			else if (worldIn.isRemote) {
				LazyBuilderClient.setTextActive(Refs.pasteEmptyMsg, 200);
			}
		}
		return ActionResultType.SUCCESS;
	}
	    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
    	Structure copypaste = LazyBuilder.COPYPASTE_LIST.get(worldIn, player);
    	
    	if (player.isSneaking()) {
			copypaste.destroy(worldIn, pos, state, player, false, true);
    	}
    	else {
			if (copypaste.isLoaded()) {
				copypaste.destroy(worldIn, pos, state, player, false, false);
			}
			else {
				copypaste.destroy(worldIn, pos, state, player, false, true);
			}
    	}
		super.onBlockHarvested(worldIn, pos, state, player);
    }

	@Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		Structure copypaste = LazyBuilder.COPYPASTE_LIST.get(worldIn, LazyBuilder.PLAYER);
		
		if (copypaste.isLoaded()) {
			int effects = 3;
			List<Double> d = new ArrayList<Double>();

			for (int i = 0; i < effects; ++i) {
				d = CalcVector.randomSpherePoint(((double) pos.getX()) + 0.5D, ((double) pos.getY()) + 0.5D, ((double) pos.getZ()) + 0.5D, 0.7D);
				worldIn.addParticle(ParticleTypes.END_ROD, d.get(0), d.get(1), d.get(2), 0.0D, 0.0D, 0.0D);
			}
		}
		super.animateTick(stateIn, worldIn, pos, rand);
	}
	
	public boolean hasTileEntity(final BlockState state) {
		return true;
	}

	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof CopyPasteBlock) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
		return TileEntityList.COPYPASTE_BLOCK.get().create();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		
		if (oldState.getBlock() != newState.getBlock()) {
			
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			
			if (tileEntity instanceof CopyPasteBlockTileEntity) {
				
				final ItemStackHandler inventory = ((CopyPasteBlockTileEntity) tileEntity).inventory;
				
				for (int slot = 0; slot < inventory.getSlots(); ++slot)
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
			}
		}
		super.onReplaced(oldState, worldIn, pos, newState, isMoving);
	}
}