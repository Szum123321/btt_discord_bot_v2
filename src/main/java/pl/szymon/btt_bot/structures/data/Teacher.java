package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class Teacher {
	TeacherId id;

	TeacherName fullName;

	public int getId() {
		return id.getId();
	}

	public String getShortName() {
		return id.getShortName();
	}

	public TeacherName getFullName() {
		return fullName;
	}

	public String print() {
		return fullName.getFirstName() + " " + fullName.getLastName();
	}

	@Override
	public String toString() {
		return "Teacher(" +
				"id=" +
				getId() +
				", short=" +
				getShortName() +
				", firstName=" +
				getFullName().getFirstName() +
				", lastName=" +
				getFullName().getLastName() +
				")";
	}

	@Value
	public static class TeacherName {
		@SerializedName("first")
		String firstName;

		@SerializedName("last")
		String lastName;
	}

	@Value
	public static class TeacherId {
		int id;

		@SerializedName("short")
		String shortName;
	}
}
