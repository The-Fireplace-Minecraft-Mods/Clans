package the_fireplace.clans.clan;

import the_fireplace.clans.io.Directories;
import the_fireplace.clans.io.FileNames;
import the_fireplace.clans.io.ThreadedJsonSerializable;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;

import java.io.File;
import java.util.UUID;

public abstract class ClanData implements ThreadedJsonSerializable {
    protected final ThreadedSaveHandler<ClanData> saveHandler = ThreadedSaveHandler.create(this);
    protected final File saveFile;
    protected final UUID clan;

    protected ClanData(UUID clan, String saveFolder) {
        this.clan = clan;
        saveFile = new File(new File(Directories.CLAN_DATA_LOCATION, saveFolder), FileNames.jsonFileNameFromUUID(clan));
    }

    @Override
    public void blockingSave() {
        writeToJson(saveFile);
    }

    @Override
    public ThreadedSaveHandler<?> getSaveHandler() {
        return saveHandler;
    }

    public boolean loadSavedData() {
        if(hasSaveFile()) {
            load(saveFile);
            return true;
        }
        return false;
    }

    protected boolean hasSaveFile() {
        return saveFile.exists();
    }

    protected void delete() {
        //noinspection ResultOfMethodCallIgnored
        saveFile.delete();
        saveHandler.disposeReferences();
    }
}
