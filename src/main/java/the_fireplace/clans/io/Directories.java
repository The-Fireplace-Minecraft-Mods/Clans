package the_fireplace.clans.io;

import the_fireplace.clans.legacy.ClansModContainer;

import java.io.File;

public final class Directories
{
    private static final File SAVE_DIRECTORY = ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory();
    public static final File CLANS_DATA_ROOT = new File(SAVE_DIRECTORY, "clans");
    public static final File CLAN_DATA_LOCATION = new File(CLANS_DATA_ROOT, "clan");
    public static final File RAID_DATA_LOCATION = new File(CLANS_DATA_ROOT, "raid");
    public static final File CHUNK_DATA_LOCATION = new File(CLANS_DATA_ROOT, "chunk");
}
