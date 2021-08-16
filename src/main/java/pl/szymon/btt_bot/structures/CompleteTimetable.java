package pl.szymon.btt_bot.structures;

import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.structures.data.*;
import pl.szymon.btt_bot.structures.time.LessonTime;
import pl.szymon.btt_bot.structures.time.PointRangeTimePeriodTree;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Log4j2
@Value
public class CompleteTimetable {
    LocalDate dateSince;
    LocalDate dateTo;

    int klasaId;

    ImmutableMap<Integer, Teacher> teachers;
    ImmutableMap<Integer, Subject> subjects;
    ImmutableMap<Integer, Classroom> classrooms;
    ImmutableMap<Integer, Klasa> klasses;

    PointRangeTimePeriodTree<LocalTime, LessonTime> lessonTimeTree;
    NoThrowArrayList<LessonTime> lessonTimes;
    NoThrowArrayList<LessonTime> pauseTimes;

    @Getter(AccessLevel.PRIVATE)
    List<Lesson>[][] lessonList; //5, 15
    //Map<Integer, List<Lesson>>[] lessonList; //5

    @NonFinal
    Map<Integer, LessonGroup>[] lessonGroups;

    public void bake() {
        //noinspection unchecked
        lessonGroups = new HashMap[5];

        for(int i = 0; i < 5; i++) {
            Map<Integer, LessonGroup> mapBuilder = new HashMap<>();

            for(int j = 0; j < 15; j++) {
                if(!lessonList[i][j].isEmpty()) {
                    LessonGroup.Builder groupBuilder = LessonGroup.builder();
                    groupBuilder.setLessonTime(lessonTimes.get(j));
                    lessonList[i][j].forEach(lesson -> {
                        lesson.setDefinition(this);
                        groupBuilder.addLesson(lesson);
                    });
                    mapBuilder.put(j, groupBuilder.build());
                }
            }
/*
            lessonList[i].forEach((k, v) -> {
                LessonGroup.Builder groupBuilder = LessonGroup.builder();
                groupBuilder.setLessonTime(lessonTimes.get(k));
                v.forEach(lesson -> {
                    lesson.setDefinition(this);
                    groupBuilder.addLesson(lesson);
                });

                mapBuilder.put(k, groupBuilder.build());
            });
*/
            lessonGroups[i] = mapBuilder;
        }
    }

    public LessonGroup getLessonGroup(int dayOfWeek, int period) {
        return lessonGroups[dayOfWeek].get(period);
    }

    public List<Lesson> getLessons() {
        ArrayList<Lesson> collector = new ArrayList<>();

        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 15; j++) {
                collector.addAll(lessonList[i][j]);
            }
            //lessonList[i].values().forEach(collector::addAll);
        }

        return collector;
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Builder {
        final Map<Integer, Teacher> teachers;
        final Map<Integer, Subject> subjects;
        final Map<Integer, Classroom> classrooms;
        final Map<Integer, Klasa> classes;

        @Setter
        ImmutableMap<String, Teacher.TeacherName> teacherNames;

        @Setter
        LocalDate dateSince;

        @Setter
        LocalDate dateTo;

        @Setter
        int klasaId;

        @Setter
        PointRangeTimePeriodTree<LocalTime, LessonTime> lessonTimeTree;

        @Setter
        NoThrowArrayList<LessonTime> lessonTimes;

        @Setter
        NoThrowArrayList<LessonTime> pauseTimes;

        @Setter
        List<Lesson>[][] lessons; //15x5

        public Builder() {
            teachers = new HashMap<>();
            subjects = new HashMap<>();
            classrooms = new HashMap<>();
            classes = new HashMap<>();
        }

        public void addTeacher(Teacher value) {
            teachers.put(value.getId(), value);
        }

        public void addSubject(Subject value) {
            subjects.put(value.getId(), value);
        }

        public void addClassroom(Classroom value) {
            classrooms.put(value.getId(), value);
        }

        public void addClass(Klasa value) {
            classes.put(value.getId(), value);
        }

        public CompleteTimetable build() {
            return new CompleteTimetable(
                    dateSince,
                    dateTo,
                    klasaId,
                    ImmutableMap.copyOf(teachers),
                    ImmutableMap.copyOf(subjects),
                    ImmutableMap.copyOf(classrooms),
                    ImmutableMap.copyOf(classes),
                    lessonTimeTree,
                    lessonTimes,
                    pauseTimes,
                    lessons,
                    null
            );
        }
    }
}
