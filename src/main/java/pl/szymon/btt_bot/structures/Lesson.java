package pl.szymon.btt_bot.structures;

import lombok.*;
import lombok.experimental.NonFinal;
import pl.szymon.btt_bot.structures.data.*;
import pl.szymon.btt_bot.structures.time.LessonTime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper=true)
public class Lesson extends RawLesson {
	@Setter
	@NonFinal
	@ToString.Exclude
	CompleteTimetable definition;

	LessonTime time;

	public Lesson(RawLesson rawLesson, LessonTime time) {
		super(rawLesson);

		this.time = time;
	}

	public boolean isBaked() {
		return definition != null;
	}

	public Subject getSubject() {
		return definition.getSubjects().get(getSubjectId());
	}

	public List<Klasa> getKlasses() {
		return Arrays.stream(getKlassIds())
				.map(id -> definition.getKlasses().get(id))
				.collect(Collectors.toList());
	}

	public List<Teacher> getTeachers() {
		return Arrays.stream(getTeacherIds())
				.map(id -> definition.getTeachers().get(id))
				.collect(Collectors.toList());
	}

	public List<Classroom> getClassrooms() {
		return Arrays.stream(getClassroomIds())
				.map(id -> definition.getClassrooms().get(id))
				.collect(Collectors.toList());
	}

	public Classroom getClassroom() {
		return definition.getClassrooms().get(getClassroomIds()[0]);
	}

	@Override
	public String toString() {
		if(definition != null) {
			StringBuilder builder = new StringBuilder();

			builder.append("Lesson(")
					.append("date=")
					.append(getDate())
					.append(", time=")
					.append(this.time)
					.append(", subject=")
					.append(getSubject())
					.append(", groups=")
					.append(Arrays.toString(getGroupNames()))
					.append(", klasses=")
					.append(Arrays.toString(getKlasses().toArray()))
					.append(", teachers=")
					.append(Arrays.toString(getTeachers().toArray()))
					.append(", classroom=")
					.append(getClassroom().toString());

			if(getInfo() != null)
				builder.append(", info=").append(getInfo());

			builder.append(")");

			return builder.toString();
		} else {
			return super.toString();
		}
	}
}
