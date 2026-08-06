package moze_intel.projecte.network.commands;

import net.minecraft.ICommandSender;
import net.minecraft.ChatComponentTranslation;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.handlers.TileEntityHandler;

public class ReloadEmcCMD extends ProjectEBaseCMD
{
	@Override
	public String getCommandName() 
	{
		return "projecte_reloadEMC";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/projecte reloadEMC";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) 
	{
		sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.reload.started").getFormattedText()));

		EMCMapper.clearMaps();
		CustomEMCParser.readUserData();
		EMCMapper.map();
		TileEntityHandler.checkAllCondensers();

		sender.sendChatToPlayer(net.minecraft.ChatMessageComponent.createFromText(new ChatComponentTranslation("pe.command.reload.success").getFormattedText()));

		PacketHandler.sendFragmentedEmcPacketToAll();
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}
}
