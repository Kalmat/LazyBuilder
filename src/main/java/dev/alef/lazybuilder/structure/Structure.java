package dev.alef.lazybuilder.structure;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.lists.BlockList;

public class Structure {
	
    private final static Logger LOGGER = LogManager.getLogger();
	
	private World WORLD;
	private PlayerEntity PLAYER;
	
    private boolean ACTIVE = false;
    
	private BlockPos STARTBLOCK_POS = null;
	private List<BlockPos> MIDBLOCK_POS = new ArrayList<BlockPos>();
	private BlockPos ENDBLOCK_POS = null;
	
	private List<BlockState> STATE_LIST = new ArrayList<BlockState>();
	
	private int PROTECT_COUNT = 0;
	
	public Structure(World worldIn, PlayerEntity player) {
		WORLD = worldIn;
		PLAYER = player;
	}

	public World getWorld() {
		return WORLD;
	}

	public PlayerEntity getPlayer() {
		return PLAYER;
	}

	public boolean setStartBlockPos(BlockPos pos, PlayerEntity player) {
		STARTBLOCK_POS = pos;
		return true;
	}
	
	public BlockPos getStartBlockPos() {
		return STARTBLOCK_POS;
	}
	
	public boolean deleteStartBlockPos() {
		if (STARTBLOCK_POS != null) {
			STARTBLOCK_POS = null;
			return true;
		}
		else {
			return false;
		}
	}

	public int setMidBlockPos(BlockPos pos) {
		
		boolean equalsLast = false;
		boolean placed = false;
		
		if (MIDBLOCK_POS.size() > 0) {
			BlockPos lastPos = MIDBLOCK_POS.get(MIDBLOCK_POS.size() - 1);
			equalsLast = lastPos.subtract(pos).equals(BlockPos.ZERO);
		}
		if (!equalsLast) {
			placed = MIDBLOCK_POS.add(pos);
		}
		if (placed || equalsLast) {
			return MIDBLOCK_POS.size();
		}
		else {
			return -1;
		}
	}
	
	public int getMidBlockListSize() {
		return MIDBLOCK_POS.size();
	}
	
	public BlockPos getMidBlockElement(int index) {
		if (MIDBLOCK_POS.size() > index) {
			return MIDBLOCK_POS.get(index);
		}
		else {
			return null;
		}
	}
	
	public boolean deleteMidBlockElement(int index) {
		if (MIDBLOCK_POS.size() > index) {
			MIDBLOCK_POS.remove(index);
			return true;
		}
		else {
			return false;
		}
	}

	public List<BlockPos> getMidBlockList() {
		return MIDBLOCK_POS;
	}
	
	public boolean deleteMidBlockList() {
		if (MIDBLOCK_POS.size() > 0) {
			MIDBLOCK_POS = new ArrayList<BlockPos>();
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean replaceMidBlockList(List<BlockPos> newList) {
		if (MIDBLOCK_POS.size() > 0) {
			MIDBLOCK_POS = newList;
			return true;
		}
		else {
			return false;
		}
	}

	public boolean setEndBlockPos(BlockPos pos, PlayerEntity player) {
		
		ENDBLOCK_POS = pos;
		return true;
	}
		
	public BlockPos getEndBlockPos() {
		return ENDBLOCK_POS;
	}
	
	public boolean deleteEndBlockPos() {
		if (ENDBLOCK_POS != null) {
			ENDBLOCK_POS = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void deleteStructure() {
		STARTBLOCK_POS = null;
		MIDBLOCK_POS = new ArrayList<BlockPos>();
		ENDBLOCK_POS = null;
	}
	
	public int addToClipBoard(BlockState state) {
		STATE_LIST.add(state);
		return STATE_LIST.size();
	}
	
	public BlockState getFromClipBoard(int index) {
		return STATE_LIST.get(index);
	}
	
	public int getClipBoardSize() {
		return STATE_LIST.size();
	}
	
	public void setClipBoard(List<BlockState> newStateList) {
		deleteClipBoard();
		STATE_LIST = newStateList;
	}
	
	public List<BlockState> getClipBoard() {
		return STATE_LIST;
	}
	
	public void deleteClipBoard() {
		STATE_LIST = new ArrayList<BlockState>();
	}
	
	public void destroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player, boolean destroyStart, boolean destroyStruct) {
		
		boolean drop = !player.isCreative();

		if (destroyStart) {
			if (STARTBLOCK_POS != null && !STARTBLOCK_POS.equals(pos)) {
				if (worldIn.getBlockState(STARTBLOCK_POS).equals(BlockList.start_block.getDefaultState()) || worldIn.getBlockState(STARTBLOCK_POS).equals(BlockList.copy_paste_block.getDefaultState())) {
					worldIn.destroyBlock(STARTBLOCK_POS, drop);
				}
			}
		}
		BlockPos midState;
		for (int i = 0; i < MIDBLOCK_POS.size(); ++i) { 
			midState = MIDBLOCK_POS.get(i);
			if (midState != null) {
				if (worldIn.getBlockState(midState).equals(BlockList.mid_block.getDefaultState()) || worldIn.getBlockState(midState).equals(BlockList.mid_block_marker.getDefaultState())) {
					worldIn.destroyBlock(MIDBLOCK_POS.get(i), drop);
				}
			}
		}
		if (ENDBLOCK_POS != null) {
			if (worldIn.getBlockState(ENDBLOCK_POS).equals(BlockList.end_block.getDefaultState())) {
				worldIn.destroyBlock(ENDBLOCK_POS, drop);
			}
		}
		if (destroyStruct) {
			deleteStructure();
			deleteClipBoard();
		}
	}
	
	public void shift(BlockPos pos) {
		
		if (STARTBLOCK_POS == null || MIDBLOCK_POS.size() < 2 || ENDBLOCK_POS == null) {
			return;
		}
	
		BlockPos bp;
		BlockPos dif;
		List<BlockPos> newList = new ArrayList<BlockPos>();
		
		dif = pos.subtract(STARTBLOCK_POS);
		
		for (int i = 0; i < MIDBLOCK_POS.size(); ++i) {
			bp = MIDBLOCK_POS.get(i).add(dif);
			newList.add(bp);
		}
		MIDBLOCK_POS = newList;

		bp = ENDBLOCK_POS.add(dif);
		ENDBLOCK_POS = bp;
	}
	
	@SuppressWarnings("deprecation")
	public void rotate(int rotateNum) {
		
		BlockPos startPos = STARTBLOCK_POS;
		BlockPos midPos = MIDBLOCK_POS.get(1);
		
		Direction dirMid = CalcVector.getHDirection(startPos, midPos);
		int dist = CalcVector.getLength(startPos, midPos);
		MIDBLOCK_POS.set(1, startPos.offset(CalcVector.getNextDirection(dirMid, rotateNum),  dist));
		
		startPos = STARTBLOCK_POS;
		BlockPos endPos = ENDBLOCK_POS;
		
		Direction dirEnd = CalcVector.getHDirection(startPos, endPos);
		dist = CalcVector.getLength(startPos, endPos);
		if (dirEnd.equals(dirMid)) {
			dirEnd = CalcVector.getHDirection(midPos, endPos);
			dist = CalcVector.getLength(midPos, endPos);
		}
		ENDBLOCK_POS = startPos.offset(CalcVector.getNextDirection(dirEnd, rotateNum),  dist);
		
		for (int i = 0; i < STATE_LIST.size(); ++i) {
			STATE_LIST.set(i, STATE_LIST.get(i).rotate(Rotation.COUNTERCLOCKWISE_90));
		}
	}
	
	public void setActive(boolean active) {
    	ACTIVE = active;
    }
    
    public boolean isActive() {
    	return ACTIVE;
    }
    
    public boolean isLoaded() {
    	return (STATE_LIST.size() > 0);
    }

    // DEBUG purposes ONLY
	public void place(World worldIn) {
		
		BlockPos bp;
			
		bp = getStartBlockPos();
		Block.replaceBlock(worldIn.getBlockState(bp), BlockList.end_block.getDefaultState(), worldIn, bp, 3);
		
		for (int i = 0; i < getMidBlockListSize(); ++i) {
			bp = getMidBlockElement(i);
			Block.replaceBlock(worldIn.getBlockState(bp), BlockList.mid_block.getDefaultState(), worldIn, bp, 3);
		}

		bp = getEndBlockPos();
		Block.replaceBlock(worldIn.getBlockState(bp), BlockList.end_block.getDefaultState(), worldIn, bp, 3);
	}

	public void read(World worldIn, PlayerEntity player, int structType) throws FileNotFoundException, CommandSyntaxException {

		String fileName = "mods\\"+Refs.MODID+"\\"+PlayerEntity.getOfflineUUID(player.getName().getString())+structType;

		try {
			File myObj = new File(fileName);
			Scanner myReader = new Scanner(myObj);
			String reg = "";
			String prefix = "";
			String data = "";
			
			ACTIVE = false;
			deleteStructure();
			deleteClipBoard();

			while (myReader.hasNextLine()) {
				reg = myReader.nextLine();
				prefix = reg.substring(0, 2);
				data = reg.substring(2, reg.length());
				
				if (prefix.equals("AC")) {
					ACTIVE = (data.equals("true"));
				}
				else if (prefix.equals("SB")) {
					STARTBLOCK_POS = BlockPos.fromLong(Long.parseLong(data));
				}
				else if (prefix.equals("MB")) {
					MIDBLOCK_POS.add(BlockPos.fromLong(Long.parseLong(data)));
				}
				else if (prefix.equals("EB")) {
					ENDBLOCK_POS = BlockPos.fromLong(Long.parseLong(data));
				}
				else {
					STATE_LIST.add(NBTUtil.readBlockState(JsonToNBT.getTagFromJson(decypher(reg))).getBlock().getDefaultState());
				}
			}
			myReader.close();
		}
		catch (FileNotFoundException e) {
			LOGGER.info("No previous Structure/ClipBoard found");	
		}
	}

	public boolean write(World worldIn, PlayerEntity player, int structType) throws IOException {
		
		try {
			String fileName = "mods\\"+Refs.MODID;
			File myObj = new File(fileName);
		    myObj.mkdirs();
			fileName += "\\"+PlayerEntity.getOfflineUUID(player.getName().getString())+structType;
		    myObj = new File(fileName);
		    if (!myObj.createNewFile()) {
		    	myObj.delete();
		    	myObj.createNewFile();
		    }
		    
			FileWriter myWriter = new FileWriter(fileName);
			
			myWriter.write("AC"+ACTIVE+System.lineSeparator());
			
			if (STARTBLOCK_POS != null) {
				myWriter.write("SB"+STARTBLOCK_POS.toLong()+System.lineSeparator());
			}
			
			for (int i = 0; i < MIDBLOCK_POS.size(); ++i) {
				if (MIDBLOCK_POS.get(i) != null) {
					myWriter.write("MB"+MIDBLOCK_POS.get(i).toLong()+System.lineSeparator());
				}
			}
			
			if (ENDBLOCK_POS != null) {
				myWriter.write("EB"+ENDBLOCK_POS.toLong()+System.lineSeparator());
			}
			
			for (int j = 0; j < STATE_LIST.size(); ++j) {
				if (STATE_LIST.get(j) != null) {
					myWriter.write(cypher(NBTToJSON(STATE_LIST.get(j)))+System.lineSeparator());
				}
			}
			myWriter.close();
		}
		catch (IOException e) {
			LOGGER.warn("Couldn't create savefile. No Structure/ClipBoard will be saved");
		}
		return true;
	}
	
	private String NBTToJSON(BlockState state) {
		String json = state.toString();
		int start = json.indexOf("[", 0);
		if (start > 0) {
			int end = Math.max(json.length(), json.indexOf("]", start));
			json = json.substring(0, start) + json.substring(end, json.length());
		}
		json = json.replace("{", "\"").replace("}", "\"").replace("[", "").replace("]", ""); 
		json = "{"+json.replace("Block", "\"Name\":")+"}";

		return json;
	}
	
	private String cypher(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
	}
	
	private String decypher(String text) {
		byte[] decBytes = Base64.getDecoder().decode(text);
		return new String(decBytes);
	}
	
	public void setProtectCount(int count) {
		PROTECT_COUNT = count;
	}
	
	public int getProtectCount() {
		return PROTECT_COUNT;
	}
}
