package dev.alef.lazybuilder.bots;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CalcVector {
	
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

	public static Direction getHDirection(BlockPos start, BlockPos end) {
		
		BlockPos vector = end.subtract(start);
		int len = getLength(start, end);
		
		if (vector.getX() != 0 && Math.abs(vector.getX()) == len) {
			if (vector.getX() > 0) {
				return Direction.EAST;
			}
			else {
				return Direction.WEST;
			}
		} 
		else {
			if (vector.getZ() != 0 && Math.abs(vector.getZ()) == len) {
				if (vector.getZ() > 0) {
					return Direction.SOUTH;
				}
				else {
					return Direction.NORTH;
				}
			}
		}
		return null;
	}
	
	public static Direction getVDirection(BlockPos start, BlockPos end) {
		
		BlockPos vector = end.subtract(start);
		
		if (vector.getY() > 0) {
			return Direction.UP;
		}
		else {
			return Direction.DOWN;
		}
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
		return dirIn   ;
	}

	public static int getLength(BlockPos start, BlockPos end) {
		BlockPos vector = end.subtract(start);
		return Math.max(Math.max(Math.abs(vector.getX()), Math.abs(vector.getY())), Math.abs(vector.getZ()));
	}
	
	public static int getHeight(BlockPos start, BlockPos end) {
		BlockPos vector = end.subtract(start);
		return Math.abs(vector.getY());
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
