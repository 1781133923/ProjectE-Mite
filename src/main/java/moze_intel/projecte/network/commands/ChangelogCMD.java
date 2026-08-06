package moze_intel.projecte.network.commands;

import com.google.common.collect.Lists;
import net.minecraft.ICommandSender;
import net.minecraft.ChatComponentText;
import net.minecraft.ChatComponentTranslation;

import java.util.List;

public class ChangelogCMD extends ProjectEBaseCMD
{
	public static final List<String> changelog = Lists.newArrayList();
	
	@Override
	public String getCommandName() 
	{
		return "projecte_changelog";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/projecte changelog";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) 
	{
		if (ChangelogCMD.changelog.isEmpty())
		{
			sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.changelog.uptodate").getFormattedText()));
		}
		else
		{
			for (String s: ChangelogCMD.changelog)
			{
				sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentText(s).getFormattedText()));
			}
		}
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 0;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}
}
