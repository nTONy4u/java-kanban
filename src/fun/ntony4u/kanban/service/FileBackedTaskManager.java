package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.utils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            Files.lines(file.toPath(), StandardCharsets.UTF_8)
                    .skip(1)  // Пропускаем заголовок
                    .takeWhile(line -> !line.isEmpty())
                    .map(TaskConverter::fromString)
                    .forEach(task -> {
                        if (task instanceof Epic) {
                            manager.epics.put(task.getId(), (Epic) task);
                        } else if (task instanceof Subtask) {
                            manager.subtasks.put(task.getId(), (Subtask) task);
                            Optional.ofNullable(manager.epics.get(((Subtask) task).getEpicId()))
                                    .ifPresent(epic -> epic.addSubtask((Subtask) task));
                            Optional.ofNullable(task.getStartTime())
                                    .ifPresent(time -> manager.prioritizedTasks.add(task));
                        } else {
                            manager.tasks.put(task.getId(), task);
                            Optional.ofNullable(task.getStartTime())
                                    .ifPresent(time -> manager.prioritizedTasks.add(task));
                        }
                        manager.nextId = Math.max(manager.nextId, task.getId() + 1);
                    });
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return manager;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic,duration,startTime");
            writer.newLine();

            getTasks().stream()
                    .map(TaskConverter::toString)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка записи задачи", e);
                        }
                    });

            getEpics().stream()
                    .map(TaskConverter::toString)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка записи эпика", e);
                        }
                    });

            getSubtasks().stream()
                    .map(TaskConverter::toString)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка записи подзадачи", e);
                        }
                    });
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении в файл: " + e.getMessage());
            throw new ManagerSaveException("Ошибка при сохранении", e);
        }
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    public static void main(String[] args) {
        try {
            File file = File.createTempFile("tasks", ".csv");
            System.out.println("Файл для сохранения задач: " + file.getAbsolutePath());

            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            Task task1 = new Task("task1", "nameOf task1");
            Task task2 = new Task("task2", "nameOf task2", Status.NEW);
            manager.addTask(task1);
            manager.addTask(task2);

            Epic epic1 = new Epic("epic1", "nameOf epic1");
            manager.addEpic(epic1);

            Subtask epic1subtask1 = new Subtask("epic1subtask1", "nameOf epic1subtask1", epic1.getId());
            Subtask epic1subtask2 = new Subtask("epic1subtask2", "", epic1.getId());
            Subtask epic1subtask3 = new Subtask("epic1subtask3", "", epic1.getId());
            manager.addSubtask(epic1subtask1);
            manager.addSubtask(epic1subtask2);
            manager.addSubtask(epic1subtask3);

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            System.out.println("\nПроверка загруженных задач:");
            System.out.println("Задачи:");
            loadedManager.getTasks().forEach(System.out::println);
            System.out.println("Эпики:");
            loadedManager.getEpics().forEach(System.out::println);
            System.out.println("Подзадачи:");
            loadedManager.getSubtasks().forEach(System.out::println);

            System.out.println("\nСодержимое файла:");
            try {
                Files.lines(file.toPath()).forEach(System.out::println);
            } catch (IOException e) {
                System.out.println("Ошибка при чтении файла: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка при создании временного файла: " + e.getMessage());
        }
    }
}