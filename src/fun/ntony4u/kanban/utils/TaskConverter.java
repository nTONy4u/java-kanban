package fun.ntony4u.kanban.utils;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskConverter {
    private static final String DELIMITER = ",";
    private static final int EXPECTED_PARTS_COUNT = 8; /* id,type,name,status,description,
                                                          epic, duration,startTime*/

    public static String toString(Task task) {
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";

        if (task instanceof Epic) {
            return String.join(DELIMITER,
                    String.valueOf(task.getId()),
                    TaskType.EPIC.name(),
                    task.getName(),
                    task.getStatus() != null ? task.getStatus().name() : Status.NEW.name(),
                    task.getDescription(),
                    "",
                    durationStr,
                    startTimeStr);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.join(DELIMITER,
                    String.valueOf(subtask.getId()),
                    TaskType.SUBTASK.name(),
                    subtask.getName(),
                    subtask.getStatus() != null ? subtask.getStatus().name() : Status.NEW.name(),
                    subtask.getDescription(),
                    String.valueOf(subtask.getEpicId()),
                    durationStr,
                    startTimeStr);
        } else {
            return String.join(DELIMITER,
                    String.valueOf(task.getId()),
                    TaskType.TASK.name(),
                    task.getName(),
                    task.getStatus() != null ? task.getStatus().name() : Status.NEW.name(),
                    task.getDescription(),
                    "",
                    durationStr,
                    startTimeStr);
        }
    }

    public static Task fromString(String value) {
        String[] parts = value.split(DELIMITER, -1); // -1 сохраняет пустые значения

        if (parts.length < EXPECTED_PARTS_COUNT) {
            throw new IllegalArgumentException("Некорректный формат строки задачи. Ожидается "
                    + EXPECTED_PARTS_COUNT + " полей, получено " + parts.length);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            TaskType type = TaskType.valueOf(parts[1].trim());
            String name = parts[2].trim();
            Status status = parts[3].isBlank() ? Status.NEW : Status.valueOf(parts[3].trim());
            String description = parts[4].trim();

            Duration duration = parts[6].isBlank() ? null : Duration.ofMinutes(Long.parseLong(parts[6].trim()));
            LocalDateTime startTime = parts[7].isBlank() ? null : LocalDateTime.parse(parts[7].trim());

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, duration, startTime);
                case EPIC:
                    return new Epic(id, name, description, status);
                case SUBTASK:
                    if (parts.length > 5) {
                        int epicId = parts[5].isBlank() ? 0 : Integer.parseInt(parts[5].trim());
                        return new Subtask(id, name, description, status, epicId, duration, startTime);
                    } else {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId");
                    }
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга строки задачи: " + value, e);
        }
    }
}