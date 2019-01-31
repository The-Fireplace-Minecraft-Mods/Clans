package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandClan extends CommandBase {
    private static final CommandBase buy = new CommandBuy();
    private static final CommandBase sell = new CommandSell();
    private static final CommandBase identify = new CommandIdentify();
    private static final CommandBase collect = new CommandCollect();
    private static final CommandBase buyoffers = new CommandBuyOffers();
    private static final CommandBase selloffers = new CommandSellOffers();
    private static final CommandBase myoffers = new CommandMyOffers();
    private static final CommandBase canceloffer = new CommandCancelOffer();

    @Override
    public String getName() {
        return "ge";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ge <command> [parameters]";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException("/ge <command> [parameters]");
        String tag = args[0];
        if(args.length > 1)
            args = Arrays.copyOfRange(args, 1, args.length);
        else
            args = new String[]{};
        switch(tag){
            case "buy":
            case "b":
                buy.execute(server, sender, args);
                return;
            case "sell":
            case "s":
                sell.execute(server, sender, args);
                return;
            case "identify":
            case "i":
                identify.execute(server, sender, args);
                return;
            case "collect":
            case "c":
                collect.execute(server, sender, args);
                return;
            case "buyoffers":
            case "bo":
                buyoffers.execute(server, sender, args);
                return;
            case "selloffers":
            case "so":
                selloffers.execute(server, sender, args);
                return;
            case "myoffers":
            case "m":
            case "mo":
                myoffers.execute(server, sender, args);
                return;
            case "canceloffer":
            case "co":
                canceloffer.execute(server, sender, args);
                return;
            case "help":
            case "h":
                sender.sendMessage(new TextComponentString("/ge commands:\n" +
                        "buy\n" +
                        "sell\n" +
                        "identify\n" +
                        "collect\n" +
                        "buyoffers\n" +
                        "selloffers\n" +
                        "myoffers\n" +
                        "canceloffer\n" +
                        "help"));
                return;
        }
        throw new WrongUsageException("/ge <command> [parameters]");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
