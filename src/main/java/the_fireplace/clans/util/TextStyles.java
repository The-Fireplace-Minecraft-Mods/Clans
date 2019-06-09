package the_fireplace.clans.util;

import com.google.common.collect.Maps;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.commands.details.CommandSetColor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TextStyles {
    public static final Style RED = new Style().setColor(TextFormatting.RED);
    public static final Style YELLOW = new Style().setColor(TextFormatting.YELLOW);
    public static final Style GREEN = new Style().setColor(TextFormatting.GREEN);
    public static final Style DARK_GREEN = new Style().setColor(TextFormatting.DARK_GREEN);
    public static final Style RESET = new Style().setColor(TextFormatting.RESET);
    public static final Style ONLINE_LEADER = new Style().setBold(Boolean.TRUE).setItalic(Boolean.TRUE).setColor(TextFormatting.GREEN);
    public static final Style ONLINE_ADMIN = new Style().setBold(Boolean.TRUE).setColor(TextFormatting.GREEN);
    public static final Style OFFLINE_LEADER = new Style().setBold(Boolean.TRUE).setItalic(Boolean.TRUE).setColor(TextFormatting.YELLOW);
    public static final Style OFFLINE_ADMIN = new Style().setBold(Boolean.TRUE).setColor(TextFormatting.YELLOW);

    private static final HashMap<Color, TextFormatting> colorMap = Maps.newHashMap();
    public static final HashMap<String, Integer> colorStrings = Maps.newHashMap();
    static {
        colorMap.put(new Color(0x000000), TextFormatting.BLACK);
        colorMap.put(new Color(0x0000AA), TextFormatting.DARK_BLUE);
        colorMap.put(new Color(0x00AA00), TextFormatting.DARK_GREEN);
        colorMap.put(new Color(0x00AAAA), TextFormatting.DARK_AQUA);
        colorMap.put(new Color(0xAA0000), TextFormatting.DARK_RED);
        colorMap.put(new Color(0xAA00AA), TextFormatting.DARK_PURPLE);
        colorMap.put(new Color(0xFFAA00), TextFormatting.GOLD);
        colorMap.put(new Color(0xAAAAAA), TextFormatting.GRAY);
        colorMap.put(new Color(0x555555), TextFormatting.DARK_GRAY);
        colorMap.put(new Color(0x5555FF), TextFormatting.BLUE);
        colorMap.put(new Color(0x55FF55), TextFormatting.GREEN);
        colorMap.put(new Color(0x55FFFF), TextFormatting.AQUA);
        colorMap.put(new Color(0xFF5555), TextFormatting.RED);
        colorMap.put(new Color(0xFF55FF), TextFormatting.LIGHT_PURPLE);
        colorMap.put(new Color(0xFFFF55), TextFormatting.YELLOW);
        colorMap.put(new Color(0xFFFFFF), TextFormatting.WHITE);

        for(Map.Entry<Color, TextFormatting> entry : colorMap.entrySet())
            colorStrings.put(entry.getValue().toString().toLowerCase(), entry.getKey().getRGB());
    }

    public static TextFormatting getNearestTextColor(int exactColor) {
        Color ec = new Color(exactColor);
        Color closest = null;
        double distance = Double.MAX_VALUE;
        for(Color c2: colorMap.keySet()) {
            double d = colorDistance(ec, c2);
            if(d < distance) {
                closest = c2;
                distance = d;
            }
        }
        return colorMap.get(closest);
    }

    private static double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return (((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8);
    }
}
