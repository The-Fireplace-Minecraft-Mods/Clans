package the_fireplace.clans.client.compat.journeymap;

import journeymap.client.api.display.IOverlayListener;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.PolygonHelper;
import journeymap.client.api.util.UIState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.client.ClansClientModContainer;
import the_fireplace.clans.client.clan.metadata.ClientColorCache;
import the_fireplace.clans.legacy.model.ChunkPosition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.geom.Point2D;
import java.util.Random;

public class ClaimedChunkOverlayFactory {
    public static PolygonOverlay create(ChunkPosition chunkPosition, String clanName) {
        String displayId = "claim_" + chunkPosition.toString();
        int clanColor = ClientColorCache.getColor(clanName);

        // Style the polygon
        ShapeProperties shapeProps = new ShapeProperties()
            .setStrokeWidth(2)
            .setStrokeColor(clanColor).setStrokeOpacity(.7f)
            .setFillColor(clanColor).setFillOpacity(.4f);

        // Style the text
        TextProperties textProps = new TextProperties()
            .setBackgroundColor(0x333333)
            .setBackgroundOpacity(.5f)
            .setColor(0xFFFFFF)
            .setOpacity(1f)
            .setMinZoom(2)
            .setFontShadow(true);

        // Define the shape
        MapPolygon polygon = PolygonHelper.createChunkPolygon(chunkPosition.getPosX(), 70, chunkPosition.getPosZ());

        // Create the overlay
        PolygonOverlay claimOverlay = new PolygonOverlay(ClansClientModContainer.MODID, displayId, chunkPosition.getDim(), shapeProps, polygon);

        // Set the text
        claimOverlay.setOverlayGroupName(clanName)
            .setLabel(clanName)
            .setTextProperties(textProps);

        // Add a listener for mouse events
        IOverlayListener overlayListener = new SlimeChunkListener(claimOverlay);
        claimOverlay.setOverlayListener(overlayListener);

        return claimOverlay;
    }

    @ParametersAreNonnullByDefault
    static class SlimeChunkListener implements IOverlayListener {
        final PolygonOverlay overlay;
        final ShapeProperties sp;
        final int fillColor;
        final int strokeColor;
        final float strokeOpacity;

        SlimeChunkListener(final PolygonOverlay overlay) {
            this.overlay = overlay;
            sp = overlay.getShapeProperties();
            fillColor = sp.getFillColor();
            strokeColor = sp.getStrokeColor();
            strokeOpacity = sp.getStrokeOpacity();
        }

        @Override
        public void onActivate(UIState uiState) {
            // Reset
            resetShapeProperties();
        }

        @Override
        public void onDeactivate(UIState uiState) {
            // Reset
            resetShapeProperties();
        }

        @Override
        public void onMouseMove(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition) {
            // Random stroke and make it opaque just to prove this works
            sp.setStrokeColor(new Random().nextInt(0xffffff));
            sp.setStrokeOpacity(1f);

            // Update title
            String title = "%s blocks away";
            BlockPos playerLoc = Minecraft.getMinecraft().player.getPosition();
            int distance = (int) Math.sqrt(playerLoc.distanceSq(blockPosition.getX(), playerLoc.getY(), blockPosition.getZ()));
            overlay.setTitle(String.format(title, distance));
        }

        @Override
        public void onMouseOut(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition) {
            // Reset
            resetShapeProperties();
            overlay.setTitle(null);
        }

        @Override
        public boolean onMouseClick(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition, int button, boolean doubleClick) {
            // Random color on click just to prove the event works.
            sp.setFillColor(new Random().nextInt(0xffffff));

            // Returning false will stop the click event from being used by other overlays,
            // including JM's invisible overlay for creating/selecting waypoints
            return false;
        }

        /**
         * Reset properties back to original
         */
        private void resetShapeProperties() {
            sp.setFillColor(fillColor);
            sp.setStrokeColor(strokeColor);
            sp.setStrokeOpacity(strokeOpacity);
        }
    }
}
