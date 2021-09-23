package dev.alef.lazybuilder.render;

import org.lwjgl.opengl.GL11;

import dev.alef.lazybuilder.Refs;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public abstract class RenderTypeMod extends RenderState {
	
    public RenderTypeMod(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, setupTaskIn, clearTaskIn);
	}
    
    private static final ResourceLocation shadersTexture = new ResourceLocation(Refs.MODID, "textures/other/shader_texture.png");
    private static final RenderState.TextureState textureState = new RenderState.TextureState(shadersTexture, false, false);
    
    private static final RenderType.State LINE_STATE = RenderType.State.getBuilder().line(RenderType.DEFAULT_LINE).layer(RenderType.field_239235_M_).target(RenderType.field_241712_U_).writeMask(RenderType.COLOR_DEPTH_WRITE).build(false);
    private static final RenderType.State TRANS_TEXTURE_STATE = RenderType.State.getBuilder().transparency(RenderType.TRANSLUCENT_TRANSPARENCY).texture(textureState).cull(RenderType.CULL_DISABLED).build(false);
    private static final RenderType.State SOLID_STATE = RenderType.State.getBuilder().shadeModel(RenderType.SHADE_ENABLED).lightmap(RenderType.LIGHTMAP_ENABLED).diffuseLighting(RenderType.DIFFUSE_LIGHTING_ENABLED).cull(RenderType.CULL_DISABLED).build(true);

    public static final RenderType LINE = RenderType.makeType(Refs.MODID + ":lines", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, LINE_STATE);
    public static final RenderType LINE_LOOP = RenderType.makeType(Refs.MODID + ":lines_loop", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256, LINE_STATE);
    public static final RenderType QUAD = RenderType.makeType(Refs.MODID + ":quads", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, false, true, TRANS_TEXTURE_STATE);
    public static final RenderType POLYGON = RenderType.makeType(Refs.MODID + ":polygon", DefaultVertexFormats.POSITION_COLOR, GL11.GL_POLYGON, 256, false, true, TRANS_TEXTURE_STATE);
    public static final RenderType SPHERE = RenderType.makeType(Refs.MODID + ":sphere", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUAD_STRIP, 256, false, false, TRANS_TEXTURE_STATE);
    public static final RenderType SOLID_SPHERE = RenderType.makeType(Refs.MODID + ":solid_sphere", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUAD_STRIP, 2097152, true, false, SOLID_STATE);
}
