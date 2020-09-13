package pl.szymon.btt_bot.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.Value;
import lombok.experimental.NonFinal;
import pl.szymon.btt_bot.structures.data.Subject;
import pl.szymon.btt_bot.structures.time.LessonTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class LessonGroup {
    ImmutableMap<Subject, ImmutableList<Lesson>> lessonMap;
    LessonTime lessonTime;

    public List<Lesson> getAllLessons() {
        return lessonMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<Lesson> getLessons(Subject subject) {
        return lessonMap.get(subject);
    }

    public List<Subject> getAllSubjects() {
        return lessonMap.keySet().asList();
    }

    public String print() {
        StringBuilder builder = new StringBuilder();

        lessonMap.forEach((k, v) -> {
            builder.append(k.getName()).append(":");

            if(v.size() > 1) {
                builder.append("\n");

                v.forEach(lesson -> {
                    builder.append("  ");

                    if (lesson.getGroupNames().length >= 1)
                        builder.append(lesson.getGroupNames()[0]).append(", ");

                    printSingleGroup(builder, lesson);
                });
            } else {
                Lesson lesson = v.get(0);

                builder.append(" ");

                printSingleGroup(builder, lesson);
            }
        });

        return builder.toString();
    }

    private static void printSingleGroup(StringBuilder builder, Lesson lesson) {
        builder.append("w: ").append(lesson.getClassroom().getName()).append(", ");

        builder.append("z: ").append(lesson.getTeachers().get(0).print());

        for (int i = 1; i < lesson.getTeachers().size(); i++)
            builder.append(", ").append(lesson.getTeachers().get(i).print());

        if(lesson.getInfo() != null)
            builder.append(", Uwaga: ").append(lesson.getInfo());

        builder.append("\n");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Value
    public static class Builder {
        Map<Subject, List<Lesson>> lessonMap;

        @NonFinal
        LessonTime lessonTime = null;

        public Builder() {
            lessonMap = new HashMap<>();
        }

        public Builder addLesson(Lesson newLesson) {
            if(lessonMap.containsKey(newLesson.getSubject())) {
                lessonMap.get(newLesson.getSubject()).add(newLesson);
            } else {
                lessonMap.put(newLesson.getSubject(), Lists.newArrayList(newLesson));
            }

            return this;
        }

        public void setLessonTime(LessonTime lessonTime) {
            this.lessonTime = lessonTime;
        }

        public LessonGroup build() {
            ImmutableMap.Builder<Subject, ImmutableList<Lesson>> mapBuilder = ImmutableMap.builder();

            lessonMap.forEach((k, v) -> mapBuilder.put(k, ImmutableList.copyOf(v)));

            return new LessonGroup(mapBuilder.build(), lessonTime);
        }
    }
}
