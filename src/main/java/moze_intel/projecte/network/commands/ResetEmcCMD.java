package moze_intel.projecte.network.commands;

import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.utils.ChatHelper;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.ICommandSender;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;

public class ResetEmcCMD extends ProjectEBaseCMD
{
	@Override
	public String getCommandName() 
	{
		return "projecte_resetEMC";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "pe.command.reset.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) 
	{
		String name = "";
		int meta = 0;

		if (params.length == 0)
		{
			ItemStack heldItem = getCommandSenderAsPlayer(sender).getHeldItemStack();

			if (heldItem == null)
			{
				sendError(sender, new ChatComponentTranslation("pe.command.reset.usage"));
				return;
			}

			name = moze_intel.projecte.compat.PECompatHelper.getItemName(heldItem.getItem());
			meta = heldItem.getItemDamage();
		}
		else
		{
			name = params[0];

			if (params.length > 1)
			{
				meta = MathUtils.parseInteger(params[1]);

				if (meta < 0)
				{
					sendError(sender, new ChatComponentTranslation("pe.command.reset.invalidmeta", params[1]));
					return;
				}
			}
		}

		if (CustomEMCParser.removeFromFile(name, meta))
		{
			sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.reset.success", name).getFormattedText()));
			sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.reload.notice").getFormattedText()));
		}
		else
		{
			sendError(sender, new ChatComponentTranslation("pe.command.reset.nochange", name, meta));
		}
	}
}
