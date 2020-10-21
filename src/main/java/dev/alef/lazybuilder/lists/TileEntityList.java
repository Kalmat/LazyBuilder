package dev.alef.lazybuilder.lists;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.tileentity.CopyPasteBlockTileEntity;
import dev.alef.lazybuilder.tileentity.StartBlockTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileEntityList {
	
	public static final DeferredRegister<TileEntityType<?>> TILEENTITY_LIST = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Refs.MODID);

	public static final RegistryObject<TileEntityType<StartBlockTileEntity>> START_BLOCK = TILEENTITY_LIST.register("start_block", () ->
								TileEntityType.Builder.create(StartBlockTileEntity::new, BlockList.start_block).build(null));
	public static final RegistryObject<TileEntityType<CopyPasteBlockTileEntity>> COPYPASTE_BLOCK = TILEENTITY_LIST.register("copy_paste_block", () ->
								TileEntityType.Builder.create(CopyPasteBlockTileEntity::new, BlockList.copy_paste_block).build(null));
}
