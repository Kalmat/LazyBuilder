package dev.alef.lazybuilder.container;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.lists.ContainerList;
import dev.alef.lazybuilder.tileentity.CopyPasteBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.SlotItemHandler;

public class CopyPasteBlockContainer extends Container {
	
	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	public final CopyPasteBlockTileEntity tileEntity;
	private final IWorldPosCallable canInteractWithCallable;


	public CopyPasteBlockContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
		this(windowId, playerInventory, getTileEntity(playerInventory, data));
	}

	public CopyPasteBlockContainer(final int windowId, final PlayerInventory playerInventory, final CopyPasteBlockTileEntity tileEntity) {

		super(ContainerList.COPYPASTE_BLOCK.get(), windowId);
		this.tileEntity = tileEntity;
		this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

		// Tile inventory slot(s)
		int InventoryStartX = 8;
		int InventoryStartY = 17;
		int slotSizePlus2 = 18; // slots are 16x16, plus 2 (for spacing/borders) is 18x18
		
		// Chest Inventory
		for (int row = 0; row < Refs.containerRows; ++row) {
			for (int column = 0; column < Refs.containerCols; ++column) {
				this.addSlot(new SlotItemHandler(tileEntity.inventory, (row * 9) + column, InventoryStartX + (column * slotSizePlus2), InventoryStartY + (row * slotSizePlus2)));
			}
		}
		
		InventoryStartX = 8;
		InventoryStartY = 84;

		// Player Top Inventory slots
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, InventoryStartX + (column * slotSizePlus2), InventoryStartY + (row * slotSizePlus2)));
			}
		}

		final int playerHotbarY = InventoryStartY + slotSizePlus2 * 3 + 4;
		// Player Hotbar slots
		for (int column = 0; column < 9; ++column) {
			this.addSlot(new Slot(playerInventory, column, InventoryStartX + (column * slotSizePlus2), playerHotbarY));
		}
	}

	private static CopyPasteBlockTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
		Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
		Objects.requireNonNull(data, "data cannot be null!");
		final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof CopyPasteBlockTileEntity)
			return (CopyPasteBlockTileEntity) tileAtPos;
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(final PlayerEntity player, final int index) {
		
		ItemStack itemstack = ItemStack.EMPTY;
		final Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack()) {
			
			final ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			Item item = itemstack1.getItem();
			Block block = Block.getBlockFromItem(itemstack1.getItem());
			boolean allowed = block.equals(Blocks.REDSTONE_BLOCK) || item.equals(Items.GOLD_INGOT) || item.equals(Items.EMERALD);
			final int containerSlots = Refs.containerRows * Refs.containerCols;
			
			if (index < containerSlots) {
				if (!this.mergeItemStack(itemstack1, containerSlots, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				else if (!allowed) {
					if (player.world.isRemote) {
						LazyBuilderClient.setTextActive(Refs.onlyBlocksMsg, 200);
					}
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, containerSlots, false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
	             slot.onSlotChanged();
			}
		}
		return itemstack;
	}
	
	@Nonnull
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		
		final int containerSlots = Refs.containerRows * Refs.containerCols;
		
		if (slotId >= containerSlots && slotId < this.inventorySlots.size()) {
			
			final Slot slot = this.inventorySlots.get(slotId);
			final ItemStack slotStack = slot.getStack();
			Item item = slotStack.getItem();
			Block block = Block.getBlockFromItem(slotStack.getItem());
			boolean allowed = !slot.getHasStack() || (slot.getHasStack() && (block.equals(Blocks.REDSTONE_BLOCK) || item.equals(Items.GOLD_INGOT) || item.equals(Items.EMERALD)));
	
			if (!allowed) {
				if (player.world.isRemote) {
					LazyBuilderClient.setTextActive(Refs.onlyItemsMsg, 200);
				}
				return ItemStack.EMPTY;
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	protected void layoutContainer(PlayerInventory playerInventory, Inventory chestInventory, int xSize, int ySize) {
    	
    	for (int chestRow = 0; chestRow < 8; chestRow++) {
    		for (int chestCol = 0; chestCol < 12; chestCol++) {
                    this.addSlot(new Slot(chestInventory, chestCol + chestRow * 8, 12 + chestCol * 18, 8 + chestRow * 18));
            }
        }

        int leftCol = (xSize - 162) / 2 + 1;
        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++)
        {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++)
            {
                this.addSlot(new Slot(playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, ySize - (4 - playerInvRow) * 18 - 10));
            }

        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++)
        {
            this.addSlot(new Slot(playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, ySize - 24));
        }
    }

	@Override
	public boolean canInteractWith(@Nonnull final PlayerEntity player) {
		return isWithinUsableDistance(canInteractWithCallable, player, BlockList.copy_paste_block);
	}

}
