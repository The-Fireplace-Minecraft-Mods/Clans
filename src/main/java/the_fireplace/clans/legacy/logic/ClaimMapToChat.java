package the_fireplace.clans.legacy.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanColors;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.OrderedPair;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;
import the_fireplace.clans.networking.SynchronizedMessageQueue;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ClaimMapToChat extends VirtualClaimMap {
    public static final char[] MAP_CHARS = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
    public static final char SECTION_SYMBOL = '\u00A7';
    private static final String DARK_GREEN = SECTION_SYMBOL+"2";
    private static final String BLUE = SECTION_SYMBOL+"9";
    private static final String LIME_GREEN = SECTION_SYMBOL+"a";
    private static final String RED = SECTION_SYMBOL+"c";
    private static final String YELLOW = SECTION_SYMBOL+"e";
    private static final String END_KEY_SYMBOL = ";";
    private static final String BORDER_STRING = "=================================================";
    public static final String WILDERNESS_SYMBOL = "-";

    private final ChunkPos playerChunk;
    private final ICommandSender messageTarget;
    private final int dimension;
    private final ConcurrentMap<UUID, Character> symbolMap = new ConcurrentHashMap<>();
    private final ITextComponent[] bodyMessages;
    private final boolean useAllianceColorScheme;
    private final UUID playerId;
    private final boolean showCacheSegment;
    protected final OrderedPair<Integer, Integer> cacheSegment;

    private ClaimMapToChat(ICommandSender messageTarget, ChunkPos playerChunk, int dimension, boolean isSmall, @Nullable OrderedPair<Integer, Integer> cacheSegment) {
        super();
        this.playerChunk = playerChunk;
        this.messageTarget = messageTarget;
        this.dimension = dimension;
        this.height = getHeight(isSmall);
        this.bodyMessages = new ITextComponent[height];
        this.showCacheSegment = cacheSegment != null;
        if(cacheSegment == null)
            cacheSegment = calculateCacheSegment(playerChunk);
        this.cacheSegment = cacheSegment;
        useAllianceColorScheme = isSmall;
        playerId = getTargetId(messageTarget);
    }

    public static void sendSingleFancyMap(EntityPlayerMP targetPlayer) {
        createFancyMap(targetPlayer, new ChunkPos(targetPlayer.getPosition()), targetPlayer.dimension).prepareAndSend();
    }

    public static void sendAllFancyMaps(EntityPlayerMP targetPlayer) {
        ConcurrentExecutionManager.runKillable(() -> {
            for (OrderedPair<Integer, Integer> section : ClaimData.INSTANCE.getOccupiedCacheSections())
                createFancyMap(targetPlayer, new ChunkPos(targetPlayer.getPosition()), targetPlayer.dimension, section).prepareAndSend();
        });
    }

    private byte getHeight(boolean isSmall) {
        return isSmall ? ClaimData.CACHE_SECTION_SIZE / 7 : ClaimData.CACHE_SECTION_SIZE;
    }

    @Nullable
    private UUID getTargetId(ICommandSender messageTarget) {
        return messageTarget instanceof Entity ? ((Entity) messageTarget).getUniqueID() : null;
    }

    @Override
    protected ChunkPos calculateCenter() {
        return playerChunk;
    }

    @Override
    protected OrderedPair<Integer, Integer> getCacheSegment() {
        return cacheSegment;
    }

    private OrderedPair<Integer, Integer> calculateCacheSegment(ChunkPos playerChunk) {
        return new OrderedPair<>(playerChunk.x / ClaimData.CACHE_SECTION_SIZE, playerChunk.z / ClaimData.CACHE_SECTION_SIZE);
    }
    
    public static ClaimMapToChat createFancyMap(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension, false, null);
    }

    public static ClaimMapToChat createFancyMap(ICommandSender messageTarget, ChunkPos originChunk, int dimension, OrderedPair<Integer, Integer> cacheSegment) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension, false, cacheSegment);
    }

    public static ClaimMapToChat createAllianceMap(ICommandSender messageTarget, ChunkPos originChunk, int dimension) {
        return new ClaimMapToChat(messageTarget, originChunk, dimension, true, null);
    }

    public void prepareAndSend() {
        ConcurrentExecutionManager.runKillable(() -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            prepareMapBodyAndKey(executor);
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            send();
        });
    }
    
    private void prepareMapBodyAndKey(ExecutorService executor) {
        int minX = getMinX();
        int maxX = getMaxX();
        int minZ = getMinZ();
        int maxZ = getMaxZ();
        byte zOff = getQuadrantZOffset();
        for(int z = minZ; z <= maxZ; z++) {
            int finalZ = z;
            executor.execute(() -> {
                String row = buildRow(finalZ, minX, maxX);
                bodyMessages[finalZ - getCenterChunk().z + height/2 - zOff] = new TextComponentString(row);
            });
        }
    }

    private String buildRow(int finalZ, int minX, int maxX) {
        StringBuilder row = new StringBuilder();
        for (int x = minX; x <= maxX; x++) {
            boolean isPlayerChunk = isPlayerChunk(x, finalZ);
            ChunkPositionWithData pos = ClaimAccessor.getInstance().getChunkPositionData(x, finalZ, dimension);
            UUID clan = ClaimAccessor.getInstance().getChunkClan(pos);
            if(pos == null || clan == null)
                row.append(getChunkColor(isPlayerChunk, getWildernessColor())).append(WILDERNESS_SYMBOL);
            else if(pos.isBorderland())
                row.append(getChunkColor(isPlayerChunk, getClanColor(clan))).append(WILDERNESS_SYMBOL);
            else {
                symbolMap.putIfAbsent(clan, MAP_CHARS[symbolMap.size() % MAP_CHARS.length]);
                row.append(getChunkColor(isPlayerChunk, getClanColor(clan))).append(symbolMap.get(clan));
            }
        }
        return row.toString();
    }

    private String getChunkColor(boolean isPlayerChunk, String nonPlayerColor) {
        return isPlayerChunk ? BLUE : nonPlayerColor;
    }

    private String getClanColor(UUID clan) {
        if(useAllianceColorScheme)
            return (isAlliedTo(clan) ? LIME_GREEN : RED);
        return SECTION_SYMBOL + Integer.toHexString(ClanColors.get(clan).getColorFormatting().getColorIndex());
    }

    private boolean isAlliedTo(UUID clan) {
        return ClanMembers.get(clan).isMember(playerId);
    }

    private String getWildernessColor() {
        return ClansModContainer.getConfig().shouldProtectWilderness() || useAllianceColorScheme ? YELLOW : DARK_GREEN;
    }

    private boolean isPlayerChunk(int x, int z) {
        return playerChunk.z == z && playerChunk.x == x;
    }

    private void send() {
        ArrayList<ITextComponent> messages = new ArrayList<>();
        if(showCacheSegment)
            messages.add(getCacheSegmentComponent());
        messages.add(getBorderComponent());
        messages.addAll(Arrays.asList(bodyMessages));
        messages.add(getBorderComponent());
        messages.addAll(getMapSymbolGuide());
        if(showCacheSegment)
            messages.add(getEndSegmentComponent());

        SynchronizedMessageQueue.queueMessages(messageTarget, messages.toArray(new ITextComponent[0]));
    }

    private List<ITextComponent> getMapSymbolGuide() {
        return symbolMap.entrySet().stream().map(this::getSymbolMapEntry).collect(Collectors.toList());
    }

    private ITextComponent getSymbolMapEntry(Map.Entry<UUID, Character> symbol) {
        UUID c = symbol.getKey();
        return new TextComponentString(symbol.getValue() + ": " + getSymbolName(c)).setStyle(getTextStyle(c));
    }

    private String getSymbolName(UUID clan) {
        return ClanIdRegistry.isValidClan(clan)
            ? ClanNames.get(clan).getName()
            : TranslationUtil.getStringTranslation(messageTarget, "clans.wilderness");
    }

    private Style getTextStyle(UUID c) {
        if(c == null)
            return TextStyles.YELLOW;
        if(useAllianceColorScheme)
            return isAlliedTo(c) ? TextStyles.GREEN : TextStyles.RED;
        else
            return new Style().setColor(ClanColors.get(c).getColorFormatting());
    }

    private ITextComponent getBorderComponent() {
        return new TextComponentString(BORDER_STRING).setStyle(TextStyles.GREEN);
    }

    private ITextComponent getCacheSegmentComponent() {
        return new TextComponentString(CACHE_SEGMENT_SEPARATOR+cacheSegment.getValue1()+CACHE_SEGMENT_SEPARATOR+cacheSegment.getValue2()+CACHE_SEGMENT_SEPARATOR).setStyle(TextStyles.BLACK);
    }

    private ITextComponent getEndSegmentComponent() {
        return new TextComponentString(END_KEY_SYMBOL).setStyle(TextStyles.BLACK);
    }
}
