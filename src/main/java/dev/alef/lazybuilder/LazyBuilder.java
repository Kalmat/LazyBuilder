package dev.alef.lazybuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.lists.ContainerList;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.network.Networking;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.structure.StructureList;
import dev.alef.lazybuilder.structure.Undo;
import dev.alef.lazybuilder.structure.UndoList;
import dev.alef.lazybuilder.client.gui.CopyPasteBlockGui;
import dev.alef.lazybuilder.client.gui.StartBlockGui;
import dev.alef.lazybuilder.blocks.CopyPasteBlock;
import dev.alef.lazybuilder.blocks.EndBlock;
import dev.alef.lazybuilder.blocks.MidBlock;
import dev.alef.lazybuilder.blocks.MidBlockMarker;
import dev.alef.lazybuilder.blocks.StartBlock;
import dev.alef.lazybuilder.client.LazyBuilderClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(Refs.MODID)
public class LazyBuilder {
	
	private final static Logger LOGGER = LogManager.getLogger();
	
	public static World WORLD;
	public static PlayerEntity PLAYER;
	
    public static StructureList BUILD_LIST;
    public static StructureList COPYPASTE_LIST;
    public static UndoList UNDO_LIST;
    private static boolean INITIALIZED = false;
    
    private static boolean debug = false;
    
	public LazyBuilder() {

		// Register modloading events
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		
		// Register other events we use
		//MinecraftForge.EVENT_BUS.register(new onLootTableLoadListener());
        MinecraftForge.EVENT_BUS.register(new onEntityJoinListener());
        MinecraftForge.EVENT_BUS.register(new onPlayerLoggedOutListener());
        MinecraftForge.EVENT_BUS.register(new onWorldUnload());
        MinecraftForge.EVENT_BUS.register(new onBlockPlacedListener());
        MinecraftForge.EVENT_BUS.register(new onBlockBreakListener());
        MinecraftForge.EVENT_BUS.register(new onRenderGameOverlayListener());
        MinecraftForge.EVENT_BUS.register(new onRenderWorldLastListener());
        MinecraftForge.EVENT_BUS.register(new onKeyInputListener());

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		BlockList.BLOCK_LIST.register(modEventBus);
		BlockList.ITEM_LIST.register(modEventBus);
		ContainerList.CONTAINER_LIST.register(modEventBus);
		TileEntityList.TILEENTITY_LIST.register(modEventBus);

		Networking.registerMessages();
		
		BUILD_LIST = new StructureList();
		COPYPASTE_LIST = new StructureList();
		UNDO_LIST = new UndoList();
	}

	private void setup(final FMLCommonSetupEvent event) {
	    // some preinit code
		if (debug) {
			LOGGER.info("HELLO from PREINIT");
	 	}
	}
	 
	@SuppressWarnings("resource")
	private void doClientStuff(final FMLClientSetupEvent event) {
		// do something that can only be done on the client
		if (debug) {
			LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
		}
		ScreenManager.registerFactory(ContainerList.START_BLOCK.get(), StartBlockGui::new);
		ScreenManager.registerFactory(ContainerList.COPYPASTE_BLOCK.get(), CopyPasteBlockGui::new);
    	LazyBuilderClient.registerKeybindings();
	}
	
	private void enqueueIMC(final InterModEnqueueEvent event) {
		// some example code to dispatch IMC to another mod
		if (debug) {
			InterModComms.sendTo(Refs.MODID, "helloworld", () -> { 
				LOGGER.info("Hello world from the MDK"); return "Hello world";});
		}
	}

	private void processIMC(final InterModProcessEvent event) {
		// some example code to receive and process InterModComms from other mods
		if (debug) {
			LOGGER.info("Got IMC {}", event.getIMCStream().
						map(m->m.getMessageSupplier().get()).
	    				collect(Collectors.toList()));
		}
	}
	
	// CLIENT & SERVER
    public class onLootTableLoadListener {
		
		@SubscribeEvent
	    public void LootTablesLoad(final LootTableLoadEvent event) {
		
			String prefix = "minecraft:chests/";
			String name = event.getName().toString();
			
			// Test: /loot give @p loot minecraft:chests/pillager_outpost
			if (name.startsWith(prefix)) {
                event.getTable().addPool(LootPool.builder()
    										.addEntry(TableLootEntry.builder(new ResourceLocation(Refs.MODID, "chests/lazy_builder"))
    										.weight(1))
    										.bonusRolls(0, 1)
    										.name(Refs.MODID)
    										.build()
    									);
			}
		}
	}
    
	// CLIENT & SERVER
    public class onEntityJoinListener {
        
		@SubscribeEvent
        public void PlayerJoin(final EntityJoinWorldEvent event) throws FileNotFoundException, CommandSyntaxException {

			if (event.getEntity() instanceof PlayerEntity) {

				WORLD = event.getWorld();
				PLAYER = (PlayerEntity) event.getEntity();
				
	            BUILD_LIST.add(WORLD, PLAYER);
	            COPYPASTE_LIST.add(WORLD, PLAYER);
	            UNDO_LIST.add(WORLD, PLAYER);
	            INITIALIZED = true;
	            
	        	BUILD_LIST.get(WORLD, PLAYER).read(WORLD, PLAYER, Refs.BUILDING);
				COPYPASTE_LIST.get(WORLD, PLAYER).read(WORLD, PLAYER, Refs.COPYPASTE);
			}
        }
    }

    // CLIENT
    public class onWorldUnload {
        
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void WorldUnload(final WorldEvent.Unload event) throws IOException {
			if (INITIALIZED && WORLD.isRemote) {
				BUILD_LIST.get(WORLD, PLAYER).write(WORLD, PLAYER, Refs.BUILDING);
				COPYPASTE_LIST.get(WORLD, PLAYER).write(WORLD, PLAYER, Refs.COPYPASTE);
				INITIALIZED = false;
			}
    	}
    }

    // SERVER
    public class onPlayerLoggedOutListener {
        
		@SubscribeEvent
        public void PlayerLogOut(final PlayerEvent.PlayerLoggedOutEvent event) throws IOException {

			PlayerEntity player = event.getPlayer();
			World world = player.world;
			
			BUILD_LIST.get(world, player).write(world, player, Refs.BUILDING);
			COPYPASTE_LIST.get(world, player).write(world, player, Refs.COPYPASTE);
			
			BUILD_LIST.delete(world, player);
	        COPYPASTE_LIST.delete(world, player);
	        UNDO_LIST.delete(world, player);
        }
    }
    
    // SERVER
	public class onBlockPlacedListener {
			
		@SubscribeEvent
		public void BlockPlaced(final EntityPlaceEvent event) {
			if (event.getEntity() instanceof PlayerEntity) {
				BlockState state = event.getState();
				if (!(state.getBlock() instanceof StartBlock || state.getBlock() instanceof CopyPasteBlock || state.getBlock() instanceof MidBlock || state.getBlock() instanceof MidBlockMarker || state.getBlock() instanceof EndBlock)) {
					Undo actionList = UNDO_LIST.get((World) event.getWorld(), (PlayerEntity) event.getEntity());
					actionList.addAction(event);
				}
			}
		}
	}
	
	// SERVER
	public class onBlockBreakListener {
		
		@SubscribeEvent
		public void BlockBreak(final BreakEvent event) {
			if (event.getPlayer() instanceof PlayerEntity) {
				BlockState state = event.getState();
				if (!(state.getBlock() instanceof StartBlock || state.getBlock() instanceof CopyPasteBlock || state.getBlock() instanceof MidBlock || state.getBlock() instanceof MidBlockMarker || state.getBlock() instanceof EndBlock)) {
					Undo actionList = UNDO_LIST.get((World) event.getWorld(), event.getPlayer());
					actionList.addAction(event);
				}
			}
		}
	}
	
	// CLIENT
	public class onRenderGameOverlayListener {
		
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void RenderGameOverlay(final RenderGameOverlayEvent.Text event) {
	    	if (LazyBuilderClient.isTextActive()) {
	    		LazyBuilderClient.showText(event.getMatrixStack());
	    	}
		}
	}
	
	// CLIENT
	public class onRenderWorldLastListener {
		
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void RenderWorldLast(final RenderWorldLastEvent event) {
			LazyBuilderClient.showGuidance(event.getMatrixStack(), WORLD, PLAYER, BUILD_LIST, COPYPASTE_LIST);
		}
	}
	
	// CLIENT
	public class onKeyInputListener {
		
		@SubscribeEvent(priority=EventPriority.NORMAL)
		@OnlyIn(Dist.CLIENT)
		public void KeyInput(final KeyInputEvent event) {
			
			if (LazyBuilderClient.isValidUndoKey(event.getAction(), event.getModifiers(), event.getKey())) {
				LazyBuilderClient.sendKeyUndoToServer(WORLD, PLAYER);
		    } 
			else if (LazyBuilderClient.isValidRotateKey(event.getAction(), event.getModifiers(), event.getKey())) {
		    	Structure copypaste = COPYPASTE_LIST.get(WORLD, PLAYER);
		    	BlockPos copypasteStartPos = copypaste.getStartBlockPos();
		    	
		    	if ((LazyBuilderClient.isLookingAtDirection(PLAYER, copypasteStartPos) || LazyBuilderClient.isLookingAtPos(WORLD, copypasteStartPos)) &&
		    					copypaste.isActive() && copypaste.isLoaded() && CopyPasteBlock.isPlaced(WORLD, copypasteStartPos)) {
		    		copypaste.rotate(1);
		    		LazyBuilderClient.sendKeyRotateToServer(WORLD, PLAYER);
		    	}
		    }
		}
    }	
}
