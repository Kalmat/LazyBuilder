package dev.alef.lazybuilder.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.blocks.CopyPasteBlock;
import dev.alef.lazybuilder.blocks.DestructBlock;
import dev.alef.lazybuilder.blocks.EndBlock;
import dev.alef.lazybuilder.blocks.StartBlock;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.client.gui.CopyPasteBlockGui;
import dev.alef.lazybuilder.client.gui.ProtectBlockGui;
import dev.alef.lazybuilder.client.gui.StartBlockGui;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.lists.ContainerList;
import dev.alef.lazybuilder.network.Networking;
import dev.alef.lazybuilder.network.PacketRedo;
import dev.alef.lazybuilder.network.PacketRotate;
import dev.alef.lazybuilder.network.PacketUndo;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.tileentity.ProtectBlockTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("resource")
public class LazyBuilderRender {

	@SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
	
	private static Structure structure = Refs.EMPTY_STRUCT;
	
	private static int tickCounter = 0;
    private static int ticksDuration = 200;
    private static List<String> textToShow = new ArrayList<String>();
    private static boolean showText = false;
    private static boolean optifineChecked = false;
    private static boolean optifinePresent = false;
    private static List<BlockPos> protectAreasToShow = new ArrayList<BlockPos>(); 
    
    private static final int KEY_UNDO = GLFW.GLFW_KEY_Z;
    private static final int KEY_REDO = GLFW.GLFW_KEY_Z;
    private static final int KEY_ROTATE = GLFW.GLFW_KEY_R;
    private static final KeyBinding KEY_UNDO_BIND =  new KeyBinding("key.lazybuilder.undo", LazyBuilderRender.KEY_UNDO, "key.lazybuilder.general");
    private static final KeyBinding KEY_REDO_BIND =  new KeyBinding("key.lazybuilder.redo", LazyBuilderRender.KEY_REDO, "key.lazybuilder.general");
    private static final KeyBinding KEY_ROTATE_BIND = new KeyBinding("key.lazybuilder.rotate", LazyBuilderRender.KEY_ROTATE, "key.lazybuilder.general");
	private static final List<KeyBinding> KEYBINDS = Arrays.asList(KEY_UNDO_BIND, KEY_REDO_BIND, KEY_ROTATE_BIND);
    
 	public static void addClientListeners(IEventBus forgeEventBus, IEventBus modEventBus) {

		// Register client mod events we use
 		modEventBus.addListener(LazyBuilderRender::clientInit);
 		forgeEventBus.register(new onRenderGameOverlayListener());
 		forgeEventBus.register(new onRenderWorldLastListener());
 		forgeEventBus.register(new onDrawScreenListener());
 		forgeEventBus.register(new onKeyInputListener());
    }
 	
 	private static void clientInit(final FMLClientSetupEvent event) {
 		
		// Register the custom GUI for our Container-like Blocks
		ScreenManager.registerFactory(ContainerList.START_BLOCK.get(), StartBlockGui::new);
		ScreenManager.registerFactory(ContainerList.COPYPASTE_BLOCK.get(), CopyPasteBlockGui::new);
		ScreenManager.registerFactory(ContainerList.PROTECT_BLOCK.get(), ProtectBlockGui::new);
		
		// Register keys to listen at
		LazyBuilderRender.registerKeybindings();
 	}
	
	public static class onRenderGameOverlayListener {
		
		@SubscribeEvent
		public void RenderGameOverlay(final RenderGameOverlayEvent.Text event) {

			if (LazyBuilderRender.isTextActive()) {
	    		LazyBuilderRender.showText(event.getMatrixStack());
	    	}
		}
	}
	
	public static class onRenderWorldLastListener {
		
		@SubscribeEvent
		public void RenderWorldLast(final RenderWorldLastEvent event) {
			LazyBuilderRender.showGuidance(event.getMatrixStack());
		}
	}
	
	public static class onDrawScreenListener {
		
		@SubscribeEvent
		public void DrawScreen(final GuiScreenEvent.DrawScreenEvent.Post event) {

			if (LazyBuilderRender.isTextActive()) {
				if (LazyBuilderRender.getCurrentScreen() instanceof CopyPasteBlockGui || 
						LazyBuilderRender.getCurrentScreen() instanceof ProtectBlockGui ||
						LazyBuilderRender.getCurrentScreen() instanceof StartBlockGui) {
					LazyBuilderRender.showGUIText(event.getMatrixStack());
				}
				else {
					LazyBuilderRender.showText(event.getMatrixStack());
				}
			}
			else if (LazyBuilderRender.getCurrentScreen() instanceof ProtectBlockGui) {
				LazyBuilderRender.showProtectGUIText(event.getMatrixStack());
			}
		}
	}
	
	public static class onKeyInputListener {
		
		@SubscribeEvent(priority=EventPriority.NORMAL)
		public void KeyInput(final KeyInputEvent event) {
			
			if (LazyBuilderRender.isValidUndoKey(event.getAction(), event.getModifiers(), event.getKey())) {
				LazyBuilderRender.sendKeyUndoToServer(LazyBuilderRender.getClientWorld(), LazyBuilderRender.getClientPlayer());
		    } 
			else if (LazyBuilderRender.isValidRedoKey(event.getAction(), event.getModifiers(), event.getKey())) {
				LazyBuilderRender.sendKeyRedoToServer(LazyBuilderRender.getClientWorld(), LazyBuilderRender.getClientPlayer());
		    } 
			else if (LazyBuilderRender.isValidRotateKey(event.getAction(), event.getModifiers(), event.getKey())) {
				LazyBuilderRender.rotateClipboard();
		    }
		}
    }

	public static ClientWorld getClientWorld() {
		return  Minecraft.getInstance().world;
	}

	public static ClientPlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}
	
	public static void netSetClientStructure(CompoundNBT tag) {
		LazyBuilderRender.structure = Structure.retrieveNBT(tag, false);
	}
	
	public static void setClientStructure(Structure struct) {
		LazyBuilderRender.structure = struct;
	}

	public static Structure getClientStructure() {
		return LazyBuilderRender.structure;
	}
	
	public static void rotateClipboard() {
		
		Structure copypaste = LazyBuilderRender.getClientStructure();
		if (copypaste.getStructType() == Refs.COPYPASTE) {
	    	BlockPos startPos = copypaste.getStartBlockPos();
	    	BlockPos endPos = copypaste.getEndBlockPos();
	    	ClientWorld world = LazyBuilderRender.getClientWorld();
	    	ClientPlayerEntity player = LazyBuilderRender.getClientPlayer();
	    	
	    	if (copypaste.getStructType() == Refs.COPYPASTE && copypaste.isLoaded() && 
	    			CopyPasteBlock.isPlaced(world, startPos) && !EndBlock.isPlaced(world, endPos) &&
	    			(LazyBuilderRender.isLookingAtDirection(player, startPos) || LazyBuilderRender.isLookingAtPos(world, startPos))) {
	    		LazyBuilderRender.sendKeyRotateToServer(LazyBuilderRender.getClientWorld(), LazyBuilderRender.getClientPlayer());
	    		copypaste.rotate();
	    		LazyBuilderRender.setClientStructure(copypaste);
	    	}
		}
	}

    public static void setTextActive(List<String> text, int ticks) {
    	LazyBuilderRender.showText = true;
    	LazyBuilderRender.textToShow = text;
    	LazyBuilderRender.ticksDuration = ticks;
    	LazyBuilderRender.tickCounter = 0;
    }
    
    public static boolean isTextActive() {
		if (LazyBuilderRender.showText) {
			if (LazyBuilderRender.tickCounter < LazyBuilderRender.ticksDuration) {
				++LazyBuilderRender.tickCounter;
				return true;
			}
			else {
				LazyBuilderRender.showText = false;
				LazyBuilderRender.tickCounter = 0;
				return false;
			}
		}
		return false;
    }
    
    public static void showText(MatrixStack matrixStack) {
    	if (LazyBuilderRender.getCurrentScreen() == null) {
    		LazyBuilderRender.drawTextCentered(LazyBuilderRender.textToShow, matrixStack, 0xFFFFFF, true, false);
    	}
    }
    
    public static void showGUIText(MatrixStack matrixStack) {
    	LazyBuilderRender.drawTextCentered(LazyBuilderRender.textToShow, matrixStack, 0xFFFFFF, false, false);
    }
     
    public static void showProtectGUIText(MatrixStack matrixStack) {
    	LazyBuilderRender.drawTextCentered(Refs.protectGuiMsg, matrixStack, 0x333333, false, false);
    }
     
    public static void drawTextCentered(List<String> text, MatrixStack matrixStack, int color, boolean shadow, boolean transparent) {
    	
		int screenWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
		int screenHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
		int lineHeight = (int) (Minecraft.getInstance().fontRenderer.FONT_HEIGHT * 1.2);
		int textHeight = (int) (lineHeight * text.size());
		int textWidth;
		int x;
		int y = (int) (screenHeight / 2) - textHeight - (lineHeight * 2);
		String line;
		
		Matrix4f mat = matrixStack.getLast().getMatrix();
		FontRenderer fr = Minecraft.getInstance().fontRenderer;
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		
		for (int i = 0; i < text.size(); ++i) {
			line = text.get(i);
			textWidth = fr.getStringWidth(line);
			x = (int) (screenWidth - textWidth) / 2;
			y += (lineHeight * i);
			fr.renderString(line, (float) x, (float) y, color, shadow, mat, buffers, transparent, 0, 0xF000F0);
		}
		buffers.finish();
	}

	public static boolean showGuidance(MatrixStack ms) {
		
		// Prepare data
		ClientWorld world = LazyBuilderRender.getClientWorld();
		ClientPlayerEntity player = LazyBuilderRender.getClientPlayer();
    	boolean showed = false;
		Structure struct = LazyBuilderRender.getClientStructure();
		if (struct.getStructType() != Refs.EMPTY) {
			BlockPos startPos = struct.getStartBlockPos();
			BlockPos endPos = struct.getEndBlockPos();
			int	structType = struct.getStructType();
			boolean startPlaced = false;
	    	if (structType == Refs.BUILDING) {
	    		startPlaced = StartBlock.isPlaced(world, startPos);
	    	}
	    	else if (structType == Refs.COPYPASTE) {
	    		startPlaced = CopyPasteBlock.isPlaced(world, startPos);
	    	}
	    	else if (structType == Refs.DESTRUCT) {
	    		startPlaced = DestructBlock.isPlaced(world, startPos);
	    	}
			boolean endPlaced = EndBlock.isPlaced(world, endPos);
	    	boolean fill = endPlaced;
			
			if (struct.isActive() && startPlaced && startPos != null) {
	
				// Check Item on Main Hand
		    	Item itemOnHand = player.getHeldItemMainhand().getItem();
		    	boolean rightItemOnHand = false;
		    	boolean midOnHand = false;
		    	boolean endOnHand = false;
		    	if (itemOnHand.equals(BlockList.mid_block.asItem())) {
		    		midOnHand = true;
		    		rightItemOnHand = true;
		    	}
		    	else if (itemOnHand.equals(BlockList.end_block.asItem())) {
		    		endOnHand = true;
		    		rightItemOnHand = true;
		    	}
		    	else if (endPlaced) {
		    		rightItemOnHand = true;
		    	}
		    	else if (structType == Refs.COPYPASTE &&struct.isLoaded()) {
		    		rightItemOnHand = true;
		    	}
		    	
		    	// Check if selected Block is in the right sequence according to structure rules
				boolean rightSequence = false;
				int midSize = struct.getMidBlockListSize();
				if (endPlaced || (structType == Refs.COPYPASTE && struct.isLoaded())) {
					rightSequence = true;
				}
				else if (structType == Refs.BUILDING) {
					if (midSize == 1) {
						int height = CalcVector.getHeight(startPos, struct.getMidBlockElement(0));
						if (height == 0) {
							if (endOnHand) {
								rightSequence = true;
							}
						}
						else if (midOnHand || endOnHand) {
							rightSequence = true;
						}
					}
					else if (midOnHand || endOnHand) {
						rightSequence = true;
					}
				}
				else if ((structType == Refs.COPYPASTE || structType == Refs.DESTRUCT) && endOnHand) {
					rightSequence = true;
				}
				
				// Check other stuff and Show proper guidance
				BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
	
				if (rightItemOnHand && rightSequence) {
	
		     		List<String> msg = Arrays.asList("");
	
					if (structType == Refs.BUILDING) {
	
						BlockPos newStartPos = startPos;
						int bottomY = startPos.getY();
						int topY = startPos.getY();
						BlockPos newEndPos = endPos;
						boolean freeEnd = false;
	
						if (midSize == 0) {
							topY = playerPos.getY();
						}
						else {
							topY = structure.getMidBlockElement(0).getY();
							if (topY == startPos.getY()) {
								freeEnd = true;
								newEndPos = CalcVector.fixHEndPos(playerPos, newStartPos, struct.getMidBlockElement(0), newEndPos);
							}
							else if (midSize >= 2) {
								newEndPos = struct.getMidBlockElement(1);
							}
						}
						if (!freeEnd) {
							newEndPos = CalcVector.fixVEndPos(playerPos, newStartPos, newEndPos, topY, (midSize != 0));
						}
						newStartPos = CalcVector.fixStartPos(newStartPos, newEndPos);
						LazyBuilderRender.showStructGuide(ms, newStartPos, newEndPos, fill);
	
						for (int i = 1; i < midSize; ++i) {
							Direction dir = CalcVector.getDirection(newEndPos, newStartPos);
							newStartPos = CalcVector.setY(newEndPos, bottomY);
							if (dir != null) {
								newStartPos = newStartPos.offset(dir, 1);
							}
							if (i == midSize - 1) {
								newEndPos = playerPos;
								if (endPlaced) {
									newEndPos = endPos;
								}
							}
							else {
								newEndPos = struct.getMidBlockElement(i + 1);
							}
							newEndPos = CalcVector.fixEndPos(playerPos, newStartPos, newEndPos, topY);
				     		LazyBuilderRender.showStructGuide(ms, newStartPos, newEndPos, fill);
						}
			     		
			     		if ((!LazyBuilderRender.showText || LazyBuilderRender.ticksDuration <= 1) && !endPlaced) {
			     			int dist = CalcVector.getHDistance(newStartPos, newEndPos);
			     			if (midSize == 1) {
			     				dist = CalcVector.getHDistance(struct.getMidBlockElement(0), newEndPos);
			     			}
		     				if (dist == 0) {
		     					dist = CalcVector.getHeight(newStartPos, newEndPos);
		     				}
			     			msg = Arrays.asList(Refs.distance+dist);
			     			LazyBuilderRender.setTextActive(msg, 1);
			     		}
			     		showed = true;
					}
					else if (structType == Refs.COPYPASTE || structType == Refs.DESTRUCT) {
						
						if ((!endPlaced || endPos == null) && !struct.isLoaded()) {
							endPos = playerPos;
						}
						startPos = CalcVector.fixStartPos(startPos, endPos);
						LazyBuilderRender.showStructGuide(ms, startPos, endPos, fill);
	
			     		if (!LazyBuilderRender.showText || LazyBuilderRender.ticksDuration <= 1) {
			     			BlockPos dif = endPos.subtract(startPos);
			     			msg = Arrays.asList(Refs.size+Math.abs(dif.getX())+"/"+dif.getY()+"/"+Math.abs(dif.getZ()));
			     			LazyBuilderRender.setTextActive(msg, 1);
			     		}
			     		showed = true;
			    	}
				}
			}
		}
		for (BlockPos protectBlock : LazyBuilderRender.protectAreasToShow) {
			int numItems = ProtectBlockTileEntity.countItems(world, protectBlock);
			if (numItems > 0) {
				int radius = Math.min(Refs.protectPerItem * numItems, Refs.protectMaxArea) + 1;
				LazyBuilderRender.showProtectArea(ms, protectBlock, radius, false, true, false);
	     		showed = true;
			}
		}
		return showed;
	}
	
	public static void showGuideLine(MatrixStack ms, ClientPlayerEntity player, BlockPos pos, Color color, BlockPos pos2, Color color2) {

		Vector3d startPos = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		Vector3d endPos = player.getPositionVec().add(-0.5F, 0.0F, -0.5F);
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		
		LazyBuilderRender.drawLine(ms, buffers, startPos, endPos, 2, color);
		if (pos2 != null) {
			startPos = new Vector3d(pos2.getX(), pos2.getY(), pos2.getZ());
			LazyBuilderRender.drawLine(ms, buffers, startPos, endPos, 2, color2);
		}

		buffers.finish();
	}
	
 	public static void showStructGuide(MatrixStack ms, BlockPos startPos, BlockPos endPos, boolean fill) {

		BlockPos difEnd = endPos.subtract(startPos);
		int x = difEnd.getX();
		int y = difEnd.getY();
		int z = difEnd.getZ();
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		
		LazyBuilderRender.draw3dOutline(ms, buffers, new AxisAlignedBB(startPos).expand(x - x/Math.max(1, Math.abs(x)), y - y/Math.max(1, Math.abs(y)), z - z/Math.max(1, Math.abs(z))), 1, Refs.outlineColor);
		if (fill) {
			LazyBuilderRender.draw3dCube(ms, buffers, new AxisAlignedBB(startPos).expand(x - x/Math.max(1, Math.abs(x)), y - y/Math.max(1, Math.abs(y)), z - z/Math.max(1, Math.abs(z))), Refs.cubeColor);
		}

		buffers.finish();
	}
	
	public static void showProtectArea(MatrixStack ms, BlockPos center, int radius, boolean grid, boolean fill, boolean solid) {

		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		
		Color sphereColor = Refs.solidSphereColor;
		if (!fill || !solid) {
			LazyBuilderRender.drawLatSphereOutline(ms, buffers, new Vector3d(center.getX(), center.getY(), center.getZ()), radius, 18, 18, Refs.outlineColor);
			if (grid) {
				LazyBuilderRender.drawLongSphereOutline(ms, buffers, new Vector3d(center.getX(), center.getY(), center.getZ()), radius, 18, 18, Refs.outlineColor);
			}
			sphereColor = Refs.transSphereColor;
		}
		if (fill) {
			LazyBuilderRender.drawSphere(ms, buffers, new Vector3d(center.getX(), center.getY(), center.getZ()), radius, 36, 18, sphereColor, solid);
		}

		buffers.finish();
	}
		
	public static void drawLine(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d start, Vector3d end, int width, Color color) {

		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);

		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
	    Matrix4f mat = ms.getLast().getMatrix();
		RenderSystem.lineWidth(width);

		buffer.pos(mat, (float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(mat, (float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		
		ms.pop();
	}

	public static void drawHPolygonOutline(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, int radius, int sides, int width, Color color) {
   	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
		Matrix4f mat = ms.getLast().getMatrix();
		RenderSystem.lineWidth(width);
		
		double xx;
		double zz;
		double angle;
		
		for(int i = 0; i < sides * 2; ++i) {
			angle = 2 * Math.PI * i / sides;
			xx = center.x + radius * Math.sin(angle);
		    zz = center.z + radius * Math.cos(angle);
			buffer.pos(mat, (float) xx, (float) center.y, (float) zz).color(r, g, b, a).endVertex();
		
			angle = 2 * Math.PI * (i + 1) / sides;
			xx = center.x + radius * Math.sin(angle);
		    zz = center.z + radius * Math.cos(angle);
			buffer.pos(mat, (float) xx, (float) center.y, (float) zz).color(r, g, b, a).endVertex();
		}
		ms.pop();
    }
   
    public static void drawHPolygon(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, int radius, int sides, Color color) {
   	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.POLYGON);
		Matrix4f mat = ms.getLast().getMatrix();
		
		double xx;
		double zz;
		double angle;
		
		for(int i = 0; i < sides; ++i) {
			angle = 2 * Math.PI * i / sides;
			xx = center.x + radius * Math.sin(angle);
		    zz = center.z + radius * Math.cos(angle);
			buffer.pos(mat, (float) xx, (float) center.y, (float) zz).color(r, g, b, a).endVertex();
		}
		ms.pop();
    }
    
	public static void drawVXPolygonOutline(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, int radius, int sides, int width, Color color) {
	   	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
		Matrix4f mat = ms.getLast().getMatrix();
		RenderSystem.lineWidth(width);
		
		double xx;
		double yy;
		double angle;
		
		for(int i = 0; i < sides * 2; ++i) {
			angle = 2 * Math.PI * i / sides;
			xx = center.x + radius * Math.sin(angle);
		    yy = center.y + radius * Math.cos(angle);
			buffer.pos(mat, (float) xx, (float) yy, (float) center.z).color(r, g, b, a).endVertex();
		
			angle = 2 * Math.PI * (i + 1) / sides;
			xx = center.x + radius * Math.sin(angle);
		    yy = center.y + radius * Math.cos(angle);
			buffer.pos(mat, (float) xx, (float) yy, (float) center.z).color(r, g, b, a).endVertex();
		}
		ms.pop();
    }
   
	public static void drawVZPolygonOutline(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, int radius, int sides, int width, Color color) {
	   	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
		Matrix4f mat = ms.getLast().getMatrix();
		RenderSystem.lineWidth(width);
		
		double zz;
		double yy;
		double angle;
		
		for(int i = 0; i < sides * 2; ++i) {
			angle = 2 * Math.PI * i / sides;
			zz = center.z + radius * Math.sin(angle);
		    yy = center.y + radius * Math.cos(angle);
			buffer.pos(mat, (float) center.x, (float) yy, (float) zz).color(r, g, b, a).endVertex();
		
			angle = 2 * Math.PI * (i + 1) / sides;
			zz = center.z + radius * Math.sin(angle);
		    yy = center.y + radius * Math.cos(angle);
			buffer.pos(mat, (float) center.x, (float) yy, (float) zz).color(r, g, b, a).endVertex();
		}
		ms.pop();
    }
   
     public static void draw3dOutline(MatrixStack ms, IRenderTypeBuffer buffers, AxisAlignedBB aabb, int width, Color color) {
		
	   int r = color.getRed();
	   int g = color.getGreen();
	   int b = color.getBlue();
	   int a = color.getAlpha();
	   Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
	   float x = (float) (aabb.maxX - aabb.minX);
	   float y = (float) (aabb.maxY - aabb.minY);
	   float z = (float) (aabb.maxZ - aabb.minZ);
	
	   ms.push();
	   ms.translate(aabb.minX - projectedView.x, aabb.minY - projectedView.y, aabb.minZ - projectedView.z);
	   
	   IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
	   Matrix4f mat = ms.getLast().getMatrix();
	   RenderSystem.lineWidth(width);
	
	   // Top edges
	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
	
	   // Bottom edges
	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
	
	   // Side edges
	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
	
	   ms.pop();
	}
	
    public static void draw3dCube(MatrixStack ms, IRenderTypeBuffer buffers, AxisAlignedBB aabb, Color color) {
    	
	   int r = color.getRed();
	   int g = color.getGreen();
	   int b = color.getBlue();
	   int a = color.getAlpha();
	   Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
	   float x = (float) (aabb.maxX - aabb.minX);
	   float y = (float) (aabb.maxY - aabb.minY);
	   float z = (float) (aabb.maxZ - aabb.minZ);
	
	   ms.push();
	   ms.translate(aabb.minX - projectedView.x, aabb.minY - projectedView.y, aabb.minZ - projectedView.z);
	   
	   IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.QUAD);
	   Matrix4f mat = ms.getLast().getMatrix();
	   
	   // TOP
       buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();

       // BOTTOM
       buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
       
       // NORTH
       buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();

       // EAST
       buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();

       // SOUTH
       buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();

       // WEST
       buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
       buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();

       ms.pop();
    }
    
    public static void drawLatSphereOutline(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, double radius, int lats, int longs, Color color) {
    	    	
    	int r = color.getRed();
    	int g = color.getGreen();
    	int b = color.getBlue();
    	int a = color.getAlpha();
    	Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
    	double latStep = Math.PI / lats;
    	double longStep = 2 * Math.PI / longs;
    	double latAngle, longAngle;
    	double x, y, z, xz;

    	ms.push();
    	ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
    	
    	IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE);
    	Matrix4f mat = ms.getLast().getMatrix();
    	
    	for(int i = 0; i <= lats; ++i) {
    		latAngle = Math.PI / 2 - i * latStep;
    		xz = radius * Math.cos(latAngle);
    		y = radius * Math.sin(latAngle);
    		
    		for(int j = 0; j <= longs; ++j) {
    		
    			longAngle = j * longStep;
    			x = xz * Math.cos(longAngle);
    			z = xz * Math.sin(longAngle);
    			buffer.pos(mat, (float) (center.x + x), (float) (center.y + y), (float) (center.z + z)).color(r, g, b, a).endVertex();
    	
    			longAngle = (j + 1) * longStep;
    			x = xz * Math.cos(longAngle);
    			z = xz * Math.sin(longAngle);
    			buffer.pos(mat, (float) (center.x + x), (float) (center.y + y), (float) (center.z + z)).color(r, g, b, a).endVertex();
    		}
    	}
    	ms.pop();
	} 
    
    public static void drawLatSphereOutlineB(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, double radius, int lats, int longs, Color color) {
    	// Works ALMOST perfect (there is a longitude line I'm not able to eliminate)
    	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		double latStep = Math.PI / lats;
		double longStep = 2 * Math.PI / longs;
		double latAngle, longAngle;
		double x, y, z;
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE_LOOP);
		Matrix4f mat = ms.getLast().getMatrix();
		
		for (int i = 0; i <= lats * 2; ++i) {
			
		    latAngle = i * latStep;
		    
		    for (int j = 0; j <= longs; ++j) {
		    	
		    	longAngle = j * longStep;
		        x = radius * Math.sin(latAngle) * Math.cos(longAngle);
		        y = radius * Math.cos(latAngle);
		        z = radius * Math.sin(latAngle) * Math.sin(longAngle);
				buffer.pos(mat, (float) (center.x + x), (float) (center.y + y), (float) (center.z + z)).color(r, g, b, a).endVertex();
		    }
		}
	    ms.pop();
    }
    
    public static void drawLongSphereOutline(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, double radius, int lats, int longs, Color color) {
    	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		double latStep = Math.PI / lats;
		double longStep = 2 * Math.PI / longs;
		double latAngle, longAngle;
		double x, y, z;
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.LINE_LOOP);
		Matrix4f mat = ms.getLast().getMatrix();
		
		for (int i = 0; i <= longs; ++i) {
			
		    longAngle = i * longStep;
		    
		    for (int j = 0; j <= lats * 2; ++j) {
		    	
		    	latAngle = j * latStep;
		        x = radius * Math.sin(latAngle) * Math.cos(longAngle);
		        y = radius * Math.cos(latAngle);
		        z = radius * Math.sin(latAngle) * Math.sin(longAngle);
				buffer.pos(mat, (float) (center.x + x), (float) (center.y + y), (float) (center.z + z)).color(r, g, b, a).endVertex();
		    }
		}
	    ms.pop();
    }
    
    public static void drawSphere(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d center, double radius, int lats, int longs, Color color, boolean solid) {
        	
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);
		
		IVertexBuilder buffer = buffers.getBuffer(RenderTypeMod.SPHERE);;
		if (solid) {
			buffer = buffers.getBuffer(RenderTypeMod.SOLID_SPHERE);
		}
		Matrix4f mat = ms.getLast().getMatrix();
		
		double lat0, xz0, y0, lat1, xz1, y1, lng, x, z;
        
        for(int i = 1; i <= lats; ++i) {
        	
            lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
            y0  = radius * Math.sin(lat0);
            xz0 = radius * Math.cos(lat0);

            lat1 = Math.PI * (-0.5 + (double) i / lats);
            y1 = radius * Math.sin(lat1);
            xz1 = radius * Math.cos(lat1);

            for(int j = 1; j <= longs; ++j) {
            	
                lng = 2 * Math.PI * (double) (j - 1) / longs;
                x = Math.cos(lng);
                z = Math.sin(lng);
                
                buffer.pos(mat, (float) (center.x + x * xz0), (float) (center.y + y0), (float) (center.z + z * xz0)).color(r, g, b, a).endVertex();
                buffer.pos(mat, (float) (center.x + x * xz1), (float) (center.y + y1), (float) (center.z + z * xz1)).color(r, g, b, a).endVertex();
            }
        }
        ms.pop();
    }
    
    public static void registerKeybindings() {
    	
	    for (KeyBinding keyBind : LazyBuilderRender.KEYBINDS) {
	        ClientRegistry.registerKeyBinding(keyBind);
	    }
	}
	
	public static boolean isValidUndoKey(int action, int modifiers, int key) {
		if (action == GLFW.GLFW_PRESS && modifiers == GLFW.GLFW_MOD_CONTROL && key == LazyBuilderRender.KEY_UNDO && getCurrentScreen() == null) {
			return true;
		}
		return false;
	}
	
	public static void sendKeyUndoToServer(ClientWorld worldIn, ClientPlayerEntity player) {
        Networking.sendToServer(new PacketUndo(player.func_233580_cy_(), player.getUniqueID()));
	}
	
	public static boolean isValidRedoKey(int action, int modifiers, int key) {
		if (action == GLFW.GLFW_PRESS && modifiers == (GLFW.GLFW_MOD_SHIFT + GLFW.GLFW_MOD_CONTROL) &&
				key == LazyBuilderRender.KEY_REDO && getCurrentScreen() == null) {
			return true;
		}
		return false;
	}
	
	public static void sendKeyRedoToServer(ClientWorld worldIn, ClientPlayerEntity player) {
        Networking.sendToServer(new PacketRedo(player.func_233580_cy_(), player.getUniqueID()));
	}
	
	public static boolean isValidRotateKey(int action, int modifiers, int key) {
		if (action == GLFW.GLFW_PRESS && key == LazyBuilderRender.KEY_ROTATE && getCurrentScreen() == null) {
			return true;
		}
		return false;
	}
	
	public static void sendKeyRotateToServer(ClientWorld worldIn, ClientPlayerEntity player) {
        Networking.sendToServer(new PacketRotate(player.func_233580_cy_(), player.getUniqueID()));
	}
	
	public static void switchProtectArea(ClientWorld worldIn, ClientPlayerEntity player, BlockPos pos) {
		if (worldIn.equals(LazyBuilderRender.getClientWorld()) && player.equals(LazyBuilderRender.getClientPlayer())) {
			if (LazyBuilderRender.protectAreasToShow.contains(pos) ) {
				LazyBuilderRender.deleteProtectArea(pos);
			}
			else {
				LazyBuilderRender.protectAreasToShow.add(pos);
			}
		}
	}
		
	public static void deleteProtectArea(BlockPos pos) {
		for (int i = 0; i < LazyBuilderRender.protectAreasToShow.size(); ++i) {
			if (pos.equals(LazyBuilderRender.protectAreasToShow.get(i))) {
				LazyBuilderRender.protectAreasToShow.remove(i);
			}
		}
	}
	
	public static boolean isLookingAtDirection(ClientPlayerEntity player, BlockPos pos) {
		
		BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
		
		if (CalcVector.getDirection(playerPos, pos) == null) {
			if(player.rotationPitch > 0 && CalcVector.getVDirection(playerPos, pos).equals(Direction.DOWN) || 
					player.rotationPitch < 0 && CalcVector.getVDirection(playerPos, pos).equals(Direction.UP)) {
				playerPos = new BlockPos(player.getPosX(), pos.getY(), player.getPosZ());
			}
		}
		if (playerPos.equals(pos) || player.getHorizontalFacing().equals(CalcVector.getDirection(playerPos, pos)) && playerPos.manhattanDistance(pos) < 16) {
			return true;
		}
		return false;
	}

	public static boolean isLookingAtPos(ClientWorld worldIn, BlockPos pos) {
		BlockPos blockpos = getMousePos();
		return blockpos.equals(pos);
	}
	
	public static BlockPos getMousePos() {
		return ((BlockRayTraceResult) Minecraft.getInstance().objectMouseOver).getPos();
	}
	
	public static Screen getCurrentScreen() {
    	return Minecraft.getInstance().currentScreen;
    }
	
	public static boolean hasOptifine() {
		
		if (!LazyBuilderRender.optifineChecked) {
			LazyBuilderRender.optifineChecked = true;
			for (ModInfo modInfo : FMLLoader.getLoadingModList().getMods()) {
				if (modInfo.getModId().toString().toLowerCase().equals("optifine")) {
					LazyBuilderRender.optifinePresent = true;
					break;
				}
			}
		}
		return LazyBuilderRender.optifinePresent;
	}
	
	public static boolean isShadersActive() {
		return (LazyBuilderRender.hasOptifine() || (!ForgeConfig.CLIENT.forgeLightPipelineEnabled.get() && !ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get()));
	}

	public static void stopSound() {
		Minecraft.getInstance().getSoundHandler().stop();
	}
	
	public static void resumeSound() {
		Minecraft.getInstance().getSoundHandler().resume();
	}
}

