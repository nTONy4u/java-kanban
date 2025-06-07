package fun.ntony4u.kanban.service;

import fun.ntony4u.kanban.model.*;
import fun.ntony4u.kanban.utils.TaskConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static fun.ntony4u.kanban.service.Status.*;

public class TaskManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;
    private static File testFile;
    private FileBackedTaskManager manager;

    @BeforeAll
    static void setUpAll() throws Exception {
        testFile = Files.createTempFile("tasks", ".csv").toFile();
    }

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        manager = new FileBackedTaskManager(testFile);
    }

    @AfterEach
    void tearDown() {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        final Task addedTask = taskManager.addTask(task);
        final Task savedTask = taskManager.getTaskById(addedTask.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task.getName(), savedTask.getName(), "Имена задач не совпадают.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статусы задач не совпадают.");

        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Список задач не должен быть null.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void addToHistory() {
        Task task = new Task("Test history", "Test history description", NEW);
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());

        final List<Task> history = taskManager.getHistory();
        assertNotNull(history, "История не должна быть null.");
        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
        assertEquals(task.getId(), history.get(0).getId(), "Id задачи в истории не совпадает.");
    }

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task(1, "task1", "Description", NEW);
        Task task2 = new Task(1, "task1 updated", "New Description", DONE);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды должны совпадать.");
    }

    @Test
    void subtasksWithSameIdAreEqual() {
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask(1, "subtask1", "Description", NEW, epic.getId());
        Subtask subtask2 = new Subtask(1, "subtask1 updated", "New Description", DONE, epic.getId());

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны.");
    }

    @Test
    void epicsWithSameIdAreEqual() {
        Epic epic1 = new Epic(1, "epic1", "Description", NEW);
        Epic epic2 = new Epic(1, "epic1 Updated", "New Description", DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны.");
    }

    @Test
    void shouldNotAddEpicAsSubtaskToItself() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.addEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("subtask", "Test Description", epicId);
        subtask.setId(epicId);

        Subtask result = taskManager.addSubtask(subtask);

        assertNull(taskManager.getSubtaskById(epicId),
                "Подзадача с id эпика не должна быть добавлена");
        assertEquals(0, taskManager.getEpicSubtasks(epic).size(),
                "У эпика не должно быть подзадач");
        assertEquals(subtask, result,
                "Метод должен вернуть ту же подзадачу, которая передавалась");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("subtask", "Description", epic.getId());

        assertDoesNotThrow(() -> taskManager.addSubtask(subtask),
                "Подзадача не может быть своим эпиком в текущей реализации");
    }

    @Test
    void managersReturnInitializedInstances() {
        assertNotNull(Managers.getDefault(), "TaskManager не должен быть null.");
        assertNotNull(Managers.getDefaultHistory(), "HistoryManager не должен быть null.");
    }

    @Test
    void taskManagerAddsAndFindsTasks() {
        Task task = new Task("task", "Description");
        Epic epic = new Epic("epic", "Description");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("subtask", "Description", epic.getId());

        taskManager.addTask(task);
        taskManager.addSubtask(subtask);

        assertNotNull(taskManager.getTaskById(task.getId()), "Задача не найдена.");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Эпик не найден.");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не найдена.");
    }

    @Test
    void generatedAndManualIdsDoNotConflict() {
        Task task1 = new Task("task1", "Description");
        Task task2 = new Task(1, "task2", "Description", NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getTasks().size(), "Обе задачи должны быть добавлены.");
        assertNotEquals(task1.getId(), task2.getId(), "Id не должны совпадать.");
    }

    @Test
    void taskRemainsUnchangedAfterAdding() {
        Task originalTask = new Task("originalTask", "Description", NEW);
        Task addedTask = taskManager.addTask(originalTask);

        assertEquals(originalTask.getName(), addedTask.getName(), "Имя изменилось.");
        assertEquals(originalTask.getDescription(), addedTask.getDescription(), "Описание изменилось.");
        assertEquals(originalTask.getStatus(), addedTask.getStatus(), "Статус изменился.");
    }

    @Test
    void historyManagerPreservesTaskState() {
        Task task = new Task(1, "task", "Description", NEW);
        Task taskCopy = new Task(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus()
        );
        historyManager.add(taskCopy);

        task.setStatus(DONE);
        List<Task> history = historyManager.getHistory();

        assertEquals(NEW, history.get(0).getStatus(),
                "История должна хранить исходное состояние.");
    }

    @Test
    void historyManagerHandlesNull() {
        Task task = new Task("Test", "Description", Status.NEW);
        taskManager.addTask(task);
        historyManager.add(null);
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    void testHistoryUnlimitedSize() {
        for (int i = 0; i < 100; i++) {
            Task task = new Task("Task " + i, "Desc");
            taskManager.addTask(task);
            taskManager.getTaskById(task.getId());
        }

        assertEquals(100, taskManager.getHistory().size(), "История должна хранить все задачи");
    }

    @Test
    void testEpicSubtaskIntegration() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("subtask2", "Description", epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteSubtaskById(subtask1.getId());

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic);
        assertEquals(1, epicSubtasks.size(), "Должна остаться одна подзадача");
        assertFalse(epicSubtasks.contains(subtask1), "Удаленная подзадача не должна оставаться в эпике");
        assertTrue(epicSubtasks.contains(subtask2), "Оставшаяся подзадача должна быть в эпике");
    }

    @Test
    void testHistoryLinkedListOperations() {
        Task task1 = new Task("Task 1", "Description1");
        Task task2 = new Task("Task 2", "Description2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(task1.getId(), history.get(0).getId(), "Первая задача должна быть первой в истории");
        assertEquals(task2.getId(), history.get(1).getId(), "Вторая задача должна быть второй в истории");

        taskManager.deleteTaskById(task1.getId());
        history = taskManager.getHistory();
        assertEquals(1, history.size(), "В истории должна остаться одна задача");
        assertEquals(task2.getId(), history.get(0).getId(), "Оставшаяся задача должна быть task2");
    }

    @Test
    void testSubtaskCleanupAfterEpicDeletion() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description1", epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть удалена");
        assertEquals(0, taskManager.getHistory().size(), "История должна быть пуста");
    }

    @Test
    void testHistoryAfterTaskUpdate() {
        Task task = new Task("Original", "Description");
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());

        task.setName("Updated");
        taskManager.updateTask(task);
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "Должна быть одна запись в истории");
        assertEquals("Updated", history.get(0).getName(), "История должна содержать обновленную версию");
    }

    @Test
    void shouldSaveAndLoadTasksWithRelations() {
        Epic epic = manager.addEpic(new Epic("Epic", ""));
        Subtask subtask = manager.addSubtask(new Subtask("Subtask", "", epic.getId()));
        Task task = manager.addTask(new Task("Task", ""));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, loaded.getTasks().size());
        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());
        assertEquals(epic.getId(), loaded.getSubtaskById(subtask.getId()).getEpicId());
    }

    @Test
    void shouldHandleEmptyFields() {
        String csvLine = "1,TASK,Only name,,,";
        Task task = TaskConverter.fromString(csvLine);

        assertEquals("Only name", task.getName());
        assertEquals(Status.NEW, task.getStatus()); // по умолчанию
        assertEquals("", task.getDescription());
    }

    @Test
    void shouldThrowOnInvalidFormat() {
        String invalidLine = "1,TASK";
        assertThrows(IllegalArgumentException.class, () -> {
            TaskConverter.fromString(invalidLine);
        });
    }
}