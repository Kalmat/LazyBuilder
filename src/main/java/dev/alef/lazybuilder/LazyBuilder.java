package dev.alef.lazybuilder;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.lists.ContainerList;
import dev.alef.lazybuilder.lists.BlockItemList;
import dev.alef.lazybuilder.lists.TileEntityList;
import dev.alef.lazybuilder.network.Networking;
import dev.alef.lazybuilder.network.PacketStructure;
import dev.alef.lazybuilder.network.PacketText;
import dev.alef.lazybuilder.playerdata.IPlayerData;
import dev.alef.lazybuilder.playerdata.PlayerData;
import dev.alef.lazybuilder.playerdata.PlayerDataProvider;
import dev.alef.lazybuilder.playerdata.PlayerDataStorage;
import dev.alef.lazybuilder.render.LazyBuilderRender;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.structure.Undo;
import dev.alef.lazybuilder.structure.UndoList;
import dev.alef.lazybuilder.blocks.ProtectBlock;
import dev.alef.lazybuilder.bots.UndoBot;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(Refs.MODID)
public class LazyBuilder {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
    public static UndoList undoList;

	public LazyBuilder() {

		// Register server mod events we use
		final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.register(new PlayerCapabilityEventListener());
		forgeEventBus.register(new onPlayerLoggedInListener());
		forgeEventBus.register(new onPlayerLoggedOutListener());
		forgeEventBus.register(new onBlockPlacedListener());
		forgeEventBus.register(new onBlockBreakListener());
		forgeEventBus.register(new onBreakSpeedListener());
		forgeEventBus.register(new onExplosionListener());
		forgeEventBus.register(new onLootTableLoadListener());

		// Register modloading events
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(LazyBuilder::init);
		
		// Register our custom blocks, items and entities
		BlockList.BLOCK_LIST.register(modEventBus);
		BlockItemList.BLOCKITEM_LIST.register(modEventBus);
		ContainerList.CONTAINER_LIST.register(modEventBus);
		TileEntityList.TILEENTITY_LIST.register(modEventBus);

		// Register custom client <--> server messages
		Networking.registerMessages();
		
		// Create non-persistent structure lists (persistent structures are stored in PlayerDataCapability)
		LazyBuilder.undoList = new UndoList();
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> LazyBuilderRender.addClientListeners(forgeEventBus, modEventBus));
	}

	private static void init(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IPlayerData.class, new PlayerDataStorage(), PlayerData::new);
	}
	
	public static class PlayerCapabilityEventListener {

	    public final static ResourceLocation PlayerDataCapability = new ResourceLocation(Refs.MODID, "player_data");
	    
	    @SubscribeEvent
	    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
	        if (event.getObject() instanceof PlayerEntity) {
	        	event.addCapability(PlayerDataCapability, new PlayerDataProvider());
	        }
	    }
    }

	public class onPlayerLoggedInListener {
		
		@SubscribeEvent
		public void PlayerLoggedIn(final PlayerLoggedInEvent event) {

			PlayerEntity player = event.getPlayer();
    		World world = player.getEntityWorld();
			ResourceLocation currentDimension = LazyBuilder.getDimension(world);
			LazyBuilder.undoList.create(world, player);
			
			IPlayerData playerData = PlayerData.getFromPlayer(player);
		    Structure struct = playerData.getActiveStructure(currentDimension);
		    if (struct.getStructType() != Refs.EMPTY) {
		    	LazyBuilder.updateClientStructure(currentDimension, player, struct);
		    }
        }
    }
    
    
    public class onPlayerLoggedOutListener {
        
		@SubscribeEvent
        public void LoggedOut(final PlayerLoggedOutEvent event) {
			PlayerEntity player = event.getPlayer();
			LazyBuilder.undoList.deleteAll(player);
        }
    }
    
	public class onBlockPlacedListener {
		
		@SubscribeEvent
		public void BlockPlaced(final EntityPlaceEvent event) {
			
			if (event.getEntity() instanceof PlayerEntity) {
				Block block = event.getState().getBlock();
				if (block.getRegistryName().getNamespace().equals("minecraft")) {
					Undo actionList = LazyBuilder.undoList.get((World) event.getWorld(), LazyBuilder.getDimension((World) event.getWorld()), (PlayerEntity) event.getEntity());
					actionList.addAction(event);
				}
			}
		}
	}
	
	public class onBlockBreakListener {
		
		@SubscribeEvent
		public void BlockBreak(final BreakEvent event) {
			
			if (event.getPlayer() instanceof PlayerEntity) {
				Block block = event.getState().getBlock();
				if (block.getRegistryName().getNamespace().equals("minecraft")) {
					Undo actionList = LazyBuilder.undoList.get((World) event.getWorld(), LazyBuilder.getDimension((World) event.getWorld()), event.getPlayer());
					actionList.addAction(event);
				}
			}
		}
	}

	public class onBreakSpeedListener {
		
		@SubscribeEvent
		public void BreakSpeed(final BreakSpeed event) {

    		if (ProtectBlock.isBlockProtected(event.getPlayer().world, event.getPos(), event.getPlayer(), 0)) {
				event.setNewSpeed(0.0F);
			}
		}
	}
	
	public class onExplosionListener {

		@SubscribeEvent
	    public void Detonate(final Detonate event) {
	    	
	        World world = event.getWorld();
	        
	        if (!world.isRemote) {
	        	Explosion explosion = event.getExplosion();
	    		BlockPos explosionPos = new BlockPos(explosion.getPosition());
	        	
        		if (ProtectBlock.isBlockProtected(world, explosionPos, null, Refs.protectExplosionBonus)) {
        			explosion.clearAffectedBlockPositions();
	        	}
	        }
	    }
	} 
	
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
    
    public static void rotateClipboard(PlayerEntity player) {

		IPlayerData playerData = PlayerData.getFromPlayer(player);
		Structure copypaste = playerData.getStructure(LazyBuilder.getDimension(player.world), Refs.COPYPASTE);
    	
		copypaste.rotate();
    }

	public static void updateClientStructure(ResourceLocation dimension, PlayerEntity player, Structure struct) {
		if (!player.world.isRemote && player instanceof ServerPlayerEntity) {
			Networking.sendToClient(new PacketStructure(Structure.createNBT(struct, false)), (ServerPlayerEntity) player);
		}
	}

    public static void showClientText(List<String> msg, int time, PlayerEntity player) {
		if (!player.world.isRemote && player instanceof ServerPlayerEntity) {
			Networking.sendToClient(new PacketText(time, msg.size(), msg), (ServerPlayerEntity) player);
		}
    }
    
    public static void undoAction(World worldIn, PlayerEntity player) {
    	
    	Undo actionList = LazyBuilder.undoList.get(worldIn, LazyBuilder.getDimension(worldIn), player);
    	
    	if (actionList != null && actionList.getActionListSize() > 0) {
        	UndoBot undoBot = new UndoBot();
    		boolean ret = undoBot.undoAction(worldIn, player, actionList);
    		if (!ret) {
				LazyBuilder.showClientText(Refs.undoBreakError, 200, player);
    		}
    	}
		else {
			LazyBuilder.showClientText(Refs.undoEmptyMsg, 200, player);
		}
	}
    
    public static void redoAction(World worldIn, PlayerEntity player) {
    	
    	Undo reActionList = LazyBuilder.undoList.get(worldIn, LazyBuilder.getDimension(worldIn), player);
    	
    	if (reActionList != null && reActionList.getReActionListSize() > 0) {
        	UndoBot undoBot = new UndoBot();
    		boolean ret = undoBot.redoAction(worldIn, player, reActionList);
    		if (!ret) {
				LazyBuilder.showClientText(Refs.redoPlaceError, 200, player);
    		}
    	}
		else {
			LazyBuilder.showClientText(Refs.redoEmptyMsg, 200, player);
		}
	}

    public static ResourceLocation getDimension(World world) {
    	
    	RegistryKey<World> dimension = world.func_234923_W_();
    	
    	if (dimension == World.field_234918_g_) {
    		return Refs.overworld;
    	}
    	else if (dimension == World.field_234919_h_) {
    		return Refs.the_nether;
    	}
    	else if (dimension == World.field_234920_i_) {
    		return Refs.the_end;
    	}
    	return Refs.overworld;
    }
}
