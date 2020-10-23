package dev.alef.lazybuilder.tileentity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.container.ProtectBlockContainer;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.tileentity.ProtectBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ProtectBlockTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private static final String INVENTORY_TAG = "inventory";
	private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal = LazyOptional.of(() -> this.inventory);
	public final ItemStackHandler inventory = new ItemStackHandler(Refs.protectContainerRows*Refs.containerCols) {
		
		@Override
		public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
			return true;
		}

		@Override
		protected void onContentsChanged(final int slot) {
			super.onContentsChanged(slot);
			ProtectBlockTileEntity.this.markDirty();
		}
	};

	public ProtectBlockTileEntity() {
		super(TileEntityList.PROTECT_BLOCK.get());
	}

	@Override
	public void tick() {
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nullable final Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == null)
				return inventoryCapabilityExternal.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt) {
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public void func_230337_a_(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
		super.func_230337_a_(p_230337_1_, p_230337_2_);
		this.inventory.deserializeNBT(p_230337_2_.getCompound(INVENTORY_TAG));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.put(INVENTORY_TAG, this.inventory.serializeNBT());
		return compound;
	}
	
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() {
		final CompoundNBT tag = new CompoundNBT();
		return new SUpdateTileEntityPacket(this.pos, 0, tag);
	}

	@Nonnull
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}

	@Override
	public void remove() {
		super.remove();
		inventoryCapabilityExternal.invalidate();
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(BlockList.protect_block.getTranslationKey());
	}

	@Nonnull
	@Override
	public Container createMenu(final int windowId, final PlayerInventory inventory, final PlayerEntity player) {
		return new ProtectBlockContainer(windowId, inventory, this);
	}
	
	
	public static int countItems(World worldIn, PlayerEntity player, BlockPos pos) {
		
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		ItemStack stack;
		int slot;
		int count = 0;
		
		if (tileEntity instanceof ProtectBlockTileEntity) {
			for (int row = 0; row < Refs.protectContainerRows; ++row) {
				for (int col = 0; col < Refs.containerCols; ++col) {
					slot = (row * Refs.containerCols) + col;
					stack = ((ProtectBlockTileEntity) tileEntity).inventory.extractItem(slot, 64, true);
					count += stack.getCount();
				}
			}
		}
		return count;
	}
	
	public static boolean extractItem(World worldIn, PlayerEntity player, BlockPos pos, int items) {
		
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		ItemStack stack;
		int slot;
		int count = 0;
		boolean ret = false;
		
		if (tileEntity instanceof ProtectBlockTileEntity) {
			for (int row = 0; row < Refs.protectContainerRows; ++row) {
				for (int col = 0; col < Refs.containerCols; ++col) {
					slot = Refs.protectContainerRows * row + col;
					stack = ((ProtectBlockTileEntity) tileEntity).inventory.extractItem(slot, items, false);
					count += stack.getCount();
					if (count >= items) {
						ret = true;
						break;
					}
					else {
						items -= count;
					}
				}
			}
		}
		return ret;
	}

	public static List<BlockState> fillClipBoardFromContainer(World worldIn, PlayerEntity player, BlockPos pos) {
		
		List<BlockState> clipBoard = new ArrayList<BlockState>();
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		ItemStack stack;
		Item item;
		BlockState block;
		int slot;
		
		if (tileEntity instanceof ProtectBlockTileEntity) {
			for (int row = 0; row < Refs.protectContainerRows; ++row) {
				for (int col = 0; col < Refs.containerCols; ++col) {
					slot = Refs.protectContainerRows * row + col;
					stack = ((ProtectBlockTileEntity) tileEntity).inventory.extractItem(slot, 64, false);
					for (int i = 0; i < stack.getCount(); ++i) {
						block = Block.getBlockFromItem(stack.getItem()).getDefaultState();
						if (block.getBlock().equals(Blocks.AIR)) {
							item = stack.getItem();
							if (item.equals(Items.DIAMOND)) {
								block = Blocks.DIAMOND_BLOCK.getDefaultState();
							}
							else if (item.equals(Items.EMERALD)) {
								block = Blocks.EMERALD_BLOCK.getDefaultState();
							}
						}
						clipBoard.add(block);
					}
				}
			}
		}
		return clipBoard;
	}
	
	public static void fillContainerFromClipBoard(World worldIn, PlayerEntity player, BlockPos pos, List<BlockState> clipBoard, int start, int end) {
		
		final TileEntity tileEntity = worldIn.getTileEntity(pos);

		if (tileEntity instanceof ProtectBlockTileEntity) {
		
			ItemStack stack = new ItemStack(Blocks.AIR, 0);
			Block block;
			Item item = Items.AIR;
			Item nextItem;
			int slot = 0;
			
			if (start < 0) {
				start = 0;
			}
			
			if (end > clipBoard.size() || end < start || end - start > clipBoard.size()) {
				end = clipBoard.size();
			}
			
			for (int i = start; i < end; ++i)  {
				block = clipBoard.get(i).getBlock();
				item = getItemFromBlock(block);
				if (!item.equals(Items.AIR) ) {
					stack = new ItemStack(item, 1);
					for (int j = 1; j <= 64 && i < end; ++j) {
						nextItem = getItemFromBlock(clipBoard.get(i).getBlock());
						if (nextItem.equals(item)) {
							stack.setCount(j);
							i += 1;
						}
						else {
							i -= 1;
							break;
						}
					}
					if (slot < Refs.protectContainerRows * Refs.containerCols) {
						((ProtectBlockTileEntity) tileEntity).inventory.insertItem(slot, stack, false);
						slot += 1;
					}
					else {
						break;
					}
				}
			}
		}
	}
	
	private static Item getItemFromBlock(Block block) {
		
		Item item = Items.AIR;
		
		if (block.equals(Blocks.DIAMOND_BLOCK)) {
			item = Items.DIAMOND;
		}
		else if (block.equals(Blocks.EMERALD_BLOCK)) {
			item = Items.EMERALD;
		}
		
	return item;
	}
}

