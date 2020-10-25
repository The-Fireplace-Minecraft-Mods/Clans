package the_fireplace.clans.legacy.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.OrderedPair;
import the_fireplace.clans.legacy.util.ChatUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ClaimMapToChat {
    private static final char[] MAP_CHARS = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
    private static final char SECTION_SYMBOL = '\u00A7';
    private static final String DARK_GREEN = SECTION_SYMBOL+"2";
    private static final String BLUE = SECTION_SYMBOL+"9";
    private static final String LIME_GREEN = SECTION_SYMBOL+"a";
    private static final String RED = SECTION_SYMBOL+"c";
    private static final String YELLOW = SECTION_SYMBOL+"e";
    private static final String CACHE_SEGMENT_SEPARATOR = "|";
    private final ChunkPos playerChunk;
    private final ChunkPos centerChunk;
    private final ICommandSender messageTarget;
    private final int dimension;
    private final ConcurrentMap<UUID, Character> symbolMap = new ConcurrentHashMap<>();
    private final ITextComponent[] bodyMessages;
    private final boolean useAllianceColorScheme;
    private final int width, height;
    private final UUID playerId;
    private final boolean showCacheSegment;
    private final OrderedPair<Integer, Integer> cacheSegment;
    
    private ClaimMapToChat(ICommandSender messageTarget, ChunkPos playerChunk, int dimension, boolean isSmall, @Nullable OrderedPair<Integer, Integer> cacheSegment) {
        this.playerChunk = playerChunk;
        this.messageTarget = messageTarget;
        this.dimension = dimension;
        this.width = ClaimData.CACHE_SECTION_SIZE;
        this.height = getHeight(isSmall);
        this.bodyMessages = new ITextComponent[height];
        this.showCacheSegment = cacheSegment != null;
        if(cacheSegment == null)
            cacheSegment = calculateCacheSegment(playerChunk);
        this.cacheSegment = cacheSegment;
        centerChunk = calculateCenter();
        useAllianceColorScheme = isSmall;
        playerId = getTargetId(messageTarget);
    }

    private byte getHeight(boolean isSmall) {
        return isSmall ? ClaimData.CACHE_SECTION_SIZE / 7 : ClaimData.CACHE_SECTION_SIZE;
    }

    @Nullable
    private UUID getTargetId(ICommandSender messageTarget) {
        return messageTarget instanceof Entity ? ((Entity) messageTarget).getUniqueID() : null;
    }

    private ChunkPos calculateCenter() {
        int centerOffsetX = getCenterOffset(width) * getQuadrant().getValue1();
        int centerOffsetZ = getCenterOffset(height) * getQuadrant().getValue2();
        int section = (playerChunk.z % ClaimData.CACHE_SECTION_SIZE) / 7;
        centerOffsetZ += section * 7;
        return new ChunkPos(cacheSegment.getValue1()*ClaimData.CACHE_SECTION_SIZE + centerOffsetX, cacheSegment.getValue2()*ClaimData.CACHE_SECTION_SIZE + centerOffsetZ);
    }

    private int getCenterOffset(int size) {
        return size / 2;
    }

    private OrderedPair<Integer, Integer> calculateCacheSegment(ChunkPos playerChunk) {
        return new OrderedPair<>(playerChunk.x / ClaimData.CACHE_SECTION_SIZE, playerChunk.z / ClaimData.CACHE_SECTION_SIZE);
    }
    
    public static ClaimMapToChat createFancyMap(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension, false, null);
    }

    public static ClaimMapToChat createAllianceMap(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension, true, null);
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
        OrderedPair<Byte, Byte> quadrant = getQuadrant();
        byte xOff = (byte) (quadrant.getValue1() < 0 ? -1 : 0);
        int minX = centerChunk.x - width/2 + xOff;
        int maxX = centerChunk.x + width/2 + xOff;
        byte zOff = (byte) (quadrant.getValue2() < 0 ? -1 : 0);
        int minZ = centerChunk.z - height/2 + zOff;
        int maxZ = centerChunk.z + height/2 + zOff;
        for(int z = minZ; z <= maxZ; z++) {
            int finalZ = z;
            executor.execute(() -> {
                String row = buildRow(finalZ, minX, maxX);
                bodyMessages[finalZ - centerChunk.z + height/2 - zOff] = new TextComponentString(row);
            });
        }
    }

    private String buildRow(int finalZ, int minX, int maxX) {
        StringBuilder row = new StringBuilder();
        for (int x = minX; x <= maxX; x++) {
            boolean isPlayerChunk = isPlayerChunk(x, finalZ);
            ChunkPositionWithData pos = ClaimData.getChunkPositionData(x, finalZ, dimension);
            Clan clan = ClaimData.getChunkClan(pos);
            if(pos == null || clan == null)
                row.append(getChunkColor(isPlayerChunk, getWildernessColor())).append('-');
            else if(pos.isBorderland())
                row.append(getChunkColor(isPlayerChunk, getClanColor(clan))).append('-');
            else {
                symbolMap.putIfAbsent(clan.getId(), MAP_CHARS[symbolMap.size() % MAP_CHARS.length]);
                row.append(getChunkColor(isPlayerChunk, getClanColor(clan))).append(symbolMap.get(clan.getId()));
            }
        }
        return row.toString();
    }

    private String getChunkColor(boolean isPlayerChunk, String nonPlayerColor) {
        return isPlayerChunk ? BLUE : nonPlayerColor;
    }

    private String getClanColor(Clan clan) {
        if(useAllianceColorScheme)
            return (isAlliedTo(clan) ? LIME_GREEN : RED);
        return SECTION_SYMBOL + Integer.toHexString(clan.getTextColor().getColorIndex());
    }

    private boolean isAlliedTo(Clan clan) {
        return clan.getMembers().containsKey(playerId);
    }

    private String getWildernessColor() {
        return ClansModContainer.getConfig().shouldProtectWilderness() || useAllianceColorScheme ? YELLOW : DARK_GREEN;
    }

    private boolean isPlayerChunk(int x, int z) {
        return playerChunk.z == z && playerChunk.x == x;
    }

    private OrderedPair<Byte, Byte> getQuadrant() {
        return new OrderedPair<>((byte)Math.copySign(1, playerChunk.x), (byte)Math.copySign(1, playerChunk.z));
    }

    private void send() {
        if(showCacheSegment)
            sendCacheSegment();
        sendMapBorder();
        sendMapBody();
        sendMapBorder();
        sendMapSymbolGuide();
    }

    private void sendCacheSegment() {
        ChatUtil.sendMessage(messageTarget, getCacheSegmentComponent());
    }

    private void sendMapBody() {
        ChatUtil.sendMessage(messageTarget, bodyMessages);
    }

    private void sendMapBorder() {
        ChatUtil.sendMessage(messageTarget, getBorderComponent());
    }

    private void sendMapSymbolGuide() {
        for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
            Clan c = ClanDatabase.getClanById(symbol.getKey());
            messageTarget.sendMessage(new TextComponentString(symbol.getValue() + ": " +(c != null ? c.getName() : TranslationUtil.getStringTranslation(messageTarget, "clans.wilderness"))).setStyle(getTextStyle(c)));
        }
    }

    private Style getTextStyle(Clan c) {
        if(c == null)
            return TextStyles.YELLOW;
        if(useAllianceColorScheme)
            return isAlliedTo(c) ? TextStyles.GREEN : TextStyles.RED;
        else
            return new Style().setColor(c.getTextColor());
    }

    private ITextComponent getBorderComponent() {
        return new TextComponentString("=================================================").setStyle(TextStyles.GREEN);
    }

    private ITextComponent getCacheSegmentComponent() {
        return new TextComponentString(CACHE_SEGMENT_SEPARATOR+cacheSegment.getValue1()+CACHE_SEGMENT_SEPARATOR+cacheSegment.getValue2()+CACHE_SEGMENT_SEPARATOR).setStyle(TextStyles.YELLOW);
    }
}
