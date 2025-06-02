package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 0;
    protected HistoryManager historyManager = Managers.getDefaultHistory();

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
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            for (Subtask subtask : epic.getSubtaskOfEpic()) {
                historyManager.remove(subtask.getId());
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
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
        updateEpicStatus(epic);

        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
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
        if (subtaskId == null || !subtasks.containsKey(subtaskId)) {
            return null;
        }
        Subtask oldSubtask = subtasks.get(subtaskId);
        int epicId = subtask.getEpicId();

        subtasks.replace(subtaskId, subtask);
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
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskOfEpic(id);
            }
            historyManager.remove(id);
        }
    }

    private void updateEpicStatus(int id) {
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

        boolean isAllDone = true;
        boolean isAllNew = true;

        for (Subtask subtask : epicSubtasks) {
            if (!(subtask.getStatus() == Status.NEW)) {
                isAllNew = false;
            }
            if (!(subtask.getStatus() == Status.DONE)) {
                isAllDone = false;
            }
        }

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
}