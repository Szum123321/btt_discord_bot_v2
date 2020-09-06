package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import javax.annotation.Nullable;

@Value
public class Subject {
	int id;

	@SerializedName("short")
	String shortName;

	@Nullable
	String name;

	public String getName() {
		if(name != null)
			return name;

		return shortName;
	}
}
