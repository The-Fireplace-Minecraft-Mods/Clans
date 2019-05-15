package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class ClanDatabase implements Serializable {
	private static final long serialVersionUID = 0x1254367;

	private static ClanDatabase instance = null;
	private static final String dataFileName = "clans.dat";
	private static File saveDir = Clans.getDataDir();

	public static ClanDatabase getInstance() {
		if(instance == null)
			readFromFile();
		return instance;
	}

	private HashMap<UUID, Clan> clans;
	private Clan opclan;

	private ClanDatabase(){
		clans = Maps.newHashMap();
	}

	private static void readFromFile() {
		if (saveDir == null)
			saveDir = Clans.getDataDir();
		File f = new File(saveDir, dataFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (ClanDatabase) stream.readObject();
				stream.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				instance = new ClanDatabase();
				f.delete();
			}
		}
		if (instance == null)
			instance = new ClanDatabase();
		for(Clan clan: instance.clans.values())
			if(clan.getClanId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) || !clan.isOpclan())
				NewClanDatabase.addClan(clan.getClanId(), new NewClan(clan.toJsonObject()));
	}

	public Clan getOpclan() {
		return opclan;
	}
}
