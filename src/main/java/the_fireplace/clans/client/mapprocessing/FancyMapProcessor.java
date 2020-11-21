package the_fireplace.clans.client.mapprocessing;

import com.google.common.collect.Maps;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.logic.VirtualClaimMap;
import the_fireplace.clans.legacy.model.ChunkPosition;

import java.util.Map;

public final class FancyMapProcessor {

    private static MapFragmentType currentState = MapFragmentType.CACHE_SEGMENT;
    private static MapFragmentType previousState = MapFragmentType.END;
    private static byte contentLineNumber = 0;

    private static final Map<ChunkPosition, Character> positionCharacters = Maps.newHashMap();
    private static final Map<Character, String> characterClans = Maps.newHashMap();
    private static int cacheX, cacheZ;

    public static boolean processLine(String line) {
        if (!isExpectedState(line))
            return false;
        processStateForLine(line);
        if (isSkippingSymbolGuide(line))
            updateState();
        if (!isContinuingSymbolGuide(line))
            updateState();
        return true;
    }

    private static boolean isExpectedState(String line) {
        if (isContinuingSymbolGuide(line) || isSkippingSymbolGuide(line))
            return true;
        return line.matches(currentState.getRegex());
    }

    private static boolean isContinuingSymbolGuide(String line) {
        return previousState == MapFragmentType.SYMBOL_GUIDE && line.matches(MapFragmentType.SYMBOL_GUIDE.getRegex());
    }

    private static boolean isSkippingSymbolGuide(String line) {
        return previousState == MapFragmentType.END_BORDER && line.matches(MapFragmentType.END.getRegex());
    }

    private static void processStateForLine(String line) {
        switch (currentState) {
            case CACHE_SEGMENT:
                processCacheSegment(line);
                break;
            case CONTENT:
                processContent(line);
                break;
            case SYMBOL_GUIDE:
                if (!isSkippingSymbolGuide(line))
                    processSymbolInformation(line);
                break;
            case END:
                if (isContinuingSymbolGuide(line))
                    processSymbolInformation(line);
        }
    }

    private static void processCacheSegment(String line) {
        String[] splitLine = line.split(MapRegex.regexEscaped(VirtualClaimMap.CACHE_SEGMENT_SEPARATOR));
        cacheX = Integer.parseInt(splitLine[1]);
        cacheZ = Integer.parseInt(splitLine[2]);
    }

    private static void processContent(String line) {
        ContentLineProcessor processor = new ContentLineProcessor(cacheX, cacheZ, line, contentLineNumber);
        positionCharacters.putAll(processor.getCharacterPositions());
    }

    private static void processSymbolInformation(String line) {
        String[] splitLine = line.split(": ");
        Character symbol = splitLine[0].charAt(0);
        String clanName = splitLine[1];
        characterClans.put(symbol, clanName);
    }

    private static void updateState() {
        previousState = currentState;
        currentState = getNextState();
        updateContentLineCounter();
        if (previousState == MapFragmentType.END)
            sendMapData();
    }

    private static MapFragmentType getNextState() {
        switch (currentState) {
            case END:
                return MapFragmentType.CACHE_SEGMENT;
            case CONTENT:
                if (contentLineNumber < ClaimData.CACHE_SECTION_SIZE - 1)
                    return MapFragmentType.CONTENT;
                //Intentionally continue to default
            default:
                return MapFragmentType.values()[currentState.ordinal()+1];
        }
    }

    private static void updateContentLineCounter() {
        if (previousState == MapFragmentType.CONTENT) {
            if (currentState == previousState) {
                contentLineNumber++;
            } else {
                contentLineNumber = 0;
            }
        }
    }

    private static void sendMapData() {
        Map<ChunkPosition, String> mapData = Maps.newHashMap();
        positionCharacters.forEach((key, value) -> mapData.put(key, characterClans.get(value)));
        MapInterceptedEvent.fire(mapData);
        reset();
    }

    private static void reset() {
        positionCharacters.clear();
        characterClans.clear();
    }
}
