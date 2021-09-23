package dev.alef.lazybuilder.lists;

import java.util.function.ToIntFunction;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.blocks.CopyPasteBlock;
import dev.alef.lazybuilder.blocks.DestructBlock;
import dev.alef.lazybuilder.blocks.EndBlock;
import dev.alef.lazybuilder.blocks.MidBlock;
import dev.alef.lazybuilder.blocks.MidBlockMarker;
import dev.alef.lazybuilder.blocks.ProtectBlock;
import dev.alef.lazybuilder.blocks.StartBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockList {
	
    private static ToIntFunction<BlockState> lightValue = (p_235830_0_) -> {return 7;};
    public static Block start_block = new StartBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block mid_block = new MidBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block mid_block_marker = new MidBlockMarker(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block end_block = new EndBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block copy_paste_block = new CopyPasteBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block destruct_block = new DestructBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    public static Block protect_block = new ProtectBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.1f, 0.1f).harvestLevel(0).sound(SoundType.METAL).func_235838_a_(lightValue));
    
    public static final DeferredRegister<Block> BLOCK_LIST = DeferredRegister.create(ForgeRegistries.BLOCKS, Refs.MODID);
    public static final RegistryObject<Block> START_BLOCK = BLOCK_LIST.register("start_block", () -> start_block);
    public static final RegistryObject<Block> MID_BLOCK = BLOCK_LIST.register("mid_block", () -> mid_block);
    public static final RegistryObject<Block> MID_BLOCK_MARKER = BLOCK_LIST.register("mid_block_marker", () -> mid_block_marker);
    public static final RegistryObject<Block> END_BLOCK = BLOCK_LIST.register("end_block", () -> end_block);
    public static final RegistryObject<Block> COPY_PASTE_BLOCK = BLOCK_LIST.register("copy_paste_block", () -> copy_paste_block);
    public static final RegistryObject<Block> DESTRUCT_BLOCK = BLOCK_LIST.register("destruct_block", () -> destruct_block);
    public static final RegistryObject<Block> PROTECT_BLOCK = BLOCK_LIST.register("protect_block", () -> protect_block);
}
