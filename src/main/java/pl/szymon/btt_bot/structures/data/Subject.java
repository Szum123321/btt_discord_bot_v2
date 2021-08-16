package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class Subject {
	int id;

	@SerializedName("short")
	String shortName;

	@Nullable
	String name;

	public String getName() {
		return Optional.ofNullable(name).orElse(shortName);
	}
}
