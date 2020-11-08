package the_fireplace.clans.io;

import java.io.File;
import java.util.UUID;

public final class FileNames {
    public static String jsonFileNameFromUUID(UUID uuid) {
        return uuid.toString()+".json";
    }

    public static File[] getUUIDJsonFolderContents(File folder) {
        return folder.listFiles((file, s) -> s.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b\\.json"));
    }
}
