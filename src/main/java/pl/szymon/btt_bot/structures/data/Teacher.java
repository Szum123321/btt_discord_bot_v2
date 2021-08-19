package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class Teacher {
	int id;

	@SerializedName("short")
	String shortName;

	@SerializedName("firstname")
	String firstName;

	@SerializedName("lastname")
	String lastName;

	public String print() {
		return firstName + " " + lastName;
	}
}
