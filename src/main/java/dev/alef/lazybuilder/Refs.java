package dev.alef.lazybuilder;

import java.util.Arrays;
import java.util.List;

public class Refs {
	
	public static final String MODID = "lazybuilder";
	public static final String NAME = "alef's Lazy Builder";
	public static final String VERSION = "0.0.1";
	
	public static final int BUILDING = 0;
	public static final int COPYPASTE = 1;
	public static final int PROTECT = 2;

	public static final int COPY = 0;
	public static final int PASTE = 1;
	
	public static final int containerRows = 3;
	public static final int containerCols = 9;
	public static final int protectContainerRows = 1;
	
	public static final int magicColumnHeight = 10;
	public static final int blocksPerItem = 20;
	public static final int maxUndoActions = 50;
	public static final int protectPerItem = 3;
	public static final int maxProtectArea = 30;
	public static final int protectionCost = 10000;
	
	public static final List<String> buildEmptyMsg = Arrays.asList("Start (green) Block is EMPTY", "Fill it with sufficient building blocks of your choice");
	public static final List<String> onlyBlocksMsg = Arrays.asList("Only Blocks are allowed to build structures");

	public static final List<String> copyMsg = Arrays.asList("COPY FINISHED!!!", "Place and sneaking-activate BLUE Block to PASTE");
	public static final List<String> copyEmptyMsg = Arrays.asList("Copy Paste (blue) Block is EMPTY", "Fill it with sufficient Redstone Blocks, Gold Ingots and/or Emeralds");
	public static final List<String> onlyItemsMsg = Arrays.asList("Only Redstone Blocks, Gold Ingots and Emeralds", "allowed to fuel your Copy Machine");
	public static final List<String> onlyItemsProtectMsg = Arrays.asList("Only Diamonds and Emeralds", "allowed to fuel your Protection Block");
	public static final List<String> copyNotEnoughMsg = Arrays.asList("COPY FAILED!!!", "Not enough items in Copy/Paste (Blue) Block");
	public static final List<String> copyAlreadyDoneMsg = Arrays.asList("COPY was previously DONE", "Place and sneaking-activate BLUE Block to PASTE");
	public static final List<String> copyHeightNotZeroMsg = Arrays.asList("Can not copy a zero-height structure", "Set height by placing first Intermediate (yellow) Block at a different level");
	public static final List<String> copyOnlyAirMsg = Arrays.asList("Can not copy an empty (only air) structure", "Define a structure with some non-air blocks inside!");

	public static final List<String> pasteMsg = Arrays.asList("PASTE FINISHED!!!", "Enjoy your copied structure");
	public static final List<String> pasteEmptyMsg = Arrays.asList("PASTE FAILED!!!", "You must COPY a structure first");
	
	public static final List<String> undoEmptyMsg = Arrays.asList("No more actions to UNDO!!!", "(Max. "+maxUndoActions+" actions back)");
	public static final List<String> breakUndoError = Arrays.asList("UNDO FAILED!!!", "You must have the block in your inventory");
	
	public static final List<String> midStartFirstMsg = Arrays.asList("Place one Start (green / blue) Block first");
	public static final List<String> midTooManyForCopyMsg = Arrays.asList("Place End (red) Block", "You just need 2 Intermediate (yellow) Blocks to delimit a 3D-Rectangle");
	public static final List<String> midTooManyForHorizontalMsg = Arrays.asList("Place End (red) Block", "You just need 1 Intermediate (yellow) Block to delimit a Horizontal Rectangle");
	public static final List<String> midBeforeEndMsg = Arrays.asList("You can't place an Intermediate Block after End (red) Block");
	public static final List<String> midNotEnoughMidMsg = Arrays.asList("You need two Intermediate (yellow) Block to delimit a 3D-Rectangle to COPY");
	
	public static final List<String> endStartFirstMsg = Arrays.asList("Place one Start (green / blue) Block first");
}
