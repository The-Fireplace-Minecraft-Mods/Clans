package the_fireplace.clans.compat.dynmap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.compat.dynmap.data.ClanDimInfo;
import the_fireplace.clans.compat.dynmap.data.GroupedChunks;
import the_fireplace.clans.compat.dynmap.data.PositionPoint;
import the_fireplace.clans.util.ChunkPosition;

import java.util.*;
import java.util.regex.Pattern;


public class DynmapCompat implements IDynmapCompat {
    @Override
    public void serverStart() {
        buildDynmapWorldNames();
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
        DynmapCommonAPIListener.register(new DynmapAPIListener());
    }

    private long tickCounter = 0;

    private long m_NextTriggerTickCount = 0;
    private int mapInitAttemptCount = 0;
    private boolean mapInitialized = false;
    private Set<ClanDimInfo> claimUpdates = new HashSet<>();


    @SubscribeEvent
    public void onServerTickEvent(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // Only process these server tickCounter events once a second, there is no need to do this on every tick.
            if (tickCounter % 20 == 0) {
                if (tickCounter >= m_NextTriggerTickCount) {
                    if (mapInitialized) {
                        // Update the claim display in dynmap for the list of teams.
                        if (!claimUpdates.isEmpty()) {
                            for (ClanDimInfo teamDim : claimUpdates)
                                updateClanClaims(teamDim);

                            claimUpdates.clear();
                        }
                    } else {
                        // We can't determine when FTB Claims information will be available so we have to check every so often, for
                        // the most part after the first tickCounter update FTB should be ready to go but we retry a few times before we
                        // consider ourselves initialized.

                        mapInitialized = initializeMap();

                        mapInitAttemptCount++;

                        // After a few attempts to initialize, just consider the system initialized. When no claims exist
                        // we will hit this case.
                        if (mapInitAttemptCount > 10)
                            mapInitialized = true;
                    }

                    m_NextTriggerTickCount = tickCounter + 40;
                }

            }
        }
    }

    /**
     * Method to queue up clan claim event updates to be processed at a later time. Multiple updates for the same
     * clan are combined in to a single update.
     *
     * @param clanDimInfo The clan and dimension the claim update is for.
     */
    @Override
    public void queueClaimEventReceived(ClanDimInfo clanDimInfo) {
        Clans.LOGGER.debug("Claim update notification received for clan [{}] in Dimension [{}], total queued events [{}]", clanDimInfo.getClanIdString(), clanDimInfo.getDim(), claimUpdates.size());

        claimUpdates.add(clanDimInfo);
    }

    /**
     * Updates all the claims in Dynamp for the specified clan in the specified dimension.
     * @param clanDimInfo The clan and dimension to update claims for.
     */
    private void updateClanClaims(ClanDimInfo clanDimInfo) {
        long startTimeNS;
        long totalChunks;
        long totalGroups;

        startTimeNS = System.nanoTime();
        Clans.LOGGER.trace("Claim update started for clan [{}] in Dimension [{}]", clanDimInfo.getClanIdString(), clanDimInfo.getDim());

        Set<ChunkPosition> teamClaimsList = ClanChunkCache.getChunks(UUID.fromString(clanDimInfo.getClanIdString()));
        totalChunks = teamClaimsList.size();

        // Build a list of groups of claim chunks where the claims are touching each other.
        List<GroupedChunks> groupList = new ArrayList<>();
        if (!teamClaimsList.isEmpty()) {
            for (ChunkPosition pos: teamClaimsList) {
                GroupedChunks group = new GroupedChunks();
                groupList.add(group);

                group.processChunk(pos, teamClaimsList);
            }
        }
        totalGroups = groupList.size();

        // Draw all the team claim markers for the specified dimension.
        clearAllTeamMarkers(clanDimInfo);
        int nIndex = 0;
        for (GroupedChunks group : groupList) {
            List<PositionPoint> perimeterPoints = group.traceShapePerimeter();

            createAreaMarker(clanDimInfo, nIndex++, perimeterPoints);
        }

        // Make sure we clean up all the object cross references so they can be garbage collected.
        for (GroupedChunks group : groupList)
            group.cleanup();

        long deltaNs = System.nanoTime() - startTimeNS;
        Clans.LOGGER.trace(" --> {} Claim chunks processed.", totalChunks);
        Clans.LOGGER.trace(" --> {} Claim groups detected.", totalGroups);
        Clans.LOGGER.trace(" --> Complete claim update in [{}ns]", deltaNs);

    }

    private boolean initializeMap() {
        Set<ClanDimInfo> teamDimList = Sets.newHashSet();

        for(NewClan clan: ClanChunkCache.clansWithClaims()) {
            List<Integer> addedDims = Lists.newArrayList();
            for(ChunkPosition chunk: ClanChunkCache.getChunks(clan.getClanId()))
                if(!addedDims.contains(chunk.dim)) {
                    teamDimList.add(new ClanDimInfo(clan.getClanId().toString(), chunk.dim, clan.getClanName(), clan.getDescription()));
                    addedDims.add(chunk.dim);
                }
        }

        for (ClanDimInfo teamDim : teamDimList)
            queueClaimEventReceived(teamDim);

        return teamDimList.size() > 0;
    }


    private MarkerAPI dynmapMarkerApi = null;
    private MarkerSet dynmapMarkerSet = null;
    private Map<Integer, String> dimensionNames = new HashMap<>();

    private static final Pattern FORMATTING_COLOR_CODES_PATTERN = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");

    private static final String MARKER_SET_ID = "clans.claims.markerset";
    private static final String MARKER_SET_LABEL = "Clans";

    /**
     * This is a call back class which Dynmap will call when it is ready to accept API requests. This is
     * also where we get the API object reference from.
     */
    private class DynmapAPIListener extends DynmapCommonAPIListener {
        @Override
        public void apiEnabled(DynmapCommonAPI api) {
            if (api != null) {
                dynmapMarkerApi = api.getMarkerAPI();

                createDynmapClaimMarkerLayer();
            }
        }
    }

    /**
     * This creates a marker layer in Dynmap for the claims to be displayed on.
     */
    private void createDynmapClaimMarkerLayer() {
        // Create / update a Dynmap Layer for claims
        dynmapMarkerSet = dynmapMarkerApi.getMarkerSet(MARKER_SET_ID);

        if(dynmapMarkerSet == null)
            dynmapMarkerSet = dynmapMarkerApi.createMarkerSet(MARKER_SET_ID, MARKER_SET_LABEL, null, false);
        else
            dynmapMarkerSet.setMarkerSetLabel(MARKER_SET_LABEL);
    }

    /**
     * This creates a single claim marker in Dynmap.
     * @param clanDimInfo Defines the clan and dimension this claim marker is for
     * @param groupIndex Defines the index number for how many claims this team has
     * @param perimeterPoints A list of X Z points representing the perimeter of the claim to draw.
     */
    public void createAreaMarker(ClanDimInfo clanDimInfo, int groupIndex, List<PositionPoint> perimeterPoints) {
        if (dynmapMarkerSet != null) {
            String worldName = getWorldName(clanDimInfo.getDim());
            String markerID = worldName + "_" + clanDimInfo.getClanIdString() + "_" + groupIndex;

            double[] xList = new double[perimeterPoints.size()];
            double[] zList = new double[perimeterPoints.size()];

            for (int index = 0; index < perimeterPoints.size(); index++) {
                xList[index] = perimeterPoints.get(index).getX();
                zList[index] = perimeterPoints.get(index).getY();
            }

            // Build the data going in to the Dynmap tooltip
            StringBuilder stToolTip = new StringBuilder("<div class=\"infowindow\">");

            stToolTip.append("<div style=\"text-align: center;\"><span style=\"font-weight:bold;\">").append(clanDimInfo.getClanName()).append("</span></div>");

            if (!clanDimInfo.getClanDescription().isEmpty()) {
                stToolTip.append("<div style=\"text-align: center;\"><span>").append(clanDimInfo.getClanDescription()).append("</span></div>");
            }

            Set<UUID> teamMembers = Objects.requireNonNull(ClanCache.getClanById(UUID.fromString(clanDimInfo.getClanIdString()))).getMembers().keySet();

            if (teamMembers.size() > 0) {
                stToolTip.append("<br><div style=\"text-align: center;\"><span style=\"font-weight:bold;\"><i>Team Members</i></span></div>");

                for (UUID member : teamMembers)
                    stToolTip.append("<div style=\"text-align: center;\"><span>").append(stripColorCodes(Objects.requireNonNull(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(member)).getName())).append("</span></div>");
            }

            stToolTip.append("</div>");

            // Create the area marker for the claim
            AreaMarker marker = dynmapMarkerSet.createAreaMarker(markerID, stToolTip.toString(), true, worldName, xList, zList, false);

            // Configure the marker style
            if (marker != null) {
                int nStrokeWeight = Clans.cfg.dynmapBorderWeight;
                double dStrokeOpacity = Clans.cfg.dynmapBorderOpacity;
                double dFillOpacity = Clans.cfg.dynmapFillOpacity;
                int nFillColor = clanDimInfo.getTeamColor();

                marker.setLineStyle(nStrokeWeight, dStrokeOpacity, nFillColor);
                marker.setFillStyle(dFillOpacity, nFillColor);
            } else
                Clans.LOGGER.error("Failed to create Dynmap area marker for claim.");
        } else
            Clans.LOGGER.error("Failed to create Dynmap area marker for claim, Dynmap Marker Set is not available.");
    }

    /**
     * Find all the markers for the specified team and clear them.
     * @param clanDimInfo Name of team and dimension you want to clear the markers for.
     */

    public void clearAllTeamMarkers(ClanDimInfo clanDimInfo) {
        if (dynmapMarkerSet != null) {
            String worldName = getWorldName(clanDimInfo.getDim());

            int nMarkerID = 0;
            AreaMarker areaMarker;
            do {
                String markerID = worldName + "_" + clanDimInfo.getClanIdString() + "_" + nMarkerID;
                areaMarker = dynmapMarkerSet.findAreaMarker(markerID);

                if (areaMarker != null && areaMarker.getWorld().equals(worldName))
                    areaMarker.deleteMarker();

                nMarkerID++;
            } while (areaMarker != null);
        }
    }

    /**
     * Build a list of dimension names which are compatible with how Dynmap makes its names.
     *
     * Note: This method needs to be called prior to any worlds being unloaded.
     */

    public void buildDynmapWorldNames() {
        WorldServer[] worldsList = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

        // This code below follows Dynmap's naming which is required to get mapping between dimensions and worlds
        // to work. As dynmap API takes world strings not dimension numbers.
        for (WorldServer world : worldsList)
            dimensionNames.put(world.provider.getDimension(),  world.getWorldInfo().getWorldName());

        Clans.LOGGER.debug("Building Dynmap compatible world name list");

        for (Map.Entry<Integer, String> entry : dimensionNames.entrySet())
            Clans.LOGGER.debug("  --> Dimension [{}] = {}", entry.getKey(), entry.getValue());
    }

    /**
     * Helper method to return the name of the world based on the dimension ID.
     *
     * @param dim The dimension ID you want the name for
     * @return Returns the string name of the dimension
     */
    private String getWorldName(int dim) {
        String worldName = "";

        if (dimensionNames.containsKey(dim))
            worldName = dimensionNames.get(dim);

        return  worldName;
    }

    /**
     * @param text Text with color codes
     * @return Removes color codes from text strings and returns the raw text
     */
    public static String stripColorCodes(String text) {
        return text.isEmpty() ? text :FORMATTING_COLOR_CODES_PATTERN.matcher(text).replaceAll("");
    }
}
