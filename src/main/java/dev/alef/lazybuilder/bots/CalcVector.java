package dev.alef.lazybuilder.bots;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CalcVector {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	public CalcVector() {
	}
	
	public static BlockPos setX(BlockPos blockPos, int X) {
		return new BlockPos(X, blockPos.getY(), blockPos.getZ());
	}
	
	public static BlockPos setY(BlockPos blockPos, int Y) {
		return new BlockPos(blockPos.getX(), Y, blockPos.getZ());
	}

	public static BlockPos setZ(BlockPos blockPos, int Z) {
		return new BlockPos(blockPos.getX(), blockPos.getY(), Z);
	}

	public static int getDistance(BlockPos start, BlockPos end) {

		if (start == null || end == null) {
			return 0;
		}

		return Math.max(Math.max(Math.abs(start.getX() - end.getX()), Math.abs(start.getY() - end.getY())), Math.abs(start.getZ() - end.getZ()));
	}
	
	public static int getHDistance(BlockPos start, BlockPos end) {

		if (start == null || end == null) {
			return 0;
		}
		
		return Math.max(Math.abs(end.getX() - start.getX()), Math.abs(end.getZ() - start.getZ()));
	}
	
	public static int getShortHDistance(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return 0;
		}
		
		return Math.min(Math.abs(end.getX() - start.getX()), Math.abs(end.getZ() - start.getZ()));
	}
	
	public static int getHeight(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return 0;
		}
		
		return Math.abs(end.getY() - start.getY());
	}
	
	public static Direction getDirection(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return null;
		}
		
		if (start.getX() == end.getX() && start.getZ() == end.getZ()) {
			return CalcVector.getVDirection(start, end);
		}
		
		int dist = CalcVector.getDistance(start, end);
		
		if (Math.abs(start.getX() - end.getX()) == dist) {
			if (start.getX() < end.getX()) {
				return Direction.EAST;
			}
			else if (start.getX() > end.getX()){
				return Direction.WEST;
			}
		}
		else if (Math.abs(start.getZ() - end.getZ()) == dist) {
			if (start.getZ() < end.getZ()) {
				return Direction.SOUTH;
			}
			else if (start.getZ() > end.getZ()){
				return Direction.NORTH;
			}
		}
		else if (Math.abs(start.getY() - end.getY()) == dist) {
			return CalcVector.getVDirection(start, end);
		}
		return null;
	}
	
	public static Direction getVDirection(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return null;
		}
		
		if (start.getY() < end.getY()) {
			return Direction.UP;
		}
		else if (start.getY() > end.getY()) {
			return Direction.DOWN;
		}
		return null;
	}
	
	public static Direction getHDirection(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return null;
		}
		
		if (start.getX() == end.getX() && start.getZ() == end.getZ()) {
			return null;
		}
		
		if (Math.abs(start.getX() - end.getX()) >= Math.abs(start.getZ() - end.getZ())) {
			if (start.getX() < end.getX()) {
				return Direction.EAST;
			}
			else if (start.getX() > end.getX()) {
				return Direction.WEST;
			}
		}
		else {
			if (start.getZ() < end.getZ()) {
				return Direction.SOUTH;
			}
			else if (start.getZ() > end.getZ()){
				return Direction.NORTH;
			}
		}
		return null;
	}
	
	public static Direction getAltHDirection(BlockPos start, BlockPos end) {
		
		if (start == null || end == null) {
			return null;
		}
		
		if (start.getX() == end.getX() && start.getZ() == end.getZ()) {
			return null;
		}
		
		if (Math.abs(start.getX() - end.getX()) < Math.abs(start.getZ() - end.getZ())) {
			if (start.getX() < end.getX()) {
				return Direction.EAST;
			}
			else if (start.getX() > end.getX()) {
				return Direction.WEST;
			}
		}
		else {
			if (start.getZ() < end.getZ()) {
				return Direction.SOUTH;
			}
			else if (start.getZ() > end.getZ()) {
				return Direction.NORTH;
			}
		}
		return null;
	}
	
	public static Direction getNextDirection(Direction dirIn, int rotateNum) {
		
		Direction[] dirs = {Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH};
		
		if (rotateNum > 0 && rotateNum < dirs.length) {
			for (int i = 0; i < dirs.length; ++i) {
				if (dirs[i].equals(dirIn)) {
					return dirs[(i + rotateNum) % dirs.length];
				}
			}
		}
		return dirIn;
	}
	
	public static boolean isHorizontal(Direction dirIn) {
		return (dirIn != null && (dirIn.equals(Direction.EAST) || dirIn.equals(Direction.NORTH) || dirIn.equals(Direction.WEST) || dirIn.equals(Direction.SOUTH)));
	}
	
	public static boolean isVertical(Direction dirIn) {
		return (dirIn != null && (dirIn.equals(Direction.UP) || dirIn.equals(Direction.DOWN)));
	}

	public static BlockPos fixStartPos(BlockPos startPos, BlockPos endPos) {
		
		BlockPos newStartPos = startPos;
		
		if (startPos.getX() == endPos.getX() && startPos.getZ() == endPos.getZ()) {
			return newStartPos.offset(Direction.UP, 1);
		}

		Direction dir = CalcVector.getHDirection(startPos, endPos);
		Direction altDir = CalcVector.getAltHDirection(startPos, endPos);
		
		if (dir != null) {
			newStartPos = startPos.offset(dir, 1);
		}
		if (altDir != null && !altDir.equals(dir)) {
			newStartPos = newStartPos.offset(altDir, 1);
		}
		return newStartPos;
	}

	public static BlockPos fixVEndPos(BlockPos playerPos, BlockPos startPos, BlockPos endPos, int height, boolean forceHeight) {
	
		if (endPos == null) {
			endPos = playerPos;
		}
		BlockPos newEndPos = endPos;
		Direction endDir = CalcVector.getDirection(startPos, endPos);
		int length = CalcVector.getDistance(startPos, endPos);
		
		if (CalcVector.isHorizontal(endDir) && !forceHeight) {
			height = startPos.getY();
		}
		newEndPos = CalcVector.setY(startPos, height);
		if (endDir != null && CalcVector.isHorizontal(endDir)) {
			newEndPos = newEndPos.offset(endDir, length);
		}
		return newEndPos;
	}
	
	public static BlockPos fixHEndPos(BlockPos playerPos, BlockPos startPos, BlockPos midPos, BlockPos endPos) {
		
		if (endPos == null) {
			endPos = playerPos;
		}
		Direction startDir = CalcVector.getHDirection(midPos, startPos);
		Direction endDir = CalcVector.getHDirection(midPos, endPos);
		int length = CalcVector.getHDistance(midPos, endPos);
		if (endDir != null && endDir.equals(startDir)) {
			endDir = CalcVector.getAltHDirection(midPos, endPos);
			length = CalcVector.getShortHDistance(midPos, endPos);
		}
		if (endDir != null) {
			midPos = midPos.offset(endDir, length);
		}
		return midPos;
	}
	
	public static BlockPos fixEndPos(BlockPos playerPos, BlockPos startPos, BlockPos endPos, int height) {
		
		if (endPos == null) {
			endPos = playerPos;
		}
		BlockPos newEndPos = endPos;
		Direction endDir = CalcVector.getHDirection(startPos, endPos);
		int length = CalcVector.getHDistance(startPos, endPos);
		
		if (length == 0) {
			endDir = CalcVector.getAltHDirection(startPos, endPos);
			length = CalcVector.getShortHDistance(startPos, endPos);
		}
		newEndPos = CalcVector.setY(startPos, height);
		if (endDir != null) {
			newEndPos = newEndPos.offset(endDir, length);
		}
		
		return newEndPos;
	}
	
	public static BlockPos fixAltEndPos(BlockPos startBlockPos, BlockPos endBlockPos) {
		
		BlockPos endPos = endBlockPos;
		Direction endDir = CalcVector.getHDirection(endBlockPos, startBlockPos);
		
		if (endBlockPos.getX() != startBlockPos.getX() || endBlockPos.getZ() != startBlockPos.getZ()) {
			if (endDir != null) {
				endPos = endPos.offset(endDir, 1);
			}
			if (endBlockPos.getX() != startBlockPos.getX() && endBlockPos.getZ() != startBlockPos.getZ()) {
				Direction altDir = CalcVector.getAltHDirection(endBlockPos, startBlockPos);
				if (altDir != null) {
					endPos = endPos.offset(altDir, 1);
				}
			}
		}
		if (startBlockPos.getY() != endBlockPos.getY()) {
			endDir = CalcVector.getVDirection(endBlockPos, startBlockPos);
			if (endDir != null) {
				endPos = endPos.offset(endDir, 1);
			}
		}
		return endPos;
	}
	
	public static BlockPos rotateBlock(BlockPos startBlockPos, BlockPos endBlockPos, int rotations) {
		
		BlockPos startPos = startBlockPos;
		BlockPos endPos = endBlockPos;
		
		if (rotations % 4 != 0) {
		
			Direction endDir = CalcVector.getHDirection(startBlockPos, endBlockPos);
			int dist = CalcVector.getHDistance(startBlockPos, endBlockPos);
			if (endDir != null) {
				endPos = CalcVector.setY(startPos.offset(CalcVector.getNextDirection(endDir, rotations), dist), endBlockPos.getY());
			}
			
			endDir = CalcVector.getAltHDirection(startBlockPos, endBlockPos);
			dist = CalcVector.getShortHDistance(startBlockPos, endBlockPos);
			if (endDir != null) {
				endPos = endPos.offset(CalcVector.getNextDirection(endDir, rotations), dist);
			}
		}
		return endPos;
	}
	
	public static List<Double> randomSpherePoint(double x0, double y0, double z0, double radius) {

	   List<Double> point = new ArrayList<Double>();
	   
	   double u = Math.random();
	   double v = Math.random();
	   double theta = 2 * Math.PI * u;
	   double phi = Math.acos(2 * v - 1);
	   double x = x0 + (radius * Math.sin(phi) * Math.cos(theta));
	   point.add(x);
	   double y = y0 + (radius * Math.sin(phi) * Math.sin(theta));
	   point.add(y);
	   double z = z0 + (radius * Math.cos(phi));
	   point.add(z);
	   
	   return point;
	}
}
