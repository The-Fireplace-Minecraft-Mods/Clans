package the_fireplace.clans.model;

public enum EnumRank {
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
				return other.equals(MEMBER);
			case ADMIN:
				return other.equals(ADMIN) || other.equals(MEMBER);
			case LEADER:
				return !other.equals(NOCLAN);
		}
	}
}
