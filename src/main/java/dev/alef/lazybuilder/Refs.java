package dev.alef.lazybuilder;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.structure.Structure;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;

public class Refs {
	
	public static final String MODID = "lazybuilder";
	public static final String NAME = "alef's Lazy Builder";
	public static final String VERSION = "0.0.1";
	
	public static final ResourceLocation overworld = DimensionType.field_242710_a;
	public static final ResourceLocation the_nether = DimensionType.field_242711_b;
	public static final ResourceLocation the_end = DimensionType.field_242712_c;
	public static final List<ResourceLocation> dimensionList = Arrays.asList(overworld, the_nether, the_end);
	
	public static final int BUILDING = 0;
	public static final int COPYPASTE = 1;
	public static final int DESTRUCT = 2;
	public static final int PROTECT = 3;
	public static final int EMPTY = -1;
	public static final List<Integer> structTypeList = Arrays.asList(BUILDING, COPYPASTE, DESTRUCT, PROTECT);
	public static final Structure EMPTY_STRUCT = new Structure(Refs.overworld, Refs.EMPTY);
	
	public static final int COPY_MODE = 0;
	public static final int PASTE_MODE = 1;
	public static final int UNDO = 0;
	public static final int REDO = 1;
	
	public static final int containerRows = 3;
	public static final int containerCols = 9;
	public static final int protectContainerRows = 1;
	
	public static final int magicColumnHeight = 10;
	public static final int copyBlocksPerItem = 20;
	public static final int maxUndoActions = 50;
	public static final int protectPerItem = 3;
	public static final int protectMaxArea = 30;
	public static final int protectExplosionBonus = 5;
	
	public static final Color lineColor = new Color(0f, 1f, 1f, 1f);
	public static final Color outlineColor = new Color(0f, 1f, 1f, 1f);
	public static final Color cubeColor = new Color(0f, 1f, 1f, 0.1f);
	public static final Color solidSphereColor = new Color(0f, 0f, 0f, 1f);
	public static final Color transSphereColor = new Color(0f, 1f, 1f, 0.1f);
	
	public static final String distance = "Distance: ";
	public static final String size = "(X/Y/Z) Size: ";
	
	public static final List<String> buildOnlyBlocksMsg = Arrays.asList("Only Blocks are allowed to build structures");
	public static final String buildBlocksReqMsg = "Total Blocks required to build: ";
	public static final String buildNotEnoughMsg = "Blocks Required: ";
	public static final String buildItemsInStartMsg = ", but you only have: ";
	public static final String buildEmptyMsg = ", but Start (green) Block is EMPTY";

	public static final List<String> copyMsg = Arrays.asList("COPY FINISHED!!!", "Place and sneaking-activate Copy-Paste (blue) Block to PASTE");
	public static final List<String> copyOnlyItemsMsg = Arrays.asList("Only Redstone Blocks, Gold Ingots and Emeralds", "allowed to fuel your Copy Machine");
	public static final List<String> copyAlreadyDoneMsg = Arrays.asList("COPY was previously DONE", "Place and sneaking-activate Copy-Paste (blue) Block to PASTE");
	public static final List<String> copyHeightNotZeroMsg = Arrays.asList("Can not copy a zero-height structure", "Set height by placingEnd (red) Block at a different level");
	public static final List<String> copyOnlyAirMsg = Arrays.asList("Can not copy an empty (only air) structure", "Define a structure with some non-air blocks inside!");
	public static final String copyBlocksReqMsg = "Total Items required to copy: ";
	public static final String copyNotEnoughMsg = "Items required: ";
	public static final String copyItemsInStartMsg = ", but you only have: ";
	public static final String copyEmptyMsg = ", but Copy-Paste (blue) Block is EMPTY";

	public static final List<String> pasteMsg = Arrays.asList("PASTE FINISHED!!!", "Enjoy your copied structure");
	public static final List<String> pasteEmptyMsg = Arrays.asList("PASTE FAILED!!!", "You must COPY a structure first");
	
	public static final List<String> undoEmptyMsg = Arrays.asList("No more actions to UNDO!!!", "(Max. "+maxUndoActions+" actions back)");
	public static final List<String> undoBreakError = Arrays.asList("UNDO FAILED!!!", "You must have the block in your inventory");
	public static final List<String> redoEmptyMsg = Arrays.asList("No more actions to REDO!!!", "(Max. "+maxUndoActions+" actions forward)");
	public static final List<String> redoPlaceError = Arrays.asList("REDO FAILED!!!", "You must have the block in your inventory");

	public static final List<String> protectGuiMsg = Arrays.asList(""+protectPerItem+" Blocks protection per Item", "max. "+protectMaxArea+" blocks radius ("+protectMaxArea/protectPerItem+" items)");
	public static final List<String> protectOnlyItemsMsg = Arrays.asList("Only Diamonds and Emeralds", "allowed for Protection Block");

	public static final List<String> midStartFirstMsg = Arrays.asList("Place one Start (green) Block first");
	public static final List<String> midTooManyMsg = Arrays.asList("Place End (red) Block at the desired distance and height", "You just need 2 Blocks to delimit a 3D-Cube");
	public static final List<String> midTooManyForHorizontalMsg = Arrays.asList("Place End (red) Block", "You just need 1 Intermediate (yellow) Block to delimit a Horizontal Rectangle");
	public static final List<String> midBeforeEndMsg = Arrays.asList("You can't place an Intermediate Block after End (red) Block");
	public static final List<String> midNotEnoughMidMsg = Arrays.asList("You need 2 Intermediate (yellow) Block to delimit a 3D-Structure");
	
	public static final List<String> endStartFirstMsg = Arrays.asList("Place one Start (green / blue / black&yellow) Block first");
	
	public static final List<Block> copyNotStateList = Arrays.asList(
			Blocks.DIAMOND_BLOCK, 
			Blocks.DIAMOND_ORE,
			Blocks.REDSTONE_BLOCK,
			Blocks.EMERALD_ORE,
			Blocks.EMERALD_BLOCK,
			Blocks.GOLD_ORE,
			Blocks.GOLD_BLOCK,
			Blocks.ENCHANTING_TABLE,
			Blocks.OBSIDIAN,
			Blocks.TNT,
			Blocks.BOOKSHELF,
			Blocks.WATER,
			Blocks.GRASS,
			Blocks.TALL_GRASS,
			Blocks.SEAGRASS,
			Blocks.TALL_SEAGRASS,
			Blocks.KELP,
			Blocks.KELP_PLANT
	);
	
	public static final List<Block> modBlockList = Arrays.asList(
			BlockList.start_block, 
			BlockList.copy_paste_block,
			BlockList.mid_block, 
			BlockList.mid_block_marker, 
			BlockList.end_block, 
			BlockList.protect_block,
			BlockList.destruct_block
	);
	
	public static final List<Item> copypasteValidItem = Arrays.asList(
			Items.GOLD_INGOT,
			Items.EMERALD,
			Items.REDSTONE_BLOCK
	);
	
	public static final List<Item> protectValidItem = Arrays.asList(
			Items.DIAMOND,
			Items.EMERALD
	);
	
	public static final List<List<Object>> copypasteItemToBlock = Arrays.asList(
			Arrays.asList(Items.GOLD_INGOT, Blocks.GOLD_BLOCK),
			Arrays.asList(Items.EMERALD, Blocks.EMERALD_BLOCK),
			Arrays.asList(Items.REDSTONE_BLOCK, Blocks.REDSTONE_BLOCK)
	);
	
	public static final List<List<Object>> protectItemToBlock = Arrays.asList(
			Arrays.asList(Items.DIAMOND, Blocks.DIAMOND_BLOCK),
			Arrays.asList(Items.EMERALD, Blocks.EMERALD_BLOCK)
	);
}
