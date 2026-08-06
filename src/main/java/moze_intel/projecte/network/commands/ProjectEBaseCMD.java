package moze_intel.projecte.network.commands;

import moze_intel.projecte.utils.ChatHelper;
import net.minecraft.CommandBase;
import net.minecraft.ICommandSender;
import net.minecraft.EnumChatFormatting;
import net.minecraft.IChatComponent;

public abstract class ProjectEBaseCMD extends CommandBase
{
	@Override
	public abstract String getCommandName();
	
	@Override
	public abstract int getRequiredPermissionLevel();

	@Override
	public abstract String getCommandUsage(ICommandSender sender);

	@Override
	public abstract void processCommand(ICommandSender sender, String[] params);
	
	protected void sendSuccess(ICommandSender sender, IChatComponent message)
	{
		sendMessage(sender, ChatHelper.modifyColor(message, EnumChatFormatting.GREEN));
	}
	
	protected void sendError(ICommandSender sender, IChatComponent message)
	{
		sendMessage(sender, ChatHelper.modifyColor(message, EnumChatFormatting.RED));
	}
	
	protected void sendMessage(ICommandSender sender, IChatComponent message)
	{
		sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(message.getFormattedText()));
	}
}
