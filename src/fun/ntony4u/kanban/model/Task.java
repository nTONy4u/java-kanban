package fun.ntony4u.kanban.model;

import fun.ntony4u.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private Status status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(int id, String name, String description, Status status) {
        this(id, name, description, status, null, null);
    }

    public Task(String name, String description, Status status) {
        this(0, name, description, status, null, null);
    }

    public Task(String name, String description) {
        this(0, name, description, Status.NEW, null, null);
    }

    public Task(int id, String name, String description, Status status,
                Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public boolean isOverlapping(Task other) {
        if (this.getStartTime() == null || this.getEndTime() == null
                || other.getStartTime() == null || other.getEndTime() == null) {
            return false;
        }

        return !this.getEndTime().isBefore(other.getStartTime())
                && !this.getStartTime().isAfter(other.getEndTime());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "fun.ntony4u.kanban.model.Task[" +
                "id=" + id +
                ", name=" + name + '\'' +
                ", description=" + description + '\'' +
                ", status=" + status + '\'' +
                ", duration=" + (duration != null ? duration.toMinutes() : "null") + "min" +
                ", startTime=" + startTime +
                "]";
    }
}