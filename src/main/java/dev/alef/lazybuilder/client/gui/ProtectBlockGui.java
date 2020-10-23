package dev.alef.lazybuilder.client.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.alef.lazybuilder.Refs;
import dev.alef.lazybuilder.container.ProtectBlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProtectBlockGui extends ContainerScreen<ProtectBlockContainer> implements IHasContainer<ProtectBlockContainer> {

	@SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger();
	
	private static final ResourceLocation field_147017_u = new ResourceLocation(Refs.MODID, "textures/gui/container/protect_block_gui.png");
	private final int field_147018_x;
	private final int field_147000_g;

	public ProtectBlockGui(ProtectBlockContainer p_i51095_1_, PlayerInventory p_i51095_2_, ITextComponent p_i51095_3_) {
		super(p_i51095_1_, p_i51095_2_, p_i51095_3_);
		this.field_230711_n_ = false;
		this.field_147018_x = Refs.containerRows;
		this.field_147000_g = 114 + this.field_147018_x * 18;
		this.field_238745_s_ = this.field_147000_g - 94;
	}

	public void func_230430_a_(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.func_230446_a_(p_230430_1_);
		super.func_230430_a_(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		this.func_230459_a_(p_230430_1_, p_230430_2_, p_230430_3_);
	}

	@SuppressWarnings("deprecation")
	protected void func_230450_a_(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	    Minecraft.getInstance().getTextureManager().bindTexture(field_147017_u);
		int i = (int) (this.field_230708_k_ - this.xSize) / 2;
		int j = (int) (this.field_230709_l_ - this.field_147000_g) / 2;
		this.func_238474_b_(p_230450_1_, i, j, 0, 0, this.xSize, this.field_147000_g + 17 * 2);
	}
}

