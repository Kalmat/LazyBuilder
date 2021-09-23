package dev.alef.lazybuilder.blocks;

import dev.alef.lazybuilder.LazyBuilder;
import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.bots.CopyPasteBot;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
import dev.alef.lazybuilder.render.LazyBuilderRender;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyPasteBlock extends Block {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

    public CopyPasteBlock(Properties properties) {
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
			destruct.destroy(worldIn, (PlayerEntity) placer, pos, true, true);
	    	destruct.setActive(false);

	    	if (copypaste.isLoaded()) {
	    		copypaste.shift(pos);
	    	}

	    	copypaste.destroy(worldIn, (PlayerEntity) placer, pos, true, !copypaste.isLoaded());
	    	copypaste.setStartBlockPos(pos);
	    	copypaste.setActive(true);
			LazyBuilder.updateClientStructure(currentDimension, (PlayerEntity) placer, copypaste);
    	}
    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

	@Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (!worldIn.isRemote) {
			if (!player.isSneaking()) {	
				final TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof CopyPasteBlockTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (CopyPasteBlockTileEntity) tileEntity, pos);
				}
			}
			else {
				IPlayerData playerData = PlayerData.getFromPlayer(player);
		    	Structure copypaste = playerData.getStructure(LazyBuilder.getDimension(worldIn), Refs.COPYPASTE);
		    	
				if (copypaste.isActive() && copypaste.isLoaded()) {
					CopyPasteBot copypasteBot = new CopyPasteBot();
					copypaste.destroy(worldIn, player, pos, false, false);
					copypasteBot.paste(worldIn, copypaste.getStartBlockPos(), copypaste.getNotRotatedEndBlockPos(), copypaste.getClipBoard(), copypaste.getRotations());
					if (!player.isCreative() ) {
						copypaste.destroy(worldIn, player, pos, false, true);
						LazyBuilder.updateClientStructure(LazyBuilder.getDimension(worldIn), player, copypaste);
					}
					LazyBuilder.showClientText(Refs.pasteMsg, 200, player);
				}
				else {
					LazyBuilder.showClientText(Refs.pasteEmptyMsg, 200, player);
				}
			}
		}
		return ActionResultType.SUCCESS;
	}
	    
	@Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)  {
		
		if (!worldIn.isRemote) {
		
			IPlayerData playerData = PlayerData.getFromPlayer(player);
	    	Structure copypaste = playerData.getStructure(LazyBuilder.getDimension(worldIn),Refs.COPYPASTE);
	    	
	    	if (player.isSneaking()) {
				copypaste.destroy(worldIn, player, pos, false, true);
	    	}
	    	else {
				if (copypaste.isLoaded()) {
					copypaste.destroy(worldIn, player, pos, false, false);
				}
				else {
					copypaste.destroy(worldIn, player, pos, false, true);
			    	copypaste.setActive(false);
				}
	    	}
			copypaste.setActive(false);
			LazyBuilder.updateClientStructure(LazyBuilder.getDimension(worldIn), player, copypaste);
		}
		super.onBlockHarvested(worldIn, pos, state, player);
    }

	//CLIENT
	@Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		Structure struct = LazyBuilderRender.getClientStructure();
		
		if (struct.getStructType() == Refs.COPYPASTE && struct.isLoaded()) {
			int effects = 3;
			List<Double> d = new ArrayList<Double>();

			for (int i = 0; i < effects; ++i) {
				d = CalcVector.randomSpherePoint(((double) pos.getX()) + 0.5D, ((double) pos.getY()) + 0.5D, ((double) pos.getZ()) + 0.5D, 0.7D);
				worldIn.addParticle(ParticleTypes.END_ROD, d.get(0), d.get(1), d.get(2), 0.0D, 0.0D, 0.0D);
			}
		}
		super.animateTick(stateIn, worldIn, pos, rand);
	}
	
	@Override
	public boolean hasTileEntity(final BlockState state) {
		return true;
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
	
	public static boolean isPlaced(World worldIn, final BlockPos pos) {
		
		if (pos != null) {
			if (worldIn.getBlockState(pos).getBlock() instanceof CopyPasteBlock) {
				return true;
			}
		}
		return false;
	}
}