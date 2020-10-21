package dev.alef.lazybuilder.lists;

import java.util.function.ToIntFunction;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.blocks.CopyPasteBlock;
import dev.alef.lazybuilder.blocks.EndBlock;
import dev.alef.lazybuilder.blocks.MidBlock;
import dev.alef.lazybuilder.blocks.MidBlockMarker;
import dev.alef.lazybuilder.blocks.StartBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class BlockList {
    
	
    private static ToIntFunction<BlockState> lightValue = (p_235830_0_) -> {return 7;};
    public static Block start_block = new StartBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block copy_paste_block = new CopyPasteBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block mid_block = new MidBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block mid_block_marker = new MidBlockMarker(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block end_block = new EndBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    
    public static final DeferredRegister<Block> BLOCK_LIST = DeferredRegister.create(ForgeRegistries.BLOCKS, Refs.MODID);

    private static final RegistryObject<Block> START_BLOCK = BLOCK_LIST.register("start_block", () -> start_block);
    private static final RegistryObject<Block> COPY_PASTE_BLOCK = BLOCK_LIST.register("copy_paste_block", () -> copy_paste_block);
    private static final RegistryObject<Block> MID_BLOCK = BLOCK_LIST.register("mid_block", () -> mid_block);
    private static final RegistryObject<Block> MID_BLOCK_MARKER = BLOCK_LIST.register("mid_block_marker", () -> mid_block_marker);
    private static final RegistryObject<Block> END_BLOCK = BLOCK_LIST.register("end_block", () -> end_block);
    
    public static final DeferredRegister<Item> ITEM_LIST = DeferredRegister.create(ForgeRegistries.ITEMS, Refs.MODID);
    
	private static final RegistryObject<Item> START_BLOCK_ITEM = ITEM_LIST.register("start_block", () -> 
													new BlockItem(START_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
	private static final RegistryObject<Item> COPY_PASTE_BLOCK_ITEM = ITEM_LIST.register("copy_paste_block", () -> 
    												new BlockItem(COPY_PASTE_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    private static final RegistryObject<Item> MID_BLOCK_ITEM = ITEM_LIST.register("mid_block", () -> 
    												new BlockItem(MID_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    private static final RegistryObject<Item> MID_BLOCK_MARKER_ITEM = ITEM_LIST.register("mid_block_marker", () -> 
    												new BlockItem(MID_BLOCK_MARKER.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
    private static final RegistryObject<Item> END_BLOCK_ITEM = ITEM_LIST.register("end_block", () -> 
    												new BlockItem(END_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
}
