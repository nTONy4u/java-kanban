package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.Epic;
import fun.ntony4u.kanban.model.Subtask;
import fun.ntony4u.kanban.model.Task;

import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 0;

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtaskOfEpic();
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask addSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
        return task;
    }

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

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        ArrayList<Subtask> epicSubtasks = epics.get(id).getSubtaskOfEpic();
        epics.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        int epicId = subtask.getEpicId();

        subtasks.remove(id);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskOfEpic = epic.getSubtaskOfEpic();
        subtaskOfEpic.remove(subtask);
        epic.setSubtaskOfEpic(subtaskOfEpic);
        updateEpicStatus(epic);
    }

    private int getNextId() {
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
}