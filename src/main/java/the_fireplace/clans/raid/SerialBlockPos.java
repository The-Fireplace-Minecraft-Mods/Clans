package the_fireplace.clans.raid;

import java.io.Serializable;

public class SerialBlockPos implements Serializable {
	private static final long serialVersionUID = 0xEA7F00D;

	private int x, y, z;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public SerialBlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
