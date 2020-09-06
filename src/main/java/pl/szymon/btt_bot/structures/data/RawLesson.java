package pl.szymon.btt_bot.structures.data;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import lombok.experimental.NonFinal;

import java.time.LocalDate;
import java.time.LocalTime;

@Value
@NonFinal
public class RawLesson {
    LocalDate date;

    @SerializedName("uniperiod")
    int period;

    @SerializedName("starttime")
    LocalTime startTime;

    @SerializedName("endtime")
    LocalTime endTime;

    @SerializedName("subjectid")
    Integer subjectId;

    @SerializedName("classids")
    Integer[] klassIds;

    @SerializedName("teacherids")
    Integer[] teacherIds;

    @SerializedName("classroomids")
    Integer[] classroomIds;

    @SerializedName("groupnames")
    String[] groupNames;

    @Setter
    @NonFinal
    String info;

    public RawLesson(RawLesson other) {
        this.date = other.date;
        this.period = other.period;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.subjectId = other.subjectId;
        this.klassIds = other.klassIds;
        this.teacherIds = other.teacherIds;
        this.classroomIds = other.classroomIds;
        this.groupNames = other.groupNames;
        this.info = other.info;
    }

    public RawLesson(RawLesson other, LocalTime startTime, LocalTime endTime, int period) {
        this.date = other.date;
        this.period = period;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectId = other.subjectId;
        this.klassIds = other.klassIds;
        this.teacherIds = other.teacherIds;
        this.classroomIds = other.classroomIds;
        this.groupNames = other.groupNames;
        this.info = other.info;
    }

    public int getDayOfWeek() {
        return date.getDayOfWeek().ordinal();
    }
}
