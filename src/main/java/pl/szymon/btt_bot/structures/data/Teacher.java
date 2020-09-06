package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class Teacher {
	TeacherId id;

	@Nullable
	TeacherName fullName;

	public int getId() {
		return id.getId();
	}

	public String getShortName() {
		return id.getShortName();
	}

	public Optional<TeacherName> getFullName() {
		return Optional.ofNullable(fullName);
	}

	public String print() {
		if(fullName != null) {
			return fullName.getFirstName() + " " + fullName.getLastName();
		}
		return id.shortName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Teacher(")
				.append("id=")
				.append(getId())
				.append(", short=")
				.append(getShortName());

		if(getFullName().isPresent()) {
			builder.append(", firstName=")
					.append(getFullName().get().getFirstName())
					.append(", lastName=")
					.append(getFullName().get().getLastName());
		} else {
			builder.append(", fullName=null");
		}

		builder.append(")");

		return builder.toString();
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
