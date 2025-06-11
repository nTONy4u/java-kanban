package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    List<Subtask> getEpicSubtasks(Epic epic);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    Optional<Task> getTaskById(int id);

    Optional<Epic> getEpicById(int id);

    Optional<Subtask> getSubtaskById(int id);

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    int getNextId();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    boolean hasTimeOverlap(Task task);
}