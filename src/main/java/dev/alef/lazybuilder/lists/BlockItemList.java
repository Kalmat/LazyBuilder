package dev.alef.lazybuilder.lists;

import java.util.function.ToIntFunction;

import dev.alef.lazybuilder.Refs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.item.BlockItem;

@SuppressWarnings("unused")
public class BlockItemList {
	
    public static final DeferredRegister<Item> BLOCKITEM_LIST = DeferredRegister.create(ForgeRegistries.ITEMS, Refs.MODID);
    public static final RegistryObject<Item> START_BLOCK_ITEM = BLOCKITEM_LIST.register("start_block", () -> new BlockItem(BlockList.START_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> MID_BLOCK_ITEM = BLOCKITEM_LIST.register("mid_block", () -> new BlockItem(BlockList.MID_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> MID_BLOCK_MARKER_ITEM = BLOCKITEM_LIST.register("mid_block_marker", () -> new BlockItem(BlockList.MID_BLOCK_MARKER.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> END_BLOCK_ITEM = BLOCKITEM_LIST.register("end_block", () -> new BlockItem(BlockList.END_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> COPY_PASTE_BLOCK_ITEM = BLOCKITEM_LIST.register("copy_paste_block", () -> new BlockItem(BlockList.COPY_PASTE_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> DESTRUCT_BLOCK_ITEM = BLOCKITEM_LIST.register("destruct_block", () -> new BlockItem(BlockList.DESTRUCT_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    public static final RegistryObject<Item> PROTECT_BLOCK_ITEM = BLOCKITEM_LIST.register("protect_block", () -> new BlockItem(BlockList.PROTECT_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
}
