package dev.alef.lazybuilder.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.blocks.CopyPasteBlock;
import dev.alef.lazybuilder.blocks.EndBlock;
import dev.alef.lazybuilder.blocks.StartBlock;
import dev.alef.lazybuilder.bots.CalcVector;
import dev.alef.lazybuilder.lists.BlockList;
import dev.alef.lazybuilder.network.Networking;
import dev.alef.lazybuilder.network.PacketRotate;
import dev.alef.lazybuilder.network.PacketUndo;
import dev.alef.lazybuilder.structure.Structure;
import dev.alef.lazybuilder.structure.StructureList;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class LazyBuilderClient {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	public static final Minecraft MC = Minecraft.getInstance();
	private static final MainWindow MW = MC.getMainWindow();
	private static final FontRenderer FR = MC.fontRenderer;
	
    private static int TICK_COUNTER = 0;
    private static int TICKS = 200;
    private static List<String> TEXT = new ArrayList<String>();
    private static boolean SHOW_TEXT = false;
    
    private static int KEY_UNDO = GLFW.GLFW_KEY_Z;
    private static int KEY_ROTATE = GLFW.GLFW_KEY_R;
    
	public LazyBuilderClient() {
    }
	
	@OnlyIn(Dist.CLIENT)
    public static void setTextActive(List<String> text, int ticks) {
    	SHOW_TEXT = true;
    	TEXT = text;
    	TICKS = ticks;
    	TICK_COUNTER = 0;
    }
    
	@OnlyIn(Dist.CLIENT)
    public static boolean isTextActive() {
		if (SHOW_TEXT) {
			if (TICK_COUNTER < TICKS) {
				++TICK_COUNTER;
				return true;
			}
			else {
				SHOW_TEXT = false;
				TICK_COUNTER = 0;
				return false;
			}
		}
		return false;
    }
	
	@OnlyIn(Dist.CLIENT)
    public static void showText(MatrixStack matrixStack) {
    	drawTextCentered(TEXT, matrixStack);
    }
     
	@OnlyIn(Dist.CLIENT)
    public static void drawTextCentered(List<String> text, MatrixStack matrixStack) {
    	
		int screenWidth = MW.getScaledWidth();
		int screenHeight = MW.getScaledHeight();
		int lineHeight = (int) (FR.FONT_HEIGHT * 1.2);
		int textHeight = (int) (lineHeight * text.size());
		int textWidth;
		int x;
		int y = (int) (screenHeight / 2) - textHeight - (lineHeight * 2);;
		
		String line;

		for (int i = 0; i < text.size(); ++i) {
			line = text.get(i);
			textWidth = FR.getStringWidth(line);
			x = (int) (screenWidth - textWidth) / 2;
			y += (lineHeight * i);
			FR.func_243246_a(matrixStack, new TranslationTextComponent(line), x, y, 0xffffffff);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean showGuidance(MatrixStack ms, World worldIn, PlayerEntity player, StructureList buildingList, StructureList copypasteList) {
		
		// Prepare data
		Structure structure = buildingList.get(worldIn, player);
    	BlockPos startPos = structure.getStartBlockPos();
    	BlockPos endPos = structure.getEndBlockPos();
		boolean startPlaced = StartBlock.isPlaced(worldIn, startPos);
		boolean endPlaced = EndBlock.isPlaced(worldIn, endPos);
		int structType = Refs.BUILDING;
    	Color color = new Color(0F, 1F, 0F, 1F);
    	if (!structure.isActive() || startPos == null) {
    		structure = copypasteList.get(worldIn, player);
        	startPos = structure.getStartBlockPos();
        	endPos = structure.getEndBlockPos();        	
			startPlaced = CopyPasteBlock.isPlaced(worldIn, startPos);
			endPlaced = EndBlock.isPlaced(worldIn, endPos);
			structType = Refs.COPYPASTE;
	    	color = new Color(0F, 0F, 1F, 1F);
    	}
    	BlockPos pos = startPos;
		BlockPos pos2 = null;
		Color color2 = new Color(1F, 1F, 0F, 1F);
		
		if (structure.isActive()) {

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
	    	
	    	// Check if selected Block is in the right sequence according to block type, blocks number, structure type and so on
			boolean rightSequence = false;
			int midSize = structure.getMidBlockListSize();
			if (!endPlaced) {
				if (structType == Refs.BUILDING) {
					if (endOnHand) {
						rightSequence = true;
						if (structure.getMidBlockListSize() > 0) {
							pos2 = structure.getMidBlockElement(midSize - 1);
						}
					}
					else {
						if (midSize == 0) {
							rightSequence = true;
						}
						else if (midSize >= 2) {
							if (midOnHand) {
								rightSequence = true;
								pos2 = structure.getMidBlockElement(midSize - 1);
								color2 = new Color(1F, 1F, 0F, 1F);
							}
						}
						else if (CalcVector.getHeight(startPos, structure.getMidBlockElement(0)) > 0) {
							rightSequence = true;
						}
					}
				}
				else if (structType == Refs.COPYPASTE) {
					if (midSize < 2 && midOnHand) {
						rightSequence = true;
					}
					else if (midSize == 2 && endOnHand) {
						rightSequence = true;
						pos2 = structure.getMidBlockElement(midSize - 1);
					}
				}
			}
	
			// Check other stuff and Show proper guidance
	    	if (rightItemOnHand && !structure.isLoaded() && startPlaced && rightSequence && pos != null) {
	    		showGuideLine(ms, player, pos, color, pos2, color2);
	    		return true;
	    	}
	    	else if (structType == Refs.COPYPASTE && midSize == 2 && structure.isLoaded() && startPlaced &&
	    			startPos != null && structure.getMidBlockElement(0) != null && structure.getMidBlockElement(1) != null && endPos != null) {
	     		showIndicators(ms, player, startPos, structure.getMidBlockElement(0), structure.getMidBlockElement(1), endPos);
	    		return true;
	    	}
		}
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void showGuideLine(MatrixStack ms, PlayerEntity player, BlockPos pos, Color color, BlockPos pos2, Color color2) {

		Vector3d startPos = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		Vector3d endPos = player.getPositionVec().add(-0.5F, 0.0F, -0.5F);
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		RenderSystem.disableCull();
		ms.push();
		
		drawLine(ms, buffers, startPos, endPos, 3, color);
		if (pos2 != null) {
			startPos = new Vector3d(pos2.getX(), pos2.getY(), pos2.getZ());
			drawLine(ms, buffers, startPos, endPos, 3, color2);
		}

		ms.pop();
		buffers.finish();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void showIndicators(MatrixStack ms, PlayerEntity player, BlockPos startPos, BlockPos midPosV, BlockPos midPosH, BlockPos endPos) {

		Vector3d startVec = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
   		midPosV = startPos.offset(CalcVector.getVDirection(startPos, midPosV), CalcVector.getHeight(startPos, midPosV));
		Vector3d midVecV = new Vector3d(midPosV.getX(), midPosV.getY(), midPosV.getZ());
   		midPosH = startPos.offset(CalcVector.getHDirection(startPos, midPosH), CalcVector.getLength(startPos, midPosH));
		Vector3d midVecH = new Vector3d(midPosH.getX(), midPosH.getY(), midPosH.getZ());
		Direction dirMid = CalcVector.getHDirection(startPos, midPosH);
		Direction dirEnd = CalcVector.getHDirection(startPos, endPos);
    	if(dirEnd.equals(dirMid)) {
			dirEnd = CalcVector.getHDirection(midPosH, endPos);
			endPos = startPos.offset(dirEnd, CalcVector.getLength(startPos, endPos));
		}
		endPos = startPos.offset(CalcVector.getHDirection(startPos, endPos), CalcVector.getLength(startPos, endPos));
		Vector3d endVec = new Vector3d(endPos.getX(), endPos.getY(), endPos.getZ());
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		RenderSystem.disableCull();
		ms.push();
		
		drawLine(ms, buffers, startVec, midVecV, 5, new Color(0F, 1F, 0F, 1F));
		drawLine(ms, buffers, startVec, midVecH, 5, new Color(1F, 1F, 0F, 1F));
		drawLine(ms, buffers, startVec, endVec, 5, new Color(1F, 0F, 0F, 1F));
		
		ms.pop();
		buffers.finish();
	}
		
	@OnlyIn(Dist.CLIENT)
    public static void drawLine(MatrixStack ms, IRenderTypeBuffer buffers, Vector3d start, Vector3d end, int width, Color color) {

		ActiveRenderInfo renderInfo = MC.gameRenderer.getActiveRenderInfo();
		Vector3d projectedView = renderInfo.getProjectedView();
		
		ms.push();
		ms.translate(0.5 - projectedView.x, 0.5 - projectedView.y, 0.5 - projectedView.z);

		RenderType.State glState = RenderType.State.getBuilder()
									    		  .line(new RenderState.LineState(OptionalDouble.of(1)))
									    		  //.layer(ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228500_J_"))  // Crashes (Why?)
									    		  //.transparency(ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_"))
									    		  .writeMask(new RenderState.WriteMaskState(true, false))
									    		  .depthTest(new RenderState.DepthTestState("", GL11.GL_ALWAYS))  // What is that String???
									    		  .build(false);
	    IVertexBuilder buffer = buffers.getBuffer(RenderType.makeType(Refs.MODID + ":line_1_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState));
	    Matrix4f mat = ms.getLast().getMatrix();
	   
		RenderSystem.lineWidth(width);

		buffer.pos(mat, (float) start.x, (float) start.y, (float) start.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(mat, (float) end.x, (float) end.y, (float) end.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		
		ms.pop();
	}

	@OnlyIn(Dist.CLIENT)
	private static Screen getCurrentScreen() {
    	return MC.currentScreen;
    }
    
	@OnlyIn(Dist.CLIENT)
	public static void registerKeybindings() {
		KeyBinding[] KEYBINDS = new KeyBinding[2];
	    KEYBINDS[0] = new KeyBinding("key.position.desc", KEY_UNDO, "key.lazybuilder.category");
	    KEYBINDS[1] = new KeyBinding("key.rotate.desc", KEY_ROTATE, "key.lazybuilder.category");
	    
	    for (int i = 0; i < KEYBINDS.length; ++i) {
	        ClientRegistry.registerKeyBinding(KEYBINDS[i]);
	    }
	}
	
	@OnlyIn(Dist.CLIENT)
	public static boolean isValidUndoKey(int action, int modifiers, int key) {
		if (action == GLFW.GLFW_PRESS && modifiers == GLFW.GLFW_MOD_CONTROL && key == KEY_UNDO && getCurrentScreen() == null) {
			return true;
		}
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void sendKeyUndoToServer(World worldIn, PlayerEntity player) {
        Networking.sendToServer(new PacketUndo(player.func_233580_cy_(), player.getUniqueID()));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static boolean isValidRotateKey(int action, int modifiers, int key) {
		if (action == GLFW.GLFW_PRESS && key == KEY_ROTATE && getCurrentScreen() == null) {
			return true;
		}
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void sendKeyRotateToServer(World worldIn, PlayerEntity player) {
        Networking.sendToServer(new PacketRotate(player.func_233580_cy_(), player.getUniqueID()));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static boolean isLookingAtDirection(PlayerEntity player, BlockPos pos) {
		
		BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
		
		if (CalcVector.getHDirection(playerPos, pos) == null) {
			if(player.rotationPitch > 0 && CalcVector.getVDirection(playerPos, pos).equals(Direction.DOWN) || 
					player.rotationPitch < 0 && CalcVector.getVDirection(playerPos, pos).equals(Direction.UP)) {
				playerPos = new BlockPos(player.getPosX(), pos.getY(), player.getPosZ());
			}
		}
		if (playerPos.equals(pos) || player.getHorizontalFacing().equals(CalcVector.getHDirection(playerPos, pos)) && CalcVector.getLength(pos, playerPos) < 16) {
			return true;
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean isLookingAtPos(World worldIn, BlockPos pos) {
		BlockPos blockpos = getMousePos();
		return blockpos.equals(pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static BlockPos getMousePos() {
		return ((BlockRayTraceResult) MC.objectMouseOver).getPos();
	}
   
//    //@SubscribeEvent
//	@OnlyIn(Dist.CLIENT)
//	public static void showCubes(RenderWorldLastEvent event) {
//		MatrixStack ms = event.getMatrixStack();
//		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
//		RenderSystem.disableCull();
//		ms.push();
//	
//	   for (Entity e : MC.world.getAllEntities()) {
//	       if (!MC.player.equals(e) && !(e instanceof ItemEntity) && !(e instanceof ExperienceOrbEntity)) {
//	           drawCube(ms, buffers, new AxisAlignedBB(new BlockPos(e.getPosX(), e.getPosY(), e.getPosZ())).expand(0, 1, 0), new Color(0, 255, 0, 127));
//	           draw3dOutline(ms, buffers, new AxisAlignedBB(new BlockPos(e.getPosX(), e.getPosY(), e.getPosZ())).expand(0, 1, 0), new Color(255, 255, 255, 255));
//	       }
//	   }
//	   draw3dOutline(ms, buffers, new AxisAlignedBB(new BlockPos(0, 10, 0)), new Color(255, 255, 255, 255));
//	
//	   ms.pop();
//	   buffers.finish();
//   }
//
//	@OnlyIn(Dist.CLIENT)
//	public static void drawCube(MatrixStack ms, IRenderTypeBuffer buffers, AxisAlignedBB aabb, Color color) {
//	   draw3dRectangle(ms, buffers, aabb, color, "TOP");
//	   draw3dRectangle(ms, buffers, aabb, color, "BOTTOM");
//	   draw3dRectangle(ms, buffers, aabb, color, "NORTH");
//	   draw3dRectangle(ms, buffers, aabb, color, "EAST");
//	   draw3dRectangle(ms, buffers, aabb, color, "SOUTH");
//	   draw3dRectangle(ms, buffers, aabb, color, "WEST");
//   }
//
//	@OnlyIn(Dist.CLIENT)
//    public static void draw3dRectangle(MatrixStack ms, IRenderTypeBuffer buffers, AxisAlignedBB aabb, Color color, String side) {
//	   int r = color.getRed();
//	   int g = color.getGreen();
//	   int b = color.getBlue();
//	   int a = color.getAlpha();
//	   double renderPosX = MC.getRenderManager().info.getProjectedView().getX();
//	   double renderPosY = MC.getRenderManager().info.getProjectedView().getY();
//	   double renderPosZ = MC.getRenderManager().info.getProjectedView().getZ();
//	
//	   ms.push();
//	   ms.translate(aabb.minX - renderPosX, aabb.minY - renderPosY, aabb.minZ - renderPosZ);
//	
//	   IVertexBuilder buffer = buffers.getBuffer(RenderType.makeType(Refs.MODID + ":rectangle_highlight", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, false, true, 
//									    		   RenderType.State.getBuilder()
//									    		   .transparency(ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_"))
//									    		   .cull(new RenderState.CullState(false))
//									    		   .build(false)));
//	   Matrix4f mat = ms.getLast().getMatrix();
//	
//	   float x = (float) (aabb.maxX - aabb.minX);
//	   float y = (float) (aabb.maxY - aabb.minY);
//	   float z = (float) (aabb.maxZ - aabb.minZ);
//	
//	   switch (side) {
//	       case "TOP":
//	           buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	           break;
//	       case "BOTTOM":
//	           buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	           break;
//	       case "NORTH":
//	           buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	           break;
//	       case "EAST":
//	           buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	           break;
//	       case "SOUTH":
//	           buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	           break;
//	       case "WEST":
//	           buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	           buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	           break;
//	   }
//	   ms.pop();
//   }
//
//	@OnlyIn(Dist.CLIENT)
//    public static void draw3dOutline(MatrixStack ms, IRenderTypeBuffer buffers, AxisAlignedBB aabb, Color color) {
//	   int r = color.getRed();
//	   int g = color.getGreen();
//	   int b = color.getBlue();
//	   int a = color.getAlpha();
//	   double renderPosX = MC.getRenderManager().info.getProjectedView().getX();
//	   double renderPosY = MC.getRenderManager().info.getProjectedView().getY();
//	   double renderPosZ = MC.getRenderManager().info.getProjectedView().getZ();
//	
//	   ms.push();
//	   ms.translate(aabb.minX - renderPosX, aabb.minY - renderPosY, aabb.minZ - renderPosZ);
//	
//	   RenderType.State glState = RenderType.State.getBuilder()
//									    		  .line(new RenderState.LineState(OptionalDouble.of(1)))
//									    		  //.layer(ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228500_J_"))  // Crashes (Why?)
//									    		  .transparency(ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_"))
//									    		  .writeMask(new RenderState.WriteMaskState(true, false))
//									    		  .depthTest(new RenderState.DepthTestState("", GL11.GL_ALWAYS))  // What is that String???
//									    		  .build(false);
//	   IVertexBuilder buffer = buffers.getBuffer(RenderType.makeType(Refs.MODID + ":line_1_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState));
//	   Matrix4f mat = ms.getLast().getMatrix();
//	
//	   float x = (float) (aabb.maxX - aabb.minX);
//	   float y = (float) (aabb.maxY - aabb.minY);
//	   float z = (float) (aabb.maxZ - aabb.minZ);
//	
//	   // Top edges
//	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	
//	   // Bottom edges
//	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	
//	   // Side edges
//	   buffer.pos(mat, x, 0, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, y, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, 0).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, y, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, 0, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, 0, z).color(r, g, b, a).endVertex();
//	   buffer.pos(mat, x, y, z).color(r, g, b, a).endVertex();
//	
//	   ms.pop();
//   }
//	
//    // When the TER::render method is called, the origin [0,0,0] is at the current [x,y,z] of the block being rendered.
//    // The tetrahedron-drawing method draws the tetrahedron in a cube region from [0,0,0] to [1,1,1] but we want it
//    //   to be in the block one above this, i.e. from [0,1,0] to [1,2,1],
//    //   so we need to translate up by one block, i.e. by [0,1,0]
//    final Vec3d TRANSLATION_OFFSET = new Vec3d(0, 1, 0);
//
//    matrixStack.push(); // push the current transformation matrix + normals matrix
//    matrixStack.translate(TRANSLATION_OFFSET.x,TRANSLATION_OFFSET.y,TRANSLATION_OFFSET.z); // translate
//    Color artifactColour = tileEntityMBE21.getArtifactColour();
//
//    drawTetrahedronWireframe(matrixStack, renderBuffers, artifactColour);
//    matrixStack.pop(); // restore the original transformation matrix + normals matrix
}
