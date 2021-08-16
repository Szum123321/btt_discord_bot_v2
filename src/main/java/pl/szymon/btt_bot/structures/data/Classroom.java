package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class Classroom {
	public final static Classroom EMPTY_CLASSROOM = new Classroom(Integer.MIN_VALUE, "NONE!");
	int id;

	@SerializedName("short")
	String name;
}
