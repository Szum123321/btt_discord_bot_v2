package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class Classroom {
	int id;

	@SerializedName("short")
	String name;
}
