package the_fireplace.clans.clan;

import java.io.Serializable;

public enum EnumRank implements Serializable {
	LEADER,
	ADMIN,
	MEMBER,
	NOCLAN,
	ANY;

	public boolean greaterOrEquals(EnumRank other) {
		if(other.equals(ANY))
			return true;
		switch(this) {
			case ANY:
			default:
				return true;
			case NOCLAN:
				return other.equals(NOCLAN);
			case MEMBER:
				return !other.equals(NOCLAN);
			case ADMIN:
				return other.equals(ADMIN) || other.equals(LEADER);
			case LEADER:
				return other.equals(LEADER);
		}
	}
}
