package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.utils.TaskConverter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    @Override
    void setUp() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv").toFile();
        try {
            super.setUp();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadTasksWithRelations() {
        File testFile = new File("test_tasks.csv");
        try {
            FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

            Epic epic = manager.addEpic(new Epic("Epic", ""));
            Subtask subtask = manager.addSubtask(new Subtask("Subtask", "", epic.getId()));

            assertEquals(0, manager.getTasks().size());
            assertEquals(1, manager.getEpics().size());
            assertEquals(1, manager.getSubtasks().size());

            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFile);

            assertEquals(0, loaded.getTasks().size(), "Не должно быть обычных задач");
            assertEquals(1, loaded.getEpics().size(), "Должен быть 1 Epic");
            assertEquals(1, loaded.getSubtasks().size(), "Должна быть 1 Subtask");

            Subtask loadedSubtask = loaded.getSubtaskById(subtask.getId()).orElseThrow();
            assertEquals(epic.getId(), loadedSubtask.getEpicId(),
                    "ID Epic у Subtask должен соответствовать");

            Epic loadedEpic = loaded.getEpicById(epic.getId()).orElseThrow();
            assertEquals(1, loadedEpic.getSubtaskOfEpic().size(),
                    "У Epic должна быть 1 Subtask");
        } finally {
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }

    @Test
    void shouldHandleEmptyFields() {
        String csvLine = "1,TASK,Only name,,,0,,,";
        Task task = TaskConverter.fromString(csvLine);

        assertEquals("Only name", task.getName());
        assertEquals(Status.NEW, task.getStatus());
        assertEquals("", task.getDescription());
    }

    @Test
    void shouldThrowOnInvalidFormat() {
        String invalidLine = "1,TASK";
        assertThrows(IllegalArgumentException.class, () -> {
            TaskConverter.fromString(invalidLine);
        });
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Task", "Description");
        taskManager.addTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Optional<Task> loadedTask = loadedManager.getTaskById(task.getId());

        assertTrue(loadedTask.isPresent(), "Задача должна быть загружена из файла");
        assertEquals(task.getName(), loadedTask.get().getName(),
                "Имя задачи должно совпадать после загрузки");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        Files.write(tempFile.toPath(), new byte[0]);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tempFile),
                "Загрузка пустого файла не должна вызывать исключений");
    }

    @Test
    void shouldNotThrowWhenFileOperationsValid() {
        assertDoesNotThrow(() -> {
            File tempFile = File.createTempFile("tasks", ".csv");
            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
            manager.addTask(new Task("Task", "Description"));
            tempFile.delete();
        }, "Не должно быть исключения при корректных операциях с файлом");
    }

    @Test
    void shouldHandleCorruptedFileContent() throws IOException {
        Files.write(tempFile.toPath(), "corrupted,data\n".getBytes());

        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tempFile),
                "Загрузка поврежденного файла не должна вызывать исключений");
    }
}