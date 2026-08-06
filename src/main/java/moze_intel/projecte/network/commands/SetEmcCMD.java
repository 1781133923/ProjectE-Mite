package moze_intel.projecte.network.commands;

import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.utils.ChatHelper;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.ICommandSender;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;

public class SetEmcCMD extends ProjectEBaseCMD
{
	@Override
	public String getCommandName() 
	{
		return "projecte_setEMC";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "pe.command.set.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) 
	{
		if (params.length < 1)
		{
			sendError(sender, new ChatComponentTranslation("pe.command.set.usage"));
			return;
		}

		String name;
		int meta;
		int emc;

		if (params.length == 1)
		{
			ItemStack heldItem = getCommandSenderAsPlayer(sender).getHeldItemStack();

			if (heldItem == null)
			{
				sendError(sender, new ChatComponentTranslation("pe.command.set.usage"));
				return;
			}

			name = moze_intel.projecte.compat.PECompatHelper.getItemName(heldItem.getItem());
			meta = heldItem.getItemDamage();
			emc = MathUtils.parseInteger(params[0]);

			if (emc < 0)
			{
				sendError(sender, new ChatComponentTranslation("pe.command.set.invalidemc", params[0]));
			}
		}
		else
		{
			name = params[0];
			meta = 0;
			boolean isOD = !name.contains(":");

			if (!isOD)
			{
				if (params.length > 2)
				{
					meta = MathUtils.parseInteger(params[1]);

					if (meta < 0)
					{
						sendError(sender, new ChatComponentTranslation("pe.command.set.invalidmeta", params[1]));
						return;
					}

					emc = MathUtils.parseInteger(params[2]);

					if (emc < 0)
					{
						sendError(sender, new ChatComponentTranslation("pe.command.set.invalidemc", params[0]));
						return;
					}
				}
				else
				{
					emc = MathUtils.parseInteger(params[1]);

					if (emc < 0)
					{
						sendError(sender, new ChatComponentTranslation("pe.command.set.invalidemc", params[0]));
						return;
					}
				}
			}
			else
			{
				emc = MathUtils.parseInteger(params[1]);

				if (emc < 0)
				{
					sendError(sender, new ChatComponentTranslation("pe.command.set.invalidemc", params[0]));
					return;
				}
			}
		}

		if (CustomEMCParser.addToFile(name, meta, emc))
		{
			sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.set.success", name, emc).getFormattedText()));
			sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.reload.notice").getFormattedText()));
		}
		else
		{
			sendError(sender, new ChatComponentTranslation("pe.command.set.invaliditem", name));
		}
	}
}
