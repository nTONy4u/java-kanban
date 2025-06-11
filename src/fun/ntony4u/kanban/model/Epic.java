package fun.ntony4u.kanban.model;

import fun.ntony4u.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Subtask> subtaskOfEpic = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public void addSubtask(Subtask subtask) {
        subtaskOfEpic.add(subtask);
    }

    public void clearSubtasks() {
        subtaskOfEpic.clear();
    }

    public ArrayList<Subtask> getSubtaskOfEpic() {
        return subtaskOfEpic;
    }

    public void setSubtaskOfEpic(ArrayList<Subtask> subtaskOfEpic) {
        this.subtaskOfEpic = subtaskOfEpic;
    }

    public void removeSubtaskOfEpic(Integer id) {
        subtaskOfEpic.removeIf(subtask -> subtask.getId() == id);
    }

    @Override
    public Duration getDuration() {
        if (subtaskOfEpic.isEmpty()) {
            return null;
        }
        return Duration.ofMinutes(subtaskOfEpic.stream()
                .mapToLong(subtask -> subtask.getDuration() != null ? subtask.getDuration().toMinutes() : 0)
                .sum());
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtaskOfEpic.isEmpty()) {
            return null;
        }
        return subtaskOfEpic.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtaskOfEpic.isEmpty()) {
            return null;
        }
        return subtaskOfEpic.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        return "fun.ntony4u.kanban.model.Epic[" +
                "id=" + getId() +
                ", name= " + getName() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", status = " + getStatus() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") + "min" +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", subtaskOfEpic.size = " + subtaskOfEpic.size() +
                "]";
    }
}