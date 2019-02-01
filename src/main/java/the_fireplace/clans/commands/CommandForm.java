package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.NOCLAN;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan form <name> [banner]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 1 || args.length == 2){
			if(sender instanceof Entity) {
				String newClanName = args[0];
				if(ClanCache.clanNameTaken(newClanName))
					sender.sendMessage(new TextComponentString(MinecraftColors.RED+"The clan name you have specified is already taken."));
				else {
					String banner = null;
					if(args.length == 2){
						try {
							JsonToNBT.getTagFromJson(args[1]);
							//TODO: Check that the tag would actually be a valid NBTTagList of patterns
							banner = args[1];
							if(ClanCache.clanBannerTaken(banner)){
								sender.sendMessage(new TextComponentString(MinecraftColors.RED+"The clan banner you have specified is already taken."));
								return;
							}
						} catch(NBTException e){
							throw new SyntaxErrorException("Invalid Banner NBT: "+args[1]);
						}
					}
					new Clan(newClanName, ((Entity) sender).getUniqueID(), banner);
				}
				return;
			} else {
				throw new WrongUsageException("You must be a player to do this");
			}
		}
		throwWrongUsage(sender);
	}
}
