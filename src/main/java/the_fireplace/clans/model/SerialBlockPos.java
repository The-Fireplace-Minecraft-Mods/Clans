package the_fireplace.clans.model;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Objects;

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

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SerialBlockPos that = (SerialBlockPos) o;
		return x == that.x &&
				y == that.y &&
				z == that.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	public JsonObject toJsonObject() {
		JsonObject ret = new JsonObject();
		ret.addProperty("x", x);
		ret.addProperty("y", y);
		ret.addProperty("z", z);

		return ret;
	}

	public SerialBlockPos(JsonObject obj){
		this.x = obj.get("x").getAsInt();
		this.y = obj.get("y").getAsInt();
		this.z = obj.get("z").getAsInt();
	}
}
