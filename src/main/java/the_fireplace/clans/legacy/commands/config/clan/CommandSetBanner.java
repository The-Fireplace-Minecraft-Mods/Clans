package the_fireplace.clans.legacy.commands.config.clan;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.metadata.ClanBanners;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetBanner extends ClanSubCommand
{
    @Override
    public String getName() {
        return "setbanner";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        if (args.length == 1) {
            try {
                String banner = args[0];
                if (new ItemStack(JsonToNBT.getTagFromJson(banner)).isEmpty()) {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setbanner.invalid").setStyle(TextStyles.RED));
                    return;
                }
                if (ClanBanners.isClanBannerAvailable(banner)) {
                    ClanBanners.set(selectedClan, banner);
                    sender.sendMessage(TranslationUtil.getTranslation("commands.clan.setbanner.success", selectedClanName).setStyle(TextStyles.GREEN));
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setbanner.taken").setStyle(TextStyles.RED));
                }
            } catch (NBTException e) {
                throw new SyntaxErrorException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.setbanner.invalid"));
            }
        } else if (sender.getHeldItemMainhand().getItem() instanceof ItemBanner) {
            NBTTagCompound tags = sender.getHeldItemMainhand().writeToNBT(new NBTTagCompound());
            setClanBannerFromItem(sender, selectedClan, tags);
        } else if (sender.getHeldItemOffhand().getItem() instanceof ItemBanner) {
            NBTTagCompound tags = sender.getHeldItemOffhand().writeToNBT(new NBTTagCompound());
            setClanBannerFromItem(sender, selectedClan, tags);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setbanner.notheld").setStyle(TextStyles.RED));
        }
    }

    private void setClanBannerFromItem(EntityPlayerMP sender, UUID clan, @Nullable NBTTagCompound tags) {
        String banner = tags != null ? tags.toString() : "";
        if (ClanBanners.isClanBannerAvailable(banner)) {
            ClanBanners.set(clan, banner);
            sender.sendMessage(TranslationUtil.getTranslation("commands.clan.setbanner.success", ClanNames.get(clan).getName()).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setbanner.taken").setStyle(TextStyles.RED));
        }
    }
}
