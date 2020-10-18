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

	public static boolean isAboveMemberRank(EnumRank rank) {
		return rank == LEADER || rank == ADMIN;
	}

	public static EnumRank getNextLowerRankInClan(EnumRank rank) {
		switch(rank) {
			case LEADER:
				return ADMIN;
			case ADMIN:
				return MEMBER;
			default:
				throw new IllegalArgumentException("There is no rank lower than "+rank.name()+"!");
		}
	}
}
