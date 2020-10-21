package dev.alef.lazybuilder.lists;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.container.CopyPasteBlockContainer;
import dev.alef.lazybuilder.container.StartBlockContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ContainerList {

	public static final DeferredRegister<ContainerType<?>> CONTAINER_LIST = DeferredRegister.create(ForgeRegistries.CONTAINERS, Refs.MODID);
	
	public static final RegistryObject<ContainerType<StartBlockContainer>> START_BLOCK = CONTAINER_LIST.register("start_block", () -> 
															IForgeContainerType.create(StartBlockContainer::new));
	public static final RegistryObject<ContainerType<CopyPasteBlockContainer>> COPYPASTE_BLOCK = CONTAINER_LIST.register("copy_paste_block", () -> 
															IForgeContainerType.create(CopyPasteBlockContainer::new));
}
