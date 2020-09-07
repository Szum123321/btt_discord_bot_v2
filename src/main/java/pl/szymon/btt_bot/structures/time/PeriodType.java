package pl.szymon.btt_bot.structures.time;

public enum PeriodType {
    LESSON(0, "Lesson"),
    PAUSE(1, "Pause");

    final int id;
    final String name;

    PeriodType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isLesson() {
        return this == LESSON;
    }

    public boolean isPause() {
        return this == PAUSE;
    }

    public String toString() {
        return name;
    }
}
