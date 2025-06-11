package fun.ntony4u.kanban.model;

import fun.ntony4u.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status,
                   int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "fun.ntony4u.kanban.model.Subtask[" +
                "id=" + getId() +
                ", epicId=" + epicId +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") + "min" +
                ", startTime=" + getStartTime() +
                "]";
    }
}
