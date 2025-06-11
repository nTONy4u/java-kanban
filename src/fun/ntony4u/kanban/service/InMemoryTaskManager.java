package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.Comparator;


public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected int nextId = 0;
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
    ));

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtaskOfEpic();
    }

    @Override
    public void deleteTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        });
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return Optional.ofNullable(epic);
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return Optional.ofNullable(subtask);
    }

    @Override
    public Task addTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (hasTimeOverlap(subtask)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
        }

        if (epic == null) {
            System.out.println("Эпик с id " + subtask.getEpicId() + " не найден");
            return subtask;
        }

        if (subtask.getEpicId() == subtask.getId()) {
            System.out.println("объект Epic нельзя добавить в самого себя в виде подзадачи");
            return subtask;
        }

        subtask.setId(getNextId());
        epic.addSubtask(subtask);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(epic);

        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }

        if (hasTimeOverlap(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
        }

        Task oldTask = tasks.get(taskId);
        if (oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }

        tasks.replace(taskId, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();
        if (epicId == null || !epics.containsKey(epicId)) {
            return null;
        }

        Epic oldEpic = epics.get(epicId);
        ArrayList<Subtask> oldSubtaskOfEpic = oldEpic.getSubtaskOfEpic();
        if (!oldSubtaskOfEpic.isEmpty()) {
            for (Subtask subtask : oldSubtaskOfEpic) {
                subtasks.remove(subtask.getId());
                historyManager.remove(subtask.getId());
            }
        }
        epics.replace(epicId, epic);

        ArrayList<Subtask> newSubtaskOfEpic = epic.getSubtaskOfEpic();
        if (!newSubtaskOfEpic.isEmpty()) {
            for (Subtask subtask : newSubtaskOfEpic) {
                subtasks.put(subtask.getId(), subtask);
            }
        }
        updateEpicStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskId = subtask.getId();
        if (hasTimeOverlap(subtask)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
        }

        if (subtaskId == null || !subtasks.containsKey(subtaskId)) {
            return null;
        }

        Subtask oldSubtask = subtasks.get(subtaskId);
        if (oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }

        int epicId = subtask.getEpicId();
        subtasks.replace(subtaskId, subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskOfEpic = epic.getSubtaskOfEpic();
        subtaskOfEpic.remove(oldSubtask);
        subtaskOfEpic.add(subtask);
        epic.setSubtaskOfEpic(subtaskOfEpic);
        updateEpicStatus(epic);
        return subtask;
    }

    @Override
    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskOfEpic()) {
                subtasks.remove(subtask.getId());
                historyManager.remove(subtask.getId());
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskOfEpic(id);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public int getNextId() {
        return ++nextId;
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Subtask> epicSubtasks = epic.getSubtaskOfEpic();
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean isAllDone = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == Status.DONE);
        boolean isAllNew = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == Status.NEW);

        if (isAllDone) {
            epic.setStatus(Status.DONE);
        } else if (isAllNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean hasTimeOverlap(Task task) {
        return checkTimeOverlap(task);
    }

    public boolean checkTimeOverlap(Task task) {
        // Проверка на нулевую длительность или отсутствие времени
        if (task.getStartTime() == null || task.getEndTime() == null
                || task.getDuration() == null || task.getDuration().isZero()) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(t -> !t.equals(task))
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .anyMatch(t -> t.isOverlapping(task));
    }
}