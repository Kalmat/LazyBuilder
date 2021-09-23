package dev.alef.lazybuilder.bots;

import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.blocks.ProtectBlock;
import dev.alef.lazybuilder.render.LazyBuilderRender;

public class DestructBot {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	private int blocksIndex = 0;
	
	public DestructBot() {
	}
	
	public int destruct(World worldIn, PlayerEntity player, BlockPos startBlockPos, BlockPos endBlockPos) {
		
		this.blocksIndex = 0;
		
		startBlockPos = CalcVector.fixStartPos(startBlockPos, endBlockPos);
		endBlockPos = CalcVector.fixAltEndPos(startBlockPos, endBlockPos);
		
		Stream<BlockPos> blocks = BlockPos.getAllInBox(startBlockPos, endBlockPos);
		if (worldIn.isRemote) {
			LazyBuilderRender.stopSound();
		}
		blocks.forEach(pos -> this.customDestructBlock(worldIn, pos));
		if (worldIn.isRemote) {
			LazyBuilderRender.resumeSound();
		}
		
		return this.blocksIndex;
	}
	
	private void customDestructBlock(World worldIn, BlockPos pos) {
		
		Block block = worldIn.getBlockState(pos).getBlock();
		
		if (!ProtectBlock.isBlockProtected(worldIn, pos, null, 0) && block.getRegistryName().getNamespace().equals("minecraft")) {
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
		++this.blocksIndex;
	}
}
