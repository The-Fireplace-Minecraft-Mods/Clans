package the_fireplace.clans.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.ChatUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ClaimMapToChat {
    private static final char[] MAP_CHARS = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
    private static final byte MAP_RADIUS = 26;
    private final ChunkPos originChunk;
    private final ICommandSender messageTarget;
    private final int dimension;
    private final ConcurrentMap<UUID, Character> symbolMap = new ConcurrentHashMap<>();
    private final ITextComponent[] bodyMessages = new ITextComponent[MAP_RADIUS*2+1];
    
    private ClaimMapToChat(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        this.originChunk = originChunk;
        this.messageTarget = messageTarget;
        this.dimension = dimension;
    }
    
    public static ClaimMapToChat create(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension);
    }

    public void prepareAndSend() {
        new Thread(() -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            prepareMapBodyAndKey(executor);
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            send();
        }).start();
    }
    
    private void prepareMapBodyAndKey(ExecutorService executor) {
        for(int z = originChunk.z-MAP_RADIUS; z <= originChunk.z + MAP_RADIUS; z++) {
            int finalZ = z;
            executor.execute(() -> {
                StringBuilder row = new StringBuilder();
                for (int x = originChunk.x - 26; x <= originChunk.x + 26; x++) {
                    String wildernessColor = originChunk.z == finalZ && originChunk.x == x ? "\u00A79" : Clans.getConfig().shouldProtectWilderness() ? "\u00A7e" : "\u00A72";
                    ChunkPositionWithData pos = ClaimData.getChunkPositionData(x, finalZ, dimension);
                    Clan clan = ClaimData.getChunkClan(pos);
                    if(pos == null || clan == null)
                        row.append(wildernessColor).append('-');
                    else if(pos.isBorderland())
                        row.append('\u00A7').append(Integer.toHexString(clan.getTextColor().getColorIndex())).append('-');
                    else {
                        if (!symbolMap.containsKey(clan.getId()))
                            symbolMap.put(clan.getId(), MAP_CHARS[symbolMap.size() % MAP_CHARS.length]);
                        row.append(originChunk.z == finalZ && originChunk.x == x ? "\u00A79": '\u00A7'+Integer.toHexString(clan.getTextColor().getColorIndex())).append(symbolMap.get(clan.getId()));
                    }
                }
                bodyMessages[finalZ - originChunk.z + MAP_RADIUS] = new TextComponentString(row.toString());
            });
        }
    }
    
    private void send() {
        sendMapHeader();
        sendMapBody();
        sendMapFooter();
        sendMapSymbolGuide();
    }

    private void sendMapHeader() {
        ChatUtil.sendMessage(messageTarget, getHeaderComponent());
    }

    private void sendMapBody() {
        ChatUtil.sendMessage(messageTarget, bodyMessages);
    }

    private void sendMapFooter() {
        ChatUtil.sendMessage(messageTarget, getFooterComponent());
    }

    private void sendMapSymbolGuide() {
        for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
            Clan c = ClanCache.getClanById(symbol.getKey());
            messageTarget.sendMessage(new TextComponentString(symbol.getValue() + ": " +(c != null ? c.getName() : TranslationUtil.getStringTranslation(messageTarget, "clans.wilderness"))).setStyle(new Style().setColor(c != null ? c.getTextColor() : TextFormatting.YELLOW)));
        }
    }

    private ITextComponent getFooterComponent() {
        return new TextComponentString("=====================================================").setStyle(TextStyles.GREEN);
    }

    private ITextComponent getHeaderComponent() {
        return new TextComponentString("=====================================================").setStyle(TextStyles.GREEN);
    }
}
