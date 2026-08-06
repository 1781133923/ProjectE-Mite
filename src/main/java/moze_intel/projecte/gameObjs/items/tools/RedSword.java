package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.StatCollector;

public class RedSword extends DarkSword
{
	public RedSword() 
	{
		super("rm_sword", (byte)3, new String[]{
				"pe.redsword.mode1",
				"pe.redsword.mode2"
		});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "sword";
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager)
	{
		boolean flag = ProjectEConfig.useOldDamage;
		attackWithCharge(stack, damaged, damager, flag ? REDSWORD_BASE_ATTACK : 1.0F);
		return true;
	}

	@Override
	public void doExtraFunction(ItemStack stack, EntityPlayer player)
	{
		// C-key special attack deals a flat 50 damage in an AOE.
		attackAOE(stack, player, getMode(stack) == 1, 50.0F, 0);
	}
}
