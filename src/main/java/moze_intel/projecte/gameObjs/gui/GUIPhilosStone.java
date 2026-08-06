package moze_intel.projecte.gameObjs.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.container.PhilosStoneContainer;
import net.minecraft.GuiContainer;
import net.minecraft.InventoryPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.StatCollector;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GUIPhilosStone extends GuiContainer
{
	private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("textures/gui/container/crafting_table.png");
	
	public GUIPhilosStone(InventoryPlayer inventoryPlayer)
	{
		super(new PhilosStoneContainer(inventoryPlayer));
	}
	
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
	{
		this.fontRenderer.drawString(StatCollector.translateToLocal("container.crafting"), 28, 6, 4210752);
		this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(craftingTableGuiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

		// MITE crafts with a progress arrow (same as the regular crafting
		// table): it fills as the craft progresses on the client.
		net.minecraft.EntityClientPlayerMP player = this.mc.thePlayer;
		if (player != null && player.crafting_ticks > 0 && player.crafting_period > 0)
		{
			this.drawTexturedModalRect(k + 90, l + 34, 176, 0,
					player.crafting_ticks * 23 / player.crafting_period, 16);
		}
	}
}
